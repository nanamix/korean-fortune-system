package com.fortune.controller;

import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;

import com.fortune.service.*;
import com.fortune.dto.*;

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
@Tag(name = "🔮 운세 API", description = "한국형 만세력 운세 시스템 - 사주, 토정비결, 별자리 운세 제공")
public class FortuneController {

    private final GanjiCalculatorService ganjiCalculatorService;
    private final DailyFortuneService dailyFortuneService;
    private final TojeongBigyeolService tojeongBigyeolService;
    private final ZodiacFortuneService zodiacFortuneService;
    private final GanjiCalendarService ganjiCalendarService;

    // AI 서비스는 Optional로 처리 (활성화되지 않을 수 있음)
    private final AIFortuneService aiFortuneService;

    @Autowired
    public FortuneController(
            GanjiCalculatorService ganjiCalculatorService,
            DailyFortuneService dailyFortuneService,
            TojeongBigyeolService tojeongBigyeolService,
            ZodiacFortuneService zodiacFortuneService,
            GanjiCalendarService ganjiCalendarService,
            @Autowired(required = false) AIFortuneService aiFortuneService) {
        this.ganjiCalculatorService = ganjiCalculatorService;
        this.dailyFortuneService = dailyFortuneService;
        this.tojeongBigyeolService = tojeongBigyeolService;
        this.zodiacFortuneService = zodiacFortuneService;
        this.ganjiCalendarService = ganjiCalendarService;
        this.aiFortuneService = aiFortuneService;
    }

    /**
     * 📊 사주팔자 계산
     * <p>생년월일시를 입력받아 전통 사주팔자를 계산합니다.</p>
     * <h3>요청 예시</h3>
     * <pre>
     */
    @PostMapping("/saju/calculate")
    @Operation(
            summary = "📊 사주팔자 계산",
            description = "생년월일시를 입력받아 전통 사주팔자를 계산합니다."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "사주팔자 계산 성공"),
            @ApiResponse(responseCode = "400", description = "잘못된 요청 데이터"),
            @ApiResponse(responseCode = "500", description = "서버 내부 오류")
    })
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
     */
    @PostMapping("/daily")
    @Operation(
            summary = "📅 일일 운세 계산",
            description = "사주정보와 날짜를 입력받아 일일 운세를 계산합니다."
    )
    public ResponseEntity<com.fortune.dto.ApiResponse<DailyFortuneResult>> calculateDailyFortune(
            @Valid @RequestBody SajuRequest sajuRequest,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
            @Parameter(description = "운세를 계산할 날짜", example = "2025-06-24") LocalDate targetDate) {

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
     */
    @PostMapping("/daily/today")
    @Operation(
            summary = "📅 오늘의 운세",
            description = "사주정보를 입력받아 오늘의 운세를 계산합니다."
    )
    public ResponseEntity<com.fortune.dto.ApiResponse<DailyFortuneResult>> getTodayFortune(
            @Valid @RequestBody SajuRequest request) {

        try {
            SajuResult saju = ganjiCalculatorService.calculateSaju(request);
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
     */
    @PostMapping("/tojeong")
    @Operation(
            summary = "📜 토정비결 계산",
            description = "생년월일과 대상연도를 입력받아 토정비결을 계산합니다."
    )
    public ResponseEntity<com.fortune.dto.ApiResponse<TojeongResult>> calculateTojeong(
            @Valid @RequestBody TojeongRequest request) {

        log.info("📜 토정비결 계산 요청: {}년생 -> {}년 운세",
                request.getBirthYear(), request.getTargetYear());

        try {
            TojeongResult result = tojeongBigyeolService.calculateTojeong(request);
            return ResponseEntity.ok(com.fortune.dto.ApiResponse.success(result));
        } catch (Exception e) {
            log.error("❌ 토정비결 계산 실패: {}", e.getMessage(), e);
            return ResponseEntity.badRequest()
                    .body(com.fortune.dto.ApiResponse.error("토정비결 계산에 실패했습니다: " + e.getMessage(), "TOJEONG_ERROR"));
        }
    }

    /**
     * ⭐ 별자리 운세 계산
     */
    @PostMapping("/zodiac")
    @Operation(
            summary = "⭐ 별자리 운세 계산",
            description = "생년월일과 대상날짜를 입력받아 별자리 운세를 계산합니다."
    )
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
                    .body(com.fortune.dto.ApiResponse.error("별자리 운세 계산에 실패했습니다: " + e.getMessage(), "ZODIAC_ERROR"));
        }
    }

    /**
     * 📆 간지달력 조회
     */
    @GetMapping("/calendar/ganji/{year}/{month}")
    @Operation(
            summary = "📆 간지달력 조회",
            description = "지정된 년월의 간지달력을 조회합니다."
    )
    public ResponseEntity<com.fortune.dto.ApiResponse<GanjiCalendarResponse>> getGanjiCalendar(
            @PathVariable @Parameter(description = "연도", example = "2025") int year,
            @PathVariable @Parameter(description = "월", example = "6") int month) {

        log.info("📆 간지달력 조회 요청: {}년 {}월", year, month);

        try {
            // 유효성 검사
            if (year < 1900 || year > 2100) {
                return ResponseEntity.badRequest()
                        .body(com.fortune.dto.ApiResponse.error("연도는 1900-2100 범위여야 합니다", "INVALID_YEAR"));
            }
            if (month < 1 || month > 12) {
                return ResponseEntity.badRequest()
                        .body(com.fortune.dto.ApiResponse.error("월은 1-12 범위여야 합니다", "INVALID_MONTH"));
            }

            GanjiCalendarResponse result = ganjiCalendarService.generateMonthlyCalendar(year, month);
            return ResponseEntity.ok(com.fortune.dto.ApiResponse.success(result));
        } catch (Exception e) {
            log.error("❌ 간지달력 조회 실패: {}", e.getMessage(), e);
            return ResponseEntity.badRequest()
                    .body(com.fortune.dto.ApiResponse.error("간지달력 조회에 실패했습니다: " + e.getMessage(), "CALENDAR_ERROR"));
        }
    }

    /**
     * 🤖 AI 사주 해석 (AI 서비스 활성화시에만 동작)
     */
    @PostMapping("/ai/interpret-saju")
    @Operation(
            summary = "🤖 AI 사주 해석",
            description = "AI를 활용하여 사주를 상세히 해석합니다."
    )
    public ResponseEntity<com.fortune.dto.ApiResponse<String>> interpretSajuWithAI(
            @Valid @RequestBody SajuRequest request) {

        if (aiFortuneService == null) {
            return ResponseEntity.badRequest()
                    .body(com.fortune.dto.ApiResponse.error("AI 서비스가 활성화되지 않았습니다", "AI_SERVICE_DISABLED"));
        }

        log.info("🤖 AI 사주 해석 요청");

        try {
            SajuResult saju = ganjiCalculatorService.calculateSaju(request);
            String interpretation = aiFortuneService.interpretSaju(saju);
            return ResponseEntity.ok(com.fortune.dto.ApiResponse.success(interpretation));
        } catch (Exception e) {
            log.error("❌ AI 사주 해석 실패: {}", e.getMessage(), e);
            return ResponseEntity.badRequest()
                    .body(com.fortune.dto.ApiResponse.error("AI 사주 해석에 실패했습니다: " + e.getMessage(), "AI_INTERPRET_ERROR"));
        }
    }

    /**
     * 🤖 AI 일일 운세 조언 (AI 서비스 활성화시에만 동작)
     */
    @PostMapping("/ai/daily-advice")
    @Operation(
            summary = "🤖 AI 일일 운세 조언",
            description = "AI를 활용하여 일일 운세에 대한 상세한 조언을 제공합니다."
    )
    public ResponseEntity<com.fortune.dto.ApiResponse<String>> getDailyAdviceWithAI(
            @Valid @RequestBody SajuRequest sajuRequest,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate targetDate) {

        if (aiFortuneService == null) {
            return ResponseEntity.badRequest()
                    .body(com.fortune.dto.ApiResponse.error("AI 서비스가 활성화되지 않았습니다", "AI_SERVICE_DISABLED"));
        }

        try {
            SajuResult saju = ganjiCalculatorService.calculateSaju(sajuRequest);
            DailyFortuneResult dailyFortune = dailyFortuneService.calculateDailyFortune(saju, targetDate);
            String advice = aiFortuneService.generateDailyAdvice(dailyFortune);
            return ResponseEntity.ok(com.fortune.dto.ApiResponse.success(advice));
        } catch (Exception e) {
            log.error("❌ AI 일일 조언 생성 실패: {}", e.getMessage(), e);
            return ResponseEntity.badRequest()
                    .body(com.fortune.dto.ApiResponse.error("AI 일일 조언 생성에 실패했습니다: " + e.getMessage(), "AI_DAILY_ADVICE_ERROR"));
        }
    }

    /**
     * 🤖 AI 운세 질문 답변 (AI 서비스 활성화시에만 동작)
     */
    @PostMapping("/ai/ask")
    @Operation(
            summary = "🤖 AI 운세 질문 답변",
            description = "사주 정보와 함께 자연어 질문을 하면 AI가 답변합니다."
    )
    public ResponseEntity<com.fortune.dto.ApiResponse<String>> askFortuneQuestion(
            @Valid @RequestBody SajuRequest sajuRequest,
            @RequestParam @Parameter(description = "운세 관련 질문", example = "올해 연애운은 어떤가요?") String question) {

        if (aiFortuneService == null) {
            return ResponseEntity.badRequest()
                    .body(com.fortune.dto.ApiResponse.error("AI 서비스가 활성화되지 않았습니다", "AI_SERVICE_DISABLED"));
        }

        try {
            SajuResult saju = ganjiCalculatorService.calculateSaju(sajuRequest);
            String answer = aiFortuneService.answerFortuneQuestion(saju, question);
            return ResponseEntity.ok(com.fortune.dto.ApiResponse.success(answer));
        } catch (Exception e) {
            log.error("❌ AI 질문 답변 실패: {}", e.getMessage(), e);
            return ResponseEntity.badRequest()
                    .body(com.fortune.dto.ApiResponse.error("AI 질문 답변에 실패했습니다: " + e.getMessage(), "AI_QA_ERROR"));
        }
    }

    /**
     * 🔍 시스템 상태 확인
     */
    @GetMapping("/health")
    @Operation(
            summary = "🔍 시스템 상태 확인",
            description = "운세 시스템의 상태를 확인합니다."
    )
    public ResponseEntity<com.fortune.dto.ApiResponse<String>> healthCheck() {
        try {
            String status = "운세 시스템이 정상적으로 동작중입니다";
            if (aiFortuneService != null) {
                status += " (AI 서비스 활성화됨)";
            } else {
                status += " (AI 서비스 비활성화됨)";
            }
            return ResponseEntity.ok(com.fortune.dto.ApiResponse.success(status));
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(com.fortune.dto.ApiResponse.error("시스템 상태 확인 실패: " + e.getMessage(), "HEALTH_CHECK_ERROR"));
        }
    }
}
