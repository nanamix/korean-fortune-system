package com.fortune.ai;

import com.fortune.dto.DailyFortuneResult;
import com.fortune.dto.FortuneByCategory;
import com.fortune.dto.MonthlyFortune;
import com.fortune.dto.SajuResult;
import com.fortune.dto.TojeongResult;
import com.fortune.dto.ZodiacDailyFortune;
import com.fortune.dto.ZodiacFortuneResult;
import com.fortune.dto.ZodiacWeeklyFortune;
import com.fortune.dto.WesternAstrologyProfile;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HexFormat;
import java.util.stream.Collectors;

/**
 * 결정론적 운세 엔진이 확정한 사실만 LLM 서술 계층에 전달하는 내부 계약.
 */
public record AiFactPacket(
        String schemaVersion,
        String engineVersion,
        String domain,
        Map<String, String> facts,
        List<String> excludedSensitiveFields
) {
    public static final String SCHEMA_VERSION = "fortune-fact-packet/v1";
    public static final String ENGINE_VERSION = "lunar-java-1.7.4+fortune-rules-v5";
    public static final String CACHE_NAMESPACE = "fact-v1-engine-v5";
    private static final List<String> DEFAULT_EXCLUSIONS = List.of(
            "name",
            "birth_date",
            "birth_time",
            "birth_location",
            "time_zone",
            "adjusted_datetime",
            "calendar_type",
            "gender",
            "notification_targets"
    );

    public AiFactPacket {
        schemaVersion = oneLine(schemaVersion);
        engineVersion = oneLine(engineVersion);
        domain = oneLine(domain);
        facts = Collections.unmodifiableMap(new LinkedHashMap<>(facts == null ? Map.of() : facts));
        excludedSensitiveFields = List.copyOf(
                excludedSensitiveFields == null ? DEFAULT_EXCLUSIONS : excludedSensitiveFields);
    }

    public static AiFactPacket empty() {
        return new AiFactPacket(SCHEMA_VERSION, ENGINE_VERSION, "unknown", Map.of(), DEFAULT_EXCLUSIONS);
    }

    public static AiFactPacket forSaju(SajuResult result) {
        Map<String, String> facts = new LinkedHashMap<>();
        facts.put("pillars", String.join(" ",
                safe(result.getYearPillar()),
                safe(result.getMonthPillar()),
                safe(result.getDayPillar()),
                safe(result.getTimePillar())));
        facts.put("day_master", safe(result.getDayMaster()));
        facts.put("day_pillar", safe(result.getDayPillar()));
        facts.put("fortune_summary", safe(result.getFortuneSummary()));

        SajuResult.WuxingAnalysis wuxing = result.getWuxingAnalysis();
        if (wuxing != null) {
            facts.put("five_elements",
                    "wood:%d,fire:%d,earth:%d,metal:%d,water:%d".formatted(
                            wuxing.getWoodCount(),
                            wuxing.getFireCount(),
                            wuxing.getEarthCount(),
                            wuxing.getMetalCount(),
                            wuxing.getWaterCount()));
            facts.put("strongest_element", safe(wuxing.getStrongestElement()));
            facts.put("weakest_element", safe(wuxing.getWeakestElement()));
            facts.put("element_balance", String.valueOf(wuxing.getBalance()));
        }

        if (result.getSipsinDistribution() != null && !result.getSipsinDistribution().isEmpty()) {
            facts.put("ten_gods_distribution", new TreeMap<>(result.getSipsinDistribution()).entrySet().stream()
                    .map(entry -> entry.getKey() + ":" + entry.getValue())
                    .collect(Collectors.joining(",")));
        }
        facts.put("daeun_direction", result.isDaeunForward() ? "forward" : "reverse");
        facts.put("daeun_start_age", String.valueOf(result.getDaeunNumber()));

        if (result.getAnnualFlows() != null && !result.getAnnualFlows().isEmpty()) {
            facts.put("annual_flows", result.getAnnualFlows().stream()
                    .map(flow -> "%d:%s:%s:%s".formatted(
                            flow.getYear(),
                            safe(flow.getGanji()),
                            safe(flow.getTwelveStage()),
                            safe(flow.getTheme())))
                    .collect(Collectors.joining("|")));
        }
        if (result.getMonthlyFlows() != null && !result.getMonthlyFlows().isEmpty()) {
            facts.put("monthly_flows", result.getMonthlyFlows().stream()
                    .map(flow -> "%d-%02d:%s:%s".formatted(
                            flow.getYear(),
                            flow.getMonth(),
                            safe(flow.getGanji()),
                            safe(flow.getTheme())))
                    .collect(Collectors.joining("|")));
        }

        return packet("saju", facts);
    }

    public static AiFactPacket forDaily(DailyFortuneResult result) {
        Map<String, String> facts = new LinkedHashMap<>();
        facts.put("target_date", String.valueOf(result.getDate()));
        facts.put("day_pillar", safe(result.getDayPillar()));
        facts.put("day_score", String.valueOf(result.getDayFortuneScore()));
        facts.put("personal_score", String.valueOf(result.getPersonalFortuneScore()));
        facts.put("total_score", String.valueOf(result.getTotalScore()));
        facts.put("score_basis", safe(result.getScoreBasis()));
        facts.put("lucky_direction", safe(result.getLuckyDirection()));
        facts.put("lucky_colors", join(result.getLuckyColors()));
        facts.put("caution", safe(result.getCaution()));

        FortuneByCategory category = result.getCategoryFortune();
        if (category != null) {
            facts.put("category_scores", "love:%d,career:%d,health:%d,wealth:%d".formatted(
                    category.getLoveScore(),
                    category.getCareerScore(),
                    category.getHealthScore(),
                    category.getWealthScore()));
        }
        return packet("daily", facts);
    }

    public static AiFactPacket forZodiac(ZodiacFortuneResult result) {
        Map<String, String> facts = new LinkedHashMap<>();
        facts.put("zodiac", safe(result.getZodiacKoreanName()));
        facts.put("target_date", String.valueOf(result.getTargetDate()));
        facts.put("lucky_color", safe(result.getLuckyColor()));
        facts.put("lucky_numbers", join(result.getLuckyNumbers()));
        facts.put("lucky_stone", safe(result.getLuckyStone()));
        facts.put("personality", safe(result.getPersonality()));

        WesternAstrologyProfile profile = result.getAstrologyProfile();
        if (profile != null) {
            facts.put("astrology_model", safe(profile.getCalculationModel()));
            facts.put("astrology_precision", safe(profile.getPrecision()));
            facts.put("sun_sign", zodiac(profile.getSunSign(), profile.getSunDegree()));
            facts.put("moon_sign", zodiac(profile.getMoonSign(), profile.getMoonDegree()));
            facts.put("rising_sign", profile.getRisingSign() == null
                    ? "정보 없음"
                    : zodiac(profile.getRisingSign(), profile.getRisingDegree()));
            facts.put("sun_element_modality",
                    safe(profile.getElement()) + "," + safe(profile.getModality()));
            facts.put("ruling_planet", safe(profile.getRulingPlanet()));
            facts.put("decan", String.valueOf(profile.getDecan()));
            facts.put("natal_moon_phase", safe(profile.getNatalMoonPhase()));
        }
        if (result.getMajorTransits() != null && !result.getMajorTransits().isEmpty()) {
            facts.put("major_transits", result.getMajorTransits().stream()
                    .map(transit -> "%s:%s:%s:orb%.1f:score%+d".formatted(
                            safe(transit.getTransitingBody()),
                            safe(transit.getNatalPoint()),
                            safe(transit.getAspect()),
                            transit.getOrb(),
                            transit.getScoreAdjustment()))
                    .collect(Collectors.joining("|")));
        }
        facts.put("transit_summary", safe(result.getTransitSummary()));

        ZodiacDailyFortune today = result.getTodayFortune();
        if (today != null) {
            facts.put("total_score", String.valueOf(today.getOverallScore()));
            facts.put("score_basis", safe(today.getScoreBasis()));
            facts.put("category_scores", "love:%d,career:%d,health:%d,money:%d".formatted(
                    today.getLoveScore(),
                    today.getCareerScore(),
                    today.getHealthScore(),
                    today.getMoneyScore()));
        }
        ZodiacWeeklyFortune weekly = result.getWeeklyFortune();
        if (weekly != null) {
            facts.put("weekly_period", weekly.getStartDate() + "~" + weekly.getEndDate());
            facts.put("weekly_score", String.valueOf(weekly.getOverallScore()));
            facts.put("weekly_score_basis", safe(weekly.getScoreBasis()));
            facts.put("weekly_overview", safe(weekly.getOverview()));
            facts.put("weekly_best_date", String.valueOf(weekly.getBestDate()));
            if (weekly.getDays() != null && !weekly.getDays().isEmpty()) {
                facts.put("weekly_days", weekly.getDays().stream()
                        .map(day -> "%s:%d:%s".formatted(
                                day.getDate(),
                                day.getOverallScore(),
                                safe(day.getHeadline())))
                        .collect(Collectors.joining("|")));
            }
        }
        return packet("zodiac", facts);
    }

    public static AiFactPacket forTojeong(TojeongResult result) {
        Map<String, String> facts = new LinkedHashMap<>();
        facts.put("target_year", String.valueOf(result.getTargetYear()));
        facts.put("gwa_number", String.valueOf(result.getGwaNumber()));
        facts.put("gwa_name", safe(result.getGwaName()));
        facts.put("summary", safe(result.getSummary()));
        facts.put("total_score", String.valueOf(result.getOverallScore()));
        facts.put("lucky_months", safe(result.getLuckyMonths()));
        facts.put("caution_months", safe(result.getCautionMonths()));

        if (result.getMonthlyFortune() != null && !result.getMonthlyFortune().isEmpty()) {
            facts.put("monthly_fortunes", result.getMonthlyFortune().stream()
                    .map(AiFactPacket::monthlyFortune)
                    .collect(Collectors.joining("|")));
        }
        return packet("tojeong", facts);
    }

    public String promptBlock() {
        List<String> lines = new ArrayList<>();
        lines.add("<fortune-fact-packet>");
        lines.add("schema_version=" + schemaVersion);
        lines.add("engine_version=" + engineVersion);
        lines.add("domain=" + domain);
        lines.add("can_override_engine=false");
        facts.forEach((key, value) -> lines.add(key + "=" + promptValue(value)));
        lines.add("privacy_excluded=" + String.join(",", excludedSensitiveFields));
        lines.add("</fortune-fact-packet>");
        return String.join("\n", lines);
    }

    public String fact(String key) {
        return facts.getOrDefault(key, "");
    }

    /**
     * 원문 사실을 저장하지 않고 동일한 결정론 결과를 식별하는 SHA-256 영수증 값.
     */
    public String factHash() {
        try {
            byte[] digest = MessageDigest.getInstance("SHA-256")
                    .digest(promptBlock().getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(digest);
        } catch (NoSuchAlgorithmException exception) {
            throw new IllegalStateException("SHA-256 algorithm is unavailable", exception);
        }
    }

    private static AiFactPacket packet(String domain, Map<String, String> facts) {
        return new AiFactPacket(SCHEMA_VERSION, ENGINE_VERSION, domain, facts, DEFAULT_EXCLUSIONS);
    }

    private static String monthlyFortune(MonthlyFortune fortune) {
        return "%02d:%d:%s:%s".formatted(
                fortune.getMonth(),
                fortune.getScore(),
                safe(fortune.getMessage()),
                join(fortune.getKeywords()));
    }

    private static String zodiac(com.fortune.enums.Zodiac zodiac, Number degree) {
        if (zodiac == null) {
            return "정보 없음";
        }
        return zodiac.getKoreanName() + ":" + (degree == null ? "정보 없음" : degree + "deg");
    }

    private static String join(List<?> values) {
        if (values == null || values.isEmpty()) {
            return "정보 없음";
        }
        return values.stream().map(String::valueOf).collect(Collectors.joining(","));
    }

    private static String safe(String value) {
        return value == null || value.isBlank() ? "정보 없음" : oneLine(value);
    }

    private static String oneLine(String value) {
        return value == null ? "" : value.replaceAll("\\s+", " ").trim();
    }

    private static String promptValue(String value) {
        return oneLine(value)
                .replace("\\", "\\\\")
                .replace("<", "\\u003c")
                .replace(">", "\\u003e");
    }
}
