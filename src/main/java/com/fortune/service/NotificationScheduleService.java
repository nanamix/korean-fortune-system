package com.fortune.service;

import com.fortune.dto.DailyFortuneResult;
import com.fortune.dto.NotificationRequest;
import com.fortune.dto.NotificationScheduleRequest;
import com.fortune.dto.NotificationScheduleResponse;
import com.fortune.dto.SajuRequest;
import com.fortune.dto.SajuResult;
import com.fortune.entity.NotificationSchedule;
import com.fortune.repository.NotificationScheduleRepository;
import jakarta.persistence.EntityNotFoundException;
import jakarta.mail.MessagingException;
import java.time.DateTimeException;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class NotificationScheduleService {
    private static final String LOCAL_OWNER = "local-development";

    private final NotificationScheduleRepository repository;
    private final GanjiCalculatorService ganjiCalculatorService;
    private final DailyFortuneService dailyFortuneService;
    private final FortuneNotificationFormatter formatter;
    private final EmailService emailService;
    private final TelegramService telegramService;
    private final DiscordService discordService;

    @Transactional
    public NotificationScheduleResponse create(NotificationScheduleRequest request) {
        validateNotification(request.getNotification());
        ZoneId zoneId = parseZone(request.getTimeZone());
        ZonedDateTime now = ZonedDateTime.now(zoneId);
        NotificationRequest notification = request.getNotification();
        NotificationSchedule schedule = NotificationSchedule.builder()
                .ownerKey(currentOwner())
                .enabled(true)
                .recipientName(notification.getRecipientName().strip())
                .email(blankToNull(notification.getEmail()))
                .telegramChatId(blankToNull(notification.getTelegramChatId()))
                .discordWebhookTarget(defaultTarget(notification.getDiscordWebhookTarget()))
                .notificationType(notification.getNotificationType())
                .birthYear(request.getBirthYear())
                .birthMonth(request.getBirthMonth())
                .birthDay(request.getBirthDay())
                .birthHour(request.getBirthHour())
                .birthMinute(request.getBirthMinute())
                .gender(request.getGender())
                .calendarType(request.getCalendarType())
                .leapMonth(request.isLeapMonth())
                .birthLongitude(request.getBirthLongitude())
                .applyEquationOfTime(request.isApplyEquationOfTime())
                .applyHistoricalDst(request.isApplyHistoricalDst())
                .scheduledTime(request.getScheduledTime())
                .timeZone(zoneId.getId())
                .build();
        // 오늘 예약 시간이 이미 지났다면 저장 즉시 발송하지 않고 다음 날부터 시작한다.
        if (!now.toLocalTime().isBefore(request.getScheduledTime())) {
            schedule.setLastRunDate(now.toLocalDate());
            schedule.setLastStatus("WAITING");
        }
        return toResponse(repository.save(schedule));
    }

    @Transactional(readOnly = true)
    public List<NotificationScheduleResponse> list() {
        return repository.findByOwnerKeyOrderByCreatedAtDesc(currentOwner()).stream()
                .map(this::toResponse)
                .toList();
    }

    @Transactional
    public NotificationScheduleResponse setEnabled(Long id, boolean enabled) {
        NotificationSchedule schedule = ownedSchedule(id);
        schedule.setEnabled(enabled);
        schedule.setLastStatus(enabled ? "WAITING" : "DISABLED");
        schedule.setLastError(null);
        return toResponse(repository.save(schedule));
    }

    @Transactional
    public void delete(Long id) {
        repository.delete(ownedSchedule(id));
    }

    /**
     * 매분 due 상태를 확인한다. lastRunDate 조건부 갱신으로 같은 날 중복 발송을 방지한다.
     */
    @Scheduled(cron = "${app.fortune.notification-schedule.cron:0 * * * * *}")
    @Transactional
    public void dispatchDueSchedules() {
        dispatchDueSchedules(Instant.now());
    }

    void dispatchDueSchedules(Instant instant) {
        for (NotificationSchedule candidate : repository.findByEnabledTrue()) {
            try {
                ZoneId zoneId = parseZone(candidate.getTimeZone());
                ZonedDateTime localNow = instant.atZone(zoneId);
                if (localNow.toLocalTime().isBefore(candidate.getScheduledTime())
                        || localNow.toLocalDate().equals(candidate.getLastRunDate())) {
                    continue;
                }
                int claimed = repository.claimRun(
                        candidate.getId(), localNow.toLocalDate(), localNow.toLocalDateTime());
                if (claimed == 1) {
                    dispatch(candidate.getId(), localNow.toLocalDate());
                }
            } catch (Exception exception) {
                log.error("예약 알림 처리 실패: scheduleId={}, cause={}",
                        candidate.getId(), exception.getClass().getSimpleName(), exception);
                markFailed(candidate.getId(), exception);
            }
        }
    }

    private void dispatch(Long id, LocalDate targetDate) throws MessagingException {
        NotificationSchedule schedule = repository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("예약을 찾을 수 없습니다."));
        SajuRequest request = SajuRequest.builder()
                .birthYear(schedule.getBirthYear())
                .birthMonth(schedule.getBirthMonth())
                .birthDay(schedule.getBirthDay())
                .birthHour(schedule.getBirthHour())
                .birthMinute(schedule.getBirthMinute())
                .gender(schedule.getGender())
                .calendarType(schedule.getCalendarType())
                .leapMonth(schedule.isLeapMonth())
                .birthLongitude(schedule.getBirthLongitude())
                .applyEquationOfTime(schedule.isApplyEquationOfTime())
                .applyHistoricalDst(schedule.isApplyHistoricalDst())
                .build();
        SajuResult saju = ganjiCalculatorService.calculateSaju(request);
        DailyFortuneResult daily = dailyFortuneService.calculateDailyFortune(saju, targetDate);
        String message = formatter.formatDaily(daily, schedule.getRecipientName());
        send(schedule, daily, message);
        schedule.setLastStatus("SENT");
        schedule.setLastError(null);
        repository.save(schedule);
        log.info("예약 운세 알림 발송 완료: scheduleId={}, date={}, channel={}",
                id, targetDate, schedule.getNotificationType());
    }

    private void send(
            NotificationSchedule schedule,
            DailyFortuneResult daily,
            String message) throws MessagingException {
        switch (schedule.getNotificationType()) {
            case "email" -> emailService.sendDailyFortuneNow(
                    schedule.getEmail(), daily, schedule.getRecipientName());
            case "telegram" -> telegramService.sendTestMessage(
                    message, schedule.getTelegramChatId());
            case "discord" -> discordService.sendTestMessage(
                    message, null, schedule.getDiscordWebhookTarget());
            case "both" -> {
                emailService.sendDailyFortuneNow(
                        schedule.getEmail(), daily, schedule.getRecipientName());
                telegramService.sendTestMessage(message, schedule.getTelegramChatId());
            }
            case "all" -> {
                emailService.sendDailyFortuneNow(
                        schedule.getEmail(), daily, schedule.getRecipientName());
                telegramService.sendTestMessage(message, schedule.getTelegramChatId());
                discordService.sendTestMessage(message, null, schedule.getDiscordWebhookTarget());
            }
            default -> throw new IllegalStateException("지원하지 않는 알림 채널입니다.");
        }
    }

    private void validateNotification(NotificationRequest notification) {
        if (notification.getDiscordWebhookUrl() != null
                && !notification.getDiscordWebhookUrl().isBlank()) {
            throw new IllegalArgumentException(
                    "예약 알림에는 일회성 Discord URL을 저장할 수 없습니다. OpenBao 대상을 선택해주세요.");
        }
        String type = notification.getNotificationType();
        if (("email".equals(type) || "both".equals(type) || "all".equals(type))
                && blankToNull(notification.getEmail()) == null) {
            throw new IllegalArgumentException("예약 이메일 주소가 필요합니다.");
        }
        if (("telegram".equals(type) || "both".equals(type) || "all".equals(type))
                && blankToNull(notification.getTelegramChatId()) == null) {
            throw new IllegalArgumentException("예약 Telegram Chat ID가 필요합니다.");
        }
    }

    private void markFailed(Long id, Exception exception) {
        repository.findById(id).ifPresent(schedule -> {
            schedule.setLastStatus("FAILED");
            schedule.setLastError("발송 실패 (" + exception.getClass().getSimpleName() + ")");
            repository.save(schedule);
        });
    }

    private NotificationSchedule ownedSchedule(Long id) {
        return repository.findByIdAndOwnerKey(id, currentOwner())
                .orElseThrow(() -> new EntityNotFoundException("예약을 찾을 수 없습니다."));
    }

    private String currentOwner() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()
                || authentication.getName() == null || authentication.getName().isBlank()
                || "anonymousUser".equals(authentication.getName())) {
            return LOCAL_OWNER;
        }
        return authentication.getName();
    }

    private ZoneId parseZone(String value) {
        try {
            return ZoneId.of(value);
        } catch (DateTimeException exception) {
            throw new IllegalArgumentException("유효한 IANA 시간대를 입력해주세요.", exception);
        }
    }

    private String defaultTarget(String target) {
        return blankToNull(target) == null ? "default" : target.strip().toLowerCase();
    }

    private String blankToNull(String value) {
        return value == null || value.isBlank() ? null : value.strip();
    }

    private NotificationScheduleResponse toResponse(NotificationSchedule schedule) {
        return new NotificationScheduleResponse(
                schedule.getId(),
                schedule.isEnabled(),
                schedule.getRecipientName(),
                schedule.getNotificationType(),
                schedule.getScheduledTime(),
                schedule.getTimeZone(),
                schedule.getLastRunDate(),
                schedule.getLastStatus(),
                schedule.getLastError(),
                schedule.getLastRunAt(),
                schedule.getCreatedAt());
    }
}
