package com.fortune.service;

import com.fortune.dto.ZodiacRequest;
import com.fortune.enums.Zodiac;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalTime;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class WesternAstrologyServiceTest {
    private final WesternAstrologyService service = new WesternAstrologyService();

    @Test
    void calculatesDeterministicBigThreeAndAnnualSunTransit() {
        ZodiacRequest request = ZodiacRequest.builder()
                .birthDate(LocalDate.of(1981, 9, 1))
                .birthTime(LocalTime.of(9, 30))
                .birthLatitude(37.5665)
                .birthLongitude(126.978)
                .timeZone("Asia/Seoul")
                .targetDate(LocalDate.of(2026, 9, 1))
                .build();

        WesternAstrologyService.Analysis first =
                service.analyze(request, request.getBirthDate());
        WesternAstrologyService.Analysis second =
                service.analyze(request, request.getBirthDate());

        assertThat(first.profile()).isEqualTo(second.profile());
        assertThat(first.profile().getSunSign()).isEqualTo(Zodiac.VIRGO);
        assertThat(first.profile().getMoonSign()).isNotNull();
        assertThat(first.profile().getRisingSign()).isNotNull();
        assertThat(first.profile().getPrecision()).isEqualTo("BIRTH_TIME_LOCATION");
        assertThat(first.transits())
                .anySatisfy(transit -> {
                    assertThat(transit.getTransitingBody()).isEqualTo("이동 태양");
                    assertThat(transit.getNatalPoint()).isEqualTo("출생 태양");
                    assertThat(transit.getAspect()).isEqualTo("합");
                });
        for (int adjustment : first.transitAdjustments()) {
            assertThat(adjustment).isBetween(-12, 12);
        }
    }

    @Test
    void keepsVernalEquinoxSunNearZeroTropicalLongitude() {
        double julianDay = WesternAstrologyService.julianDay(
                Instant.parse("2024-03-20T03:06:00Z"));
        double longitude = WesternAstrologyService.sunLongitude(julianDay);
        double distanceFromAriesPoint = Math.min(longitude, 360.0 - longitude);

        assertThat(distanceFromAriesPoint).isLessThan(0.5);
        assertThat(WesternAstrologyService.zodiacAt(longitude)).isEqualTo(Zodiac.ARIES);
    }

    @Test
    void omitsRisingSignWithoutTimeAndLocationAndRejectsInvalidZone() {
        ZodiacRequest dateOnly = ZodiacRequest.builder()
                .birthDate(LocalDate.of(1981, 3, 20))
                .targetDate(LocalDate.of(2026, 7, 28))
                .build();

        assertThat(service.analyze(dateOnly, dateOnly.getBirthDate())
                .profile().getRisingSign()).isNull();

        ZodiacRequest invalidZone = ZodiacRequest.builder()
                .birthDate(LocalDate.of(1981, 3, 20))
                .targetDate(LocalDate.of(2026, 7, 28))
                .timeZone("Not/A_Zone")
                .build();
        assertThatThrownBy(() -> service.analyze(invalidZone, invalidZone.getBirthDate()))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("IANA 시간대");
    }
}
