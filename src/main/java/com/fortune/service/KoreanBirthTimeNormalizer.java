package com.fortune.service;

import com.fortune.dto.SajuRequest;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.zone.ZoneRules;
import java.util.List;

/**
 * 한국 출생시각을 역사적 표준시와 출생지의 겉보기 태양시로 정규화한다.
 */
final class KoreanBirthTimeNormalizer {
    static final double DEFAULT_LONGITUDE = 127.5;
    private static final ZoneId SEOUL = ZoneId.of("Asia/Seoul");
    private static final int SECONDS_PER_LONGITUDE_DEGREE = 240;

    private KoreanBirthTimeNormalizer() {
    }

    static Result normalize(LocalDateTime localBirthTime, SajuRequest request) {
        ZoneRules rules = SEOUL.getRules();
        List<ZoneOffset> offsets = rules.getValidOffsets(localBirthTime);
        boolean applyDst = request.getApplyHistoricalDst() == null
                || request.getApplyHistoricalDst();
        if (applyDst && offsets.isEmpty()) {
            throw new IllegalArgumentException(
                    "서머타임 전환으로 존재하지 않는 출생시각입니다. 전환 이후의 실제 시각을 입력해 주세요.");
        }
        if (applyDst && offsets.size() > 1) {
            throw new IllegalArgumentException(
                    "서머타임 종료로 두 번 존재하는 출생시각입니다. 표준시 여부를 확인해 주세요.");
        }

        var zoned = offsets.isEmpty()
                ? localBirthTime.atZone(SEOUL)
                : localBirthTime.atOffset(offsets.getFirst()).toZonedDateTime();
        ZoneOffset standardOffset = rules.getStandardOffset(zoned.toInstant());
        Duration daylightSaving = applyDst
                ? rules.getDaylightSavings(zoned.toInstant())
                : Duration.ZERO;
        LocalDateTime standardTime = localBirthTime.minus(daylightSaving);

        double longitude = request.getBirthLongitude() == null
                ? DEFAULT_LONGITUDE
                : request.getBirthLongitude();
        double standardMeridian = standardOffset.getTotalSeconds() / 240.0;
        long longitudeSeconds = Math.round(
                (longitude - standardMeridian) * SECONDS_PER_LONGITUDE_DEGREE);
        long equationSeconds = Boolean.TRUE.equals(request.getApplyEquationOfTime())
                ? Math.round(equationOfTimeMinutes(standardTime) * 60.0)
                : 0L;

        return new Result(
                standardTime.plusSeconds(longitudeSeconds + equationSeconds),
                daylightSaving.toSeconds(),
                longitudeSeconds,
                equationSeconds,
                standardMeridian);
    }

    /**
     * NOAA의 저차 근사식으로 균시차(분)를 계산한다.
     */
    static double equationOfTimeMinutes(LocalDateTime dateTime) {
        double days = dateTime.toLocalDate().isLeapYear() ? 366.0 : 365.0;
        double gamma = 2.0 * Math.PI / days
                * (dateTime.getDayOfYear() - 1
                + (dateTime.getHour() - 12) / 24.0);
        return 229.18 * (0.000075
                + 0.001868 * Math.cos(gamma)
                - 0.032077 * Math.sin(gamma)
                - 0.014615 * Math.cos(2 * gamma)
                - 0.040849 * Math.sin(2 * gamma));
    }

    record Result(
            LocalDateTime apparentSolarTime,
            long daylightSavingSeconds,
            long longitudeCorrectionSeconds,
            long equationOfTimeSeconds,
            double standardMeridian) {
    }
}
