package com.fortune.service;

import com.fortune.dto.ZodiacFortuneResult;
import com.fortune.enums.Zodiac;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.time.LocalTime;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ZodiacFortuneServiceTest {

    private final ZodiacFortuneService service = new ZodiacFortuneService();

    @Test
    void returnsDetailedDeterministicFortune() {
        LocalDate birthDate = LocalDate.of(1981, 9, 1);
        LocalDate targetDate = LocalDate.of(2026, 7, 15);

        var request = com.fortune.dto.ZodiacRequest.builder()
                .birthDate(birthDate)
                .birthTime(LocalTime.of(9, 30))
                .birthLatitude(37.5665)
                .birthLongitude(126.978)
                .timeZone("Asia/Seoul")
                .calendarType("SOLAR")
                .targetDate(targetDate)
                .build();
        ZodiacFortuneResult first = service.calculateZodiacFortune(request);
        ZodiacFortuneResult second = service.calculateZodiacFortune(request);

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
        assertTrue(first.getTodayFortune().getScoreBasis().contains("출생 차트"));
        assertTrue(first.getTodayFortune().getScoreBasis().contains("transit"));
        assertTrue(first.getTodayFortune().getScoreBasis().contains("날짜 리듬"));
        assertEquals(LocalDate.of(2026, 7, 13), first.getWeeklyFortune().getStartDate());
        assertEquals(LocalDate.of(2026, 7, 19), first.getWeeklyFortune().getEndDate());
        assertEquals(7, first.getWeeklyFortune().getDays().size());
        assertEquals(first.getWeeklyFortune(), second.getWeeklyFortune());
        int expectedWeeklyAverage = Math.round((float) first.getWeeklyFortune().getDays().stream()
                .mapToInt(day -> day.getOverallScore())
                .average()
                .orElse(0));
        assertEquals(expectedWeeklyAverage, first.getWeeklyFortune().getOverallScore());
        assertTrue(first.getWeeklyFortune().getScoreBasis().contains("7일 종합 점수"));
        assertTrue(first.getWeeklyFortune().getOverview().contains("이번 주의 평균"));
        assertTrue(first.getWeeklyFortune().getBestDate() != null);
        assertTrue(first.getMonthlyFortune().getDetailedMessage().length() >= 180);
        assertTrue(first.getMonthlyFortune().getOpportunity().length() >= 120);
        assertTrue(first.getMonthlyFortune().getCaution().length() >= 120);
        assertTrue(first.getMonthlyFortune().getScoreBasis().contains("중립 기준 60점"));
        assertTrue(first.getMonthlyFortune().getScoreBasis().contains("tropical-approx-v1"));
        assertTrue(first.getPersonality().length() >= 140);
        assertEquals("BIRTH_TIME_LOCATION", first.getAstrologyProfile().getPrecision());
        assertEquals(Zodiac.VIRGO, first.getAstrologyProfile().getSunSign());
        assertTrue(first.getAstrologyProfile().getMoonSign() != null);
        assertTrue(first.getAstrologyProfile().getRisingSign() != null);
        assertTrue(first.getTransitSummary().length() >= 30);
    }
}
