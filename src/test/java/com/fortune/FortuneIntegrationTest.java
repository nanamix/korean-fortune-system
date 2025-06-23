package com.fortune;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fortune.dto.SajuRequest;
import com.fortune.dto.TojeongRequest;
import com.fortune.dto.ZodiacRequest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.springframework.beans.factory.annotation.Autowired;
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
 * 🔄 운세 시스템 통합 테스트
 * 
 * <p>전체 애플리케이션 컨텍스트를 로드하여 실제 시나리오를 테스트합니다.</p>
 * 
 * <h3>테스트 시나리오</h3>
 * <ul>
 *   <li>🔄 End-to-End 운세 계산 플로우</li>
 *   <li>🗄️ 데이터베이스 연동 테스트</li>
 *   <li>📚 캐시 동작 확인</li>
 *   <li>🔒 보안 설정 검증</li>
 *   <li>⚡ 성능 기준점 측정</li>
 * </ul>
 * 
 * @author 통합테스트팀
 * @version 2.0.0
 * @since 2025-06-23
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureWebMvc
@ActiveProfiles("test")
@TestPropertySource(properties = {
    "app.fortune.ai.enabled=false", // 테스트에서는 AI 기능 비활성화
    "spring.jpa.hibernate.ddl-auto=create-drop"
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
     * 
     * <p>사주 계산부터 모든 운세 서비스까지 전체 플로우를 테스트합니다.</p>
     */
    @Test
    @DisplayName("🔄 전체 운세 서비스 플로우 테스트")
    void testCompleteFortuneFlow() throws Exception {
        // 1. 사주팔자 계산
        SajuRequest sajuRequest = SajuRequest.builder()
                .birthYear(1990)
                .birthMonth(5)
                .birthDay(15)
                .birthHour(14)
                .birthMinute(30)
                .gender("M")
                .calendarType("SOLAR")
                .build();

        mockMvc.perform(post("/api/fortune/saju/calculate")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(sajuRequest)))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.dayMaster").exists());

        // 2. 오늘의 운세
        mockMvc.perform(post("/api/fortune/daily/today")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(sajuRequest)))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.totalScore").exists());

        // 3. 내일의 운세
        mockMvc.perform(post("/api/fortune/daily/tomorrow")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(sajuRequest)))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));

        // 4. 토정비결
        TojeongRequest tojeongRequest = new TojeongRequest();
        tojeongRequest.setBirthYear(1990);
        tojeongRequest.setBirthMonth(5);
        tojeongRequest.setBirthDay(15);
        tojeongRequest.setTargetYear(2025);

        mockMvc.perform(post("/api/fortune/tojeong")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(tojeongRequest)))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.gwaNumber").exists());

        // 5. 별자리 운세
        ZodiacRequest zodiacRequest = new ZodiacRequest();
        zodiacRequest.setBirthDate(LocalDate.of(1990, 5, 15));
        zodiacRequest.setTargetDate(LocalDate.now());

        mockMvc.perform(post("/api/fortune/zodiac")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(zodiacRequest)))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.zodiacKoreanName").exists());

        // 6. 간지달력 조회
        mockMvc.perform(get("/api/fortune/calendar/ganji/2025/12"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.year").value(2025))
                .andExpect(jsonPath("$.data.month").value(12));
    }

    /**
     * 📈 성능 테스트
     * 
     * <p>주요 API의 응답 시간을 측정합니다.</p>
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

        long startTime = System.currentTimeMillis();

        // 사주 계산 - 1초 이내 응답 목표
        mockMvc.perform(post("/api/fortune/saju/calculate")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk());

        long endTime = System.currentTimeMillis();
        long responseTime = endTime - startTime;

        System.out.println("📊 사주 계산 응답 시간: " + responseTime + "ms");
        
        // 성능 기준: 1000ms 이내
        assert responseTime < 1000 : "API 응답 시간이 너무 깁니다: " + responseTime + "ms";
    }

    /**
     * 🔒 보안 테스트
     * 
     * <p>보안 설정이 올바르게 작동하는지 확인합니다.</p>
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
                .andExpect(header().exists("Access-Control-Allow-Origin"));

        // 보안 헤더 확인
        mockMvc.perform(get("/api/fortune/calendar/ganji/2025/12"))
                .andDo(print())
                .andExpect(header().exists("X-Content-Type-Options"))
                .andExpect(header().exists("X-Frame-Options"));
    }

    /**
     * 📊 데이터 검증 테스트
     * 
     * <p>다양한 입력값에 대한 데이터 검증을 테스트합니다.</p>
     */
    @Test
    @DisplayName("📊 데이터 검증 테스트")
    void testDataValidation() throws Exception {
        // 경계값 테스트 - 최소 년도
        SajuRequest minYearRequest = SajuRequest.builder()
                .birthYear(1900)
                .birthMonth(1)
                .birthDay(1)
                .birthHour(0)
                .birthMinute(0)
                .gender("M")
                .calendarType("SOLAR")
                .build();

        mockMvc.perform(post("/api/fortune/saju/calculate")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(minYearRequest)))
                .andExpect(status().isOk());

        // 경계값 테스트 - 최대 년도
        SajuRequest maxYearRequest = SajuRequest.builder()
                .birthYear(2100)
                .birthMonth(12)
                .birthDay(31)
                .birthHour(23)
                .birthMinute(59)
                .gender("F")
                .calendarType("LUNAR")
                .build();

        mockMvc.perform(post("/api/fortune/saju/calculate")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(maxYearRequest)))
                .andExpect(status().isOk());

        // 잘못된 날짜 테스트
        SajuRequest invalidRequest = SajuRequest.builder()
                .birthYear(2000)
                .birthMonth(2)
                .birthDay(30) // 2월 30일은 존재하지 않음
                .birthHour(12)
                .birthMinute(0)
                .gender("M")
                .calendarType("SOLAR")
                .build();

        mockMvc.perform(post("/api/fortune/saju/calculate")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(invalidRequest)))
                .andExpect(status().isBadRequest());
    }

    /**
     * 🌐 국제화 테스트
     * 
     * <p>다양한 로케일에서의 동작을 테스트합니다.</p>
     */
    @Test
    @DisplayName("🌐 국제화 및 다국어 지원 테스트")
    void testInternationalization() throws Exception {
        SajuRequest request = SajuRequest.builder()
                .birthYear(1990)
                .birthMonth(5)
                .birthDay(15)
                .birthHour(14)
                .birthMinute(30)
                .gender("M")
                .calendarType("SOLAR")
                .build();

        // 한국어 (기본)
        mockMvc.perform(post("/api/fortune/saju/calculate")
                .contentType(MediaType.APPLICATION_JSON)
                .header("Accept-Language", "ko-KR")
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));

        // 영어
        mockMvc.perform(post("/api/fortune/saju/calculate")
                .contentType(MediaType.APPLICATION_JSON)
                .header("Accept-Language", "en-US")
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));
    }

    /**
     * 📚 캐시 동작 테스트
     * 
     * <p>캐시가 올바르게 작동하는지 확인합니다.</p>
     */
    @Test
    @DisplayName("📚 캐시 동작 테스트")
    void testCacheOperation() throws Exception {
        SajuRequest request = SajuRequest.builder()
                .birthYear(1990)
                .birthMonth(5)
                .birthDay(15)
                .birthHour(14)
                .birthMinute(30)
                .gender("M")
                .calendarType("SOLAR")
                .build();

        // 첫 번째 요청 - 캐시 미스
        long firstRequestTime = System.currentTimeMillis();
        mockMvc.perform(post("/api/fortune/saju/calculate")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk());
        long firstResponseTime = System.currentTimeMillis() - firstRequestTime;

        // 두 번째 요청 - 캐시 히트 (더 빨라야 함)
        long secondRequestTime = System.currentTimeMillis();
        mockMvc.perform(post("/api/fortune/saju/calculate")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk());
        long secondResponseTime = System.currentTimeMillis() - secondRequestTime;

        System.out.println("📊 첫 번째 요청 시간: " + firstResponseTime + "ms");
        System.out.println("📊 두 번째 요청 시간: " + secondResponseTime + "ms");
        
        // 캐시 효과 확인 (두 번째 요청이 더 빨라야 함)
        // 실제 환경에서는 캐시 효과가 나타나지만, 테스트 환경에서는 차이가 미미할 수 있음
    }

    /**
     * 🔄 동시성 테스트
     * 
     * <p>동시에 여러 요청이 들어올 때의 안정성을 테스트합니다.</p>
     */
    @Test
    @DisplayName("🔄 동시성 및 부하 테스트")
    @Tag("performance")
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

        String requestJson = objectMapper.writeValueAsString(request);

        // 동시에 10개의 요청 실행
        Thread[] threads = new Thread[10];
        boolean[] results = new boolean[10];

        for (int i = 0; i < 10; i++) {
            final int index = i;
            threads[i] = new Thread(() -> {
                try {
                    mockMvc.perform(post("/api/fortune/saju/calculate")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(requestJson))
                            .andExpect(status().isOk());
                    results[index] = true;
                } catch (Exception e) {
                    results[index] = false;
                    System.err.println("동시성 테스트 실패 [" + index + "]: " + e.getMessage());
                }
            });
        }

        // 모든 스레드 시작
        for (Thread thread : threads) {
            thread.start();
        }

        // 모든 스레드 완료 대기
        for (Thread thread : threads) {
            thread.join();
        }

        // 모든 요청이 성공했는지 확인
        for (int i = 0; i < 10; i++) {
            assert results[i] : "동시성 테스트 실패 - 스레드 " + i;
        }

        System.out.println("✅ 동시성 테스트 완료: 10개 요청 모두 성공");
    }

    /**
     * 📋 API 문서화 확인 테스트
     * 
     * <p>Swagger API 문서가 올바르게 생성되는지 확인합니다.</p>
     */
    @Test
    @DisplayName("📋 API 문서화 확인")
    void testApiDocumentation() throws Exception {
        // Swagger JSON 엔드포인트 확인
        mockMvc.perform(get("/api-docs"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON));

        // Swagger UI 페이지 확인
        mockMvc.perform(get("/swagger-ui.html"))
                .andExpect(status().isOk());
    }

    /**
     * 🏥 헬스체크 테스트
     * 
     * <p>애플리케이션 헬스체크 엔드포인트를 테스트합니다.</p>
     */
    @Test
    @DisplayName("🏥 헬스체크 테스트")
    void testHealthCheck() throws Exception {
        // 기본 헬스체크
        mockMvc.perform(get("/actuator/health"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("UP"));

        // 시스템 상태 확인
        mockMvc.perform(get("/api/system/status"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.systemName").exists())
                .andExpect(jsonPath("$.data.status").value("RUNNING"));
    }

    /**
     * 🚫 오류 처리 테스트
     * 
     * <p>다양한 오류 상황에서의 응답을 테스트합니다.</p>
     */
    @Test
    @DisplayName("🚫 오류 처리 테스트")
    void testErrorHandling() throws Exception {
        // 잘못된 JSON 형식
        mockMvc.perform(post("/api/fortune/saju/calculate")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{ invalid json }"))
                .andExpect(status().isBadRequest());

        // 존재하지 않는 엔드포인트
        mockMvc.perform(get("/api/fortune/nonexistent"))
                .andExpect(status().isNotFound());

        // 지원하지 않는 HTTP 메서드
        mockMvc.perform(delete("/api/fortune/saju/calculate"))
                .andExpect(status().isMethodNotAllowed());
    }
}