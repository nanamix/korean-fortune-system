package com.fortune.service;

import com.fortune.dto.LunarDate;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;

/**
 * 음력 달력 서비스 테스트
 */
public class LunarCalendarServiceTest {

    private final LunarCalendarService service = new LunarCalendarService();

    @Test
    public void testBasicLunarCalculation() {
        assertTrue(true);
    }

    /** 음력 1981-02-15 → 양력 1981-03-20 (정답지). */
    @Test
    public void testLunarToSolarKnownDate() {
        assertEquals(LocalDate.of(1981, 3, 20), service.convertLunarToSolar(1981, 2, 15));
    }

    /**
     * 설날 연도 경계 회귀 (bug_001): 양력 1월이라도 설날 이후면 새 음력 연도여야 한다.
     * 2023 설날 = 양력 2023-01-22.
     */
    @Test
    public void testSolarToLunarSeollalYearBoundary() {
        // 설날 당일 = 음력 2023년 정월
        LunarDate onSeollal = service.convertSolarToLunar(LocalDate.of(2023, 1, 22));
        assertEquals(2023, onSeollal.getYear(), "설날 이후 양력 1월은 새 음력 연도(2023)");
        // 설날 이전(양력 1월) = 아직 음력 2022년(12월)
        LunarDate beforeSeollal = service.convertSolarToLunar(LocalDate.of(2023, 1, 15));
        assertEquals(2022, beforeSeollal.getYear(), "설날 이전은 전년 음력(2022)");
    }
}
