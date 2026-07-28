package com.fortune.service;

import com.fortune.dto.AstrologyTransit;
import com.fortune.dto.WesternAstrologyProfile;
import com.fortune.dto.ZodiacRequest;
import com.fortune.enums.Zodiac;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.List;
import org.springframework.stereotype.Service;

/**
 * 열대황도 기준 Sun·Moon·Rising과 대상일 주요 transit를 계산한다.
 *
 * <p>태양·달 위치는 저차 천문 근사식, Rising은 UTC Julian date와 USNO 계열
 * GMST 식을 사용한다. Swiss Ephemeris 수준의 전문 차트가 아니라 문화·오락용
 * 개인화 계산이며 모델 이름과 정밀도 상태를 결과에 명시한다.</p>
 */
@Service
public class WesternAstrologyService {
    static final String MODEL = "tropical-approx-v1";
    static final String DEFAULT_ZONE = "Asia/Seoul";
    private static final double J2000 = 2451545.0;
    private static final double SECONDS_PER_DAY = 86400.0;

    public Analysis analyze(ZodiacRequest request, LocalDate solarBirthDate) {
        ZoneId zone = zone(request.getTimeZone());
        LocalTime birthTime = request.getBirthTime() == null
                ? LocalTime.NOON
                : request.getBirthTime();
        ZonedDateTime birthMoment = solarBirthDate.atTime(birthTime).atZone(zone);
        ZonedDateTime targetMoment = request.getTargetDate().atTime(12, 0).atZone(zone);

        double natalSun = sunLongitude(julianDay(birthMoment.toInstant()));
        double natalMoon = moonLongitude(julianDay(birthMoment.toInstant()));
        double transitSun = sunLongitude(julianDay(targetMoment.toInstant()));
        double transitMoon = moonLongitude(julianDay(targetMoment.toInstant()));

        boolean locationReady = request.getBirthTime() != null
                && request.getBirthLatitude() != null
                && request.getBirthLongitude() != null;
        Double rising = locationReady
                ? ascendantLongitude(
                        julianDay(birthMoment.toInstant()),
                        request.getBirthLatitude(),
                        request.getBirthLongitude())
                : null;

        Zodiac sunSign = zodiacAt(natalSun);
        Zodiac moonSign = zodiacAt(natalMoon);
        Zodiac risingSign = rising == null ? null : zodiacAt(rising);
        WesternAstrologyProfile profile = WesternAstrologyProfile.builder()
                .calculationModel(MODEL)
                .precision(locationReady ? "BIRTH_TIME_LOCATION" : "DATE_ONLY")
                .sunSign(sunSign)
                .sunDegree(degreeInSign(natalSun))
                .moonSign(moonSign)
                .moonDegree(degreeInSign(natalMoon))
                .risingSign(risingSign)
                .risingDegree(rising == null ? null : degreeInSign(rising))
                .element(element(sunSign))
                .modality(modality(sunSign))
                .rulingPlanet(ruler(sunSign))
                .decan(Math.min(3, (int) (degreeInSign(natalSun) / 10.0) + 1))
                .natalMoonPhase(moonPhase(natalSun, natalMoon))
                .summary(profileSummary(sunSign, moonSign, risingSign))
                .build();

        List<AstrologyTransit> transits = new ArrayList<>();
        addAspect(transits, "이동 태양", transitSun, "출생 태양", natalSun, 6.0);
        addAspect(transits, "이동 태양", transitSun, "출생 달", natalMoon, 6.0);
        addAspect(transits, "이동 달", transitMoon, "출생 태양", natalSun, 8.0);
        addAspect(transits, "이동 달", transitMoon, "출생 달", natalMoon, 8.0);
        if (rising != null) {
            addAspect(transits, "이동 태양", transitSun, "상승궁", rising, 6.0);
            addAspect(transits, "이동 달", transitMoon, "상승궁", rising, 8.0);
        }

        int[] natalAdjustments = natalAdjustments(profile);
        int[] transitAdjustments = transitAdjustments(
                transitSun, transitMoon, natalSun, natalMoon, rising);
        String transitSummary = transits.isEmpty()
                ? "대상일에는 설정한 orb 안에 들어오는 주요 각이 없어, 기본 성향과 완만한 행성 각도 흐름을 중심으로 봅니다."
                : transits.stream()
                        .map(transit -> "%s %s %s(orb %.1f°)".formatted(
                                transit.getTransitingBody(),
                                transit.getNatalPoint(),
                                transit.getAspect(),
                                transit.getOrb()))
                        .reduce((left, right) -> left + ", " + right)
                        .orElse("");
        return new Analysis(
                profile,
                List.copyOf(transits),
                natalAdjustments,
                transitAdjustments,
                transitSummary);
    }

    private ZoneId zone(String zoneId) {
        try {
            return ZoneId.of(zoneId == null || zoneId.isBlank() ? DEFAULT_ZONE : zoneId);
        } catch (Exception exception) {
            throw new IllegalArgumentException("올바른 IANA 시간대를 입력해 주세요. 예: Asia/Seoul");
        }
    }

    static double julianDay(Instant instant) {
        return 2440587.5
                + instant.getEpochSecond() / SECONDS_PER_DAY
                + instant.getNano() / 1_000_000_000.0 / SECONDS_PER_DAY;
    }

    /**
     * 태양의 진황경 저차 근사.
     */
    static double sunLongitude(double julianDay) {
        double t = (julianDay - J2000) / 36525.0;
        double meanLongitude = normalize(
                280.46646 + t * (36000.76983 + t * 0.0003032));
        double meanAnomaly = normalize(
                357.52911 + t * (35999.05029 - 0.0001537 * t));
        double center = (1.914602 - t * (0.004817 + 0.000014 * t))
                * sin(meanAnomaly)
                + (0.019993 - 0.000101 * t) * sin(2 * meanAnomaly)
                + 0.000289 * sin(3 * meanAnomaly);
        return normalize(meanLongitude + center);
    }

    /**
     * 달의 황경 주요 주기항 근사.
     */
    static double moonLongitude(double julianDay) {
        double t = (julianDay - J2000) / 36525.0;
        double l = normalize(218.3164477 + 481267.88123421 * t);
        double d = normalize(297.8501921 + 445267.1114034 * t);
        double m = normalize(357.5291092 + 35999.0502909 * t);
        double mp = normalize(134.9633964 + 477198.8675055 * t);
        double f = normalize(93.2720950 + 483202.0175233 * t);
        return normalize(l
                + 6.289 * sin(mp)
                + 1.274 * sin(2 * d - mp)
                + 0.658 * sin(2 * d)
                + 0.214 * sin(2 * mp)
                - 0.186 * sin(m)
                - 0.114 * sin(2 * f)
                + 0.059 * sin(2 * d - 2 * mp)
                + 0.057 * sin(2 * d - m - mp)
                + 0.053 * sin(2 * d + mp)
                + 0.046 * sin(2 * d - m)
                + 0.041 * sin(m - mp)
                - 0.035 * sin(d)
                - 0.031 * sin(m + mp));
    }

    /**
     * Local mean sidereal time에서 열대황도 Ascendant를 구한다.
     */
    static double ascendantLongitude(double julianDay, double latitude, double longitude) {
        double t = (julianDay - J2000) / 36525.0;
        double gmst = normalize(
                280.46061837
                        + 360.98564736629 * (julianDay - J2000)
                        + 0.000387933 * t * t
                        - t * t * t / 38710000.0);
        double localSidereal = Math.toRadians(normalize(gmst + longitude));
        double obliquity = Math.toRadians(23.439291 - 0.0130042 * t);
        double phi = Math.toRadians(latitude);
        double ascendant = Math.toDegrees(Math.atan2(
                -Math.cos(localSidereal),
                Math.sin(obliquity) * Math.tan(phi)
                        + Math.cos(obliquity) * Math.sin(localSidereal)));
        return normalize(ascendant + 180.0);
    }

    static Zodiac zodiacAt(double longitude) {
        return Zodiac.values()[(int) Math.floor(normalize(longitude) / 30.0) % 12];
    }

    private void addAspect(
            List<AstrologyTransit> target,
            String transitingBody,
            double transitLongitude,
            String natalPoint,
            double natalLongitude,
            double orbLimit) {
        Aspect aspect = nearestAspect(separation(transitLongitude, natalLongitude), orbLimit);
        if (aspect == null) {
            return;
        }
        target.add(AstrologyTransit.builder()
                .transitingBody(transitingBody)
                .natalPoint(natalPoint)
                .aspect(aspect.korean())
                .orb(roundOne(aspect.orb()))
                .scoreAdjustment(aspect.adjustment())
                .interpretation(aspect.interpretation())
                .build());
    }

    private Aspect nearestAspect(double separation, double orbLimit) {
        AspectDefinition[] definitions = {
                new AspectDefinition(0, "합", 6,
                        "집중력이 커지는 각입니다. 한 가지 주제를 선명하게 다루되 과몰입은 점검하세요."),
                new AspectDefinition(60, "육합", 4,
                        "협력과 작은 기회를 활용하기 좋은 각입니다. 가벼운 제안과 연결을 행동으로 옮겨 보세요."),
                new AspectDefinition(90, "사각", -5,
                        "긴장과 조정이 필요한 각입니다. 속도를 낮추고 조건·감정·일정을 분리해 확인하세요."),
                new AspectDefinition(120, "삼합", 6,
                        "자연스러운 강점과 지원을 활용하기 좋은 각입니다. 익숙한 장점을 구체적인 결과로 연결하세요."),
                new AspectDefinition(180, "충", -6,
                        "나와 타인의 요구가 마주 보는 각입니다. 한쪽을 단정하기보다 균형점과 경계를 협의하세요.")
        };
        AspectDefinition nearest = null;
        double nearestOrb = Double.MAX_VALUE;
        for (AspectDefinition definition : definitions) {
            double orb = Math.abs(separation - definition.angle());
            if (orb < nearestOrb) {
                nearest = definition;
                nearestOrb = orb;
            }
        }
        return nearest != null && nearestOrb <= orbLimit
                ? new Aspect(nearest.korean(), nearest.adjustment(), nearest.interpretation(), nearestOrb)
                : null;
    }

    private int[] natalAdjustments(WesternAstrologyProfile profile) {
        int[] element = switch (profile.getElement()) {
            case "불" -> new int[]{1, 4, 1, 0};
            case "흙" -> new int[]{0, 2, 2, 4};
            case "공기" -> new int[]{4, 3, 0, 1};
            default -> new int[]{4, 0, 3, 1};
        };
        int[] modality = switch (profile.getModality()) {
            case "활동궁" -> new int[]{0, 2, -1, 0};
            case "고정궁" -> new int[]{0, 0, 1, 2};
            default -> new int[]{1, 0, 2, -1};
        };
        int[] result = new int[4];
        for (int index = 0; index < result.length; index++) {
            result[index] = element[index] + modality[index];
        }
        return result;
    }

    private int[] transitAdjustments(
            double transitSun,
            double transitMoon,
            double natalSun,
            double natalMoon,
            Double rising) {
        int sunToSun = scaledAspectAdjustment(transitSun, natalSun, 6.0);
        int sunToMoon = scaledAspectAdjustment(transitSun, natalMoon, 6.0);
        int moonToSun = scaledAspectAdjustment(transitMoon, natalSun, 8.0);
        int moonToMoon = scaledAspectAdjustment(transitMoon, natalMoon, 8.0);
        int sunToRising = rising == null ? 0 : scaledAspectAdjustment(transitSun, rising, 6.0);
        int moonToRising = rising == null ? 0 : scaledAspectAdjustment(transitMoon, rising, 8.0);
        return new int[]{
                clampAdjustment(moonToMoon + moonToRising / 2 + sunToMoon / 2),
                clampAdjustment(sunToSun + sunToRising / 2 + moonToSun / 3),
                clampAdjustment(moonToMoon + moonToSun / 2),
                clampAdjustment(sunToSun + sunToMoon / 2)
        };
    }

    private int scaledAspectAdjustment(double transit, double natal, double orbLimit) {
        Aspect aspect = nearestAspect(separation(transit, natal), orbLimit);
        if (aspect == null) {
            return 0;
        }
        double scale = 1.0 - aspect.orb() / orbLimit;
        return (int) Math.round(aspect.adjustment() * scale);
    }

    private int clampAdjustment(int value) {
        return Math.max(-12, Math.min(12, value));
    }

    private String profileSummary(Zodiac sun, Zodiac moon, Zodiac rising) {
        String risingText = rising == null
                ? "출생 시각·위치를 모두 입력하면 상승궁까지 계산할 수 있습니다."
                : "상승궁 " + rising.getKoreanName()
                        + "은 첫인상과 새로운 상황에 접근하는 방식을 상징합니다.";
        return "태양 " + sun.getKoreanName()
                + "은 의식적인 목표와 정체성을, 달 " + moon.getKoreanName()
                + "은 감정적 안정과 익숙한 반응을 상징합니다. " + risingText;
    }

    private String moonPhase(double sun, double moon) {
        double angle = normalize(moon - sun);
        if (angle < 22.5 || angle >= 337.5) return "삭(새달)";
        if (angle < 67.5) return "초승달";
        if (angle < 112.5) return "상현달";
        if (angle < 157.5) return "차오르는 달";
        if (angle < 202.5) return "보름달";
        if (angle < 247.5) return "기우는 달";
        if (angle < 292.5) return "하현달";
        return "그믐달";
    }

    private String element(Zodiac zodiac) {
        return switch (zodiac) {
            case ARIES, LEO, SAGITTARIUS -> "불";
            case TAURUS, VIRGO, CAPRICORN -> "흙";
            case GEMINI, LIBRA, AQUARIUS -> "공기";
            case CANCER, SCORPIO, PISCES -> "물";
        };
    }

    private String modality(Zodiac zodiac) {
        return switch (zodiac) {
            case ARIES, CANCER, LIBRA, CAPRICORN -> "활동궁";
            case TAURUS, LEO, SCORPIO, AQUARIUS -> "고정궁";
            case GEMINI, VIRGO, SAGITTARIUS, PISCES -> "변통궁";
        };
    }

    private String ruler(Zodiac zodiac) {
        return switch (zodiac) {
            case ARIES -> "화성";
            case TAURUS, LIBRA -> "금성";
            case GEMINI, VIRGO -> "수성";
            case CANCER -> "달";
            case LEO -> "태양";
            case SCORPIO -> "화성·명왕성";
            case SAGITTARIUS -> "목성";
            case CAPRICORN -> "토성";
            case AQUARIUS -> "토성·천왕성";
            case PISCES -> "목성·해왕성";
        };
    }

    private static double degreeInSign(double longitude) {
        return roundOne(normalize(longitude) % 30.0);
    }

    private static double separation(double left, double right) {
        double difference = Math.abs(normalize(left) - normalize(right));
        return difference > 180.0 ? 360.0 - difference : difference;
    }

    private static double sin(double degrees) {
        return Math.sin(Math.toRadians(degrees));
    }

    private static double normalize(double value) {
        return ((value % 360.0) + 360.0) % 360.0;
    }

    private static double roundOne(double value) {
        return Math.round(value * 10.0) / 10.0;
    }

    public record Analysis(
            WesternAstrologyProfile profile,
            List<AstrologyTransit> transits,
            int[] natalAdjustments,
            int[] transitAdjustments,
            String transitSummary) {
        public Analysis {
            natalAdjustments = natalAdjustments.clone();
            transitAdjustments = transitAdjustments.clone();
        }

        @Override
        public int[] natalAdjustments() {
            return natalAdjustments.clone();
        }

        @Override
        public int[] transitAdjustments() {
            return transitAdjustments.clone();
        }
    }

    private record AspectDefinition(
            double angle,
            String korean,
            int adjustment,
            String interpretation) {
    }

    private record Aspect(
            String korean,
            int adjustment,
            String interpretation,
            double orb) {
    }
}
