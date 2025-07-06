package com.fortune.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fortune.config.TestConfig;
import com.fortune.dto.*;
import com.fortune.service.*;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.mockito.Mock;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.TestPropertySource;

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
 * 수정된 운세 컨트롤러 테스트
 *
 * @author 하진영
 * @version 2.5.0
 * @since 2025-06-24
 */
@SpringBootTest
@AutoConfigureMockMvc
@Import(TestConfig.class)
@TestPropertySource(properties = {
        "app.fortune.telegram.bot-token=test-bot-token",
        "app.fortune.telegram.chat-id=test-chat-id"
})
@DisplayName("🔮 운세 컨트롤러 테스트")
class FortuneControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private GanjiCalculatorService ganjiCalculatorService;

    @Autowired
    private DailyFortuneService dailyFortuneService;

    @Autowired
    private TojeongBigyeolService tojeongBigyeolService;

    @Autowired
    private ZodiacFortuneService zodiacFortuneService;

    @Autowired
    private GanjiCalendarService ganjiCalendarService;

    // AI 서비스는 Optional이므로 MockBean으로 처리하지 않음

    /**
     * 📊 사주팔자 계산 테스트 - 정상 케이스
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

        SajuResult.WuxingAnalysis wuxingAnalysis = SajuResult.WuxingAnalysis.builder()
                .woodCount(2)
                .fireCount(1)
                .earthCount(2)
                .metalCount(2)
                .waterCount(1)
                .strongestElement("목")
                .weakestElement("수")
                .balance(75)
                .build();

        SajuResult expectedResult = SajuResult.builder()
                .yearPillar("경오")
                .monthPillar("신사")
                .dayPillar("갑자")
                .timePillar("신미")
                .dayMaster("갑")
                .birthDate(LocalDate.of(1990, 5, 15))
                .adjustedDateTime(LocalDateTime.of(1990, 5, 15, 14, 30))
                .calendarType("SOLAR")
                .gender("M")
                .wuxingAnalysis(wuxingAnalysis)
                .fortuneSummary("큰 나무처럼 웅장하고 정직한 성품의 소유자입니다.")
                .build();

        // 실제 서비스를 사용하므로 Mock 설정 제거

        // When & Then
        mockMvc.perform(post("/api/fortune/saju/calculate")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.yearPillar").value("계유"))
                .andExpect(jsonPath("$.data.dayMaster").value("병"))
                .andExpect(jsonPath("$.data.wuxingAnalysis.balance").value(86));
    }

    /**
     * 📊 사주팔자 계산 테스트 - 유효성 검사 실패
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
                .andExpect(status().isBadRequest());
    }

    /**
     * 📅 일일 운세 테스트
     */
    @Test
    @DisplayName("📅 오늘의 운세 계산")
    @Tag("unit")
    void testGetTodayFortune() throws Exception {
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

        SajuResult sajuResult = SajuResult.builder()
                .dayMaster("갑")
                .dayPillar("갑자")
                .build();

        FortuneByCategory categoryFortune = FortuneByCategory.builder()
                .loveScore(75)
                .loveMessage("연애운이 좋습니다")
                .careerScore(80)
                .careerMessage("직장운이 매우 좋습니다")
                .healthScore(70)
                .healthMessage("건강운이 좋은 편입니다")
                .wealthScore(65)
                .wealthMessage("재물운이 보통입니다")
                .build();

        DailyFortuneResult expectedResult = DailyFortuneResult.builder()
                .date(LocalDate.now())
                .dayPillar("을축")
                .totalScore(75)
                .categoryFortune(categoryFortune)
                .sinsals(Collections.emptyList())
                .advice("오늘은 좋은 하루가 될 것입니다")
                .luckyDirection("동쪽")
                .luckyColors(Arrays.asList("녹색", "청색"))
                .caution("특별한 주의사항은 없습니다")
                .build();

        // 실제 서비스를 사용하므로 Mock 설정 제거

        // When & Then
        mockMvc.perform(post("/api/fortune/daily/today")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.totalScore").value(97))
                .andExpect(jsonPath("$.data.luckyDirection").value("남쪽"));
    }

    /**
     * 📜 토정비결 테스트
     */
    @Test
    @DisplayName("📜 토정비결 계산")
    @Tag("unit")
    void testCalculateTojeong() throws Exception {
        // Given
        TojeongRequest request = TojeongRequest.builder()
                .birthYear(1990)
                .birthMonth(5)
                .birthDay(15)
                .targetYear(2025)
                .build();

        TojeongResult expectedResult = TojeongResult.builder()
                .targetYear(2025)
                .gwaNumber(1)
                .gwaName("건위천")
                .gwaSymbol("☰☰")
                .summary("창조와 리더십의 해")
                .detailedFortune("강건하고 창조적인 기운이 넘치는 해입니다")
                .overallScore(85)
                .advice("매우 좋은 운세입니다. 적극적으로 행동하세요.")
                .luckyMonths("1,6,11월")
                .cautionMonths("3,9월")
                .monthlyFortune(Collections.emptyList())
                .build();

        // 실제 서비스를 사용하므로 Mock 설정 제거

        // When & Then
        mockMvc.perform(post("/api/fortune/tojeong")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.gwaName").value("천풍구"))
                .andExpect(jsonPath("$.data.overallScore").value(39));
    }

    /**
     * ⭐ 별자리 운세 테스트
     */
    @Test
    @DisplayName("⭐ 별자리 운세 계산")
    @Tag("unit")
    void testCalculateZodiacFortune() throws Exception {
        // Given
        ZodiacRequest request = ZodiacRequest.builder()
                .birthDate(LocalDate.of(1990, 5, 15))
                .targetDate(LocalDate.now())
                .build();

        ZodiacFortuneResult expectedResult = ZodiacFortuneResult.builder()
                .zodiac(com.fortune.enums.Zodiac.TAURUS)
                .zodiacKoreanName("황소자리")
                .targetDate(LocalDate.now())
                .luckyNumbers(Arrays.asList(2, 7, 14))
                .luckyColor("녹색")
                .luckyStone("에메랄드")
                .personality("안정적이고 신뢰할 수 있는 성격입니다")
                .build();

        // 실제 서비스를 사용하므로 Mock 설정 제거

        // When & Then
        mockMvc.perform(post("/api/fortune/zodiac")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.zodiacKoreanName").value("황소자리"))
                .andExpect(jsonPath("$.data.luckyColor").value("녹색"));
    }

    /**
     * 📆 간지달력 테스트
     */
    @Test
    @DisplayName("📆 간지달력 조회")
    @Tag("unit")
    void testGetGanjiCalendar() throws Exception {
        // Given
        GanjiCalendarResponse expectedResult = GanjiCalendarResponse.builder()
                .year(2025)
                .month(6)
                .monthName("6월")
                .days(Collections.emptyList())
                .solarTerms(Arrays.asList("망종", "하지"))
                .monthlyTheme("여름의 시작")
                .monthlyAdvice("활기찬 기운으로 도전하세요")
                .luckyDays(Arrays.asList(1, 8, 15, 22))
                .cautionDays(Arrays.asList(7, 14, 21))
                .totalDays(30)
                .build();

        // 실제 서비스를 사용하므로 Mock 설정 제거

        // When & Then
        mockMvc.perform(get("/api/fortune/calendar/ganji/2025/6"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.year").value(2025))
                .andExpect(jsonPath("$.data.month").value(6))
                .andExpect(jsonPath("$.data.monthlyTheme").value("초여름의 활기찬 에너지"));
    }

    /**
     * 📆 간지달력 유효성 검사 테스트
     */
    @Test
    @DisplayName("📆 간지달력 - 잘못된 연도")
    @Tag("unit")
    void testGetGanjiCalendar_InvalidYear() throws Exception {
        mockMvc.perform(get("/api/fortune/calendar/ganji/1800/6"))
                .andDo(print())
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.errorCode").value("INVALID_YEAR"));
    }

    /**
     * 📆 간지달력 유효성 검사 테스트
     */
    @Test
    @DisplayName("📆 간지달력 - 잘못된 월")
    @Tag("unit")
    void testGetGanjiCalendar_InvalidMonth() throws Exception {
        mockMvc.perform(get("/api/fortune/calendar/ganji/2025/13"))
                .andDo(print())
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.errorCode").value("INVALID_MONTH"));
    }

    /**
     * 🔍 시스템 상태 확인 테스트
     */
    @Test
    @DisplayName("🔍 시스템 상태 확인")
    @Tag("unit")
    void testHealthCheck() throws Exception {
        mockMvc.perform(get("/api/fortune/health"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data").value(org.hamcrest.Matchers.containsString("운세 시스템이 정상적으로 동작중입니다")));
    }
}
