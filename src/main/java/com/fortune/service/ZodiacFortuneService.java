package com.fortune.service;

import com.fortune.dto.*;
import com.fortune.enums.Zodiac;
import org.springframework.stereotype.Service;
import lombok.extern.slf4j.Slf4j;

import java.time.LocalDate;
import java.time.MonthDay;
import java.util.*;

/**
 * 별자리 운세 서비스
 *
 * @author 하진영
 * @version 2.5.0
 * @since 2025-06-24
 */
@Slf4j
@Service
public class ZodiacFortuneService {

    /**
     * 별자리 날짜 범위
     */
    private static final Map<Zodiac, List<MonthDay>> ZODIAC_DATES = new HashMap<>();

    /**
     * 별자리별 운세 문구
     */
    private static final Map<Zodiac, Map<String, String>> ZODIAC_FORTUNES = new HashMap<>();

    /**
     * 별자리 궁합
     */
    private static final Map<Zodiac, List<Zodiac>> ZODIAC_COMPATIBILITY = new HashMap<>();

    /**
     * 별자리별 성격 특성
     */
    private static final Map<Zodiac, String> ZODIAC_PERSONALITIES = new HashMap<>();

    /**
     * 별자리별 행운의 색깔
     */
    private static final Map<Zodiac, String> ZODIAC_LUCKY_COLORS = new HashMap<>();

    /**
     * 별자리별 행운의 보석
     */
    private static final Map<Zodiac, String> ZODIAC_LUCKY_STONES = new HashMap<>();

    /**
     * 정적 초기화 메서드들
     */
    static {
        initializeZodiacDates();
        initializeZodiacFortunes();
        initializeZodiacCompatibility();
        initializeZodiacPersonalities();
        initializeZodiacLuckyColors();
        initializeZodiacLuckyStones();
    }

    /**
     * 별자리 운세 계산
     * SQL: SELECT * FROM zodiac_fortunes;
     * @param birthDate 생년월일
     * @param targetDate 대상 날짜
     * @return 별자리 운세 결과
     */
    public ZodiacFortuneResult calculateZodiacFortune(LocalDate birthDate, LocalDate targetDate) {
        log.info("⭐ 별자리 운세 계산 시작: {} -> {}", birthDate, targetDate);

        try {
            /* 1. 별자리 판정 */
            Zodiac zodiac = determineZodiac(birthDate);

            /* 2. 오늘의 운세 계산 */
            ZodiacDailyFortune dailyFortune = calculateDetailedDailyFortune(zodiac, targetDate);

            /* 3. 월별 운세 계산 */
            ZodiacMonthlyFortune monthlyFortune = calculateMonthlyFortune(zodiac, targetDate);

            /* 4. 궁합 별자리 조회 */
            List<Zodiac> compatibleZodiacs = ZODIAC_COMPATIBILITY.getOrDefault(zodiac, new ArrayList<>());

            /* 5. 행운의 숫자 생성 */
            List<Integer> luckyNumbers = generateLuckyNumbers(birthDate);

            /* 별자리 운세 결과 생성 */
            ZodiacFortuneResult result = ZodiacFortuneResult.builder()
                    .zodiac(zodiac)
                    .zodiacKoreanName(zodiac.getKoreanName())
                    .targetDate(targetDate)
                    .todayFortune(dailyFortune)
                    .monthlyFortune(monthlyFortune)
                    .compatibleZodiacs(compatibleZodiacs)
                    .luckyNumbers(luckyNumbers)
                    .luckyColor(ZODIAC_LUCKY_COLORS.getOrDefault(zodiac, "흰색"))
                    .luckyStone(ZODIAC_LUCKY_STONES.getOrDefault(zodiac, "수정"))
                    .personality(ZODIAC_PERSONALITIES.getOrDefault(zodiac, "균형잡힌 성격입니다."))
                    .build();

            /* 별자리 운세 결과 반환 */
            log.info("✅ 별자리 운세 계산 완료: {}", zodiac.getKoreanName());
            return result;

        } catch (Exception e) {
            log.error("❌ 별자리 운세 계산 중 오류 발생: {}", e.getMessage(), e);
            throw new RuntimeException("별자리 운세 계산 중 오류가 발생했습니다: " + e.getMessage(), e);
        }
    }

    /**
     * 별자리 판정
     * SQL: SELECT * FROM zodiac_fortunes;
     * @param birthDate 생년월일
     * @return 별자리
     */
    private Zodiac determineZodiac(LocalDate birthDate) {
        /* 생년월일 월일 변환 */
        MonthDay birthMonthDay = MonthDay.from(birthDate);

        /* 별자리 판정 */
        for (Map.Entry<Zodiac, List<MonthDay>> entry : ZODIAC_DATES.entrySet()) {
            /* 별자리 날짜 범위 조회 */
            List<MonthDay> dateRange = entry.getValue();

            /* 날짜 범위 시작일 */
            MonthDay startDate = dateRange.get(0);

            /* 날짜 범위 종료일 */
            MonthDay endDate = dateRange.get(1);

            /* 날짜 범위 확인 */
            if (isWithinRange(birthMonthDay, startDate, endDate)) {
                return entry.getKey();
            }
        }
        return Zodiac.ARIES; // 기본값
    }

    /**
     * 날짜 범위 확인
     * SQL: SELECT * FROM zodiac_fortunes;
     * @param target 대상 날짜
     * @param start 날짜 범위 시작일
     * @param end 날짜 범위 종료일
     * @return 날짜 범위 확인 여부
     */
    private boolean isWithinRange(MonthDay target, MonthDay start, MonthDay end) {
        /* 날짜 범위 확인 */
        if (start.compareTo(end) <= 0) {
            /* 날짜 범위 시작일이 종료일보다 작거나 같은 경우 */
            return target.compareTo(start) >= 0 && target.compareTo(end) <= 0;
        } else {
            /* 날짜 범위 시작일이 종료일보다 큰 경우 */
            return target.compareTo(start) >= 0 || target.compareTo(end) <= 0;
        }
    }

    /**
     * 일일 운세 계산
     * SQL: SELECT * FROM zodiac_fortunes;
     * @param zodiac 별자리
     * @param targetDate 대상 날짜
     * @return 일일 운세 결과
     */
    private ZodiacDailyFortune calculateDetailedDailyFortune(Zodiac zodiac, LocalDate targetDate) {
        /* 날짜 기반 랜덤 시드 생성 */
        Random random = new Random(targetDate.toEpochDay() + zodiac.ordinal());

        /* 기본 점수 생성 (별자리별 기본 점수 + 변동) */
        int loveScore = generateScore(zodiac, "love", random);

        /* 직장 점수 생성 */
        int careerScore = generateScore(zodiac, "career", random);

        /* 건강 점수 생성 */
        int healthScore = generateScore(zodiac, "health", random);

        /* 금전 점수 생성 */
        int moneyScore = generateScore(zodiac, "money", random);

        /* 상세 메시지 생성 */
        String loveMessage = generateDetailedMessage(zodiac, "love", loveScore);

        /* 직장 메시지 생성 */
        String careerMessage = generateDetailedMessage(zodiac, "career", careerScore);

        /* 건강 메시지 생성 */
        String healthMessage = generateDetailedMessage(zodiac, "health", healthScore);

        /* 금전 메시지 생성 */
        String moneyMessage = generateDetailedMessage(zodiac, "money", moneyScore);

        /* 종합 메시지 생성 */
        Map<String, String> fortuneTexts = ZODIAC_FORTUNES.get(zodiac);

        /* 종합 메시지 생성 */
        String overall = (fortuneTexts != null) ?
                fortuneTexts.getOrDefault("overall", zodiac.getKoreanName() + "님의 하루입니다.") :
                zodiac.getKoreanName() + "님의 하루입니다.";

        return ZodiacDailyFortune.builder()
                .overallMessage(overall)
                .loveScore(loveScore)
                .loveMessage(loveMessage)
                .careerScore(careerScore)
                .careerMessage(careerMessage)
                .healthScore(healthScore)
                .healthMessage(healthMessage)
                .moneyScore(moneyScore)
                .moneyMessage(moneyMessage)
                .build();
    }

    /**
     * 점수 생성
     * SQL: SELECT * FROM zodiac_fortunes;
     * @param zodiac 별자리
     * @param category 카테고리
     * @param random 랜덤 시드
     * @return 점수
     */
    private int generateScore(Zodiac zodiac, String category, Random random) {
        /* 별자리별 기본 점수 생성 */
        int base = 50 + zodiac.ordinal() * 3;

        /* -20 ~ +20 변동 */
        int variation = random.nextInt(41) - 20;

        /* 0 ~ 100 점수 범위 제한 */
        return Math.max(0, Math.min(100, base + variation));
    }

    /**
     * 상세 메시지 생성
     * SQL: SELECT * FROM zodiac_fortunes;
     * @param zodiac 별자리
     * @param category 카테고리
     * @param score 점수
     * @return 상세 메시지
     */
    private String generateDetailedMessage(Zodiac zodiac, String category, int score) {
        /* 별자리 이름 생성 */
        String zodiacName = zodiac.getKoreanName();

        /* 점수 80점 이상 생성 */
        if (score >= 80) {
            /* 카테고리별 메시지 생성 */
            return switch (category) {
                case "love" -> zodiacName + "의 연애운이 최고조입니다! 좋은 만남이나 깊어지는 관계를 기대하세요.";
                case "career" -> "직장에서 인정받고 승진 기회가 올 수 있습니다.";
                case "health" -> "건강 상태가 매우 좋습니다. 활력이 넘치는 하루입니다.";
                case "money" -> "금전운이 상승세입니다. 투자나 부업에 좋은 기회가 있을 수 있습니다.";
                default -> "모든 면에서 좋은 하루입니다.";
            };
        } else if (score >= 60) {
            /* 카테고리별 메시지 생성 */
            return switch (category) {
                case "love" -> "연애운이 안정적입니다. 기존 관계가 더욱 돈독해질 수 있습니다.";
                case "career" -> "업무가 순조롭게 진행됩니다. 꾸준한 노력이 결실을 맺을 것입니다.";
                case "health" -> "건강 상태가 양호합니다. 규칙적인 생활을 유지하세요.";
                case "money" -> "재정 상태가 안정적입니다. 계획적인 소비를 권합니다.";
                default -> "전반적으로 무난한 하루입니다.";
            };
        } else {
            /* 카테고리별 메시지 생성 */
            return switch (category) {
                case "love" -> "연애운이 다소 주춤합니다. 상대방을 이해하려는 노력이 필요합니다.";
                case "career" -> "업무에서 어려움이 있을 수 있습니다. 신중하게 행동하세요.";
                case "health" -> "건강에 신경쓰세요. 충분한 휴식과 영양 섭취가 필요합니다.";
                case "money" -> "금전 관리에 주의가 필요합니다. 불필요한 지출을 피하세요.";
                default -> "조심스러운 하루입니다.";
            };
        }
    }

    /**
     * 월별 운세 계산
     * 
     * @param zodiac 별자리
     * @param targetDate 대상 날짜
     * @return 월별 운세 결과
     */
    private ZodiacMonthlyFortune calculateMonthlyFortune(Zodiac zodiac, LocalDate targetDate) {
        // 대상 날짜 월 값
        int month = targetDate.getMonthValue();

        // 날짜 기반 랜덤 시드 생성
        Random random = new Random(zodiac.ordinal() + month);

        // 종합 점수 생성
        int overallScore = 40 + random.nextInt(40);

        // 월별 테마 생성
        String theme = generateMonthlyTheme(zodiac, month);

        // 상세 메시지 생성
        String detailedMessage = generateMonthlyMessage(zodiac, month, overallScore);

        // 주의 메시지 생성
        String caution = generateCaution(overallScore);

        // 기회 메시지 생성
        String opportunity = generateOpportunity(overallScore);

        return ZodiacMonthlyFortune.builder()
                .month(month)
                .overallScore(overallScore)
                .theme(theme)
                .detailedMessage(detailedMessage)
                .caution(caution)
                .opportunity(opportunity)
                .build();
    }

    /**
     * 행운의 숫자 생성
     * 
     * @param birthDate 생년월일
     * @return 행운의 숫자 리스트
     */
    private List<Integer> generateLuckyNumbers(LocalDate birthDate) {
        /* 날짜 기반 랜덤 시드 생성 
         * - 날짜 기반 랜덤 시드 생성
        */
        Random random = new Random(birthDate.toEpochDay());

        /* 행운의 숫자 리스트 생성 */
        Set<Integer> numbers = new HashSet<>();

        /* 3개의 행운의 숫자 생성 */
        while (numbers.size() < 3) {
            numbers.add(random.nextInt(99) + 1);
        }

        return new ArrayList<>(numbers);
    }

    /**
     * 별자리 날짜 초기화
     * SQL: SELECT * FROM zodiac_fortunes;
     */
    private static void initializeZodiacDates() {
        ZODIAC_DATES.put(Zodiac.ARIES, Arrays.asList(MonthDay.of(3, 21), MonthDay.of(4, 19)));
        ZODIAC_DATES.put(Zodiac.TAURUS, Arrays.asList(MonthDay.of(4, 20), MonthDay.of(5, 20)));
        ZODIAC_DATES.put(Zodiac.GEMINI, Arrays.asList(MonthDay.of(5, 21), MonthDay.of(6, 20)));
        ZODIAC_DATES.put(Zodiac.CANCER, Arrays.asList(MonthDay.of(6, 21), MonthDay.of(7, 22)));
        ZODIAC_DATES.put(Zodiac.LEO, Arrays.asList(MonthDay.of(7, 23), MonthDay.of(8, 22)));
        ZODIAC_DATES.put(Zodiac.VIRGO, Arrays.asList(MonthDay.of(8, 23), MonthDay.of(9, 22)));
        ZODIAC_DATES.put(Zodiac.LIBRA, Arrays.asList(MonthDay.of(9, 23), MonthDay.of(10, 22)));
        ZODIAC_DATES.put(Zodiac.SCORPIO, Arrays.asList(MonthDay.of(10, 23), MonthDay.of(11, 21)));
        ZODIAC_DATES.put(Zodiac.SAGITTARIUS, Arrays.asList(MonthDay.of(11, 22), MonthDay.of(12, 21)));
        ZODIAC_DATES.put(Zodiac.CAPRICORN, Arrays.asList(MonthDay.of(12, 22), MonthDay.of(1, 19)));
        ZODIAC_DATES.put(Zodiac.AQUARIUS, Arrays.asList(MonthDay.of(1, 20), MonthDay.of(2, 18)));
        ZODIAC_DATES.put(Zodiac.PISCES, Arrays.asList(MonthDay.of(2, 19), MonthDay.of(3, 20)));
    }

    /**
     * 별자리 운세 초기화
     * SQL: SELECT * FROM zodiac_fortunes;
     */
    private static void initializeZodiacFortunes() {
        /* 양자리 운세 초기화 */
        Map<String, String> ariesFortunes = Map.of(
                "overall", "에너지가 넘치는 양자리님, 오늘은 새로운 도전의 기회가 찾아올 것입니다.",
                "love", "적극적인 어프로치가 좋은 결과를 가져올 것입니다.",
                "career", "리더십을 발휘할 수 있는 기회가 생깁니다.",
                "health", "활동적인 운동이 건강에 도움이 됩니다."
        );

        /* 양자리 운세 초기화 */
        ZODIAC_FORTUNES.put(Zodiac.ARIES, ariesFortunes);

        /* 황소자리 운세 초기화 */
        Map<String, String> taurusFortunes = Map.of(
                "overall", "안정적인 황소자리님, 꾸준함이 빛을 발하는 하루입니다.",
                "love", "진실한 마음이 상대방에게 전해질 것입니다.",
                "career", "차근차근 진행하는 일이 좋은 성과를 가져올 것입니다.",
                "health", "규칙적인 생활이 건강의 비결입니다."
        );

        /* 황소자리 운세 초기화 */
        ZODIAC_FORTUNES.put(Zodiac.TAURUS, taurusFortunes);

        /* 나머지 별자리들도 유사하게 초기화 */
        for (Zodiac zodiac : Zodiac.values()) {
            /* 별자리별 운세 초기화 */
            if (!ZODIAC_FORTUNES.containsKey(zodiac)) {
                /* 별자리별 운세 초기화 */
                Map<String, String> defaultFortunes = Map.of(
                        "overall", zodiac.getKoreanName() + "님의 특별한 하루입니다.",
                        "love", "사랑에 있어서 긍정적인 변화가 있을 것입니다.",
                        "career", "업무에서 좋은 성과를 기대할 수 있습니다.",
                        "health", "건강 관리에 신경쓰는 것이 좋겠습니다."
                );

                /* 별자리별 운세 초기화 */
                ZODIAC_FORTUNES.put(zodiac, defaultFortunes);
            }
        }
    }

    /**
     * 별자리 궁합 초기화
     * SQL: SELECT * FROM zodiac_fortunes;
     */
    private static void initializeZodiacCompatibility() {
        ZODIAC_COMPATIBILITY.put(Zodiac.ARIES, Arrays.asList(Zodiac.LEO, Zodiac.SAGITTARIUS, Zodiac.GEMINI));
        ZODIAC_COMPATIBILITY.put(Zodiac.TAURUS, Arrays.asList(Zodiac.VIRGO, Zodiac.CAPRICORN, Zodiac.CANCER));
        ZODIAC_COMPATIBILITY.put(Zodiac.GEMINI, Arrays.asList(Zodiac.LIBRA, Zodiac.AQUARIUS, Zodiac.ARIES));
        ZODIAC_COMPATIBILITY.put(Zodiac.CANCER, Arrays.asList(Zodiac.SCORPIO, Zodiac.PISCES, Zodiac.TAURUS));
        ZODIAC_COMPATIBILITY.put(Zodiac.LEO, Arrays.asList(Zodiac.ARIES, Zodiac.SAGITTARIUS, Zodiac.LIBRA));
        ZODIAC_COMPATIBILITY.put(Zodiac.VIRGO, Arrays.asList(Zodiac.TAURUS, Zodiac.CAPRICORN, Zodiac.SCORPIO));
        ZODIAC_COMPATIBILITY.put(Zodiac.LIBRA, Arrays.asList(Zodiac.GEMINI, Zodiac.AQUARIUS, Zodiac.LEO));
        ZODIAC_COMPATIBILITY.put(Zodiac.SCORPIO, Arrays.asList(Zodiac.CANCER, Zodiac.PISCES, Zodiac.VIRGO));
        ZODIAC_COMPATIBILITY.put(Zodiac.SAGITTARIUS, Arrays.asList(Zodiac.ARIES, Zodiac.LEO, Zodiac.AQUARIUS));
        ZODIAC_COMPATIBILITY.put(Zodiac.CAPRICORN, Arrays.asList(Zodiac.TAURUS, Zodiac.VIRGO, Zodiac.PISCES));
        ZODIAC_COMPATIBILITY.put(Zodiac.AQUARIUS, Arrays.asList(Zodiac.GEMINI, Zodiac.LIBRA, Zodiac.SAGITTARIUS));
        ZODIAC_COMPATIBILITY.put(Zodiac.PISCES, Arrays.asList(Zodiac.CANCER, Zodiac.SCORPIO, Zodiac.CAPRICORN));
    }

    /**
     * 별자리 성격 초기화
     * SQL: SELECT * FROM zodiac_fortunes;
     */
    private static void initializeZodiacPersonalities() {
        ZODIAC_PERSONALITIES.put(Zodiac.ARIES, "열정적이고 진취적인 성격으로 새로운 도전을 좋아합니다.");
        ZODIAC_PERSONALITIES.put(Zodiac.TAURUS, "안정적이고 신뢰할 수 있는 성격으로 꾸준함이 장점입니다.");
        ZODIAC_PERSONALITIES.put(Zodiac.GEMINI, "호기심이 많고 다재다능한 성격으로 적응력이 뛰어납니다.");
        ZODIAC_PERSONALITIES.put(Zodiac.CANCER, "감성적이고 배려심이 깊은 성격으로 가족을 소중히 여깁니다.");
        ZODIAC_PERSONALITIES.put(Zodiac.LEO, "자신감 있고 관대한 성격으로 타고난 리더십을 지녔습니다.");
        ZODIAC_PERSONALITIES.put(Zodiac.VIRGO, "완벽주의적이고 세심한 성격으로 분석력이 뛰어납니다.");
        ZODIAC_PERSONALITIES.put(Zodiac.LIBRA, "조화를 중시하고 우아한 성격으로 균형감각이 뛰어납니다.");
        ZODIAC_PERSONALITIES.put(Zodiac.SCORPIO, "강인하고 신비로운 성격으로 깊은 통찰력을 지녔습니다.");
        ZODIAC_PERSONALITIES.put(Zodiac.SAGITTARIUS, "자유롭고 낙천적인 성격으로 모험을 즐깁니다.");
        ZODIAC_PERSONALITIES.put(Zodiac.CAPRICORN, "책임감이 강하고 현실적인 성격으로 목표 달성에 집중합니다.");
        ZODIAC_PERSONALITIES.put(Zodiac.AQUARIUS, "독창적이고 인도주의적인 성격으로 혁신을 추구합니다.");
        ZODIAC_PERSONALITIES.put(Zodiac.PISCES, "상상력이 풍부하고 직감적인 성격으로 예술적 감각이 뛰어납니다.");
    }

    /**
     * 별자리 행운의 색깔 초기화
     * SQL: SELECT * FROM zodiac_fortunes;
     */
    private static void initializeZodiacLuckyColors() {
        ZODIAC_LUCKY_COLORS.put(Zodiac.ARIES, "빨간색");
        ZODIAC_LUCKY_COLORS.put(Zodiac.TAURUS, "녹색");
        ZODIAC_LUCKY_COLORS.put(Zodiac.GEMINI, "노란색");
        ZODIAC_LUCKY_COLORS.put(Zodiac.CANCER, "은색");
        ZODIAC_LUCKY_COLORS.put(Zodiac.LEO, "금색");
        ZODIAC_LUCKY_COLORS.put(Zodiac.VIRGO, "갈색");
        ZODIAC_LUCKY_COLORS.put(Zodiac.LIBRA, "분홍색");
        ZODIAC_LUCKY_COLORS.put(Zodiac.SCORPIO, "검은색");
        ZODIAC_LUCKY_COLORS.put(Zodiac.SAGITTARIUS, "보라색");
        ZODIAC_LUCKY_COLORS.put(Zodiac.CAPRICORN, "회색");
        ZODIAC_LUCKY_COLORS.put(Zodiac.AQUARIUS, "파란색");
        ZODIAC_LUCKY_COLORS.put(Zodiac.PISCES, "바다색");
    }

    /**
     * 별자리 행운의 보석 초기화
     * SQL: SELECT * FROM zodiac_fortunes;
     */
    private static void initializeZodiacLuckyStones() {
        ZODIAC_LUCKY_STONES.put(Zodiac.ARIES, "다이아몬드");
        ZODIAC_LUCKY_STONES.put(Zodiac.TAURUS, "에메랄드");
        ZODIAC_LUCKY_STONES.put(Zodiac.GEMINI, "펄");
        ZODIAC_LUCKY_STONES.put(Zodiac.CANCER, "루비");
        ZODIAC_LUCKY_STONES.put(Zodiac.LEO, "페리도트");
        ZODIAC_LUCKY_STONES.put(Zodiac.VIRGO, "사파이어");
        ZODIAC_LUCKY_STONES.put(Zodiac.LIBRA, "오팔");
        ZODIAC_LUCKY_STONES.put(Zodiac.SCORPIO, "토파즈");
        ZODIAC_LUCKY_STONES.put(Zodiac.SAGITTARIUS, "터키석");
        ZODIAC_LUCKY_STONES.put(Zodiac.CAPRICORN, "가넷");
        ZODIAC_LUCKY_STONES.put(Zodiac.AQUARIUS, "자수정");
        ZODIAC_LUCKY_STONES.put(Zodiac.PISCES, "아쿠아마린");
    }

    /**
     * 월별 테마 생성
     * SQL: SELECT * FROM zodiac_fortunes;
     * @param zodiac 별자리
     * @param month 월
     * @return 월별 테마
     */
    private String generateMonthlyTheme(Zodiac zodiac, int month) {
        /* 월별 테마 생성 */
        return switch (month) {
            case 1, 2, 12 -> "새로운 시작과 계획의 시기";
            case 3, 4, 5 -> "성장과 발전의 시기";
            case 6, 7, 8 -> "활동과 성취의 시기";
            case 9, 10, 11 -> "수확과 정리의 시기";
            default -> "변화와 도전의 시기";
        };
    }

    /**
     * 월별 메시지 생성
     * SQL: SELECT * FROM zodiac_fortunes;
     * @param zodiac 별자리
     * @param month 월
     * @param score 점수
     * @return 월별 메시지
     */
    private String generateMonthlyMessage(Zodiac zodiac, int month, int score) {
        /* 별자리 이름 생성 */
        String zodiacName = zodiac.getKoreanName();
        /* 점수가 70점 이상 */
        if (score >= 70) {
            return month + "월은 " + zodiacName + "님에게 특별히 좋은 달입니다. 많은 기회가 찾아올 것입니다.";
        } else if (score >= 50) {
            return month + "월은 " + zodiacName + "님에게 안정적인 달입니다. 꾸준한 노력이 빛을 발할 것입니다.";
        } else {
            return month + "월은 " + zodiacName + "님에게 조심스러운 달입니다. 신중한 판단이 필요합니다.";
        }
    }

    /**
     * 주의 메시지 생성
     * SQL: SELECT * FROM zodiac_fortunes;
     * @param score 점수
     * @return 주의 메시지
     */
    private String generateCaution(int score) {
        /* 점수가 50점 미만 */
        if (score < 50) {
            return "급한 결정은 피하고, 충분히 생각한 후 행동하세요.";
        } else if (score < 70) {
            return "큰 변화보다는 현재 상황을 안정화하는 것이 좋겠습니다.";
        } else {
            return "특별한 주의사항은 없지만, 겸손한 마음가짐을 유지하세요.";
        }
    }

    /**
     * 기회 메시지 생성
     * SQL: SELECT * FROM zodiac_fortunes;
     * @param score 점수
     * @return 기회 메시지
     */
    private String generateOpportunity(int score) {
        /* 점수가 70점 이상 */
        if (score >= 70) {
            return "새로운 프로젝트나 인맥 확장에 좋은 기회가 있을 것입니다.";
        } else if (score >= 50) {
            return "기존 관계나 업무에서 발전할 수 있는 기회를 찾아보세요.";
        } else {
            return "작은 변화부터 시작하여 점진적으로 개선해 나가세요.";
        }
    }
}
