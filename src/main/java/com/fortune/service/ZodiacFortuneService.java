package com.fortune.service;

import com.fortune.enums.Zodiac;
import com.fortune.dto.ZodiacFortuneResult;
import com.fortune.dto.ZodiacFortune;
import org.springframework.stereotype.Service;
import java.time.LocalDate;
import java.time.Month;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

/**
 * 별자리 운세 서비스
 * 별자리 운세 계산
 * 별자리 운세 결과 반환
 */
@Service
public class ZodiacFortuneService {

    /**
     * 별자리 운세 계산
     * @param birthDate 생일
     * @param targetDate 대상 날짜
     * @return 별자리 운세 결과
     */
    public ZodiacFortuneResult calculateZodiacFortune(LocalDate birthDate, LocalDate targetDate) {
        // 1. 생일로부터 별자리 결정
        Zodiac zodiac = determineZodiac(birthDate);

        // 2. 오늘의 별자리 운세 계산
        ZodiacFortune todayFortune = calculateTodayZodiacFortune(zodiac, targetDate);

        // 3. 주간 운세
        ZodiacFortune weeklyFortune = calculateWeeklyZodiacFortune(zodiac, targetDate);

        // 4. 월간 운세
        ZodiacFortune monthlyFortune = calculateMonthlyZodiacFortune(zodiac, targetDate);

        return ZodiacFortuneResult.builder()
                .zodiac(zodiac)
                .targetDate(targetDate)
                .todayFortune(todayFortune)
                .weeklyFortune(weeklyFortune)
                .monthlyFortune(monthlyFortune)
                .compatibleZodiacs(getCompatibleZodiacs(zodiac))
                .luckyNumbers(getLuckyNumbers(zodiac, targetDate))
                .build();
    }

    /**
     * 생일로부터 별자리 결정
     * @param birthDate 생일
     * @return 별자리
     */
    private Zodiac determineZodiac(LocalDate birthDate) {
        Month month = birthDate.getMonth();
        int day = birthDate.getDayOfMonth();

        switch (month) {
            case MARCH:
                return day >= 21 ? Zodiac.ARIES : Zodiac.PISCES;
            case APRIL:
                return day <= 19 ? Zodiac.ARIES : Zodiac.TAURUS;
            case MAY:
                return day <= 20 ? Zodiac.TAURUS : Zodiac.GEMINI;
            case JUNE:
                return day <= 21 ? Zodiac.GEMINI : Zodiac.CANCER;
            case JULY:
                return day <= 22 ? Zodiac.CANCER : Zodiac.LEO;
            case AUGUST:
                return day <= 22 ? Zodiac.LEO : Zodiac.VIRGO;
            case SEPTEMBER:
                return day <= 22 ? Zodiac.VIRGO : Zodiac.LIBRA;
            case OCTOBER:
                return day <= 22 ? Zodiac.LIBRA : Zodiac.SCORPIO;
            case NOVEMBER:
                return day <= 22 ? Zodiac.SCORPIO : Zodiac.SAGITTARIUS;
            case DECEMBER:
                return day <= 21 ? Zodiac.SAGITTARIUS : Zodiac.CAPRICORN;
            case JANUARY:
                return day <= 19 ? Zodiac.CAPRICORN : Zodiac.AQUARIUS;
            case FEBRUARY:
                return day <= 18 ? Zodiac.AQUARIUS : Zodiac.PISCES;
            default:
                return Zodiac.ARIES;
        }
    }

    /**
     * 오늘의 별자리 운세
     * @param zodiac 별자리
     * @param date 날짜
     * @return 오늘의 별자리 운세
     */
    private ZodiacFortune calculateTodayZodiacFortune(Zodiac zodiac, LocalDate date) {
        // 날짜 기반 운세 계산 (간단화된 로직)
        int dayOfYear = date.getDayOfYear();
        int zodiacMultiplier = zodiac.ordinal() + 1;

        int loveScore = (dayOfYear * zodiacMultiplier) % 100;
        int careerScore = (dayOfYear * zodiacMultiplier + 25) % 100;
        int healthScore = (dayOfYear * zodiacMultiplier + 50) % 100;
        int moneyScore = (dayOfYear * zodiacMultiplier + 75) % 100;

        return ZodiacFortune.builder()
                .overall(generateOverallMessage(zodiac, (loveScore + careerScore + healthScore + moneyScore) / 4))
                .love(generateLoveMessage(zodiac, loveScore))
                .career(generateCareerMessage(zodiac, careerScore))
                .health(generateHealthMessage(zodiac, healthScore))
                .money(generateMoneyMessage(zodiac, moneyScore))
                .loveScore(loveScore)
                .careerScore(careerScore)
                .healthScore(healthScore)
                .moneyScore(moneyScore)
                .build();
    }

    /**
     * 별자리별 메시지 생성 메서드들
     * @param zodiac 별자리
     * @param score 점수
     * @return 메시지
     */
    private String generateOverallMessage(Zodiac zodiac, int score) {
        if (score >= 80) {
            return zodiac.getKoreanName() + " 님의 오늘은 매우 밝고 활기찬 하루가 될 것입니다!";
        } else if (score >= 60) {
            return zodiac.getKoreanName() + " 님의 오늘은 평온하고 안정적인 하루입니다.";
        } else {
            return zodiac.getKoreanName() + " 님은 오늘 조금 더 신중하게 행동하는 것이 좋겠습니다.";
        }
    }

    /**
     * 별자리별 애정운 메시지 생성
     * @param zodiac 별자리
     * @param score 점수
     * @return 메시지
     */
    private String generateLoveMessage(Zodiac zodiac, int score) {
        // 별자리별 애정운 메시지 생성
        return "애정 운세 메시지";
    }

    /**
     * 별자리별 직업운 메시지 생성
     * @param zodiac 별자리
     * @param score 점수
     * @return 메시지
     */
    private String generateCareerMessage(Zodiac zodiac, int score) {
        if (score >= 80) return zodiac.getKoreanName() + " 님의 직업운이 매우 좋습니다.";
        if (score >= 60) return zodiac.getKoreanName() + " 님의 직업운이 평안합니다.";
        return zodiac.getKoreanName() + " 님의 직업에 주의가 필요합니다.";
    }

    /**
     * 별자리별 건강운 메시지 생성
     * @param zodiac 별자리
     * @param score 점수
     * @return 메시지
     */
    private String generateHealthMessage(Zodiac zodiac, int score) {
        if (score >= 80) return zodiac.getKoreanName() + " 님의 건강운이 매우 좋습니다.";
        if (score >= 60) return zodiac.getKoreanName() + " 님의 건강운이 평안합니다.";
        return zodiac.getKoreanName() + " 님의 건강에 주의가 필요합니다.";
    }

    /**
     * 별자리별 재물운 메시지 생성
     * @param zodiac 별자리
     * @param score 점수
     * @return 메시지
     */
    private String generateMoneyMessage(Zodiac zodiac, int score) {
        if (score >= 80) return zodiac.getKoreanName() + " 님의 재물운이 매우 좋습니다.";
        if (score >= 60) return zodiac.getKoreanName() + " 님의 재물운이 평안합니다.";
        return zodiac.getKoreanName() + " 님의 재물에 주의가 필요합니다.";
    }

    /**
     * 주간 운세 계산
     * @param zodiac 별자리
     * @param date 날짜
     * @return 주간 운세
     */
    private ZodiacFortune calculateWeeklyZodiacFortune(Zodiac zodiac, LocalDate date) {
        // 주간 운세 계산 (간단화된 로직)
        int weekOfYear = date.get(java.time.temporal.WeekFields.ISO.weekOfYear());
        int zodiacMultiplier = zodiac.ordinal() + 1;
        
        int loveScore = (weekOfYear * zodiacMultiplier) % 100;
        int careerScore = (weekOfYear * zodiacMultiplier + 15) % 100;
        int healthScore = (weekOfYear * zodiacMultiplier + 30) % 100;
        int moneyScore = (weekOfYear * zodiacMultiplier + 45) % 100;

        return ZodiacFortune.builder()
                .overall(generateOverallMessage(zodiac, (loveScore + careerScore + healthScore + moneyScore) / 4))
                .love(generateLoveMessage(zodiac, loveScore))
                .career(generateCareerMessage(zodiac, careerScore))
                .health(generateHealthMessage(zodiac, healthScore))
                .money(generateMoneyMessage(zodiac, moneyScore))
                .loveScore(loveScore)
                .careerScore(careerScore)
                .healthScore(healthScore)
                .moneyScore(moneyScore)
                .build();
    }

    /**
     * 월간 운세 계산
     * @param zodiac 별자리
     * @param date 날짜
     * @return 월간 운세
     */
    private ZodiacFortune calculateMonthlyZodiacFortune(Zodiac zodiac, LocalDate date) {
        // 월간 운세 계산 (간단화된 로직)
        int month = date.getMonthValue();
        int zodiacMultiplier = zodiac.ordinal() + 1;
        
        int loveScore = (month * zodiacMultiplier * 3) % 100;
        int careerScore = (month * zodiacMultiplier * 3 + 20) % 100;
        int healthScore = (month * zodiacMultiplier * 3 + 40) % 100;
        int moneyScore = (month * zodiacMultiplier * 3 + 60) % 100;

        return ZodiacFortune.builder()
                .overall(generateOverallMessage(zodiac, (loveScore + careerScore + healthScore + moneyScore) / 4))
                .love(generateLoveMessage(zodiac, loveScore))
                .career(generateCareerMessage(zodiac, careerScore))
                .health(generateHealthMessage(zodiac, healthScore))
                .money(generateMoneyMessage(zodiac, moneyScore))
                .loveScore(loveScore)
                .careerScore(careerScore)
                .healthScore(healthScore)
                .moneyScore(moneyScore)
                .build();
    }

    /**
     * 별자리별 궁합 리스트
     * @param zodiac 별자리
     * @return 궁합 리스트
     */
    private List<Zodiac> getCompatibleZodiacs(Zodiac zodiac) {
        // 별자리별 궁합 리스트
        Map<Zodiac, List<Zodiac>> compatibilityMap = Map.of(
                Zodiac.ARIES, Arrays.asList(Zodiac.LEO, Zodiac.SAGITTARIUS, Zodiac.GEMINI),
                Zodiac.TAURUS, Arrays.asList(Zodiac.VIRGO, Zodiac.CAPRICORN, Zodiac.CANCER)
        );

        return compatibilityMap.getOrDefault(zodiac, Arrays.asList());
    }

    /**
     * 별자리와 날짜 기반 행운의 숫자
     * @param zodiac 별자리
     * @param date 날짜
     * @return 행운의 숫자
     */
    private List<Integer> getLuckyNumbers(Zodiac zodiac, LocalDate date) {
        // 별자리와 날짜 기반 행운의 숫자
        int base = zodiac.ordinal() + 1;
        int dayFactor = date.getDayOfMonth();

        return Arrays.asList(
                (base * dayFactor) % 50 + 1,
                (base * dayFactor + 7) % 50 + 1,
                (base * dayFactor + 14) % 50 + 1
        );
    }
}
