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
        assertTrue(first.getTodayFortune().getCareerMessage().length() >= 70);
        assertTrue(first.getMonthlyFortune().getDetailedMessage().length() >= 80);
        assertTrue(first.getMonthlyFortune().getOpportunity().length() >= 50);
        assertTrue(first.getMonthlyFortune().getCaution().length() >= 50);
    }
}
