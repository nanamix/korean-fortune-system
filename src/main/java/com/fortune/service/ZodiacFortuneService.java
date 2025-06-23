package com.fortune.service;

import com.fortune.dto.*;
import com.fortune.enums.Zodiac;
import org.springframework.stereotype.Service;
import java.time.LocalDate;
import java.time.MonthDay;
import java.util.*;

@Service
public class ZodiacFortuneService {

    // 별자리 날짜 범위
    private static final Map<Zodiac, List<MonthDay>> ZODIAC_DATES = new HashMap<>();

    // 별자리별 운세 문구
    private static final Map<Zodiac, Map<String, String>> ZODIAC_FORTUNES = new HashMap<>();

    // 별자리 궁합
    private static final Map<Zodiac, List<Zodiac>> ZODIAC_COMPATIBILITY = new HashMap<>();

    // 별자리별 성격 특성
    private static final Map<Zodiac, String> ZODIAC_PERSONALITIES = new HashMap<>();

    // 별자리별 행운의 색깔
    private static final Map<Zodiac, String> ZODIAC_LUCKY_COLORS = new HashMap<>();

    // 별자리별 행운의 보석
    private static final Map<Zodiac, String> ZODIAC_LUCKY_STONES = new HashMap<>();

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
     * 이 메서드는 사용자의 생년월일과 대상 날짜를 기반으로 별자리를 판별하고,
     * 해당 별자리의 운세를 계산하여 결과를 반환합니다.
     * @param birthDate 사용자의 생년월일
     * @param targetDate 대상 날짜 (운세를 계산할 날짜)
     * @return ZodiacFortuneResult 별자리 운세 결과 객체
     */
    public ZodiacFortuneResult calculateZodiacFortune(LocalDate birthDate, LocalDate targetDate) {
        // 1. 별자리 판정
        Zodiac zodiac = determineZodiac(birthDate);

        // 2. 오늘의 운세 계산
        ZodiacDailyFortune dailyFortune = calculateDetailedDailyFortune(zodiac, targetDate);

        // 3. 월별 운세 계산
        ZodiacMonthlyFortune monthlyFortune = calculateMonthlyFortune(zodiac, targetDate);

        // 4. 궁합 별자리 조회
        List<Zodiac> compatibleZodiacs = ZODIAC_COMPATIBILITY.getOrDefault(zodiac, new ArrayList<>());

        // 5. 행운의 숫자 생성
        List<Integer> luckyNumbers = generateLuckyNumbers(birthDate);

        return ZodiacFortuneResult.builder()
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
    }

    /**
     * 별자리 판정
     * 이 메서드는 사용자의 생년월일을 기반으로 별자리를 판별합니다.
     * 각 별자리는 특정 날짜 범위에 해당하며,
     * 사용자의 생년월일이 해당 범위에 속하는지 확인합니다.
     * @param birthDate 사용자의 생년월일
     * @return Zodiac 사용자의 별자리
     */
    private Zodiac determineZodiac(LocalDate birthDate) {
        MonthDay birthMonthDay = MonthDay.from(birthDate);

        for (Map.Entry<Zodiac, List<MonthDay>> entry : ZODIAC_DATES.entrySet()) {
            List<MonthDay> dateRange = entry.getValue();
            MonthDay startDate = dateRange.get(0);
            MonthDay endDate = dateRange.get(1);

            if (isWithinRange(birthMonthDay, startDate, endDate)) {
                return entry.getKey();
            }
        }
        return Zodiac.ARIES; // 기본값
    }

    /**
     * 날짜 범위 확인
     * 이 메서드는 특정 날짜가 주어진 시작일과 종료일 범위 내에 있는지 확인합니다.
     * 범위가 연말-연초를 걸치는 경우도 처리합니다.
     * @param target 대상 날짜
     * @param start 시작 날짜
     * @param end 종료 날짜
     * @return boolean 대상 날짜가 범위 내에 있는지 여부
     */
    private boolean isWithinRange(MonthDay target, MonthDay start, MonthDay end) {
        if (start.compareTo(end) <= 0) {
            return target.compareTo(start) >= 0 && target.compareTo(end) <= 0;
        } else {
            return target.compareTo(start) >= 0 || target.compareTo(end) <= 0;
        }
    }

    /**
     * 일일 운세 계산
     * 이 메서드는 별자리와 대상 날짜를 기반으로 일일 운세를 계산합니다.
     * 별자리별 운세 문구와 분야별 점수를 생성합니다.
     * @param zodiac 사용자의 별자리
     * @param targetDate 대상 날짜 (운세를 계산할 날짜)
     * @return ZodiacDailyFortune 일일 운세 결과 객체
     */
    private ZodiacDailyFortune calculateDetailedDailyFortune(Zodiac zodiac, LocalDate targetDate) {
        Random random = new Random(targetDate.toEpochDay());

        // 기본 점수 생성
        int loveScore = generateScore(zodiac, "love", random);
        int careerScore = generateScore(zodiac, "career", random);
        int healthScore = generateScore(zodiac, "health", random);
        int moneyScore = generateScore(zodiac, "money", random);

        // 상세 메시지 생성
        String loveMessage = generateDetailedMessage(zodiac, "love", loveScore);
        String careerMessage = generateDetailedMessage(zodiac, "career", careerScore);
        String healthMessage = generateDetailedMessage(zodiac, "health", healthScore);
        String moneyMessage = generateDetailedMessage(zodiac, "money", moneyScore);

        // 종합 메시지
        Map<String, String> fortuneTexts = ZODIAC_FORTUNES.get(zodiac);
        String overall = (fortuneTexts != null) ?
                fortuneTexts.getOrDefault("overall", zodiac.getKoreanName() + "님의 하루입니다.") :
                zodiac.getKoreanName() + "님의 하루입니다.";

        return ZodiacDailyFortune.builder()
                .overall(overall)
                .loveScore(loveScore)
                .careerScore(careerScore)
                .healthScore(healthScore)
                .moneyScore(moneyScore)
                .loveMessage(loveMessage)
                .careerMessage(careerMessage)
                .healthMessage(healthMessage)
                .moneyMessage(moneyMessage)
                .build();
    }

    /**
     * 월별 운세 계산
     * 이 메서드는 별자리와 분야를 기반으로 상세 운세 메시지를 생성합니다.
     * 각 분야별로 점수에 따라 다른 메시지를 반환합니다.
     * @param zodiac 사용자의 별자리
     * @param targetDate 분야 (예: "love", "career", "health", "money")
     * @return String 상세 운세 메시지
     */
    private ZodiacMonthlyFortune calculateMonthlyFortune(Zodiac zodiac, LocalDate targetDate) {
        Map<Integer, ZodiacMonthInfo> monthlyInfo = new HashMap<>();
        List<Integer> bestMonths = new ArrayList<>();
        List<Integer> cautionMonths = new ArrayList<>();

        for (int month = 1; month <= 12; month++) {
            int score = generateMonthlyScore(zodiac, month);
            String theme = generateMonthlyTheme(zodiac, month);
            String advice = generateMonthlyAdvice(score);
            String keyEvent = generateKeyEvent(zodiac, month);

            monthlyInfo.put(month, ZodiacMonthInfo.builder()
                    .month(month)
                    .overallScore(score)
                    .theme(theme)
                    .advice(advice)
                    .keyEvent(keyEvent)
                    .build());

            if (score >= 75) bestMonths.add(month);
            if (score <= 45) cautionMonths.add(month);
        }

        String yearlyTrend = generateYearlyTrend(zodiac);

        return ZodiacMonthlyFortune.builder()
                .monthlyInfo(monthlyInfo)
                .yearlyTrend(yearlyTrend)
                .bestMonths(bestMonths)
                .cautionMonths(cautionMonths)
                .build();
    }

    /**
     * 분야별 상세 메시지 생성
     */
    private String generateDetailedMessage(Zodiac zodiac, String category, int score) {
        if (zodiac == null || category == null) {
            return "운세 정보를 불러올 수 없습니다.";
        }

        if (score >= 80) {
            return getHighScoreMessage(zodiac, category);
        } else if (score >= 60) {
            return getMediumScoreMessage(zodiac, category);
        } else {
            return getLowScoreMessage(zodiac, category);
        }
    }

    private String getHighScoreMessage(Zodiac zodiac, String category) {
        String zodiacName = (zodiac != null) ? zodiac.getKoreanName() : "회원";

        switch (category) {
            case "love":
                return zodiacName + "님의 애정운이 매우 좋습니다. 새로운 만남이나 관계 발전이 기대됩니다.";
            case "career":
                return "직업운이 상승세입니다. 중요한 결정이나 새로운 도전에 좋은 시기입니다.";
            case "health":
                return "건강상태가 양호합니다. 활력이 넘치는 하루가 될 것입니다.";
            case "money":
                return "재물운이 좋습니다. 투자나 새로운 수입원에 대해 고려해보세요.";
            default:
                return "좋은 운세입니다.";
        }
    }

    private String getMediumScoreMessage(Zodiac zodiac, String category) {
        switch (category) {
            case "love":
                return "평범한 애정운입니다. 현재 관계를 소중히 여기며 안정감을 찾으세요.";
            case "career":
                return "무난한 직업운입니다. 꾸준한 노력이 좋은 결과를 가져다줄 것입니다.";
            case "health":
                return "건강에 특별한 문제는 없으나 관리에 신경 써주세요.";
            case "money":
                return "재물운이 보통입니다. 계획적인 소비와 저축을 실천하세요.";
            default:
                return "보통의 운세입니다.";
        }
    }

    private String getLowScoreMessage(Zodiac zodiac, String category) {
        switch (category) {
            case "love":
                return "애정운에 주의가 필요합니다. 소통과 이해심을 중시하세요.";
            case "career":
                return "직업적으로 어려운 시기일 수 있습니다. 신중한 판단이 필요합니다.";
            case "health":
                return "건강관리에 더욱 신경 써야 할 때입니다. 무리하지 마세요.";
            case "money":
                return "재물운이 다소 약합니다. 불필요한 지출을 줄이고 절약하세요.";
            default:
                return "주의가 필요한 시기입니다.";
        }
    }

    /**
     * 분야별 점수 생성
     */
    private int generateScore(Zodiac zodiac, String category, Random random) {
        int baseScore = 50 + random.nextInt(30); // 50-79

        int bonus = 0;
        switch (zodiac) {
            case ARIES:
                bonus = category.equals("career") ? 10 : 0;
                break;
            case TAURUS:
                bonus = category.equals("money") ? 10 : 0;
                break;
            case GEMINI:
                bonus = category.equals("love") ? 10 : 0;
                break;
            case CANCER:
                bonus = category.equals("health") ? 10 : 0;
                break;
            case LEO:
                bonus = category.equals("career") ? 15 : 0;
                break;
            case VIRGO:
                bonus = category.equals("health") ? 15 : 0;
                break;
            case LIBRA:
                bonus = category.equals("love") ? 15 : 0;
                break;
            case SCORPIO:
                bonus = category.equals("money") ? 15 : 0;
                break;
            case SAGITTARIUS:
                bonus = category.equals("career") ? 20 : 0;
                break;
            case CAPRICORN:
                bonus = category.equals("money") ? 20 : 0;
                break;
            case AQUARIUS:
                bonus = category.equals("love") ? 20 : 0;
                break;
            case PISCES:
                bonus = category.equals("health") ? 20 : 0;
                break;
            default:
                bonus = 0;
        }

        return Math.max(0, Math.min(100, baseScore + bonus));
    }

    /**
     * 월별 점수 생성
     */
    private int generateMonthlyScore(Zodiac zodiac, int month) {
        Random random = new Random(zodiac.ordinal() * month);
        int baseScore = 50 + random.nextInt(30);

        if (isLuckyMonth(zodiac, month)) {
            baseScore += 20;
        }

        return Math.min(100, baseScore);
    }

    /**
     * 별자리별 행운의 달 확인
     */
    private boolean isLuckyMonth(Zodiac zodiac, int month) {
        switch (zodiac) {
            case ARIES: return Arrays.asList(3, 4, 7, 11).contains(month);
            case TAURUS: return Arrays.asList(4, 5, 8, 12).contains(month);
            case GEMINI: return Arrays.asList(5, 6, 9, 1).contains(month);
            case CANCER: return Arrays.asList(6, 7, 10, 2).contains(month);
            case LEO: return Arrays.asList(7, 8, 11, 3).contains(month);
            case VIRGO: return Arrays.asList(8, 9, 12, 4).contains(month);
            case LIBRA: return Arrays.asList(9, 10, 1, 5).contains(month);
            case SCORPIO: return Arrays.asList(10, 11, 2, 6).contains(month);
            case SAGITTARIUS: return Arrays.asList(11, 12, 3, 7).contains(month);
            case CAPRICORN: return Arrays.asList(12, 1, 4, 8).contains(month);
            case AQUARIUS: return Arrays.asList(1, 2, 5, 9).contains(month);
            case PISCES: return Arrays.asList(2, 3, 6, 10).contains(month);
            default: return false;
        }
    }


    /**
     * 행운의 숫자 생성
     * 이 메서드는 사용자의 생년월일을 기반으로 행운의 숫자를 생성합니다.
     * 행운의 숫자는 1부터 45 사이의 숫자 중에서 무작위로 3개를 선택합니다.
     * @param birthDate 사용자의 생년월일
     * @return List<Integer> 행운의 숫자 리스트
     */
    private List<Integer> generateLuckyNumbers(LocalDate birthDate) {
        if (birthDate == null) {
            return Arrays.asList(7, 14, 21);
        }

        Random random = new Random(birthDate.toEpochDay());
        Set<Integer> numbers = new HashSet<>();

        int attempts = 0;
        while (numbers.size() < 3 && attempts < 100) {
            numbers.add(random.nextInt(45) + 1);
            attempts++;
        }

        List<Integer> result = new ArrayList<>(numbers);
        while (result.size() < 3) {
            result.add(random.nextInt(45) + 1);
        }

        return result;
    }


    /**
     * 별자리 날짜 초기화
     * 이 메서드는 각 별자리에 해당하는 날짜 범위를 초기화합니다.
     * 각 별자리는 시작일과 종료일로 정의되며,
     * 연말-연초를 걸치는 경우도 처리합니다.
     * * 예: 천칭자리(9월 23일 ~ 10월 22일), 전갈자리(10월 23일 ~ 11월 21일) 등
     * * @see Zodiac
     * * @see MonthDay
     */
    private static void initializeZodiacDates() {
        ZODIAC_DATES.put(Zodiac.ARIES, Arrays.asList(MonthDay.of(3, 21), MonthDay.of(4, 19)));
        ZODIAC_DATES.put(Zodiac.TAURUS, Arrays.asList(MonthDay.of(4, 20), MonthDay.of(5, 20)));
        ZODIAC_DATES.put(Zodiac.GEMINI, Arrays.asList(MonthDay.of(5, 21), MonthDay.of(6, 21)));
        ZODIAC_DATES.put(Zodiac.CANCER, Arrays.asList(MonthDay.of(6, 22), MonthDay.of(7, 22)));
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
     * 별자리별 운세 문구 초기화
     * 이 메서드는 각 별자리에 대한 운세 문구를 초기화합니다.
     * 각 별자리는 "overall" 키를 사용하여 종합 운세 문구를 저장합니다.
     * * 예: 양자리의 운세 문구는 "양자리님의 오늘은 활력이 넘치는 하루가 될 것입니다." 등
     * * @see Zodiac
     * * @see Map
     */
    private static void initializeZodiacFortunes() {
        Map<String, String> ariesFortune = new HashMap<>();
        ariesFortune.put("overall", "양자리님의 오늘은 활력이 넘치는 하루가 될 것입니다. 새로운 도전을 두려워하지 마세요.");
        ZODIAC_FORTUNES.put(Zodiac.ARIES, ariesFortune);

        Map<String, String> taurusFortune = new HashMap<>();
        taurusFortune.put("overall", "황소자리님의 오늘은 안정감 있는 하루입니다. 꾸준함이 좋은 결과를 가져다줄 것입니다.");
        ZODIAC_FORTUNES.put(Zodiac.TAURUS, taurusFortune);

        Map<String, String> geminiFortune = new HashMap<>();
        geminiFortune.put("overall", "쌍둥이자리님의 오늘은 소통과 만남이 활발한 하루입니다. 새로운 인연을 만날 수 있어요.");
        ZODIAC_FORTUNES.put(Zodiac.GEMINI, geminiFortune);

        Map<String, String> cancerFortune = new HashMap<>();
        cancerFortune.put("overall", "게자리님의 오늘은 가족과 가까운 사람들과의 시간이 소중한 하루입니다.");
        ZODIAC_FORTUNES.put(Zodiac.CANCER, cancerFortune);

        Map<String, String> leoFortune = new HashMap<>();
        leoFortune.put("overall", "사자자리님의 오늘은 자신감과 리더십을 발휘할 수 있는 하루입니다.");
        ZODIAC_FORTUNES.put(Zodiac.LEO, leoFortune);

        Map<String, String> virgoFortune = new HashMap<>();
        virgoFortune.put("overall", "처녀자리님의 오늘은 세심함과 완벽함이 빛나는 하루입니다.");
        ZODIAC_FORTUNES.put(Zodiac.VIRGO, virgoFortune);

        Map<String, String> libraFortune = new HashMap<>();
        libraFortune.put("overall", "천칭자리님의 오늘은 조화와 균형이 중요한 하루입니다.");
        ZODIAC_FORTUNES.put(Zodiac.LIBRA, libraFortune);

        Map<String, String> scorpioFortune = new HashMap<>();
        scorpioFortune.put("overall", "전갈자리님의 오늘은 깊이 있는 통찰력이 발휘되는 하루입니다.");
        ZODIAC_FORTUNES.put(Zodiac.SCORPIO, scorpioFortune);

        Map<String, String> sagittariusFortune = new HashMap<>();
        sagittariusFortune.put("overall", "사수자리님의 오늘은 모험과 탐험의 기운이 강한 하루입니다.");
        ZODIAC_FORTUNES.put(Zodiac.SAGITTARIUS, sagittariusFortune);

        Map<String, String> capricornFortune = new HashMap<>();
        capricornFortune.put("overall", "염소자리님의 오늘은 목표 달성을 위한 노력이 결실을 맺는 하루입니다.");
        ZODIAC_FORTUNES.put(Zodiac.CAPRICORN, capricornFortune);

        Map<String, String> aquariusFortune = new HashMap<>();
        aquariusFortune.put("overall", "물병자리님의 오늘은 독창적이고 혁신적인 아이디어가 돋보이는 하루입니다.");
        ZODIAC_FORTUNES.put(Zodiac.AQUARIUS, aquariusFortune);

        Map<String, String> piscesFortune = new HashMap<>();
        piscesFortune.put("overall", "물고기자리님의 오늘은 감성이 풍부하고 직감이 뛰어난 하루가 될 것입니다.");
        ZODIAC_FORTUNES.put(Zodiac.PISCES, piscesFortune);
    }

    /**
     * 별자리 궁합 초기화
     * 이 메서드는 각 별자리의 궁합을 초기화합니다.
     * 각 별자리는 호환되는 다른 별자리 목록을 가지고 있으며,
     * 이 목록은 별자리 간의 궁합을 나타냅니다.
     * * 예: 양자리는 사자자리, 궁수자리, 쌍둥이자리와 호환됩니다.
     * * @see Zodiac
     * * @see List
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

    private static void initializeZodiacPersonalities() {
        ZODIAC_PERSONALITIES.put(Zodiac.ARIES, "활동적이고 도전적인 성격으로 리더십이 강합니다.");
        ZODIAC_PERSONALITIES.put(Zodiac.TAURUS, "안정을 추구하며 끈기가 있는 현실적인 성격입니다.");
        ZODIAC_PERSONALITIES.put(Zodiac.GEMINI, "호기심이 많고 변화를 좋아하는 다재다능한 성격입니다.");
        ZODIAC_PERSONALITIES.put(Zodiac.CANCER, "감정이 풍부하고 가족을 중시하는 따뜻한 성격입니다.");
        ZODIAC_PERSONALITIES.put(Zodiac.LEO, "자신감이 넘치고 창조적인 표현을 좋아하는 성격입니다.");
        ZODIAC_PERSONALITIES.put(Zodiac.VIRGO, "완벽주의적이고 세심한 분석력을 가진 성격입니다.");
        ZODIAC_PERSONALITIES.put(Zodiac.LIBRA, "조화와 균형을 추구하며 사교적인 성격입니다.");
        ZODIAC_PERSONALITIES.put(Zodiac.SCORPIO, "깊이 있고 집중력이 강한 신비로운 성격입니다.");
        ZODIAC_PERSONALITIES.put(Zodiac.SAGITTARIUS, "자유로우며 모험을 좋아하는 낙천적인 성격입니다.");
        ZODIAC_PERSONALITIES.put(Zodiac.CAPRICORN, "책임감이 강하고 목표 지향적인 현실적 성격입니다.");
        ZODIAC_PERSONALITIES.put(Zodiac.AQUARIUS, "독창적이고 미래지향적인 인도주의적 성격입니다.");
        ZODIAC_PERSONALITIES.put(Zodiac.PISCES, "직관력이 뛰어나고 예술적 감각이 있는 감성적 성격입니다.");
    }





    /**
     * 별자리 행운의 색깔 초기화
     */
    private static void initializeZodiacLuckyColors() {
        ZODIAC_LUCKY_COLORS.put(Zodiac.ARIES, "빨간색");
        ZODIAC_LUCKY_COLORS.put(Zodiac.TAURUS, "초록색");
        ZODIAC_LUCKY_COLORS.put(Zodiac.GEMINI, "노란색");
        ZODIAC_LUCKY_COLORS.put(Zodiac.CANCER, "은색");
        ZODIAC_LUCKY_COLORS.put(Zodiac.LEO, "금색");
        ZODIAC_LUCKY_COLORS.put(Zodiac.VIRGO, "네이비");
        ZODIAC_LUCKY_COLORS.put(Zodiac.LIBRA, "분홍색");
        ZODIAC_LUCKY_COLORS.put(Zodiac.SCORPIO, "검은색");
        ZODIAC_LUCKY_COLORS.put(Zodiac.SAGITTARIUS, "보라색");
        ZODIAC_LUCKY_COLORS.put(Zodiac.CAPRICORN, "갈색");
        ZODIAC_LUCKY_COLORS.put(Zodiac.AQUARIUS, "하늘색");
        ZODIAC_LUCKY_COLORS.put(Zodiac.PISCES, "바다색");
    }

    /**
     * 별자리 행운의 보석 초기화
     */
    private static void initializeZodiacLuckyStones() {
        ZODIAC_LUCKY_STONES.put(Zodiac.ARIES, "다이아몬드");
        ZODIAC_LUCKY_STONES.put(Zodiac.TAURUS, "에메랄드");
        ZODIAC_LUCKY_STONES.put(Zodiac.GEMINI, "진주");
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

    // 유틸리티 메서드들
    private String generateMonthlyTheme(Zodiac zodiac, int month) {
        String[] themes = {
                "새로운 시작", "관계 발전", "창조적 활동", "안정 추구",
                "모험과 도전", "자기계발", "휴식과 치유", "협력과 팀워크",
                "성취와 결실", "변화와 적응", "소통과 화합", "준비와 계획"
        };
        return themes[(zodiac.ordinal() + month) % themes.length];
    }

    private String generateMonthlyAdvice(int score) {
        if (score >= 80) {
            return "적극적으로 행동하며 기회를 잡으세요.";
        } else if (score >= 60) {
            return "꾸준한 노력으로 좋은 결과를 만들어가세요.";
        } else {
            return "신중하게 행동하며 안정을 우선시하세요.";
        }
    }

    private String generateKeyEvent(Zodiac zodiac, int month) {
        String[] events = {
                "중요한 만남", "새로운 기회", "성공적인 프로젝트", "건강 개선",
                "재물 증가", "인간관계 발전", "학습 성과", "여행 계획"
        };
        return events[(zodiac.ordinal() + month) % events.length];
    }

    private String generateYearlyTrend(Zodiac zodiac) {
        return zodiac.getKoreanName() + "님의 올해는 성장과 발전의 해입니다. " +
                "꾸준한 노력으로 목표를 달성할 수 있을 것입니다.";
    }
}
