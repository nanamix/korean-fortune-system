package com.fortune.service;


import com.fortune.dto.SajuResult;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import java.time.LocalDate;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@ActiveProfiles("test")
public class GanjiCalculatorServiceTest {

    @Autowired
    private GanjiCalculatorService ganjiCalculator;

    @Test
    public void testSajuCalculationWith1981TestData() {
        // 테스트 데이터: 1981년 3월 20일 남자, 양력, 01:59
        SajuResult result = ganjiCalculator.calculateCompleteSaju(
                1981, 3, 20, 1, 59, "M", "SOLAR"
        );

        assertNotNull(result);
        assertNotNull(result.getYearPillar());
        assertNotNull(result.getMonthPillar());
        assertNotNull(result.getDayPillar());
        assertNotNull(result.getTimePillar());
        assertNotNull(result.getDayMaster());

        assertEquals("M", result.getGender());
        assertEquals("SOLAR", result.getCalendarType());
        assertEquals("남성", result.getGenderDescription());
        assertEquals("양력", result.getCalendarDescription());

        // 1981년은 신유년
        assertEquals("신유", result.getYearPillar());

        // 3월 20일의 일주 계산 (1981년 3월 20일 = 을미일)
        System.out.println("계산된 사주: " + result.getFormattedSaju());
        System.out.println("일간(본인): " + result.getDayMaster());
        System.out.println("생일: " + result.getBirthDate());
        System.out.println("보정 시간: " + result.getAdjustedDateTime());
    }

    @Test
    public void testLunarCalendarConversion() {
        // 음력 생일 테스트
        SajuResult result = ganjiCalculator.calculateCompleteSaju(
                1981, 3, 20, 1, 59, "M", "LUNAR"
        );

        assertNotNull(result);
        assertEquals("LUNAR", result.getCalendarType());
        assertNotNull(result.getLunarBirthDate());
        assertEquals("1981년 3월 20일", result.getLunarBirthDate());

        // 음력을 양력으로 변환한 실제 생일이 다를 것
        System.out.println("음력 생일: " + result.getLunarBirthDate());
        System.out.println("양력 변환 생일: " + result.getBirthDate());
    }

    @Test
    public void testSolarTimeAdjustment() {
        // 태양시 보정 테스트
        SajuResult result = ganjiCalculator.calculateCompleteSaju(
                1981, 3, 20, 1, 59, "M", "SOLAR"
        );

        // 01:59 -> 태양시 보정 (약 32분 늦춤) -> 01:27
        LocalDateTime expected = LocalDateTime.of(1981, 3, 20, 1, 27);
        assertEquals(expected, result.getAdjustedDateTime());

        // 시주는 보정된 시간으로 계산 (01:27 = 축시)
        assertTrue(result.getTimePillar().endsWith("축"));
    }
}
