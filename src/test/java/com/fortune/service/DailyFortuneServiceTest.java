package com.fortune.service;

import com.fortune.dto.DailyFortuneResult;
import com.fortune.dto.SajuResult;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.time.LocalDate;
import java.util.ArrayList;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * 수정된 일일 운세 서비스 테스트
 *
 * @author 하진영
 * @version 2.5.0
 * @since 2025-06-24
 */
@DisplayName("📅 일일 운세 서비스 테스트")
public class DailyFortuneServiceTest {

    @Mock
    private GanjiCalculatorService ganjiCalculatorService;

    @Mock
    private SinsalService sinsalService;

    @InjectMocks
    private DailyFortuneService dailyFortuneService;

    private GanjiCalculatorService realGanjiCalculator;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        realGanjiCalculator = new GanjiCalculatorService();
    }

    /**
     * 일일 운세 테스트 (1981년 3월 20일 남자, 양력, 01:59)
     */
    @Test
    @DisplayName("📅 1981년 3월 20일생 남성의 일일 운세 테스트")
    public void testDailyFortuneWith1981TestData() {
        // Given: 테스트용 사주 데이터 (1981년 3월 20일 남자, 양력, 01:59)
        SajuResult saju = realGanjiCalculator.calculateCompleteSaju(
                1981, 3, 20, 1, 59, "M", "SOLAR"
        );
        LocalDate targetDate = LocalDate.of(2025, 6, 24);

        // Mock 설정
        when(ganjiCalculatorService.calculateDayPillar(targetDate)).thenReturn("갑자");
        when(sinsalService.calculateDailySinsals(targetDate, saju)).thenReturn(new ArrayList<>());

        // When: 일일 운세 계산
        DailyFortuneResult result = dailyFortuneService.calculateDailyFortune(saju, targetDate);

        // Then: 결과 검증
        assertNotNull(result);
        assertEquals(targetDate, result.getDate());
        assertNotNull(result.getDayPillar());
        assertTrue(result.getTotalScore() >= 0 && result.getTotalScore() <= 100);
        assertNotNull(result.getAdvice());
        assertNotNull(result.getLuckyDirection());
        assertNotNull(result.getLuckyColors());
        assertFalse(result.getLuckyColors().isEmpty());
        assertNotNull(result.getCategoryFortune());

        // 결과 출력
        System.out.println("=== 1981년 3월 20일생 남성의 2025년 6월 24일 운세 ===");
        System.out.println("사주: " + saju.getFormattedSaju());
        System.out.println("오늘 일주: " + result.getDayPillar());
        System.out.println("종합 점수: " + result.getTotalScore());
        System.out.println("조언: " + result.getAdvice());
        System.out.println("길방위: " + result.getLuckyDirection());
        System.out.println("길한 색깔: " + result.getLuckyColors());
        System.out.println("연애운: " + result.getCategoryFortune().getLoveScore());
        System.out.println("직장운: " + result.getCategoryFortune().getCareerScore());
        System.out.println("건강운: " + result.getCategoryFortune().getHealthScore());
        System.out.println("재물운: " + result.getCategoryFortune().getWealthScore());
    }

    /**
     * 다양한 일간에 대한 일일 운세 테스트
     */
    @Test
    @DisplayName("📅 다양한 일간별 운세 테스트")
    public void testDailyFortuneForVariousDayMasters() {
        LocalDate targetDate = LocalDate.of(2025, 6, 24);

        // 10개 천간에 대해 테스트
        String[] dayMasters = {"갑", "을", "병", "정", "무", "기", "경", "신", "임", "계"};

        for (String dayMaster : dayMasters) {
            // Given
            SajuResult saju = SajuResult.builder()
                    .dayMaster(dayMaster)
                    .dayPillar(dayMaster + "자")
                    .yearPillar("갑자")
                    .monthPillar("을축")
                    .timePillar("병인")
                    .build();

            when(ganjiCalculatorService.calculateDayPillar(targetDate)).thenReturn("정묘");
            when(sinsalService.calculateDailySinsals(targetDate, saju)).thenReturn(new ArrayList<>());

            // When
            DailyFortuneResult result = dailyFortuneService.calculateDailyFortune(saju, targetDate);

            // Then
            assertNotNull(result, dayMaster + " 일간의 운세 결과가 null입니다");
            assertTrue(result.getTotalScore() >= 0 && result.getTotalScore() <= 100,
                    dayMaster + " 일간의 총점이 유효 범위를 벗어났습니다: " + result.getTotalScore());
            assertNotNull(result.getAdvice(), dayMaster + " 일간의 조언이 null입니다");

            System.out.println(dayMaster + "일간 운세 - 총점: " + result.getTotalScore() +
                    ", 길방위: " + result.getLuckyDirection());
        }
    }
}
