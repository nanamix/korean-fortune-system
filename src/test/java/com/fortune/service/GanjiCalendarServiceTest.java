package com.fortune.service;

import com.fortune.dto.GanjiCalendarResponse;
import com.fortune.dto.GanjiCalendarDay;
import org.junit.jupiter.api.Test;

import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class GanjiCalendarServiceTest {

    private final GanjiCalendarService service = new GanjiCalendarService(
            new GanjiCalculatorService(),
            new LunarCalendarService());

    @Test
    void usesOneConsistentScoreRuleForDayClassification() {
        GanjiCalendarResponse calendar = service.generateMonthlyCalendar(2026, 7);
        Set<Integer> allowedScores = Set.of(42, 54, 64, 70, 82);

        assertEquals(31, calendar.getDays().size());
        assertTrue(calendar.getDays().stream().map(GanjiCalendarDay::getFortuneScore).allMatch(allowedScores::contains));
        assertTrue(calendar.getDays().stream().allMatch(day -> day.isLuckyDay() == (day.getFortuneScore() >= 75)));
        assertTrue(calendar.getLuckyDays().stream().allMatch(dayNumber -> calendar.getDays().get(dayNumber - 1).getFortuneScore() >= 75));
        assertTrue(calendar.getCautionDays().stream().allMatch(dayNumber -> calendar.getDays().get(dayNumber - 1).getFortuneScore() <= 45));
        assertEquals(java.util.List.of("7일 소서", "23일 대서"), calendar.getSolarTerms());
        assertEquals("SOLAR_WITH_LUNAR", calendar.getCalendarBasis());
        assertEquals("TIME4J_ASTRONOMICAL_KOREA", calendar.getSolarTermsBasis());
        assertTrue(calendar.getDays().stream().allMatch(day -> day.getLunarMonth() >= 1 && day.getLunarMonth() <= 12));
        assertTrue(calendar.getDays().stream().allMatch(day -> day.getLunarDay() >= 1 && day.getLunarDay() <= 30));
        assertTrue(calendar.getDays().stream().allMatch(day -> day.getBriefAdvice().length() >= 90));
        assertTrue(calendar.getDays().stream().noneMatch(day -> day.getBriefAdvice().contains("무난한 하루입니다")));
        assertTrue(calendar.getDays().stream().allMatch(day -> day.getBriefAdvice().contains("관계")));
    }

    @Test
    void calculatesYearSpecificSolarTermsAndLunarDates() {
        GanjiCalendarResponse january = service.generateMonthlyCalendar(2023, 1);
        GanjiCalendarDay seollal = january.getDays().get(21);

        assertEquals(2023, seollal.getLunarYear());
        assertEquals(1, seollal.getLunarMonth());
        assertEquals(1, seollal.getLunarDay());
        assertTrue(january.getSolarTerms().contains("6일 소한"));
        assertTrue(january.getSolarTerms().contains("20일 대한"));
    }
}
