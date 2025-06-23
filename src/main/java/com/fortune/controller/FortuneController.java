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
import java.util.concurrent.CompletableFuture;

/**
 * 🔮 운세 관련 API 컨트롤러
 * 
 * <p>한국형 만세력 운세 시스템의 모든 운세 계산 기능을 제공하는 REST API 컨트롤러입니다.</p>
 * 
 * <h3>제공하는 주요 기능</h3>
 * <ul>
 *   <li>📊 사주팔자 계산 및 AI 해석</li>
 *   <li>📅 일일 운세 (오늘, 내일, 특정 날짜)</li>
 *   <li>📜 토정비결 64괘 연간 운세</li>
 *   <li>📆 간지달력 조회 및 길일 안내</li>
 *   <li>⭐ 서양 별자리 운세</li>
 *   <li>🤖 AI 기반 운세 질문 응답</li>
 * </ul>
 * 
 * <h3>API 사용 가이드</h3>
 * <ul>
 *   <li>모든 POST 요청은 JSON 형태의 요청 본문이 필요합니다</li>
 *   <li>날짜 형식은 ISO 8601 (YYYY-MM-DD)을 사용합니다</li>
 *   <li>응답은 ApiResponse 래퍼로 통일되어 제공됩니다</li>
 *   <li>AI 기능 사용 시 응답 시간이 다소 걸릴 수 있습니다</li>
 * </ul>
 * 
 * @author 운세API팀
 * @version 2.0.0
 * @since 2025-06-23
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
    private final AIFortuneService aiFortuneService;

    @Autowired
    public FortuneController(
            GanjiCalculatorService ganjiCalculatorService,
            DailyFortuneService dailyFortuneService,
            TojeongBigyeolService tojeongBigyeolService,
            ZodiacFortuneService zodiacFortuneService,
            GanjiCalendarService ganjiCalendarService,
            AIFortuneService aiFortuneService) {
        this.ganjiCalculatorService = ganjiCalculatorService;
        this.dailyFortuneService = dailyFortuneService;
        this.tojeongBigyeolService = tojeongBigyeolService;
        this.zodiacFortuneService = zodiacFortuneService;
        this.ganjiCalendarService = ganjiCalendarService;
        this.aiFortuneService = aiFortuneService;
    }

    /**
     * 📊 사주팔자 계산
     * 
     * <p>사용자의 생년월일시를 입력받아 사주팔자를 계산합니다.</p>
     * <p>사주팔자는 한국 전통 사주학에서 개인의 운명을 분석하는 중요한 요소입니다.</p>
     * 
     * @param request 사주 계산 요청 정보 (생년월일시, 성별, 달력구분)
     * @return 사주팔자 계산 결과 (연주, 월주, 일주, 시주, 일간)
     */
    @PostMapping("/saju/calculate")
    @Operation(
        summary = "📊 사주팔자 계산", 
        description = """
            생년월일시를 입력받아 전통 사주팔자를 계산합니다.
            
            **입력 정보:**
            - 생년월일시 (양력/음력 구분 가능)
            - 성별 (남성/여성)
            - 출생지 정보 (태양시 보정용)
            
            **결과 정보:**
            - 사주팔자 (연주, 월주, 일주, 시주)
            - 일간 (본인의 천간)
            - 오행 분석
            """
    )
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "사주팔자 계산 성공"),
        @ApiResponse(responseCode = "400", description = "잘못된 입력 데이터"),
        @ApiResponse(responseCode = "500", description = "서버 내부 오류")
    })
    public ResponseEntity<com.fortune.dto.ApiResponse<SajuResult>> calculateSaju(
            @Valid @RequestBody SajuRequest request) {
        
        log.info("📊 사주팔자 계산 요청: {}년 {}월 {}일 {}시 {}분", 
                request.getBirthYear(), request.getBirthMonth(), request.getBirthDay(),
                request.getBirthHour(), request.getBirthMinute());

        SajuResult result = ganjiCalculatorService.calculateSaju(request);
        
        log.info("✅ 사주팔자 계산 완료: {}", result.getFormattedSaju());
        return ResponseEntity.ok(com.fortune.dto.ApiResponse.success(result));
    }

    /**
     * 🤖 AI 사주 해석
     * 
     * <p>계산된 사주팔자를 AI가 분석하여 개인화된 해석을 제공합니다.</p>
     * 
     * @param request 사주 계산 요청 정보
     * @return AI가 생성한 사주 해석 결과
     */
    @PostMapping("/saju/ai-interpretation")
    @Operation(
        summary = "🤖 AI 사주 해석", 
        description = """
            사주팔자 데이터를 AI가 분석하여 개인화된 해석을 제공합니다.
            
            **AI 분석 내용:**
            - 기본 성격과 기질 분석
            - 타고난 재능과 장점
            - 주의할 약점과 경향
            - 인생 전반적인 운세 흐름
            - 실용적인 삶의 조언
            """
    )
    public ResponseEntity<com.fortune.dto.ApiResponse<String>> getAISajuInterpretation(
            @Valid @RequestBody SajuRequest request) {
        
        log.info("🤖 AI 사주 해석 요청");
        
        SajuResult sajuResult = ganjiCalculatorService.calculateSaju(request);
        String interpretation = aiFortuneService.interpretSaju(sajuResult);
        
        log.info("✅ AI 사주 해석 완료");
        return ResponseEntity.ok(com.fortune.dto.ApiResponse.success(interpretation));
    }

    /**
     * 📅 오늘의 운세
     * 
     * <p>오늘 날짜 기준으로 종합적인 일일 운세를 제공합니다.</p>
     * 
     * @param request 사주 계산 요청 정보
     * @return 오늘의 종합 운세 (길흉, 분야별 운세, 조언 등)
     */
    @PostMapping("/daily/today")
    @Operation(
        summary = "📅 오늘의 운세", 
        description = """
            오늘 날짜 기준으로 개인 맞춤 일일 운세를 계산합니다.
            
            **제공 정보:**
            - 오늘의 일주와 길흉
            - 분야별 운세 점수 (애정, 재물, 건강, 직업)
            - 길방위와 길한 색깔
            - 신살 정보
            - 구체적인 조언
            """
    )
    public ResponseEntity<com.fortune.dto.ApiResponse<DailyFortuneResult>> getTodayFortune(
            @Valid @RequestBody SajuRequest request) {
        
        log.info("📅 오늘의 운세 요청");
        
        SajuResult saju = ganjiCalculatorService.calculateSaju(request);
        DailyFortuneResult fortune = dailyFortuneService.calculateDailyFortune(saju, LocalDate.now());
        
        log.info("✅ 오늘의 운세 계산 완료: 총점 {}점", fortune.getTotalScore());
        return ResponseEntity.ok(com.fortune.dto.ApiResponse.success(fortune));
    }

    /**
     * 🤖 AI 강화 오늘의 운세
     * 
     * <p>일반 일일 운세에 AI 분석을 추가하여 더 상세한 해석을 제공합니다.</p>
     * 
     * @param request 사주 계산 요청 정보
     * @return AI가 강화한 오늘의 운세
     */
    @PostMapping("/daily/today/ai-enhanced")
    @Operation(
        summary = "🤖 AI 강화 오늘의 운세", 
        description = """
            일반 일일 운세에 AI 분석을 추가하여 더욱 상세하고 개인화된 해석을 제공합니다.
            
            **AI 강화 내용:**
            - 오늘 하루 전체적인 에너지 분석
            - 시간대별 행동 가이드
            - 각 분야별 구체적 조언
            - 실제 상황별 대응 방법
            - 마음가짐과 태도 조언
            """
    )
    public ResponseEntity<com.fortune.dto.ApiResponse<String>> getAIEnhancedTodayFortune(
            @Valid @RequestBody SajuRequest request) {
        
        log.info("🤖 AI 강화 오늘의 운세 요청");
        
        SajuResult saju = ganjiCalculatorService.calculateSaju(request);
        DailyFortuneResult fortune = dailyFortuneService.calculateDailyFortune(saju, LocalDate.now());
        String enhancedFortune = aiFortuneService.enhanceDailyFortune(fortune, saju);
        
        log.info("✅ AI 강화 운세 완료");
        return ResponseEntity.ok(com.fortune.dto.ApiResponse.success(enhancedFortune));
    }

    /**
     * 📅 내일의 운세
     * 
     * <p>내일 날짜 기준으로 일일 운세를 미리 확인할 수 있습니다.</p>
     * 
     * @param request 사주 계산 요청 정보
     * @return 내일의 운세 정보
     */
    @PostMapping("/daily/tomorrow")
    @Operation(
        summary = "📅 내일의 운세", 
        description = "내일 날짜 기준으로 일일 운세를 미리 계산하여 준비할 수 있도록 도와드립니다."
    )
    public ResponseEntity<com.fortune.dto.ApiResponse<DailyFortuneResult>> getTomorrowFortune(
            @Valid @RequestBody SajuRequest request) {
        
        log.info("📅 내일의 운세 요청");
        
        SajuResult saju = ganjiCalculatorService.calculateSaju(request);
        DailyFortuneResult fortune = dailyFortuneService.calculateDailyFortune(saju, LocalDate.now().plusDays(1));
        
        log.info("✅ 내일의 운세 계산 완료");
        return ResponseEntity.ok(com.fortune.dto.ApiResponse.success(fortune));
    }

    /**
     * 📅 특정 날짜 운세
     * 
     * <p>사용자가 지정한 특정 날짜의 운세를 계산합니다.</p>
     * 
     * @param date 운세를 확인할 특정 날짜 (YYYY-MM-DD 형식)
     * @param request 사주 계산 요청 정보
     * @return 지정된 날짜의 운세 정보
     */
    @PostMapping("/daily/{date}")
    @Operation(
        summary = "📅 특정 날짜 운세", 
        description = """
            지정한 날짜의 일일 운세를 계산합니다.
            
            **활용 예시:**
            - 중요한 약속이나 행사 전 운세 확인
            - 여행 날짜 선택 시 참고
            - 계약이나 결정 날짜 검토
            - 특별한 날의 운세 미리 확인
            """
    )
    public ResponseEntity<com.fortune.dto.ApiResponse<DailyFortuneResult>> getDateFortune(
            @Parameter(description = "날짜 (YYYY-MM-DD 형식)", example = "2025-12-25")
            @PathVariable @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date,
            @Valid @RequestBody SajuRequest request) {
        
        log.info("📅 특정 날짜 운세 요청: {}", date);
        
        SajuResult saju = ganjiCalculatorService.calculateSaju(request);
        DailyFortuneResult fortune = dailyFortuneService.calculateDailyFortune(saju, date);
        
        log.info("✅ 특정 날짜 운세 계산 완료");
        return ResponseEntity.ok(com.fortune.dto.ApiResponse.success(fortune));
    }

    /**
     * 📜 토정비결 64괘
     * 
     * <p>전통 토정비결 64괘를 이용하여 연간 운세를 계산합니다.</p>
     * 
     * @param request 토정비결 계산 요청 정보 (생년월일, 대상년도)
     * @return 토정비결 64괘 운세 결과
     */
    @PostMapping("/tojeong")
    @Operation(
        summary = "📜 토정비결 64괘", 
        description = """
            전통 토정비결 64괘를 이용하여 연간 운세를 계산합니다.
            
            **토정비결 특징:**
            - 이조 토정 이지함 선생의 비결서 기반
            - 64괘 각각의 고유한 의미와 해석
            - 연간 전체 흐름과 월별 길흉
            - 전통적 지혜와 현대적 적용
            
            **제공 정보:**
            - 해당 괘의 이름과 상징
            - 연간 종합 운세 점수
            - 길한 달과 주의할 달
            - 월별 상세 운세
            - 구체적인 조언과 지침
            """
    )
    public ResponseEntity<com.fortune.dto.ApiResponse<TojeongResult>> calculateTojeong(
            @Valid @RequestBody TojeongRequest request) {
        
        log.info("📜 토정비결 계산 요청: {}년도", request.getTargetYear());
        
        TojeongResult result = tojeongBigyeolService.calculateTojeong(request);
        
        log.info("✅ 토정비결 계산 완료: {}괘 {}", result.getGwaNumber(), result.getGwaName());
        return ResponseEntity.ok(com.fortune.dto.ApiResponse.success(result));
    }

    /**
     * 🤖 AI 토정비결 해석
     * 
     * <p>토정비결 결과를 AI가 현대적 관점에서 재해석합니다.</p>
     * 
     * @param request 토정비결 계산 요청 정보
     * @return AI가 생성한 토정비결 현대적 해석
     */
    @PostMapping("/tojeong/ai-interpretation")
    @Operation(
        summary = "🤖 AI 토정비결 현대적 해석", 
        description = """
            전통 토정비결 괘를 AI가 현대적 관점에서 재해석합니다.
            
            **AI 해석 특징:**
            - 전통 지혜와 현대적 실용성 조화
            - 구체적이고 실행 가능한 조언
            - 시대적 상황을 반영한 적용법
            - 개인의 상황에 맞춘 맞춤형 가이드
            """
    )
    public ResponseEntity<com.fortune.dto.ApiResponse<String>> getAITojeongInterpretation(
            @Valid @RequestBody TojeongRequest request) {
        
        log.info("🤖 AI 토정비결 해석 요청");
        
        TojeongResult tojeongResult = tojeongBigyeolService.calculateTojeong(request);
        String interpretation = aiFortuneService.interpretTojeong(tojeongResult);
        
        log.info("✅ AI 토정비결 해석 완료");
        return ResponseEntity.ok(com.fortune.dto.ApiResponse.success(interpretation));
    }

    /**
     * 📆 간지달력 조회
     * 
     * <p>지정한 년월의 간지달력을 조회하여 길일과 흉일 정보를 제공합니다.</p>
     * 
     * @param year 조회할 년도 (예: 2025)
     * @param month 조회할 월 (1-12)
     * @return 간지달력 정보 (일별 간지, 길흉, 절기 등)
     */
    @GetMapping("/calendar/ganji/{year}/{month}")
    @Operation(
        summary = "📆 간지달력 조회", 
        description = """
            지정한 년월의 간지달력을 조회합니다.
            
            **제공 정보:**
            - 일별 간지 (천간과 지지)
            - 오행 정보
            - 길흉 수준과 점수
            - 길방위와 길한 색깔
            - 24절기 정보
            - 길일과 주의할 날
            - 월별 테마와 조언
            
            **활용 방법:**
            - 중요한 일정 계획 시 참고
            - 결혼식, 이사 등 길일 선택
            - 사업 시작일 결정
            - 여행이나 계약 날짜 검토
            """
    )
    public ResponseEntity<com.fortune.dto.ApiResponse<GanjiCalendarResponse>> getGanjiCalendar(
            @Parameter(description = "년도", example = "2025")
            @PathVariable int year,
            @Parameter(description = "월 (1-12)", example = "12")
            @PathVariable int month) {
        
        log.info("📆 간지달력 조회 요청: {}년 {}월", year, month);
        
        GanjiCalendarResponse calendar = ganjiCalendarService.generateMonthlyCalendar(year, month);
        
        log.info("✅ 간지달력 조회 완료: 총 {}일", calendar.getTotalDays());
        return ResponseEntity.ok(com.fortune.dto.ApiResponse.success(calendar));
    }

    /**
     * ⭐ 별자리 운세
     * 
     * <p>서양 점성술 기반으로 12별자리 운세를 제공합니다.</p>
     * 
     * @param request 별자리 운세 요청 정보 (생년월일, 대상날짜)
     * @return 별자리 운세 결과 (일일/월별 운세, 궁합, 행운의 요소)
     */
    @PostMapping("/zodiac")
    @Operation(
        summary = "⭐ 서양 별자리 운세", 
        description = """
            생년월일을 기준으로 12별자리 운세를 계산합니다.
            
            **12별자리 지원:**
            양자리, 황소자리, 쌍둥이자리, 게자리, 사자자리, 처녀자리,
            천칭자리, 전갈자리, 사수자리, 염소자리, 물병자리, 물고기자리
            
            **제공 정보:**
            - 일일 운세 (애정, 직업, 건강, 재물)
            - 월별 운세 트렌드
            - 궁합 좋은 별자리
            - 행운의 숫자, 색깔, 보석
            - 성격 특성 분석
            """
    )
    public ResponseEntity<com.fortune.dto.ApiResponse<ZodiacFortuneResult>> getZodiacFortune(
            @Valid @RequestBody ZodiacRequest request) {
        
        log.info("⭐ 별자리 운세 요청: {}", request.getBirthDate());
        
        ZodiacFortuneResult result = zodiacFortuneService.calculateZodiacFortune(
                request.getBirthDate(), request.getTargetDate());
        
        log.info("✅ 별자리 운세 계산 완료: {}", result.getZodiacKoreanName());
        return ResponseEntity.ok(com.fortune.dto.ApiResponse.success(result));
    }

    /**
     * 🤖 AI 별자리 분석
     * 
     * <p>별자리 운세를 AI가 더욱 상세하게 분석합니다.</p>
     * 
     * @param request 별자리 운세 요청 정보
     * @return AI가 생성한 별자리 심화 분석
     */
    @PostMapping("/zodiac/ai-analysis")
    @Operation(
        summary = "🤖 AI 별자리 심화 분석", 
        description = """
            별자리 운세를 AI가 더욱 상세하고 개인화된 관점에서 분석합니다.
            
            **AI 분석 내용:**
            - 우주 에너지 흐름 해석
            - 각 영역별 구체적 기회와 조언
            - 행운의 아이템 활용법
            - 궁합 별자리와의 관계 가이드
            - 시간대별 주의사항과 권장사항
            """
    )
    public ResponseEntity<com.fortune.dto.ApiResponse<String>> getAIZodiacAnalysis(
            @Valid @RequestBody ZodiacRequest request) {
        
        log.info("🤖 AI 별자리 분석 요청");
        
        ZodiacFortuneResult zodiacResult = zodiacFortuneService.calculateZodiacFortune(
                request.getBirthDate(), request.getTargetDate());
        String analysis = aiFortuneService.analyzeZodiacFortune(zodiacResult);
        
        log.info("✅ AI 별자리 분석 완료");
        return ResponseEntity.ok(com.fortune.dto.ApiResponse.success(analysis));
    }

    /**
     * 💬 AI 운세 질문 응답
     * 
     * <p>운세와 관련된 자연어 질문에 AI가 전문적으로 답변합니다.</p>
     * 
     * @param question 운세 관련 질문
     * @param context 추가 컨텍스트 (선택사항)
     * @return AI 답변 (비동기 처리)
     */
    @PostMapping("/ai/question")
    @Operation(
        summary = "💬 AI 운세 질문 응답", 
        description = """
            운세와 관련된 다양한 질문에 AI가 전문적으로 답변합니다.
            
            **질문 예시:**
            - "내 사주에서 재물운이 언제 좋아질까요?"
            - "이번 달에 이사하기 좋은 날은 언제인가요?"
            - "내 별자리와 잘 맞는 직업은 무엇인가요?"
            - "토정비결에서 말하는 길한 달의 의미는?"
            
            **AI 답변 특징:**
            - 전문적이면서 이해하기 쉬운 설명
            - 구체적이고 실용적인 조언 제공
            - 긍정적이고 건설적인 방향 제시
            - 전통 지혜와 현대적 관점의 조화
            """
    )
    public CompletableFuture<ResponseEntity<com.fortune.dto.ApiResponse<String>>> askFortuneQuestion(
            @Parameter(description = "운세 관련 질문", example = "내 사주에서 재물운은 어떤가요?")
            @RequestParam String question,
            @Parameter(description = "추가 컨텍스트 정보 (선택사항)")
            @RequestParam(required = false) String context) {
        
        log.info("💬 AI 운세 질문: {}", question.substring(0, Math.min(question.length(), 50)));
        
        return aiFortuneService.answerFortuneQuestion(question, context)
            .thenApply(answer -> {
                log.info("✅ AI 질문 응답 완료");
                return ResponseEntity.ok(com.fortune.dto.ApiResponse.success(answer));
            })
            .exceptionally(throwable -> {
                log.error("❌ AI 질문 응답 실패: {}", throwable.getMessage());
                return ResponseEntity.ok(com.fortune.dto.ApiResponse.error("질문 처리 중 오류가 발생했습니다."));
            });
    }
}