package com.fortune.service;


import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.BeforeEach;
import com.fortune.dto.SajuRequest;
import com.fortune.dto.SajuResult;
import static org.junit.jupiter.api.Assertions.*;

class GanjiCalculatorServiceTest {

    private GanjiCalculatorService ganjiCalculatorService;

    @BeforeEach
    void setUp() {
        ganjiCalculatorService = new GanjiCalculatorService();
    }

    @Test
    @DisplayName("사주팔자 계산 - 기본 테스트")
    void testCalculateSaju() {
        // Given
        SajuRequest request = new SajuRequest();
        request.setBirthYear(1981);
        request.setBirthMonth(3);
        request.setBirthDay(20);
        request.setBirthHour(1);
        request.setBirthMinute(59);
        request.setGender("M");
        request.setCalendarType("SOLAR");

        // When
        SajuResult result = ganjiCalculatorService.calculateSaju(request);

        // Then
        assertNotNull(result);
        assertEquals("신유", result.getYearPillar());
        assertNotNull(result.getMonthPillar());
        assertNotNull(result.getDayPillar());
        assertNotNull(result.getTimePillar());
        assertNotNull(result.getDayMaster());

        System.out.println("계산 결과: " + result.getFormattedSaju());
        System.out.println("계산 결과(요약): " + result.getFortuneSummary());
        System.out.println("연주: " + result.getYearPillar());
        System.out.println("일간: " + result.getDayMaster());
    }

    @Test
    @DisplayName("다양한 생년월일 테스트")
    void testVariousBirthDates() {
        // 여러 날짜 테스트
        int[][] testDates = {
                {1990, 5, 15, 12, 30},
                {2000, 12, 25, 23, 59},
                {1975, 8, 8, 6, 0}
        };

        for (int[] testDate : testDates) {
            SajuRequest request = new SajuRequest();
            request.setBirthYear(testDate[0]);
            request.setBirthMonth(testDate[1]);
            request.setBirthDay(testDate[2]);
            request.setBirthHour(testDate[3]);
            request.setBirthMinute(testDate[4]);
            request.setGender("F");
            request.setCalendarType("SOLAR");

            SajuResult result = ganjiCalculatorService.calculateSaju(request);

            assertNotNull(result);
            assertNotNull(result.getFormattedSaju());
            System.out.println(String.format("%d-%d-%d %d:%d -> %s",
                    testDate[0], testDate[1], testDate[2], testDate[3], testDate[4],
                    result.getFortuneSummary()));
        }
    }
}
