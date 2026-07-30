package com.fortune.service;

import com.fortune.dto.AstrologyTransit;
import com.fortune.dto.DailyFortuneResult;
import com.fortune.dto.FortuneByCategory;
import com.fortune.dto.MonthlyFortune;
import com.fortune.dto.SajuResult;
import com.fortune.dto.SinsalInfo;
import com.fortune.dto.TojeongResult;
import com.fortune.dto.WesternAstrologyProfile;
import com.fortune.dto.ZodiacAnnualFortune;
import com.fortune.dto.ZodiacAnnualMonth;
import com.fortune.dto.ZodiacDailyFortune;
import com.fortune.dto.ZodiacFortuneResult;
import com.fortune.dto.ZodiacMonthlyFortune;
import com.fortune.dto.ZodiacWeeklyDay;
import com.fortune.dto.ZodiacWeeklyFortune;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;
import java.util.StringJoiner;
import org.springframework.stereotype.Component;

/**
 * 화면에 표시하는 상세 결과와 같은 순서/데이터로 메시지 채널용 평문을 만든다.
 */
@Component
public class FortuneNotificationFormatter {

    private static final DateTimeFormatter DATE = DateTimeFormatter.ofPattern("yyyy년 MM월 dd일");
    private static final DateTimeFormatter DATE_TIME =
            DateTimeFormatter.ofPattern("yyyy년 MM월 dd일 HH시 mm분");

    public String formatSaju(SajuResult result, String recipientName) {
        StringBuilder out = new StringBuilder()
                .append("🔮 ").append(name(recipientName)).append("님의 사주팔자 결과\n\n");
        line(out, "📅 생년월일시", result.getAdjustedDateTime() == null
                ? null : result.getAdjustedDateTime().format(DATE_TIME));
        line(out, "📊 사주팔자", result.getFormattedSaju());
        line(out, "🌟 일간", result.getDayMaster());
        line(out, "📈 운세 요약", result.getFortuneSummary());

        appendPillar(out, "년주", result.getYearDetail());
        appendPillar(out, "월주", result.getMonthDetail());
        appendPillar(out, "일주", result.getDayDetail());
        appendPillar(out, "시주", result.getTimeDetail());

        SajuResult.WuxingAnalysis wuxing = result.getWuxingAnalysis();
        if (wuxing != null) {
            out.append("\n💡 오행 분석\n")
                    .append("• 목(木) ").append(wuxing.getWoodCount()).append(" · 화(火) ")
                    .append(wuxing.getFireCount()).append(" · 토(土) ")
                    .append(wuxing.getEarthCount()).append(" · 금(金) ")
                    .append(wuxing.getMetalCount()).append(" · 수(水) ")
                    .append(wuxing.getWaterCount()).append('\n');
            line(out, "• 강한 오행", wuxing.getStrongestElement());
            line(out, "• 약한 오행", wuxing.getWeakestElement());
            out.append("• 균형 점수: ").append(wuxing.getBalance()).append('\n');
        }

        appendMap(out, "⚖️ 십신 분포", result.getSipsinDistribution());
        appendPersonality(out, result.getPersonalityAnalysis());

        if (hasItems(result.getDaeun())) {
            out.append("\n🧭 대운 (").append(result.isDaeunForward() ? "순행" : "역행")
                    .append(" · ").append(result.getDaeunNumber()).append("세 시작)\n");
            for (SajuResult.DaeUn item : result.getDaeun()) {
                out.append("• ").append(item.getAge()).append("세 ").append(text(item.getGanji()))
                        .append(" ").append(text(item.getGanjiHanja())).append(" · ")
                        .append(text(item.getStemSipsin())).append("/")
                        .append(text(item.getBranchSipsin())).append(" · ")
                        .append(text(item.getTwelveStage())).append('\n');
            }
        }
        if (hasItems(result.getAnnualFlows())) {
            out.append("\n📆 향후 세운\n");
            for (SajuResult.AnnualFlow item : result.getAnnualFlows()) {
                out.append("• ").append(item.getYear()).append("년 (").append(item.getAge())
                        .append("세) ").append(text(item.getGanji())).append(" · ")
                        .append(text(item.getTheme())).append(" · ")
                        .append(text(item.getTwelveStage())).append('\n');
            }
        }
        if (hasItems(result.getMonthlyFlows())) {
            out.append("\n🗓️ 월별 흐름\n");
            for (SajuResult.MonthlyFlow item : result.getMonthlyFlows()) {
                out.append("• ").append(item.getMonth()).append("월 ")
                        .append(text(item.getGanji())).append(" · ")
                        .append(text(item.getTheme())).append('\n');
            }
        }
        return out.toString().strip();
    }

    public String formatDaily(DailyFortuneResult result, String recipientName) {
        StringBuilder out = new StringBuilder()
                .append("📅 ").append(name(recipientName)).append("님의 ")
                .append(result.getDate() == null ? "오늘" : result.getDate().format(DATE))
                .append(" 운세\n\n")
                .append("🎯 총점: ").append(result.getTotalScore()).append("점 (")
                .append(grade(result.getTotalScore())).append(")\n");
        line(out, "📊 일진", result.getDayPillar());
        line(out, "💡 오늘의 조언", result.getAdvice());
        line(out, "🧮 점수 근거", result.getScoreBasis());

        FortuneByCategory category = result.getCategoryFortune();
        if (category != null) {
            out.append("\n💕 연애운 ").append(category.getLoveScore()).append("점\n")
                    .append(text(category.getLoveMessage())).append('\n')
                    .append("\n💼 직업운 ").append(category.getCareerScore()).append("점\n")
                    .append(text(category.getCareerMessage())).append('\n')
                    .append("\n💪 건강운 ").append(category.getHealthScore()).append("점\n")
                    .append(text(category.getHealthMessage())).append('\n')
                    .append("\n💰 재물운 ").append(category.getWealthScore()).append("점\n")
                    .append(text(category.getWealthMessage())).append('\n');
        }
        out.append("\n🍀 행운 정보\n");
        line(out, "• 방향", result.getLuckyDirection());
        line(out, "• 색상", join(result.getLuckyColors()));
        line(out, "⚠️ 주의사항", result.getCaution());

        if (hasItems(result.getSinsals())) {
            out.append("\n✨ 오늘의 신살\n");
            for (SinsalInfo item : result.getSinsals()) {
                out.append("• ").append(text(item.getName())).append(" · ")
                        .append(item.isLucky() ? "길신" : "흉신").append(" · 영향 ")
                        .append(item.getInfluence()).append('\n')
                        .append("  ").append(text(item.getDescription())).append('\n');
            }
        }
        return out.toString().strip();
    }

    public String formatTojeong(TojeongResult result, String recipientName) {
        StringBuilder out = new StringBuilder()
                .append("📜 ").append(name(recipientName)).append("님의 ")
                .append(result.getTargetYear()).append("년 토정비결\n\n")
                .append("🎯 ").append(result.getGwaNumber()).append("번 ")
                .append(text(result.getGwaName())).append(" ")
                .append(text(result.getGwaSymbol())).append('\n')
                .append("📊 총점: ").append(result.getOverallScore()).append("점 (")
                .append(grade(result.getOverallScore())).append(")\n");
        line(out, "📝 한 해 요약", result.getSummary());
        line(out, "🔎 상세 운세", result.getDetailedFortune());
        line(out, "💡 조언", result.getAdvice());
        line(out, "🍀 길한 달", result.getLuckyMonths());
        line(out, "⚠️ 조심할 달", result.getCautionMonths());
        if (hasItems(result.getMonthlyFortune())) {
            out.append("\n🗓️ 월별 운세\n");
            for (MonthlyFortune item : result.getMonthlyFortune()) {
                out.append("• ").append(item.getMonth()).append("월 · ")
                        .append(item.getScore()).append("점 · ")
                        .append(text(item.getMessage()));
                if (hasItems(item.getKeywords())) {
                    out.append(" · ").append(join(item.getKeywords()));
                }
                out.append('\n');
            }
        }
        return out.toString().strip();
    }

    public String formatZodiac(ZodiacFortuneResult result, String recipientName) {
        StringBuilder out = new StringBuilder()
                .append("⭐ ").append(name(recipientName)).append("님의 ")
                .append(text(result.getZodiacKoreanName())).append(" 점성술 운세\n\n");
        line(out, "📅 대상일", result.getTargetDate() == null ? null : result.getTargetDate().format(DATE));
        appendAstrologyProfile(out, result.getAstrologyProfile());
        line(out, "🪐 트랜짓 요약", result.getTransitSummary());
        appendTransits(out, result.getMajorTransits());
        appendZodiacDaily(out, result.getTodayFortune());
        appendZodiacWeekly(out, result.getWeeklyFortune());
        appendZodiacMonthly(out, result.getMonthlyFortune());
        appendZodiacAnnual(out, result.getAnnualFortune());
        out.append("\n🍀 행운과 성향\n");
        line(out, "• 숫자", result.getLuckyNumbers() == null ? null : result.getLuckyNumbers().toString());
        line(out, "• 색상", result.getLuckyColor());
        line(out, "• 보석", result.getLuckyStone());
        line(out, "• 잘 맞는 별자리", result.getCompatibleZodiacs() == null
                ? null : result.getCompatibleZodiacs().toString());
        line(out, "• 성향", result.getPersonality());
        return out.toString().strip();
    }

    private void appendPillar(StringBuilder out, String label, SajuResult.Pillar pillar) {
        if (pillar == null) {
            return;
        }
        out.append("\n🏛️ ").append(label).append(" ")
                .append(text(pillar.getStem())).append(text(pillar.getBranch()))
                .append(" (").append(text(pillar.getStemHanja())).append(text(pillar.getBranchHanja()))
                .append(")\n• 십신: ").append(text(pillar.getStemSipsin())).append(" / ")
                .append(text(pillar.getBranchSipsin())).append('\n')
                .append("• 12운성: ").append(text(pillar.getTwelveStage())).append('\n');
        if (hasItems(pillar.getHiddenStems())) {
            out.append("• 지장간: ").append(join(pillar.getHiddenStems()));
            if (hasItems(pillar.getHiddenStemsSipsin())) {
                out.append(" (").append(join(pillar.getHiddenStemsSipsin())).append(")");
            }
            out.append('\n');
        }
    }

    private void appendMap(StringBuilder out, String title, Map<String, Integer> values) {
        if (values == null || values.isEmpty()) {
            return;
        }
        out.append('\n').append(title).append('\n');
        values.forEach((key, value) -> out.append("• ").append(key).append(": ")
                .append(value).append('\n'));
    }

    private void appendPersonality(StringBuilder out, SajuResult.PersonalityAnalysis personality) {
        if (personality == null) {
            return;
        }
        out.append("\n🧠 성향 분석\n");
        line(out, "• 핵심", personality.getCore());
        line(out, "• 강점", join(personality.getStrengths()));
        line(out, "• 주의", join(personality.getCautions()));
        line(out, "• 성장 팁", personality.getGrowthTip());
    }

    private void appendAstrologyProfile(StringBuilder out, WesternAstrologyProfile profile) {
        if (profile == null) {
            return;
        }
        out.append("\n🌌 출생 차트\n")
                .append("• 태양: ").append(profile.getSunSign()).append(" ")
                .append(decimal(profile.getSunDegree())).append("°\n")
                .append("• 달: ").append(profile.getMoonSign()).append(" ")
                .append(decimal(profile.getMoonDegree())).append("°\n");
        if (profile.getRisingSign() != null) {
            out.append("• 상승궁: ").append(profile.getRisingSign()).append(" ")
                    .append(profile.getRisingDegree() == null ? "" : decimal(profile.getRisingDegree()) + "°")
                    .append('\n');
        }
        line(out, "• 원소/양식", text(profile.getElement()) + " / " + text(profile.getModality()));
        line(out, "• 수호 행성", profile.getRulingPlanet());
        line(out, "• 달의 위상", profile.getNatalMoonPhase());
        line(out, "• 해석", profile.getSummary());
    }

    private void appendTransits(StringBuilder out, List<AstrologyTransit> transits) {
        if (!hasItems(transits)) {
            return;
        }
        out.append("\n🪐 주요 트랜짓\n");
        for (AstrologyTransit item : transits) {
            out.append("• ").append(text(item.getTransitingBody())).append(" → ")
                    .append(text(item.getNatalPoint())).append(" · ")
                    .append(text(item.getAspect())).append(" · orb ")
                    .append(decimal(item.getOrb())).append("° · ")
                    .append(item.getScoreAdjustment() >= 0 ? "+" : "")
                    .append(item.getScoreAdjustment()).append("점\n  ")
                    .append(text(item.getInterpretation())).append('\n');
        }
    }

    private void appendZodiacDaily(StringBuilder out, ZodiacDailyFortune daily) {
        if (daily == null) {
            return;
        }
        out.append("\n☀️ 일간 운세 · ").append(daily.getOverallScore()).append("점\n");
        line(out, "• 종합", daily.getOverallMessage());
        line(out, "• 점수 근거", daily.getScoreBasis());
        scoreLine(out, "💕 연애", daily.getLoveScore(), daily.getLoveMessage());
        scoreLine(out, "💼 직업", daily.getCareerScore(), daily.getCareerMessage());
        scoreLine(out, "💪 건강", daily.getHealthScore(), daily.getHealthMessage());
        scoreLine(out, "💰 재물", daily.getMoneyScore(), daily.getMoneyMessage());
    }

    private void appendZodiacWeekly(StringBuilder out, ZodiacWeeklyFortune weekly) {
        if (weekly == null) {
            return;
        }
        out.append("\n📆 주간 운세 · ").append(weekly.getOverallScore()).append("점\n");
        line(out, "• 기간", weekly.getStartDate() + " ~ " + weekly.getEndDate());
        line(out, "• 개요", weekly.getOverview());
        line(out, "• 점수 근거", weekly.getScoreBasis());
        line(out, "• 최고일", weekly.getBestDate() + " · " + text(weekly.getBestDayReason()));
        line(out, "• 주의", weekly.getCaution());
        if (hasItems(weekly.getDays())) {
            for (ZodiacWeeklyDay day : weekly.getDays()) {
                out.append("  - ").append(day.getDate()).append(" · ")
                        .append(day.getOverallScore()).append("점 · ")
                        .append(text(day.getHeadline())).append(" · ")
                        .append(text(day.getTransitSummary())).append('\n');
            }
        }
    }

    private void appendZodiacMonthly(StringBuilder out, ZodiacMonthlyFortune monthly) {
        if (monthly == null) {
            return;
        }
        out.append("\n🗓️ 월간 운세 · ").append(monthly.getMonth()).append("월 · ")
                .append(monthly.getOverallScore()).append("점\n");
        line(out, "• 주제", monthly.getTheme());
        line(out, "• 상세", monthly.getDetailedMessage());
        line(out, "• 기회", monthly.getOpportunity());
        line(out, "• 주의", monthly.getCaution());
        line(out, "• 점수 근거", monthly.getScoreBasis());
    }

    private void appendZodiacAnnual(StringBuilder out, ZodiacAnnualFortune annual) {
        if (annual == null) {
            return;
        }
        out.append("\n🗓️ 연간 운세 · ").append(annual.getYear()).append("년 · ")
                .append(annual.getOverallScore()).append("점\n");
        line(out, "• 개요", annual.getOverview());
        line(out, "• 점수 근거", annual.getScoreBasis());
        line(out, "• 최고의 달", annual.getBestMonth() + "월 · " + text(annual.getBestMonthReason()));
        line(out, "• 주의할 달", annual.getCautionMonth() + "월 · " + text(annual.getCaution()));
        if (hasItems(annual.getMonths())) {
            for (ZodiacAnnualMonth month : annual.getMonths()) {
                out.append("  - ").append(month.getMonth()).append("월 · ")
                        .append(month.getOverallScore()).append("점 · ")
                        .append(text(month.getTheme())).append(" · ")
                        .append(text(month.getSummary())).append('\n');
            }
        }
    }

    private void scoreLine(StringBuilder out, String label, int score, String message) {
        out.append(label).append(": ").append(score).append("점 · ")
                .append(text(message)).append('\n');
    }

    private void line(StringBuilder out, String label, Object value) {
        if (value != null && !value.toString().isBlank() && !"-".equals(value.toString())) {
            out.append(label).append(": ").append(value).append('\n');
        }
    }

    private String grade(int score) {
        if (score >= 90) return "최상";
        if (score >= 80) return "매우 좋음";
        if (score >= 70) return "좋음";
        if (score >= 60) return "보통";
        if (score >= 50) return "주의";
        return "신중";
    }

    private String name(String value) {
        return value == null || value.isBlank() ? "사용자" : value.strip();
    }

    private String text(Object value) {
        return value == null || value.toString().isBlank() ? "-" : value.toString();
    }

    private String join(List<?> values) {
        if (!hasItems(values)) {
            return null;
        }
        StringJoiner joiner = new StringJoiner(", ");
        values.stream().filter(value -> value != null && !value.toString().isBlank())
                .forEach(value -> joiner.add(value.toString()));
        return joiner.toString();
    }

    private boolean hasItems(List<?> values) {
        return values != null && !values.isEmpty();
    }

    private String decimal(double value) {
        return String.format("%.1f", value);
    }
}
