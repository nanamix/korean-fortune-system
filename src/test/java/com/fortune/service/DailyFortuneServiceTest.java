package com.fortune.service;

import com.fortune.dto.DailyFortuneResult;
import com.fortune.dto.SajuResult;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.time.LocalDate;
import java.util.ArrayList;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/*
 * 일주 운세 테스트 (1981년 3월 20일 남자, 양력, 01:59)
 * @author 하진영
 * @version 1.0
 * @since 2025-06-21
 */
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
     * 일주 운세 테스트 (1981년 3월 20일 남자, 양력, 01:59)
     */
    @Test
    public void testDailyFortuneWith1981TestData() {

        // 테스트용 사주 데이터: 1981년 3월 20일 남자, 양력, 01:59
        SajuResult saju = realGanjiCalculator.calculateCompleteSaju(
                1981, 3, 20, 1, 59, "M", "SOLAR"
        );
        LocalDate targetDate = LocalDate.of(2025, 6, 21);

        // Mock 설정
        when(ganjiCalculatorService.calculateDayPillar(targetDate)).thenReturn("갑자");
        when(sinsalService.calculateDailySinsals(targetDate, saju)).thenReturn(new ArrayList<>());

        DailyFortuneResult result = dailyFortuneService.calculateDailyFortune(saju, targetDate);

        assertNotNull(result);
        assertEquals(targetDate, result.getDate());
        assertNotNull(result.getDayPillar());
        assertTrue(result.getTotalScore() >= 0 && result.getTotalScore() <= 100);
        assertNotNull(result.getAdvice());
        assertNotNull(result.getLuckyDirection());
        assertNotNull(result.getLuckyColors());
        assertFalse(result.getLuckyColors().isEmpty());

        System.out.println("=== 1981년 3월 20일생 남성의 2025년 6월 21일 운세 ===");
        System.out.println("사주: " + saju.getFormattedSaju());
        System.out.println("오늘 일주: " + result.getDayPillar());
        System.out.println("종합 점수: " + result.getTotalScore());
        System.out.println("조언: " + result.getAdvice());
        System.out.println("길방위: " + result.getLuckyDirection());
        System.out.println("길한 색깔: " + result.getLuckyColors());
    }
}
