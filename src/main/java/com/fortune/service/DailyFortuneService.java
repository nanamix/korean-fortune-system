package com.fortune.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.fortune.dto.*;
import com.fortune.enums.*;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.Arrays;

@Service
public class DailyFortuneService {

    @Autowired
    private GanjiCalculatorService ganjiCalculator;

    @Autowired
    private SinsalService sinsalService;

    /**
     * 일일 운세 계산
     */
    public DailyFortuneResult calculateDailyFortune(SajuResult saju, LocalDate targetDate) {
        // 1. 해당 날짜의 일주 계산
        String dayPillar = ganjiCalculator.calculateDayPillar(targetDate);

        // 2. 길신/흉신 계산
        List<SinsalInfo> sinsals = sinsalService.calculateDailySinsals(targetDate, saju);

        // 3. 오행 상생상극 분석
        WuxingAnalysis wuxingAnalysis = analyzeWuxingRelation(saju, dayPillar);

        // 4. 종합 점수 계산
        int totalScore = calculateTotalScore(sinsals, wuxingAnalysis);

        // 5. 분야별 운세 계산
        FortuneByCategory categoryFortune = calculateCategoryFortune(saju, targetDate, totalScore);

        return DailyFortuneResult.builder()
                .date(targetDate)
                .dayPillar(dayPillar)
                .sinsals(sinsals)
                .wuxingAnalysis(wuxingAnalysis)
                .totalScore(totalScore)
                .categoryFortune(categoryFortune)
                .advice(generateAdvice(totalScore, sinsals))
                .luckyDirection(calculateLuckyDirection(dayPillar))
                .luckyColors(calculateLuckyColors(saju.getDayMaster()))
                .build();
    }

    /**
     * 오행 상생상극 분석
     */
    private WuxingAnalysis analyzeWuxingRelation(SajuResult saju, String dayPillar) {
        String dayMaster = saju.getDayMaster();
        String targetDayStem = dayPillar.substring(0, 1);

        WuxingElement dayMasterElement = getWuxingElement(dayMaster);
        WuxingElement targetDayElement = getWuxingElement(targetDayStem);

        WuxingRelation relation = getWuxingRelation(dayMasterElement, targetDayElement);

        return WuxingAnalysis.builder()
                .dayMasterElement(dayMasterElement)
                .targetDayElement(targetDayElement)
                .relation(relation)
                .relationScore(getRelationScore(relation))
                .description(getRelationDescription(relation))
                .build();
    }

    /**
     * 종합 점수 계산
     */
    private int calculateTotalScore(List<SinsalInfo> sinsals, WuxingAnalysis wuxing) {
        int baseScore = 50; // 기본 점수

        // 신살 영향
        int sinsalScore = sinsals.stream()
                .mapToInt(SinsalInfo::getImpact)
                .sum();

        // 오행 영향
        int wuxingScore = wuxing.getRelationScore();

        int totalScore = baseScore + (sinsalScore / 10) + (wuxingScore / 5);

        // 0-100 범위로 제한
        return Math.max(0, Math.min(100, totalScore));
    }

    /**
     * 분야별 운세 계산
     */
    private FortuneByCategory calculateCategoryFortune(SajuResult saju, LocalDate date, int baseScore) {
        return FortuneByCategory.builder()
                .overall(FortuneCategory.builder()
                        .score(baseScore)
                        .description(getOverallDescription(baseScore))
                        .build())
                .love(FortuneCategory.builder()
                        .score(calculateLoveFortune(saju, date, baseScore))
                        .description(getLoveDescription(calculateLoveFortune(saju, date, baseScore)))
                        .build())
                .money(FortuneCategory.builder()
                        .score(calculateMoneyFortune(saju, date, baseScore))
                        .description(getMoneyDescription(calculateMoneyFortune(saju, date, baseScore)))
                        .build())
                .health(FortuneCategory.builder()
                        .score(calculateHealthFortune(saju, date, baseScore))
                        .description(getHealthDescription(calculateHealthFortune(saju, date, baseScore)))
                        .build())
                .work(FortuneCategory.builder()
                        .score(calculateWorkFortune(saju, date, baseScore))
                        .description(getWorkDescription(calculateWorkFortune(saju, date, baseScore)))
                        .build())
                .build();
    }

    // 길방위 계산
    private String calculateLuckyDirection(String dayPillar) {
        String dayStem = dayPillar.substring(0, 1);
        Map<String, String> directionMap = Map.of(
                "갑", "동쪽", "을", "동남쪽", "병", "남쪽", "정", "남서쪽",
                "무", "중앙", "기", "중앙", "경", "서쪽", "신", "서북쪽",
                "임", "북쪽", "계", "북동쪽"
        );
        return directionMap.getOrDefault(dayStem, "중앙");
    }

    // 길한 색깔 계산
    private List<String> calculateLuckyColors(String dayMaster) {
        WuxingElement element = getWuxingElement(dayMaster);
        Map<WuxingElement, List<String>> colorMap = Map.of(
                WuxingElement.WOOD, Arrays.asList("녹색", "청색", "검정색"),
                WuxingElement.FIRE, Arrays.asList("빨간색", "주황색", "녹색"),
                WuxingElement.EARTH, Arrays.asList("노란색", "갈색", "빨간색"),
                WuxingElement.METAL, Arrays.asList("흰색", "금색", "노란색"),
                WuxingElement.WATER, Arrays.asList("검정색", "파란색", "흰색")
        );
        return colorMap.getOrDefault(element, Arrays.asList("흰색"));
    }

    // 보조 메서드들
    private WuxingElement getWuxingElement(String stem) {
        Map<String, WuxingElement> elementMap = Map.of(
                "갑", WuxingElement.WOOD, "을", WuxingElement.WOOD,
                "병", WuxingElement.FIRE, "정", WuxingElement.FIRE,
                "무", WuxingElement.EARTH, "기", WuxingElement.EARTH,
                "경", WuxingElement.METAL, "신", WuxingElement.METAL,
                "임", WuxingElement.WATER, "계", WuxingElement.WATER
        );
        return elementMap.get(stem);
    }

    private String generateAdvice(int score, List<SinsalInfo> sinsals) {
        if (score >= 80) {
            return "매우 길한 날입니다! 중요한 일을 시작하거나 결정하기 좋은 시기입니다.";
        } else if (score >= 60) {
            return "평안한 하루가 될 것입니다. 꾸준히 노력하시면 좋은 결과가 있을 것입니다.";
        } else if (score >= 40) {
            return "보통의 하루입니다. 신중하게 행동하시면 무난하게 지낼 수 있습니다.";
        } else {
            return "조심스러운 하루입니다. 중요한 결정은 미루고 안전에 유의하세요.";
        }
    }

    // 누락된 메서드들 추가
    private WuxingRelation getWuxingRelation(WuxingElement dayMaster, WuxingElement targetDay) {
        // 간단한 오행 관계 판단
        if (dayMaster == targetDay) return WuxingRelation.SAME;
        if ((dayMaster == WuxingElement.WOOD && targetDay == WuxingElement.FIRE) ||
            (dayMaster == WuxingElement.FIRE && targetDay == WuxingElement.EARTH) ||
            (dayMaster == WuxingElement.EARTH && targetDay == WuxingElement.METAL) ||
            (dayMaster == WuxingElement.METAL && targetDay == WuxingElement.WATER) ||
            (dayMaster == WuxingElement.WATER && targetDay == WuxingElement.WOOD)) {
            return WuxingRelation.SUPPORT;
        }
        if ((dayMaster == WuxingElement.WOOD && targetDay == WuxingElement.METAL) ||
            (dayMaster == WuxingElement.METAL && targetDay == WuxingElement.FIRE) ||
            (dayMaster == WuxingElement.FIRE && targetDay == WuxingElement.WATER) ||
            (dayMaster == WuxingElement.WATER && targetDay == WuxingElement.EARTH) ||
            (dayMaster == WuxingElement.EARTH && targetDay == WuxingElement.WOOD)) {
            return WuxingRelation.CONFLICT;
        }
        return WuxingRelation.WEAK;
    }

    private int getRelationScore(WuxingRelation relation) {
        switch (relation) {
            case SUPPORT: return 20;
            case SAME: return 15;
            case WEAK: return 0;
            case CONFLICT: return -10;
            case DRAIN: return -5;
            default: return 0;
        }
    }

    private String getRelationDescription(WuxingRelation relation) {
        switch (relation) {
            case SUPPORT: return "상생 관계로 매우 길합니다.";
            case SAME: return "동일한 기운으로 안정적입니다.";
            case WEAK: return "중성적인 관계입니다.";
            case CONFLICT: return "상극 관계로 주의가 필요합니다.";
            case DRAIN: return "설기 관계로 조심해야 합니다.";
            default: return "관계를 알 수 없습니다.";
        }
    }

    private String getOverallDescription(int score) {
        if (score >= 80) return "매우 길한 날입니다.";
        if (score >= 60) return "평안한 하루입니다.";
        if (score >= 40) return "보통의 하루입니다.";
        return "조심스러운 하루입니다.";
    }

    private int calculateLoveFortune(SajuResult saju, LocalDate date, int baseScore) {
        return Math.max(0, Math.min(100, baseScore + (date.getDayOfMonth() % 20)));
    }

    private String getLoveDescription(int score) {
        if (score >= 80) return "애정운이 매우 좋습니다.";
        if (score >= 60) return "애정운이 평안합니다.";
        if (score >= 40) return "애정운이 보통입니다.";
        return "애정운에 주의가 필요합니다.";
    }

    private int calculateMoneyFortune(SajuResult saju, LocalDate date, int baseScore) {
        return Math.max(0, Math.min(100, baseScore + (date.getMonthValue() % 15)));
    }

    private String getMoneyDescription(int score) {
        if (score >= 80) return "재물운이 매우 좋습니다.";
        if (score >= 60) return "재물운이 평안합니다.";
        if (score >= 40) return "재물운이 보통입니다.";
        return "재물운에 주의가 필요합니다.";
    }

    private int calculateHealthFortune(SajuResult saju, LocalDate date, int baseScore) {
        return Math.max(0, Math.min(100, baseScore + (date.getYear() % 10)));
    }

    private String getHealthDescription(int score) {
        if (score >= 80) return "건강운이 매우 좋습니다.";
        if (score >= 60) return "건강운이 평안합니다.";
        if (score >= 40) return "건강운이 보통입니다.";
        return "건강에 주의가 필요합니다.";
    }

    private int calculateWorkFortune(SajuResult saju, LocalDate date, int baseScore) {
        return Math.max(0, Math.min(100, baseScore + (saju.getDayMaster().hashCode() % 20)));
    }

    private String getWorkDescription(int score) {
        if (score >= 80) return "직업운이 매우 좋습니다.";
        if (score >= 60) return "직업운이 평안합니다.";
        if (score >= 40) return "직업운이 보통입니다.";
        return "직업에 주의가 필요합니다.";
    }
}
