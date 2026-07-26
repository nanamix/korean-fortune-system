package com.fortune.service;

import com.fortune.dto.ZodiacFortuneResult;
import com.fortune.enums.Zodiac;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ZodiacFortuneServiceTest {

    private final ZodiacFortuneService service = new ZodiacFortuneService();

    @Test
    void returnsDetailedDeterministicFortune() {
        LocalDate birthDate = LocalDate.of(1981, 9, 1);
        LocalDate targetDate = LocalDate.of(2026, 7, 15);

        ZodiacFortuneResult first = service.calculateZodiacFortune(birthDate, targetDate);
        ZodiacFortuneResult second = service.calculateZodiacFortune(birthDate, targetDate);

        assertEquals(Zodiac.VIRGO, first.getZodiac());
        assertEquals(targetDate, first.getTargetDate());
        assertEquals(first.getTodayFortune(), second.getTodayFortune());
        assertTrue(first.getTodayFortune().getOverallMessage().contains("영역("));
        assertTrue(first.getTodayFortune().getOverallMessage().length() >= 180);
        assertTrue(first.getTodayFortune().getLoveMessage().length() >= 150);
        assertTrue(first.getTodayFortune().getCareerMessage().length() >= 150);
        assertTrue(first.getTodayFortune().getHealthMessage().length() >= 150);
        assertTrue(first.getTodayFortune().getMoneyMessage().length() >= 150);
        int expectedAverage = Math.round((
                first.getTodayFortune().getLoveScore()
                        + first.getTodayFortune().getCareerScore()
                        + first.getTodayFortune().getHealthScore()
                        + first.getTodayFortune().getMoneyScore()) / 4.0f);
        assertEquals(expectedAverage, first.getTodayFortune().getOverallScore());
        assertTrue(first.getTodayFortune().getScoreBasis().contains("중립 기준 60점"));
        assertTrue(first.getTodayFortune().getScoreBasis().contains("별자리 리듬"));
        assertTrue(first.getTodayFortune().getScoreBasis().contains("날짜 리듬"));
        assertTrue(first.getMonthlyFortune().getDetailedMessage().length() >= 180);
        assertTrue(first.getMonthlyFortune().getOpportunity().length() >= 120);
        assertTrue(first.getMonthlyFortune().getCaution().length() >= 120);
        assertTrue(first.getMonthlyFortune().getScoreBasis().contains("중립 기준 60점"));
        assertTrue(first.getPersonality().length() >= 140);
    }
}
