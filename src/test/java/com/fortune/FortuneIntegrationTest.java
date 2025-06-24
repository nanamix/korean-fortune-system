package com.fortune;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fortune.dto.*;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureWebMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;

/**
 * 수정된 운세 시스템 통합 테스트
 *
 * @author 하진영
 * @version 2.5.0
 * @since 2025-06-24
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc
@ActiveProfiles("test")
@TestPropertySource(properties = {
        "app.fortune.ai.enabled=false", // 테스트에서는 AI 기능 비활성화
        "app.fortune.cache.enabled=false", // 테스트에서는 캐시 비활성화
        "spring.jpa.hibernate.ddl-auto=create-drop",
        "logging.level.com.fortune=DEBUG"
})
@Transactional
@DisplayName("🔄 운세 시스템 통합 테스트")
@Tag("integration")
class FortuneIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    /**
     * 🔄 전체 운세 계산 플로우 테스트
     */
    @Test
    @DisplayName("🔄 전체 운세 서비스 플로우 테스트")
    void testCompleteFortuneFlow() throws Exception {
        // 1. 시스템 상태 확인
        mockMvc.perform(get("/api/fortune/health"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));

        // 2. 사주팔자 계산
        SajuRequest sajuRequest = SajuRequest.builder()
                .birthYear(1990)
                .birthMonth(5)
                .birthDay(15)
                .birthHour(14)
                .birthMinute(30)
                .gender("M")
                .calendarType("SOLAR")
                .build();

        String sajuResponse = mockMvc.perform(post("/api/fortune/saju/calculate")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(sajuRequest)))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.dayMaster").exists())
                .andExpect(jsonPath("$.data.yearPillar").exists())
                .andExpect(jsonPath("$.data.wuxingAnalysis").exists())
                .andReturn().getResponse().getContentAsString();

        // 3. 오늘의 운세
        mockMvc.perform(post("/api/fortune/daily/today")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(sajuRequest)))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.totalScore").exists())
                .andExpect(jsonPath("$.data.categoryFortune").exists())
                .andExpect(jsonPath("$.data.luckyDirection").exists())
                .andExpect(jsonPath("$.data.luckyColors").isArray());

        // 4. 특정 날짜 운세
        mockMvc.perform(post("/api/fortune/daily")
                        .param("targetDate", "2025-12-25")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(sajuRequest)))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.date").value("2025-12-25"));

        // 5. 토정비결
        TojeongRequest tojeongRequest = TojeongRequest.builder()
                .birthYear(1990)
                .birthMonth(5)
                .birthDay(15)
                .targetYear(2025)
                .build();

        mockMvc.perform(post("/api/fortune/tojeong")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(tojeongRequest)))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.gwaNumber").exists())
                .andExpect(jsonPath("$.data.gwaName").exists())
                .andExpect(jsonPath("$.data.overallScore").exists());

        // 6. 별자리 운세
        ZodiacRequest zodiacRequest = ZodiacRequest.builder()
                .birthDate(LocalDate.of(1990, 5, 15))
                .targetDate(LocalDate.now())
                .build();

        mockMvc.perform(post("/api/fortune/zodiac")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(zodiacRequest)))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.zodiacKoreanName").exists())
                .andExpect(jsonPath("$.data.luckyColor").exists());

        // 7. 간지달력 조회
        mockMvc.perform(get("/api/fortune/calendar/ganji/2025/12"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.year").value(2025))
                .andExpect(jsonPath("$.data.month").value(12))
                .andExpect(jsonPath("$.data.days").isArray())
                .andExpect(jsonPath("$.data.luckyDays").isArray());
    }

    /**
     * 📈 성능 테스트
     */
    @Test
    @DisplayName("📈 API 성능 테스트")
    @Tag("performance")
    void testApiPerformance() throws Exception {
        SajuRequest request = SajuRequest.builder()
                .birthYear(1990)
                .birthMonth(5)
                .birthDay(15)
                .birthHour(14)
                .birthMinute(30)
                .gender("M")
                .calendarType("SOLAR")
                .build();

        // 사주 계산 성능 테스트
        long startTime = System.currentTimeMillis();

        mockMvc.perform(post("/api/fortune/saju/calculate")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk());

        long endTime = System.currentTimeMillis();
        long responseTime = endTime - startTime;

        System.out.println("📊 사주 계산 응답 시간: " + responseTime + "ms");

        // 성능 기준: 2000ms 이내 (통합 테스트이므로 여유있게)
        assert responseTime < 2000 : "API 응답 시간이 너무 깁니다: " + responseTime + "ms";

        // 연속 요청 성능 테스트
        startTime = System.currentTimeMillis();

        for (int i = 0; i < 5; i++) {
            mockMvc.perform(post("/api/fortune/saju/calculate")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isOk());
        }

        endTime = System.currentTimeMillis();
        long averageTime = (endTime - startTime) / 5;

        System.out.println("📊 평균 응답 시간 (5회): " + averageTime + "ms");
        assert averageTime < 1500 : "평균 응답 시간이 너무 깁니다: " + averageTime + "ms";
    }

    /**
     * 🔒 보안 테스트
     */
    @Test
    @DisplayName("🔒 보안 설정 테스트")
    @Tag("security")
    void testSecurityConfiguration() throws Exception {
        // CORS 헤더 확인
        mockMvc.perform(options("/api/fortune/saju/calculate")
                        .header("Origin", "http://localhost:3000")
                        .header("Access-Control-Request-Method", "POST"))
                .andDo(print())
                .andExpect(status().isOk());

        // 보안 헤더 확인
        mockMvc.perform(get("/api/fortune/calendar/ganji/2025/12"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(header().exists("X-Content-Type-Options"))
                .andExpect(header().exists("X-Frame-Options"));
    }

    /**
     * 🧪 데이터 유효성 검사 테스트
     */
    @Test
    @DisplayName("🧪 입력 데이터 유효성 검사 테스트")
    @Tag("validation")
    void testInputValidation() throws Exception {
        // 잘못된 사주 요청
        SajuRequest invalidRequest = SajuRequest.builder()
                .birthYear(1800) // 유효 범위 밖
                .birthMonth(13)  // 잘못된 월
                .birthDay(32)    // 잘못된 일
                .birthHour(25)   // 잘못된 시간
                .birthMinute(70) // 잘못된 분
                .gender("X")     // 잘못된 성별
                .calendarType("INVALID") // 잘못된 달력 타입
                .build();

        mockMvc.perform(post("/api/fortune/saju/calculate")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidRequest)))
                .andDo(print())
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false));

        // 잘못된 토정비결 요청
        TojeongRequest invalidTojeong = TojeongRequest.builder()
                .birthYear(1800)
                .birthMonth(13)
                .birthDay(32)
                .targetYear(1900) // 너무 이른 년도
                .build();

        mockMvc.perform(post("/api/fortune/tojeong")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidTojeong)))
                .andDo(print())
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false));

        // 잘못된 간지달력 요청
        mockMvc.perform(get("/api/fortune/calendar/ganji/1800/13"))
                .andDo(print())
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false));
    }

    /**
     * 🔄 동시성 테스트
     */
    @Test
    @DisplayName("🔄 동시성 테스트")
    @Tag("concurrency")
    void testConcurrency() throws Exception {
        SajuRequest request = SajuRequest.builder()
                .birthYear(1990)
                .birthMonth(5)
                .birthDay(15)
                .birthHour(14)
                .birthMinute(30)
                .gender("M")
                .calendarType("SOLAR")
                .build();

        // 동시 요청 시뮬레이션
        Runnable task = () -> {
            try {
                mockMvc.perform(post("/api/fortune/saju/calculate")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request)))
                        .andExpect(status().isOk())
                        .andExpect(jsonPath("$.success").value(true));
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        };

        // 여러 스레드에서 동시 실행
        Thread[] threads = new Thread[3];
        for (int i = 0; i < threads.length; i++) {
            threads[i] = new Thread(task);
        }

        for (Thread thread : threads) {
            thread.start();
        }

        for (Thread thread : threads) {
            thread.join(5000); // 5초 타임아웃
        }

        System.out.println("✅ 동시성 테스트 완료");
    }

    /**
     * 📊 다양한 사주 데이터 테스트
     */
    @Test
    @DisplayName("📊 다양한 사주 데이터 정확성 테스트")
    @Tag("accuracy")
    void testVariousSajuData() throws Exception {
        // 테스트 케이스들
        Object[][] testCases = {
                {1981, 3, 20, 1, 59, "M", "SOLAR"}, // 원본 테스트 데이터
                {1990, 5, 15, 14, 30, "F", "SOLAR"}, // 여성, 오후
                {2000, 1, 1, 0, 0, "M", "SOLAR"},   // 새천년 자정
                {1985, 12, 31, 23, 59, "F", "LUNAR"}, // 음력, 연말
                {1995, 6, 21, 12, 0, "M", "SOLAR"}   // 하지, 정오
        };

        for (Object[] testCase : testCases) {
            SajuRequest request = SajuRequest.builder()
                    .birthYear((Integer) testCase[0])
                    .birthMonth((Integer) testCase[1])
                    .birthDay((Integer) testCase[2])
                    .birthHour((Integer) testCase[3])
                    .birthMinute((Integer) testCase[4])
                    .gender((String) testCase[5])
                    .calendarType((String) testCase[6])
                    .build();

            String response = mockMvc.perform(post("/api/fortune/saju/calculate")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.data.dayMaster").exists())
                    .andExpect(jsonPath("$.data.yearPillar").exists())
                    .andExpect(jsonPath("$.data.monthPillar").exists())
                    .andExpect(jsonPath("$.data.dayPillar").exists())
                    .andExpect(jsonPath("$.data.timePillar").exists())
                    .andReturn().getResponse().getContentAsString();

            System.out.printf("✅ 테스트 케이스 %d/%d/%d %d:%d %s %s - 통과%n",
                    (Integer) testCase[0], (Integer) testCase[1], (Integer) testCase[2],
                    (Integer) testCase[3], (Integer) testCase[4], testCase[5], testCase[6]);
        }
    }

    /**
     * 🌐 API 문서 접근성 테스트
     */
    @Test
    @DisplayName("🌐 API 문서 접근성 테스트")
    @Tag("documentation")
    void testApiDocumentation() throws Exception {
        // Swagger UI 접근 테스트
        mockMvc.perform(get("/swagger-ui.html"))
                .andDo(print())
                .andExpect(status().isOk());

        // API 문서 JSON 접근 테스트
        mockMvc.perform(get("/api-docs"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON));
    }

    /**
     * 🔧 에러 처리 테스트
     */
    @Test
    @DisplayName("🔧 에러 처리 및 복구 테스트")
    @Tag("error-handling")
    void testErrorHandling() throws Exception {
        // 잘못된 JSON 형식
        mockMvc.perform(post("/api/fortune/saju/calculate")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{invalid json}"))
                .andDo(print())
                .andExpect(status().isBadRequest());

        // 빈 요청 본문
        mockMvc.perform(post("/api/fortune/saju/calculate")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andDo(print())
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false));

        // 존재하지 않는 엔드포인트
        mockMvc.perform(get("/api/fortune/nonexistent"))
                .andDo(print())
                .andExpect(status().isNotFound());

        // 잘못된 HTTP 메서드
        mockMvc.perform(put("/api/fortune/health"))
                .andDo(print())
                .andExpect(status().isMethodNotAllowed());
    }

    /**
     * 🎯 실제 사용 시나리오 테스트
     */
    @Test
    @DisplayName("🎯 실제 사용 시나리오 통합 테스트")
    @Tag("scenario")
    void testRealWorldScenario() throws Exception {
        // 시나리오: 새로운 사용자가 운세 앱을 사용하는 과정

        // 1. 앱 상태 확인
        mockMvc.perform(get("/api/fortune/health"))
                .andExpect(status().isOk());

        // 2. 사주 입력 및 계산
        SajuRequest sajuRequest = SajuRequest.builder()
                .birthYear(1987)
                .birthMonth(8)
                .birthDay(24)
                .birthHour(9)
                .birthMinute(15)
                .gender("F")
                .calendarType("SOLAR")
                .build();

        // 3. 기본 사주 정보 조회
        mockMvc.perform(post("/api/fortune/saju/calculate")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(sajuRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.dayMaster").exists());

        // 4. 오늘 운세 확인
        mockMvc.perform(post("/api/fortune/daily/today")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(sajuRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.advice").exists());

        // 5. 이번 달 간지달력 확인
        LocalDate now = LocalDate.now();
        mockMvc.perform(get("/api/fortune/calendar/ganji/" + now.getYear() + "/" + now.getMonthValue()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.luckyDays").isArray());

        // 6. 별자리 운세도 확인
        ZodiacRequest zodiacRequest = ZodiacRequest.builder()
                .birthDate(LocalDate.of(1987, 8, 24))
                .targetDate(LocalDate.now())
                .build();

        mockMvc.perform(post("/api/fortune/zodiac")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(zodiacRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.zodiacKoreanName").exists());

        // 7. 올해 토정비결 확인
        TojeongRequest tojeongRequest = TojeongRequest.builder()
                .birthYear(1987)
                .birthMonth(8)
                .birthDay(24)
                .targetYear(now.getYear())
                .build();

        mockMvc.perform(post("/api/fortune/tojeong")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(tojeongRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.gwaName").exists());

        System.out.println("✅ 실제 사용 시나리오 테스트 완료");
    }
}
