package com.fortune.service;

import com.fortune.enums.Zodiac;
import java.io.InputStream;
import java.time.Instant;
import java.util.List;
import org.junit.jupiter.api.Test;
import tools.jackson.databind.ObjectMapper;

import static org.assertj.core.api.Assertions.assertThat;

class WesternAstrologySwissGoldenFixtureTest {
    private static final String FIXTURE =
            "/fixtures/western-astrology-swiss-golden-v1.json";
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Test
    void staysWithinDocumentedSwissEphemerisTolerance() {
        InputStream stream = getClass().getResourceAsStream(FIXTURE);
        assertThat(stream).isNotNull();
        GoldenSuite suite = objectMapper.readValue(stream, GoldenSuite.class);

        assertThat(suite.schemaVersion())
                .isEqualTo("western-astrology-swiss-golden/v1");
        assertThat(suite.reference().name()).isEqualTo("Swiss Ephemeris");
        assertThat(suite.reference().version()).isEqualTo("v2.10.3final");
        assertThat(suite.reference().commit()).hasSize(40);
        assertThat(suite.cases()).hasSizeGreaterThanOrEqualTo(6);

        for (GoldenCase fixture : suite.cases()) {
            Instant instant = Instant.parse(fixture.input().instant());
            double julianDay = WesternAstrologyService.julianDay(instant);
            double sun = WesternAstrologyService.sunLongitude(julianDay);
            double moon = WesternAstrologyService.moonLongitude(julianDay);
            double rising = WesternAstrologyService.ascendantLongitude(
                    julianDay,
                    fixture.input().latitude(),
                    fixture.input().longitude());

            assertThat(Math.abs(julianDay - fixture.expected().julianDay()))
                    .as(fixture.id() + " Julian day")
                    .isLessThanOrEqualTo(0.0000001);
            assertThat(circularDifference(sun, fixture.expected().sunLongitude()))
                    .as(fixture.id() + " Sun")
                    .isLessThanOrEqualTo(suite.toleranceDegrees().sun());
            assertThat(circularDifference(moon, fixture.expected().moonLongitude()))
                    .as(fixture.id() + " Moon")
                    .isLessThanOrEqualTo(suite.toleranceDegrees().moon());
            assertThat(circularDifference(rising, fixture.expected().risingLongitude()))
                    .as(fixture.id() + " Rising")
                    .isLessThanOrEqualTo(suite.toleranceDegrees().rising());

            assertSignWhenAwayFromBoundary(
                    fixture.id() + " Sun sign",
                    sun,
                    fixture.expected().sunLongitude(),
                    suite.toleranceDegrees().sun());
            assertSignWhenAwayFromBoundary(
                    fixture.id() + " Moon sign",
                    moon,
                    fixture.expected().moonLongitude(),
                    suite.toleranceDegrees().moon());
            assertSignWhenAwayFromBoundary(
                    fixture.id() + " Rising sign",
                    rising,
                    fixture.expected().risingLongitude(),
                    suite.toleranceDegrees().rising());
        }
    }

    private void assertSignWhenAwayFromBoundary(
            String label,
            double actual,
            double expected,
            double tolerance) {
        double degreeInSign = normalize(expected) % 30.0;
        double boundaryDistance = Math.min(degreeInSign, 30.0 - degreeInSign);
        if (boundaryDistance > tolerance) {
            assertThat(WesternAstrologyService.zodiacAt(actual))
                    .as(label)
                    .isEqualTo(zodiacAt(expected));
        }
    }

    private Zodiac zodiacAt(double longitude) {
        return Zodiac.values()[(int) Math.floor(normalize(longitude) / 30.0) % 12];
    }

    private double circularDifference(double left, double right) {
        double difference = Math.abs(normalize(left) - normalize(right));
        return Math.min(difference, 360.0 - difference);
    }

    private double normalize(double value) {
        return ((value % 360.0) + 360.0) % 360.0;
    }

    private record GoldenSuite(
            String schemaVersion,
            Reference reference,
            Tolerance toleranceDegrees,
            List<GoldenCase> cases) {
    }

    private record Reference(
            String name,
            String version,
            String commit,
            String source,
            String flags,
            String houseFunction,
            String generationMethod,
            String coordinateContract) {
    }

    private record Tolerance(double sun, double moon, double rising) {
    }

    private record GoldenCase(
            String id,
            String evidence,
            Input input,
            Expected expected) {
    }

    private record Input(String instant, double latitude, double longitude) {
    }

    private record Expected(
            double julianDay,
            double sunLongitude,
            double moonLongitude,
            double risingLongitude) {
    }
}
