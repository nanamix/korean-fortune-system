package com.fortune.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fortune.dto.*;
import com.fortune.service.*;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Collections;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;

/**
 * 🧪 운세 컨트롤러 테스트
 * 
 * <p>FortuneController의 모든 엔드포인트에 대한 단위 테스트입니다.</p>
 * 
 * <h3>테스트 범위</h3>
 * <ul>
 *   <li>📊 사주팔자 계산 API</li>
 *   <li>📅 일일 운세 API</li>
 *   <li>📜 토정비결 API</li>
 *   <li>⭐ 별자리 운세 API</li>
 *   <li>📆 간지달력 API</li>
 *   <li>🤖 AI 기능 API</li>
 * </ul>
 * 
 * @author 테스트팀
 * @version 2.0.0
 * @since 2025-06-23
 */
@WebMvcTest(FortuneController.class)
@DisplayName("🔮 운세 컨트롤러 테스트")
class FortuneControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private GanjiCalculatorService ganjiCalculatorService;

    @MockBean
    private DailyFortuneService dailyFortuneService;

    @MockBean
    private TojeongBigyeolService tojeongBigyeolService;

    @MockBean
    private ZodiacFortuneService zodiacFortuneService;

    @MockBean
    private GanjiCalendarService ganjiCalendarService;

    @MockBean
    private AIFortuneService aiFortuneService;

    /**
     * 📊 사주팔자 계산 테스트
     */
    @Test
    @DisplayName("📊 사주팔자 계산 - 정상 케이스")
    @Tag("unit")
    void testCalculateSaju_Success() throws Exception {
        // Given
        SajuRequest request = SajuRequest.builder()
                .birthYear(1990)
                .birthMonth(5)
                .birthDay(15)
                .birthHour(14)
                .birthMinute(30)
                .gender("M")
                .calendarType("SOLAR")
                .build();

        SajuResult expectedResult = SajuResult.builder()
                .yearPillar("경오")
                .monthPillar("신사")
                .dayPillar("갑자")
                .timePillar("신미")
                .dayMaster("갑")
                .birthDate(LocalDate.of(1990, 5, 15))
                .adjustedDateTime(LocalDateTime.of(1990, 5, 15, 14, 0))
                .calendarType("SOLAR")
                .gender("M")
                .build();

        given(ganjiCalculatorService.calculateSaju(any(SajuRequest.class)))
                .willReturn(expectedResult);

        // When & Then
        mockMvc.perform(post("/api/fortune/saju/calculate")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.yearPillar").value("경오"))
                .andExpect(jsonPath("$.data.dayMaster").value("갑"));
    }

    /**
     * 📊 사주팔자 계산 - 유효성 검사 실패
     */
    @Test
    @DisplayName("📊 사주팔자 계산 - 잘못된 입력값")
    @Tag("unit")
    void testCalculateSaju_InvalidInput() throws Exception {
        // Given
        SajuRequest invalidRequest = SajuRequest.builder()
                .birthYear(1800) // 유효 범위를 벗어난 년도
                .birthMonth(13)  // 잘못된 월
                .birthDay(32)    // 잘못된 일
                .birthHour(25)   // 잘못된 시간
                .birthMinute(70) // 잘못된 분
                .gender("X")     // 잘못된 성별
                .calendarType("INVALID")
                .build();

        // When & Then
        mockMvc.perform(post("/api/fortune/saju/calculate")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(invalidRequest)))
                .andDo(print())
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false));
    }

    /**
     * 📅 오늘의 운세 테스트
     */
    @Test
    @DisplayName("📅 오늘의 운세 - 정상 케이스")
    @Tag("unit")
    void testGetTodayFortune_Success() throws Exception {
        // Given
        SajuRequest request = createValidSajuRequest();
        SajuResult sajuResult = createMockSajuResult();
        DailyFortuneResult expectedFortune = createMockDailyFortune();

        given(ganjiCalculatorService.calculateSaju(any(SajuRequest.class)))
                .willReturn(sajuResult);
        given(dailyFortuneService.calculateDailyFortune(any(SajuResult.class), any(LocalDate.class)))
                .willReturn(expectedFortune);

        // When & Then
        mockMvc.perform(post("/api/fortune/daily/today")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.totalScore").value(75))
                .andExpect(jsonPath("$.data.dayPillar").value("갑자"));
    }

    /**
     * 📜 토정비결 테스트
     */
    @Test
    @DisplayName("📜 토정비결 계산 - 정상 케이스")
    @Tag("unit")
    void testCalculateTojeong_Success() throws Exception {
        // Given
        TojeongRequest request = new TojeongRequest();
        request.setBirthYear(1990);
        request.setBirthMonth(5);
        request.setBirthDay(15);
        request.setTargetYear(2025);

        TojeongResult expectedResult = TojeongResult.builder()
                .targetYear(2025)
                .gwaNumber(1)
                .gwaName("건위천")
                .gwaSymbol("☰☰")
                .summary("하늘의 기운이 충만하다")
                .detailedFortune("올해는 하늘의 도움을 받아 모든 일이 순조롭게 풀릴 것입니다.")
                .overallScore(90)
                .advice("적극적으로 행동하세요")
                .luckyMonths(Arrays.asList(1, 4, 7, 10))
                .cautionMonths(Collections.emptyList())
                .build();

        given(tojeongBigyeolService.calculateTojeong(any(TojeongRequest.class)))
                .willReturn(expectedResult);

        // When & Then
        mockMvc.perform(post("/api/fortune/tojeong")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.gwaName").value("건위천"))
                .andExpect(jsonPath("$.data.overallScore").value(90));
    }

    /**
     * 📆 간지달력 조회 테스트
     */
    @Test
    @DisplayName("📆 간지달력 조회 - 정상 케이스")
    @Tag("unit")
    void testGetGanjiCalendar_Success() throws Exception {
        // Given
        GanjiCalendarResponse expectedCalendar = GanjiCalendarResponse.builder()
                .year(2025)
                .month(12)
                .monthName("12월")
                .days(Collections.emptyList())
                .monthlyTheme("마무리와 정리")
                .monthlyAdvice("한 해를 마무리하며 새해를 준비하세요")
                .luckyDays(Arrays.asList(1, 15, 30))
                .cautionDays(Arrays.asList(13))
                .totalDays(31)
                .build();

        given(ganjiCalendarService.generateMonthlyCalendar(2025, 12))
                .willReturn(expectedCalendar);

        // When & Then
        mockMvc.perform(get("/api/fortune/calendar/ganji/2025/12"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.year").value(2025))
                .andExpect(jsonPath("$.data.month").value(12))
                .andExpect(jsonPath("$.data.totalDays").value(31));
    }

    /**
     * ⭐ 별자리 운세 테스트
     */
    @Test
    @DisplayName("⭐ 별자리 운세 - 정상 케이스")
    @Tag("unit")
    void testGetZodiacFortune_Success() throws Exception {
        // Given
        ZodiacRequest request = new ZodiacRequest();
        request.setBirthDate(LocalDate.of(1990, 5, 15));
        request.setTargetDate(LocalDate.now());

        ZodiacFortuneResult expectedResult = ZodiacFortuneResult.builder()
                .zodiacKoreanName("황소자리")
                .targetDate(LocalDate.now())
                .luckyColor("초록색")
                .luckyStone("에메랄드")
                .luckyNumbers(Arrays.asList(7, 14, 21))
                .personality("안정을 추구하며 끈기가 있는 현실적인 성격입니다.")
                .build();

        given(zodiacFortuneService.calculateZodiacFortune(any(LocalDate.class), any(LocalDate.class)))
                .willReturn(expectedResult);

        // When & Then
        mockMvc.perform(post("/api/fortune/zodiac")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.zodiacKoreanName").value("황소자리"))
                .andExpect(jsonPath("$.data.luckyColor").value("초록색"));
    }

    /**
     * 🤖 AI 질문 응답 테스트
     */
    @Test
    @DisplayName("🤖 AI 질문 응답 - 정상 케이스")
    @Tag("integration")
    void testAskFortuneQuestion_Success() throws Exception {
        // Given
        String question = "내 사주에서 재물운은 어떤가요?";
        String expectedAnswer = "귀하의 사주를 분석한 결과, 재물운이 점진적으로 상승하는 흐름을 보입니다...";

        given(aiFortuneService.answerFortuneQuestion(question, null))
                .willReturn(java.util.concurrent.CompletableFuture.completedFuture(expectedAnswer));

        // When & Then
        mockMvc.perform(post("/api/fortune/ai/question")
                .param("question", question))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data").value(expectedAnswer));
    }

    // === 헬퍼 메서드들 ===

    private SajuRequest createValidSajuRequest() {
        return SajuRequest.builder()
                .birthYear(1990)
                .birthMonth(5)
                .birthDay(15)
                .birthHour(14)
                .birthMinute(30)
                .gender("M")
                .calendarType("SOLAR")
                .build();
    }

    private SajuResult createMockSajuResult() {
        return SajuResult.builder()
                .yearPillar("경오")
                .monthPillar("신사")
                .dayPillar("갑자")
                .timePillar("신미")
                .dayMaster("갑")
                .birthDate(LocalDate.of(1990, 5, 15))
                .calendarType("SOLAR")
                .gender("M")
                .build();
    }

    private DailyFortuneResult createMockDailyFortune() {
        return DailyFortuneResult.builder()
                .date(LocalDate.now())
                .dayPillar("갑자")
                .totalScore(75)
                .categoryFortune(FortuneByCategory.builder()
                        .overall(75)
                        .love(70)
                        .money(80)
                        .health(75)
                        .career(78)
                        .build())
                .advice("좋은 하루가 될 것입니다.")
                .luckyDirection("동쪽")
                .luckyColors(Arrays.asList("녹색", "청색"))
                .sinsals(Collections.emptyList())
                .build();
    }
}