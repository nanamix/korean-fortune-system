package com.fortune.controller;

import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.fortune.service.*;
import com.fortune.dto.*;
import com.fortune.dto.NotificationRequest;
import com.fortune.dto.TelegramTestRequest;

import lombok.extern.slf4j.Slf4j;

import java.time.LocalDate;

/**
 * 🔮 운세 관련 API 컨트롤러
 *
 * <p>한국형 만세력 운세 시스템의 모든 운세 계산 기능을 제공하는 REST API 컨트롤러입니다.</p>
 *
 * @author 하진영
 * @version 2.5.0
 * @since 2025-06-24
 */
@Slf4j
@RestController
@RequestMapping("/api/fortune")
public class FortuneController {

    private final GanjiCalculatorService ganjiCalculatorService;
    private final DailyFortuneService dailyFortuneService;
    private final TojeongBigyeolService tojeongBigyeolService;
    private final ZodiacFortuneService zodiacFortuneService;
    private final GanjiCalendarService ganjiCalendarService;

    // AI 서비스는 Optional로 처리 (활성화되지 않을 수 있음)
    private final AIFortuneService aiFortuneService;
    
    // 알림 발송 서비스
    private final EmailService emailService;
    private final TelegramService telegramService;

    @Autowired
    public FortuneController(
            GanjiCalculatorService ganjiCalculatorService,
            DailyFortuneService dailyFortuneService,
            TojeongBigyeolService tojeongBigyeolService,
            ZodiacFortuneService zodiacFortuneService,
            GanjiCalendarService ganjiCalendarService,
            @Autowired(required = false) AIFortuneService aiFortuneService,
            EmailService emailService,
            TelegramService telegramService) {
        this.ganjiCalculatorService = ganjiCalculatorService;
        this.dailyFortuneService = dailyFortuneService;
        this.tojeongBigyeolService = tojeongBigyeolService;
        this.zodiacFortuneService = zodiacFortuneService;
        this.ganjiCalendarService = ganjiCalendarService;
        this.aiFortuneService = aiFortuneService;
        this.emailService = emailService;
        this.telegramService = telegramService;
    }

    /**
     * 📊 사주팔자 계산
     * <p>생년월일시를 입력받아 전통 사주팔자를 계산합니다.</p>
     * <h3>요청 예시</h3>
     * <pre>
     */
    @PostMapping("/saju/calculate")
    public ResponseEntity<com.fortune.dto.ApiResponse<SajuResult>> calculateSaju(
            @Valid @RequestBody SajuRequest request) {

        log.info("🔮 사주팔자 계산 요청: {}년 {}월 {}일",
                request.getBirthYear(), request.getBirthMonth(), request.getBirthDay());

        try {
            SajuResult result = ganjiCalculatorService.calculateSaju(request);
            return ResponseEntity.ok(com.fortune.dto.ApiResponse.success(result));
        } catch (Exception e) {
            log.error("❌ 사주팔자 계산 실패: {}", e.getMessage(), e);
            return ResponseEntity.badRequest()
                    .body(com.fortune.dto.ApiResponse.error("사주팔자 계산에 실패했습니다: " + e.getMessage(), "SAJU_CALC_ERROR"));
        }
    }

    /**
     * 📅 일일 운세 계산
     * <p>사주정보와 날짜를 입력받아 일일 운세를 계산합니다.</p>
     * <h3>요청 예시</h3>
     * <pre>
     * {
     *   "birthYear": 1990,
     *   "birthMonth": 1,
     *   "birthDay": 1
     * }
     * </pre>
     * @param sajuRequest 사주정보
     * @param targetDate 운세를 계산할 날짜
     * @return 일일 운세 결과
     */
    @PostMapping("/daily")
    public ResponseEntity<com.fortune.dto.ApiResponse<DailyFortuneResult>> calculateDailyFortune(
            @Valid @RequestBody SajuRequest sajuRequest,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate targetDate) {

        log.info("📅 일일 운세 계산 요청: {} 날짜 {}", sajuRequest.getBirthYear(), targetDate);

        try {
            // 1. 사주 계산
            SajuResult saju = ganjiCalculatorService.calculateSaju(sajuRequest);

            // 2. 일일 운세 계산
            DailyFortuneResult result = dailyFortuneService.calculateDailyFortune(saju, targetDate);

            return ResponseEntity.ok(com.fortune.dto.ApiResponse.success(result));
        } catch (Exception e) {
            log.error("❌ 일일 운세 계산 실패: {}", e.getMessage(), e);
            return ResponseEntity.badRequest()
                    .body(com.fortune.dto.ApiResponse.error("일일 운세 계산에 실패했습니다: " + e.getMessage(), "DAILY_FORTUNE_ERROR"));
        }
    }

    /**
     * 📅 오늘의 운세
     * <p>사주정보를 입력받아 오늘의 운세를 계산합니다.</p>
     * <h3>요청 예시</h3>
     * <pre>
     * {
     *   "birthYear": 1990,
     *   "birthMonth": 1,
     *   "birthDay": 1
     * }
     * </pre>
     * @param request 사주정보
     * @return 오늘의 운세 결과
     */
    @PostMapping("/daily/today")
    public ResponseEntity<com.fortune.dto.ApiResponse<DailyFortuneResult>> getTodayFortune(
            @Valid @RequestBody SajuRequest request) {

        log.info("📅 오늘의 운세 계산 요청: {}년 {}월 {}일",
                request.getBirthYear(), request.getBirthMonth(), request.getBirthDay());
        // 오늘의 날짜 계산
        try {
            // 1. 사주 계산
            SajuResult saju = ganjiCalculatorService.calculateSaju(request);

            // 2. 오늘의 운세 계산
            DailyFortuneResult result = dailyFortuneService.calculateDailyFortune(saju, LocalDate.now());
            return ResponseEntity.ok(com.fortune.dto.ApiResponse.success(result));
        } catch (Exception e) {
            log.error("❌ 오늘의 운세 계산 실패: {}", e.getMessage(), e);
            return ResponseEntity.badRequest()
                    .body(com.fortune.dto.ApiResponse.error("오늘의 운세 계산에 실패했습니다: " + e.getMessage(), "TODAY_FORTUNE_ERROR"));
        }
    }

    /**
     * 📜 토정비결 계산
     * <p>생년월일과 대상연도를 입력받아 토정비결을 계산합니다.</p>
     * <h3>요청 예시</h3>
     * <pre>
     * {
     *   "birthYear": 1990,
     *   "targetYear": 2025
     * }
     * </pre>
     * @param request 사주정보
     * @return 토정비결 결과
     */
    @PostMapping("/tojeong")
    public ResponseEntity<com.fortune.dto.ApiResponse<TojeongResult>> calculateTojeong(
            @Valid @RequestBody TojeongRequest request) {

        log.info("📜 토정비결 계산 요청: {}년생 -> {}년 운세",
                request.getBirthYear(), request.getTargetYear());
        // 토정비결 계산
        try {
            // 1. 토정비결 계산 (사주 계산 없이 직접 계산)
            TojeongResult result = tojeongBigyeolService.calculateTojeong(request);
            return ResponseEntity.ok(com.fortune.dto.ApiResponse.success(result));
        } catch (Exception e) {
            log.error("❌ 토정비결 계산 실패: {}", e.getMessage(), e);
            return ResponseEntity.badRequest()
                    .body(com.fortune.dto.ApiResponse.error("토정비결 계산에 실패했습니다: " + e.getMessage(), "TOJEONG_CALC_ERROR"));
        }
    }

    /**
     * ⭐ 별자리 운세 계산
     * <p>생년월일과 대상날짜를 입력받아 별자리 운세를 계산합니다.</p>
     * <h3>요청 예시</h3>
     * <pre>
     * {
     *   "birthYear": 1990,
     *   "birthMonth": 1,
     *   "birthDay": 1,
     *   "targetDate": "2025-06-24"
     * }
     * </pre>
     * @param request 별자리 운세 요청
     * @return 별자리 운세 결과
     */
    @PostMapping("/zodiac")
    public ResponseEntity<com.fortune.dto.ApiResponse<ZodiacFortuneResult>> calculateZodiacFortune(
            @Valid @RequestBody ZodiacRequest request) {

        log.info("⭐ 별자리 운세 계산 요청: {} -> {}",
                request.getBirthDate(), request.getTargetDate());

        try {
            ZodiacFortuneResult result = zodiacFortuneService.calculateZodiacFortune(
                    request.getBirthDate(), request.getTargetDate());
            return ResponseEntity.ok(com.fortune.dto.ApiResponse.success(result));
        } catch (Exception e) {
            log.error("❌ 별자리 운세 계산 실패: {}", e.getMessage(), e);
            return ResponseEntity.badRequest()
                    .body(com.fortune.dto.ApiResponse.error("별자리 운세 계산에 실패했습니다: " + e.getMessage(), "ZODIAC_FORTUNE_ERROR"));
        }
    }

    /**
     * 📆 간지달력 조회
     * <p>지정된 년월의 간지달력을 조회합니다.</p>
     * @param year 연도
     * @param month 월
     * @return 간지달력 정보
     */
    @GetMapping("/calendar/ganji/{year}/{month}")
    public ResponseEntity<com.fortune.dto.ApiResponse<GanjiCalendarResponse>> getGanjiCalendar(
            @PathVariable int year,
            @PathVariable int month) {

        log.info("📆 간지달력 조회 요청: {}년 {}월", year, month);

        if (year < 1900 || year > 2100) {
            return ResponseEntity.badRequest()
                    .body(com.fortune.dto.ApiResponse.error("유효하지 않은 연도입니다: " + year, "INVALID_YEAR"));
        }
        if (month < 1 || month > 12) {
            return ResponseEntity.badRequest()
                    .body(com.fortune.dto.ApiResponse.error("유효하지 않은 월입니다: " + month, "INVALID_MONTH"));
        }

        try {
            GanjiCalendarResponse result = ganjiCalendarService.generateMonthlyCalendar(year, month);
            return ResponseEntity.ok(com.fortune.dto.ApiResponse.success(result));
        } catch (Exception e) {
            log.error("❌ 간지달력 조회 실패: {}", e.getMessage(), e);
            return ResponseEntity.badRequest()
                    .body(com.fortune.dto.ApiResponse.error("간지달력 조회에 실패했습니다: " + e.getMessage(), "GANJI_CALENDAR_ERROR"));
        }
    }

    /**
     * 🤖 AI 사주 해석
     * <p>AI를 활용하여 사주를 상세히 해석합니다.</p>
     * <h3>요청 예시</h3>
     * <pre>
     * {
     *   "birthYear": 1990,
     *   "birthMonth": 1,
     *   "birthDay": 1
     * }
     * </pre>
     * @param request 사주정보
     * @return AI 해석 결과
     */
    @PostMapping("/ai/interpret-saju")
    public ResponseEntity<com.fortune.dto.ApiResponse<String>> interpretSajuWithAI(
            @Valid @RequestBody SajuRequest request) {

        log.info("🤖 AI 사주 해석 요청: {}년 {}월 {}일",
                request.getBirthYear(), request.getBirthMonth(), request.getBirthDay());

        if (aiFortuneService == null) {
            return ResponseEntity.badRequest()
                    .body(com.fortune.dto.ApiResponse.error("AI 서비스가 활성화되지 않았습니다.", "AI_SERVICE_DISABLED"));
        }

        try {
            // 1. 사주 계산
            SajuResult saju = ganjiCalculatorService.calculateSaju(request);

            // 2. AI 해석
            String interpretation = aiFortuneService.interpretSaju(saju);
            return ResponseEntity.ok(com.fortune.dto.ApiResponse.success(interpretation));
        } catch (Exception e) {
            log.error("❌ AI 사주 해석 실패: {}", e.getMessage(), e);
            return ResponseEntity.badRequest()
                    .body(com.fortune.dto.ApiResponse.error("AI 사주 해석에 실패했습니다: " + e.getMessage(), "AI_INTERPRETATION_ERROR"));
        }
    }

    /**
     * 🤖 AI 일일 운세 조언
     * <p>AI를 활용하여 일일 운세에 대한 상세한 조언을 제공합니다.</p>
     * @param sajuRequest 사주정보
     * @param targetDate 대상 날짜
     * @return AI 조언
     */
    @PostMapping("/ai/daily-advice")
    public ResponseEntity<com.fortune.dto.ApiResponse<String>> getDailyAdviceWithAI(
            @Valid @RequestBody SajuRequest sajuRequest,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate targetDate) {

        log.info("🤖 AI 일일 운세 조언 요청: {} 날짜 {}", sajuRequest.getBirthYear(), targetDate);

        if (aiFortuneService == null) {
            return ResponseEntity.badRequest()
                    .body(com.fortune.dto.ApiResponse.error("AI 서비스가 활성화되지 않았습니다.", "AI_SERVICE_DISABLED"));
        }

        try {
            // 1. 사주 계산
            SajuResult saju = ganjiCalculatorService.calculateSaju(sajuRequest);

            // 2. 일일 운세 계산
            DailyFortuneResult dailyFortune = dailyFortuneService.calculateDailyFortune(saju, targetDate);

            // 3. AI 조언
            String advice = aiFortuneService.generateDailyAdvice(dailyFortune);
            return ResponseEntity.ok(com.fortune.dto.ApiResponse.success(advice));
        } catch (Exception e) {
            log.error("❌ AI 일일 운세 조언 실패: {}", e.getMessage(), e);
            return ResponseEntity.badRequest()
                    .body(com.fortune.dto.ApiResponse.error("AI 일일 운세 조언에 실패했습니다: " + e.getMessage(), "AI_ADVICE_ERROR"));
        }
    }

    /**
     * 🤖 AI 운세 질문 답변
     * <p>사주 정보와 함께 자연어 질문을 하면 AI가 답변합니다.</p>
     * @param sajuRequest 사주정보
     * @param question 운세 관련 질문
     * @return AI 답변
     */
    @PostMapping("/ai/ask")
    public ResponseEntity<com.fortune.dto.ApiResponse<String>> askFortuneQuestion(
            @Valid @RequestBody SajuRequest sajuRequest,
            @RequestParam String question) {

        log.info("🤖 AI 운세 질문 요청: {} 질문: {}", sajuRequest.getBirthYear(), question);

        if (aiFortuneService == null) {
            return ResponseEntity.badRequest()
                    .body(com.fortune.dto.ApiResponse.error("AI 서비스가 활성화되지 않았습니다.", "AI_SERVICE_DISABLED"));
        }

        try {
            // 1. 사주 계산
            SajuResult saju = ganjiCalculatorService.calculateSaju(sajuRequest);

            // 2. AI 질문 답변
            String answer = aiFortuneService.answerFortuneQuestion(saju, question);
            return ResponseEntity.ok(com.fortune.dto.ApiResponse.success(answer));
        } catch (Exception e) {
            log.error("❌ AI 운세 질문 답변 실패: {}", e.getMessage(), e);
            return ResponseEntity.badRequest()
                    .body(com.fortune.dto.ApiResponse.error("AI 운세 질문 답변에 실패했습니다: " + e.getMessage(), "AI_QUESTION_ERROR"));
        }
    }

    /**
     * 🔍 시스템 상태 확인
     * <p>운세 시스템의 상태를 확인합니다.</p>
     * @return 시스템 상태
     */
    @GetMapping("/health")
    public ResponseEntity<com.fortune.dto.ApiResponse<String>> healthCheck() {
        log.info("🔍 시스템 상태 확인 요청");
        return ResponseEntity.ok(com.fortune.dto.ApiResponse.success("운세 시스템이 정상적으로 동작 중입니다."));
    }

    /**
     * 📧 사주팔자 결과를 이메일/텔레그램으로 발송
     * 
     * @param sajuRequest 사주팔자 요청 (알림 정보 포함)
     * @return 발송 결과
     */
    @PostMapping("/saju/calculate-and-send")
    public ResponseEntity<com.fortune.dto.ApiResponse<String>> calculateSajuAndSend(
            @Valid @RequestBody SajuRequest sajuRequest) {

        log.info("🔮 사주팔자 계산 및 발송 요청: {}님", 
                sajuRequest.getNotification() != null ? sajuRequest.getNotification().getRecipientName() : "알 수 없음");
        log.info("🔍 요청 데이터 확인: notification={}", sajuRequest.getNotification());

        try {
            // 1. 사주팔자 계산
            SajuResult sajuResult = ganjiCalculatorService.calculateSaju(sajuRequest);

            // 2. 알림 발송 (알림 정보가 있는 경우에만)
            if (sajuRequest.getNotification() != null) {
                sendNotification(sajuRequest.getNotification(), sajuResult, null, null, null, "saju");
            }

            return ResponseEntity.ok(com.fortune.dto.ApiResponse.success("사주팔자 결과가 성공적으로 발송되었습니다."));
        } catch (Exception e) {
            log.error("❌ 사주팔자 계산 및 발송 실패: {}", e.getMessage(), e);
            return ResponseEntity.badRequest()
                    .body(com.fortune.dto.ApiResponse.error("사주팔자 계산 및 발송에 실패했습니다: " + e.getMessage(), "SAJU_SEND_ERROR"));
        }
    }

    /**
     * 📧 일일운세 결과를 이메일/텔레그램으로 발송
     * 
     * @param sajuRequest 사주팔자 요청
     * @param notificationRequest 알림 발송 요청
     * @return 발송 결과
     */
    @PostMapping("/daily/today-and-send")
    public ResponseEntity<com.fortune.dto.ApiResponse<String>> getTodayFortuneAndSend(
            @Valid @RequestBody SajuRequest sajuRequest) {

        log.info("📅 오늘의 운세 계산 및 발송 요청: {}님", 
                sajuRequest.getNotification() != null ? sajuRequest.getNotification().getRecipientName() : "알 수 없음");

        try {
            // 1. 사주팔자 계산
            SajuResult saju = ganjiCalculatorService.calculateSaju(sajuRequest);

            // 2. 오늘의 운세 계산
            DailyFortuneResult dailyResult = dailyFortuneService.calculateDailyFortune(saju, LocalDate.now());

            // 3. 알림 발송
            if (sajuRequest.getNotification() != null) {
                sendNotification(sajuRequest.getNotification(), null, dailyResult, null, null, "daily");
            }

            return ResponseEntity.ok(com.fortune.dto.ApiResponse.success("오늘의 운세 결과가 성공적으로 발송되었습니다."));
        } catch (Exception e) {
            log.error("❌ 오늘의 운세 계산 및 발송 실패: {}", e.getMessage(), e);
            return ResponseEntity.badRequest()
                    .body(com.fortune.dto.ApiResponse.error("오늘의 운세 계산 및 발송에 실패했습니다: " + e.getMessage(), "DAILY_SEND_ERROR"));
        }
    }

    /**
     * 📧 토정비결 결과를 이메일/텔레그램으로 발송
     * 
     * @param tojeongRequest 토정비결 요청
     * @param notificationRequest 알림 발송 요청
     * @return 발송 결과
     */
    @PostMapping("/tojeong/calculate-and-send")
    public ResponseEntity<com.fortune.dto.ApiResponse<String>> calculateTojeongAndSend(
            @Valid @RequestBody TojeongRequest tojeongRequest) {

        log.info("📜 토정비결 계산 및 발송 요청: {}님", 
                tojeongRequest.getNotification() != null ? tojeongRequest.getNotification().getRecipientName() : "알 수 없음");

        try {
            // 1. 토정비결 계산
            TojeongResult tojeongResult = tojeongBigyeolService.calculateTojeong(tojeongRequest);

            // 2. 알림 발송
            if (tojeongRequest.getNotification() != null) {
                sendNotification(tojeongRequest.getNotification(), null, null, tojeongResult, null, "tojeong");
            }

            return ResponseEntity.ok(com.fortune.dto.ApiResponse.success("토정비결 결과가 성공적으로 발송되었습니다."));
        } catch (Exception e) {
            log.error("❌ 토정비결 계산 및 발송 실패: {}", e.getMessage(), e);
            return ResponseEntity.badRequest()
                    .body(com.fortune.dto.ApiResponse.error("토정비결 계산 및 발송에 실패했습니다: " + e.getMessage(), "TOJEONG_SEND_ERROR"));
        }
    }

    /**
     * 📧 별자리 운세 결과를 이메일/텔레그램으로 발송
     * 
     * @param zodiacRequest 별자리 운세 요청
     * @param notificationRequest 알림 발송 요청
     * @return 발송 결과
     */
    @PostMapping("/zodiac/calculate-and-send")
    public ResponseEntity<com.fortune.dto.ApiResponse<String>> calculateZodiacFortuneAndSend(
            @Valid @RequestBody ZodiacRequest zodiacRequest) {

        log.info("⭐ 별자리 운세 계산 및 발송 요청: {}님", 
                zodiacRequest.getNotification() != null ? zodiacRequest.getNotification().getRecipientName() : "알 수 없음");

        try {
            // 1. 별자리 운세 계산
            ZodiacFortuneResult zodiacResult = zodiacFortuneService.calculateZodiacFortune(
                    zodiacRequest.getBirthDate(), zodiacRequest.getTargetDate());

            // 2. 알림 발송
            if (zodiacRequest.getNotification() != null) {
                sendNotification(zodiacRequest.getNotification(), null, null, null, zodiacResult, "zodiac");
            }

            return ResponseEntity.ok(com.fortune.dto.ApiResponse.success("별자리 운세 결과가 성공적으로 발송되었습니다."));
        } catch (Exception e) {
            log.error("❌ 별자리 운세 계산 및 발송 실패: {}", e.getMessage(), e);
            return ResponseEntity.badRequest()
                    .body(com.fortune.dto.ApiResponse.error("별자리 운세 계산 및 발송에 실패했습니다: " + e.getMessage(), "ZODIAC_SEND_ERROR"));
        }
    }

    /**
     * 텔레그램 발송 테스트 (API 문서/테스트용)
     */
    @PostMapping("/telegram/test")
    public ResponseEntity<com.fortune.dto.ApiResponse<String>> testTelegramSend(
            @RequestBody TelegramTestRequest request) {
        try {
            log.info("📱 텔레그램 발송 테스트: {} -> {}", 
                    request.getChatId() != null ? request.getChatId() : "기본값", request.getMessage());
            
            if (request.getChatId() != null) {
                telegramService.sendMessage(request.getMessage(), String.valueOf(request.getChatId()));
            } else {
                telegramService.sendMessage(request.getMessage());
            }
            return ResponseEntity.ok(com.fortune.dto.ApiResponse.success("텔레그램 메시지가 성공적으로 발송되었습니다."));
        } catch (Exception e) {
            log.error("❌ 텔레그램 발송 테스트 실패", e);
            return ResponseEntity.badRequest()
                    .body(com.fortune.dto.ApiResponse.error("텔레그램 발송 실패: " + e.getMessage(), "TELEGRAM_TEST_ERROR"));
        }
    }

    /**
     * 알림 발송 처리
     * 
     * @param notificationRequest 알림 요청
     * @param sajuResult 사주팔자 결과
     * @param dailyResult 일일운세 결과
     * @param tojeongResult 토정비결 결과
     * @param zodiacResult 별자리 운세 결과
     * @param type 발송 타입
     */
    private void sendNotification(NotificationRequest notificationRequest,
                                SajuResult sajuResult,
                                DailyFortuneResult dailyResult,
                                TojeongResult tojeongResult,
                                ZodiacFortuneResult zodiacResult,
                                String type) {

        String recipientName = notificationRequest.getRecipientName();

        switch (notificationRequest.getNotificationType().toLowerCase()) {
            case "email":
                if (notificationRequest.getEmail() != null) {
                    sendEmailNotification(notificationRequest.getEmail(), recipientName, 
                                        sajuResult, dailyResult, tojeongResult, zodiacResult, type);
                }
                break;
            case "telegram":
                if (notificationRequest.getTelegramChatId() != null) {
                    sendTelegramNotification(notificationRequest.getTelegramChatId(), recipientName,
                                           sajuResult, dailyResult, tojeongResult, zodiacResult, type);
                }
                break;
            case "both":
                if (notificationRequest.getEmail() != null) {
                    sendEmailNotification(notificationRequest.getEmail(), recipientName,
                                        sajuResult, dailyResult, tojeongResult, zodiacResult, type);
                }
                if (notificationRequest.getTelegramChatId() != null) {
                    sendTelegramNotification(notificationRequest.getTelegramChatId(), recipientName,
                                           sajuResult, dailyResult, tojeongResult, zodiacResult, type);
                }
                break;
        }
    }

    /**
     * 이메일 알림 발송
     */
    private void sendEmailNotification(String email, String recipientName,
                                     SajuResult sajuResult, DailyFortuneResult dailyResult,
                                     TojeongResult tojeongResult, ZodiacFortuneResult zodiacResult,
                                     String type) {
        switch (type) {
            case "saju":
                emailService.sendSajuResult(email, sajuResult, recipientName);
                break;
            case "daily":
                emailService.sendDailyFortune(email, dailyResult, recipientName);
                break;
            case "tojeong":
                emailService.sendTojeongResult(email, tojeongResult, recipientName);
                break;
            case "zodiac":
                emailService.sendZodiacFortune(email, zodiacResult, recipientName);
                break;
        }
    }

    /**
     * 텔레그램 알림 발송
     */
    private void sendTelegramNotification(String chatId, String recipientName,
                                        SajuResult sajuResult, DailyFortuneResult dailyResult,
                                        TojeongResult tojeongResult, ZodiacFortuneResult zodiacResult,
                                        String type) {
        String message = "";
        
        switch (type) {
            case "saju":
                message = generateSajuTelegramMessage(sajuResult, recipientName);
                break;
            case "daily":
                message = generateDailyFortuneTelegramMessage(dailyResult, recipientName);
                break;
            case "tojeong":
                message = generateTojeongTelegramMessage(tojeongResult, recipientName);
                break;
            case "zodiac":
                message = generateZodiacTelegramMessage(zodiacResult, recipientName);
                break;
        }
        
        if (!message.isEmpty()) {
            telegramService.sendMessage(message);
        }
    }

    /**
     * 사주팔자 텔레그램 메시지 생성
     */
    private String generateSajuTelegramMessage(SajuResult sajuResult, String recipientName) {
        return String.format("""
            🔮 %s님의 사주팔자 결과
            
            📅 생년월일시: %s
            📊 사주팔자: %s
            
            🌟 일간: %s
            📈 운세 요약: %s
            
            💡 오행 분석:
            • 목(木): %d개
            • 화(火): %d개
            • 토(土): %d개
            • 금(金): %d개
            • 수(水): %d개
            
            🎯 가장 강한 오행: %s
            ⚠️ 가장 약한 오행: %s
            
            📝 발송일: %s
            """,
            recipientName,
            sajuResult.getAdjustedDateTime().format(java.time.format.DateTimeFormatter.ofPattern("yyyy년 MM월 dd일 HH시 mm분")),
            sajuResult.getFormattedSaju(),
            sajuResult.getDayMaster(),
            sajuResult.getFortuneSummary(),
            sajuResult.getWuxingAnalysis().getWoodCount(),
            sajuResult.getWuxingAnalysis().getFireCount(),
            sajuResult.getWuxingAnalysis().getEarthCount(),
            sajuResult.getWuxingAnalysis().getMetalCount(),
            sajuResult.getWuxingAnalysis().getWaterCount(),
            sajuResult.getWuxingAnalysis().getStrongestElement(),
            sajuResult.getWuxingAnalysis().getWeakestElement(),
            java.time.LocalDate.now().format(java.time.format.DateTimeFormatter.ofPattern("yyyy년 MM월 dd일"))
        );
    }

    /**
     * 일일운세 텔레그램 메시지 생성
     */
    private String generateDailyFortuneTelegramMessage(DailyFortuneResult dailyResult, String recipientName) {
        return String.format("""
            📅 %s님의 %s 운세
            
            🎯 총점: %d점
            📊 일간: %s
            
            💕 연애운: %d점
            %s
            
            💼 직업운: %d점
            %s
            
            💪 건강운: %d점
            %s
            
            💰 재물운: %d점
            %s
            
            🧭 길한 방향: %s
            🎨 길한 색깔: %s
            
            ⚠️ 주의사항: %s
            💡 조언: %s
            
            📝 발송일: %s
            """,
            recipientName,
            dailyResult.getDate().format(java.time.format.DateTimeFormatter.ofPattern("MM월 dd일")),
            dailyResult.getTotalScore(),
            dailyResult.getDayPillar(),
            dailyResult.getCategoryFortune().getLoveScore(),
            dailyResult.getCategoryFortune().getLoveMessage(),
            dailyResult.getCategoryFortune().getCareerScore(),
            dailyResult.getCategoryFortune().getCareerMessage(),
            dailyResult.getCategoryFortune().getHealthScore(),
            dailyResult.getCategoryFortune().getHealthMessage(),
            dailyResult.getCategoryFortune().getWealthScore(),
            dailyResult.getCategoryFortune().getWealthMessage(),
            dailyResult.getLuckyDirection(),
            String.join(", ", dailyResult.getLuckyColors()),
            dailyResult.getCaution(),
            dailyResult.getAdvice(),
            java.time.LocalDate.now().format(java.time.format.DateTimeFormatter.ofPattern("yyyy년 MM월 dd일"))
        );
    }

    /**
     * 토정비결 텔레그램 메시지 생성
     */
    private String generateTojeongTelegramMessage(TojeongResult tojeongResult, String recipientName) {
        return String.format("""
            📜 %s님의 %d년 토정비결
            
            🎯 괘 번호: %d번
            📛 괘 이름: %s
            🎨 괘 기호: %s
            
            📊 총점: %d점
            📝 요약: %s
            
            💡 상세 운세:
            %s
            
            🎯 조언: %s
            🍀 길한 달: %s
            ⚠️ 조심할 달: %s
            
            📝 발송일: %s
            """,
            recipientName,
            tojeongResult.getTargetYear(),
            tojeongResult.getGwaNumber(),
            tojeongResult.getGwaName(),
            tojeongResult.getGwaSymbol(),
            tojeongResult.getOverallScore(),
            tojeongResult.getSummary(),
            tojeongResult.getDetailedFortune(),
            tojeongResult.getAdvice(),
            tojeongResult.getLuckyMonths(),
            tojeongResult.getCautionMonths(),
            java.time.LocalDate.now().format(java.time.format.DateTimeFormatter.ofPattern("yyyy년 MM월 dd일"))
        );
    }

    /**
     * 별자리 운세 텔레그램 메시지 생성
     */
    private String generateZodiacTelegramMessage(ZodiacFortuneResult zodiacResult, String recipientName) {
        return String.format("""
            ⭐ %s님의 %s 운세
            
            📅 대상일: %s
            🎯 총점: %d점
            
            💕 연애운: %d점
            %s
            
            💼 직업운: %d점
            %s
            
            💪 건강운: %d점
            %s
            
            💰 재물운: %d점
            %s
            
            🎲 행운의 숫자: %s
            🎨 행운의 색깔: %s
            💎 행운의 보석: %s
            
            💡 성격: %s
            
            📝 발송일: %s
            """,
            recipientName,
            zodiacResult.getZodiacKoreanName(),
            zodiacResult.getTargetDate().format(java.time.format.DateTimeFormatter.ofPattern("yyyy년 MM월 dd일")),
            (zodiacResult.getTodayFortune().getLoveScore() + zodiacResult.getTodayFortune().getCareerScore() + 
             zodiacResult.getTodayFortune().getHealthScore() + zodiacResult.getTodayFortune().getMoneyScore()) / 4,
            zodiacResult.getTodayFortune().getLoveScore(),
            zodiacResult.getTodayFortune().getLoveMessage(),
            zodiacResult.getTodayFortune().getCareerScore(),
            zodiacResult.getTodayFortune().getCareerMessage(),
            zodiacResult.getTodayFortune().getHealthScore(),
            zodiacResult.getTodayFortune().getHealthMessage(),
            zodiacResult.getTodayFortune().getMoneyScore(),
            zodiacResult.getTodayFortune().getMoneyMessage(),
            zodiacResult.getLuckyNumbers().toString(),
            zodiacResult.getLuckyColor(),
            zodiacResult.getLuckyStone(),
            zodiacResult.getPersonality(),
            java.time.LocalDate.now().format(java.time.format.DateTimeFormatter.ofPattern("yyyy년 MM월 dd일"))
        );
    }
}
