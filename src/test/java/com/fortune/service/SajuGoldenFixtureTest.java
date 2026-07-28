package com.fortune.service;

import com.fortune.dto.SajuRequest;
import com.fortune.dto.SajuResult;
import java.io.InputStream;
import java.util.List;
import org.junit.jupiter.api.Test;
import tools.jackson.databind.ObjectMapper;

import static org.assertj.core.api.Assertions.assertThat;

class SajuGoldenFixtureTest {
    private final GanjiCalculatorService service = new GanjiCalculatorService();
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Test
    void matchesVersionedGoldenFixtures() {
        InputStream stream = getClass().getResourceAsStream("/fixtures/saju-golden-v2.json");
        assertThat(stream).isNotNull();
        GoldenSuite suite = objectMapper.readValue(stream, GoldenSuite.class);

        assertThat(suite.schemaVersion()).isEqualTo("saju-golden/v2");
        assertThat(suite.cases()).hasSizeGreaterThanOrEqualTo(11);

        for (GoldenCase fixture : suite.cases()) {
            SajuResult actual = service.calculateSaju(SajuRequest.builder()
                    .birthYear(fixture.input().year())
                    .birthMonth(fixture.input().month())
                    .birthDay(fixture.input().day())
                    .birthHour(fixture.input().hour())
                    .birthMinute(fixture.input().minute())
                    .birthSecond(fixture.input().second())
                    .gender(fixture.input().gender())
                    .calendarType(fixture.input().calendarType())
                    .leapMonth(fixture.input().leapMonth())
                    .birthLongitude(fixture.input().birthLongitude())
                    .applyEquationOfTime(fixture.input().applyEquationOfTime())
                    .applyHistoricalDst(fixture.input().applyHistoricalDst())
                    .build());

            Expected expected = fixture.expected();
            assertThat(actual.getYearPillar()).as(fixture.id()).isEqualTo(expected.yearPillar());
            assertThat(actual.getMonthPillar()).as(fixture.id()).isEqualTo(expected.monthPillar());
            assertThat(actual.getDayPillar()).as(fixture.id()).isEqualTo(expected.dayPillar());
            assertThat(actual.getTimePillar()).as(fixture.id()).isEqualTo(expected.timePillar());
            assertThat(actual.getDayMaster()).as(fixture.id()).isEqualTo(expected.dayMaster());
            if (expected.adjustedDateTime() != null) {
                assertThat(actual.getAdjustedDateTime().toString())
                        .as(fixture.id()).isEqualTo(expected.adjustedDateTime());
            }
            if (expected.daeunForward() != null) {
                assertThat(actual.isDaeunForward()).as(fixture.id()).isEqualTo(expected.daeunForward());
            }
            if (expected.daeunStartAge() != null) {
                assertThat(actual.getDaeunNumber()).as(fixture.id()).isEqualTo(expected.daeunStartAge());
            }
            if (expected.firstDaeun() != null) {
                assertThat(actual.getDaeun().getFirst().getGanji())
                        .as(fixture.id()).isEqualTo(expected.firstDaeun());
            }
            if (expected.yearStemSipsin() != null) {
                assertThat(actual.getYearDetail().getStemSipsin())
                        .as(fixture.id()).isEqualTo(expected.yearStemSipsin());
            }
            if (expected.dayTwelveStage() != null) {
                assertThat(actual.getDayDetail().getTwelveStage())
                        .as(fixture.id()).isEqualTo(expected.dayTwelveStage());
            }
        }
    }

    private record GoldenSuite(String schemaVersion, List<GoldenCase> cases) {
    }

    private record GoldenCase(String id, String evidence, Input input, Expected expected) {
    }

    private record Input(
            int year,
            int month,
            int day,
            int hour,
            int minute,
            Integer second,
            String gender,
            String calendarType,
            boolean leapMonth,
            Double birthLongitude,
            Boolean applyEquationOfTime,
            Boolean applyHistoricalDst) {
    }

    private record Expected(
            String yearPillar,
            String monthPillar,
            String dayPillar,
            String timePillar,
            String dayMaster,
            Boolean daeunForward,
            Integer daeunStartAge,
            String firstDaeun,
            String yearStemSipsin,
            String dayTwelveStage,
            String adjustedDateTime) {
    }
}
