package com.fortune.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.fortune.dto.DailyFortuneResult;
import com.fortune.dto.NotificationRequest;
import com.fortune.dto.NotificationScheduleRequest;
import com.fortune.dto.SajuResult;
import com.fortune.entity.NotificationSchedule;
import com.fortune.repository.NotificationScheduleRepository;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class NotificationScheduleServiceTest {
    private NotificationScheduleRepository repository;
    private GanjiCalculatorService ganji;
    private DailyFortuneService daily;
    private TelegramService telegram;
    private NotificationScheduleService service;

    @BeforeEach
    void setUp() {
        repository = mock(NotificationScheduleRepository.class);
        ganji = mock(GanjiCalculatorService.class);
        daily = mock(DailyFortuneService.class);
        telegram = mock(TelegramService.class);
        service = new NotificationScheduleService(
                repository,
                ganji,
                daily,
                new FortuneNotificationFormatter(),
                mock(EmailService.class),
                telegram,
                mock(DiscordService.class));
    }

    @Test
    void persistsOnlyNamedDiscordTarget() {
        when(repository.save(any())).thenAnswer(invocation -> {
            NotificationSchedule schedule = invocation.getArgument(0);
            schedule.setId(1L);
            schedule.setCreatedAt(LocalDateTime.now());
            return schedule;
        });
        NotificationScheduleRequest request = baseRequest(NotificationRequest.builder()
                .recipientName("마스터")
                .discordWebhookTarget("openbao")
                .notificationType("discord")
                .build());

        service.create(request);

        verify(repository).save(org.mockito.ArgumentMatchers.argThat(schedule ->
                schedule.getDiscordWebhookTarget().equals("openbao")
                        && schedule.getTelegramChatId() == null
                        && schedule.getEmail() == null));
    }

    @Test
    void sendsDueScheduleOnceWithFreshDailyResult() {
        NotificationSchedule schedule = telegramSchedule();
        DailyFortuneResult result = DailyFortuneResult.builder()
                .date(LocalDate.of(2026, 7, 30))
                .totalScore(80)
                .build();
        when(repository.findByEnabledTrue()).thenReturn(List.of(schedule));
        when(repository.claimRun(eq(7L), eq(LocalDate.of(2026, 7, 30)), any())).thenReturn(1);
        when(repository.findById(7L)).thenReturn(Optional.of(schedule));
        when(ganji.calculateSaju(any())).thenReturn(SajuResult.builder().build());
        when(daily.calculateDailyFortune(any(), eq(LocalDate.of(2026, 7, 30)))).thenReturn(result);

        service.dispatchDueSchedules(Instant.parse("2026-07-30T00:00:00Z"));

        verify(telegram).sendTestMessage(
                org.mockito.ArgumentMatchers.contains("총점: 80점"),
                eq("-100123"));
        assertThat(schedule.getLastStatus()).isEqualTo("SENT");
    }

    @Test
    void doesNotSendBeforeConfiguredLocalTime() {
        NotificationSchedule schedule = telegramSchedule();
        when(repository.findByEnabledTrue()).thenReturn(List.of(schedule));

        service.dispatchDueSchedules(Instant.parse("2026-07-29T22:00:00Z"));

        verify(repository, never()).claimRun(any(), any(), any());
        verify(telegram, never()).sendTestMessage(any(), any());
    }

    private NotificationScheduleRequest baseRequest(NotificationRequest notification) {
        return NotificationScheduleRequest.builder()
                .birthYear(1981).birthMonth(3).birthDay(20)
                .birthHour(1).birthMinute(59)
                .gender("M").calendarType("SOLAR")
                .scheduledTime(LocalTime.of(8, 0))
                .timeZone("Asia/Seoul")
                .notification(notification)
                .build();
    }

    private NotificationSchedule telegramSchedule() {
        return NotificationSchedule.builder()
                .id(7L)
                .ownerKey("owner")
                .enabled(true)
                .recipientName("마스터")
                .telegramChatId("-100123")
                .notificationType("telegram")
                .birthYear(1981).birthMonth(3).birthDay(20)
                .birthHour(1).birthMinute(59)
                .gender("M").calendarType("SOLAR")
                .scheduledTime(LocalTime.of(8, 0))
                .timeZone("Asia/Seoul")
                .build();
    }
}
