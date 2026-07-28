package com.fortune.service;

import com.fortune.dto.SajuRequest;
import java.time.LocalDateTime;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class KoreanBirthTimeNormalizerTest {

    @Test
    void preservesLegacyDefaultOutsideHistoricalDst() {
        var result = KoreanBirthTimeNormalizer.normalize(
                LocalDateTime.of(1981, 3, 20, 3, 30),
                SajuRequest.builder().build());

        assertThat(result.apparentSolarTime())
                .isEqualTo(LocalDateTime.of(1981, 3, 20, 3, 0));
        assertThat(result.longitudeCorrectionSeconds()).isEqualTo(-1800);
        assertThat(result.daylightSavingSeconds()).isZero();
    }

    @Test
    void appliesTzdbDaylightSavingAndHistoricalStandardMeridian() {
        var result = KoreanBirthTimeNormalizer.normalize(
                LocalDateTime.of(1960, 6, 1, 12, 0),
                SajuRequest.builder().build());

        assertThat(result.apparentSolarTime())
                .isEqualTo(LocalDateTime.of(1960, 6, 1, 11, 0));
        assertThat(result.daylightSavingSeconds()).isEqualTo(3600);
        assertThat(result.standardMeridian()).isEqualTo(127.5);
        assertThat(result.longitudeCorrectionSeconds()).isZero();
    }

    @Test
    void appliesOptionalEquationOfTimeWithoutChangingDefaultContract() {
        LocalDateTime birth = LocalDateTime.of(2024, 11, 3, 12, 0);
        var result = KoreanBirthTimeNormalizer.normalize(
                birth,
                SajuRequest.builder()
                        .birthLongitude(126.978)
                        .applyEquationOfTime(true)
                        .build());

        assertThat(result.equationOfTimeSeconds()).isEqualTo(982);
        assertThat(result.apparentSolarTime())
                .isEqualTo(LocalDateTime.of(2024, 11, 3, 11, 44, 17));
    }

    @Test
    void rejectsNonexistentDstWallClockTime() {
        assertThatThrownBy(() -> KoreanBirthTimeNormalizer.normalize(
                LocalDateTime.of(1988, 5, 8, 2, 30),
                SajuRequest.builder().build()))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("존재하지 않는 출생시각");
    }
}
