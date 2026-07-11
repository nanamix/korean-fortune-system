package com.fortune.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.BeforeEach;
import com.fortune.dto.TojeongRequest;
import com.fortune.dto.TojeongResult;
import static org.junit.jupiter.api.Assertions.*;

/**
 * 토정비결 서비스 테스트
 *
 * @author 하진영
 * @version 2.5.0
 * @since 2025-06-24
 */

class TojeongBigyeolServiceTest {

    private TojeongBigyeolService tojeongService;

    @BeforeEach
    void setUp() {
        tojeongService = new TojeongBigyeolService(new GanjiCalculatorService());
    }

    /**
     * 토정비결 계산 테스트
     * <p>기본적인 토정비결 계산을 수행하고 결과를 검증합니다.</p>
     * <p>이 테스트는 1981년 3월 20일에 태어난 사람의 2025년 토정비결을 계산합니다.</p>
     * <p>결과는 괘 번호, 괘 이름, 조언, 점수 등을 포함합니다.</p>
     * <p>토정비결은 한국 전통의 운세 시스템으로, 개인의 생년월일을 기반으로 특정 해의 운세를 예측합니다.</p>
     * <p>이 테스트는 토정비결 서비스의 기본적인 기능을 검증하며, 입력값에 대한 일관성을 확인합니다.</p>
     * <p>토정비결의 결과는 괘 번호(1~64), 괘 이름, 조언, 점수(0~100) 등을 포함합니다.</p>
     * <p>이 테스트는 토정비결 서비스가 올바르게 작동하는지 확인하기 위한 기본적인 테스트입니다.</p>
     * @param void
     * @return void
     */
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
        // 정통 토정비결 = 144괘 (상8 × 중6 × 하3)
        assertTrue(result.getGwaNumber() >= 1 && result.getGwaNumber() <= 144);
        assertNotNull(result.getGwaName());
        assertNotNull(result.getAdvice());
        assertTrue(result.getOverallScore() >= 0 && result.getOverallScore() <= 100);

        System.out.println("토정비결 결과:");
        System.out.println("괘: " + result.getGwaNumber() + "번 " + result.getGwaName());
        System.out.println("점수: " + result.getOverallScore());
        System.out.println("조언: " + result.getAdvice());
    }

    /**
     * 토정비결 일관성 테스트
     * <p>같은 입력에 대해 여러 번 계산했을 때 결과가 일관되어야 함을 검증합니다.</p>
     * <p>이 테스트는 동일한 생년월일과 대상 연도를 가진 요청에 대해 두 번 계산하여 결과가 동일한지 확인합니다.</p>
     * <p>토정비결의 결과는 매번 동일해야 하며, 이는 서비스의 일관성을 보장합니다.</p>
     * @param void
     * @return void
     */
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

        // 결과가 일관되어야 함 (결정론성: 같은 입력 = 같은 출력)
        assertEquals(result1.getGwaNumber(), result2.getGwaNumber());
        assertEquals(result1.getGwaName(), result2.getGwaName());
        assertEquals(result1.getOverallScore(), result2.getOverallScore());
        // 월별 운세까지 완전 결정론적이어야 함
        assertEquals(result1.getMonthlyFortune().get(0).getScore(),
                result2.getMonthlyFortune().get(0).getScore());
        assertEquals(result1.getLuckyMonths(), result2.getLuckyMonths());
    }
}
