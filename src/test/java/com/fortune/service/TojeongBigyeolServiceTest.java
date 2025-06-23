package com.fortune.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.BeforeEach;
import com.fortune.dto.TojeongRequest;
import com.fortune.dto.TojeongResult;
import static org.junit.jupiter.api.Assertions.*;

class TojeongBigyeolServiceTest {

    private TojeongBigyeolService tojeongService;

    @BeforeEach
    void setUp() {
        tojeongService = new TojeongBigyeolService();
    }

    @Test
    @DisplayName("토정비결 계산 - 기본 테스트")
    void testCalculateTojeong() {
        // Given
        TojeongRequest request = new TojeongRequest();
        request.setBirthYear(1981);
        request.setBirthMonth(3);
        request.setBirthDay(20);
        request.setTargetYear(2025);

        // When
        TojeongResult result = tojeongService.calculateTojeong(request);

        // Then
        assertNotNull(result);
        assertEquals(2025, result.getTargetYear());
        assertTrue(result.getGwaNumber() >= 1 && result.getGwaNumber() <= 64);
        assertNotNull(result.getGwaName());
        assertNotNull(result.getAdvice());
        assertTrue(result.getOverallScore() >= 0 && result.getOverallScore() <= 100);

        System.out.println("토정비결 결과:");
        System.out.println("괘: " + result.getGwaNumber() + "번 " + result.getGwaName());
        System.out.println("점수: " + result.getOverallScore());
        System.out.println("조언: " + result.getAdvice());
    }

    @Test
    @DisplayName("토정비결 일관성 테스트")
    void testTojeongConsistency() {
        TojeongRequest request = new TojeongRequest();
        request.setBirthYear(1981);
        request.setBirthMonth(3);
        request.setBirthDay(20);
        request.setTargetYear(2025);

        // 같은 입력에 대해 여러 번 계산
        TojeongResult result1 = tojeongService.calculateTojeong(request);
        TojeongResult result2 = tojeongService.calculateTojeong(request);

        // 결과가 일관되어야 함
        assertEquals(result1.getGwaNumber(), result2.getGwaNumber());
        assertEquals(result1.getGwaName(), result2.getGwaName());
        assertEquals(result1.getOverallScore(), result2.getOverallScore());
    }
}
