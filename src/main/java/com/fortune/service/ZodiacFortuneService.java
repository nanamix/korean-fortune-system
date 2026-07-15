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
        /* null 체크 */
        if (birthDate == null) {
            throw new IllegalArgumentException("생년월일이 null입니다.");
        }
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
        String overall = generateDailyOverview(zodiac, loveScore, careerScore, healthScore, moneyScore);
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
        return switch (category) {
            case "love" -> loveMessage(score);
            case "career" -> careerMessage(score);
            case "health" -> healthMessage(score);
            case "money" -> moneyMessage(score);
            default -> "오늘의 흐름을 차분히 살피고 중요한 일은 한 번 더 점검하세요.";
        };
    }

    private String generateDailyOverview(Zodiac zodiac, int love, int career, int health, int money) {
        Map<String, Integer> scores = new LinkedHashMap<>();
        scores.put("관계", love);
        scores.put("일과 성취", career);
        scores.put("건강", health);
        scores.put("재정", money);
        Map.Entry<String, Integer> strongest = Collections.max(scores.entrySet(), Map.Entry.comparingByValue());
        Map.Entry<String, Integer> weakest = Collections.min(scores.entrySet(), Map.Entry.comparingByValue());
        int average = (love + career + health + money) / 4;
        return zodiac.getKoreanName() + "의 오늘 종합 흐름은 " + scoreLabel(average) + "입니다. "
                + strongest.getKey() + " 영역(" + strongest.getValue() + "점)은 적극적으로 활용하고, "
                + weakest.getKey() + " 영역(" + weakest.getValue() + "점)은 속도를 조절하며 확인하세요.";
    }

    private String scoreLabel(int score) {
        if (score >= 80) return "매우 활발한 편";
        if (score >= 65) return "안정적으로 상승하는 편";
        if (score >= 50) return "균형을 유지하는 편";
        if (score >= 35) return "신중한 조율이 필요한 편";
        return "회복과 정비가 우선인 편";
    }

    private String loveMessage(int score) {
        if (score >= 80) return "마음을 표현했을 때 긍정적인 반응을 얻기 좋은 날입니다. 새로운 만남은 먼저 가볍게 대화를 열고, 가까운 관계에서는 고마움을 구체적으로 전해 보세요.";
        if (score >= 65) return "관계의 온도가 안정적으로 올라갑니다. 상대의 말을 끝까지 듣고 작은 약속을 지키는 태도가 신뢰를 더 단단하게 만듭니다.";
        if (score >= 50) return "큰 변화보다는 편안한 소통에 어울리는 날입니다. 결론을 서두르지 말고 서로의 기대를 확인하면 불필요한 오해를 줄일 수 있습니다.";
        if (score >= 35) return "말의 의도와 전달 방식이 다르게 받아들여질 수 있습니다. 감정적인 답변은 잠시 미루고 사실과 느낌을 나누어 표현하세요.";
        return "관계에서 거리와 휴식이 필요한 흐름입니다. 상대를 단정하기보다 자신의 감정을 먼저 정리하고 중요한 대화는 컨디션이 나아진 뒤 진행하세요.";
    }

    private String careerMessage(int score) {
        if (score >= 80) return "주도권을 잡고 성과를 보여주기 좋은 날입니다. 핵심 과제를 먼저 마무리하고 결과와 근거를 함께 공유하면 평가와 협업 모두에 유리합니다.";
        if (score >= 65) return "계획한 업무가 안정적으로 진전됩니다. 오전에는 집중 과제를, 오후에는 협의와 피드백을 배치하면 흐름을 효율적으로 사용할 수 있습니다.";
        if (score >= 50) return "새 일을 벌이기보다 진행 중인 업무의 완성도를 높이기 좋습니다. 우선순위와 마감 조건을 다시 확인해 작은 누락을 막으세요.";
        if (score >= 35) return "일정 충돌이나 의사소통 지연이 생길 수 있습니다. 구두 합의는 문서로 남기고 의존 작업의 담당자와 완료 시점을 명확히 하세요.";
        return "무리한 확장보다 리스크를 줄이는 데 집중할 날입니다. 중요한 배포·계약·결재는 체크리스트와 동료 검토를 거쳐 진행하세요.";
    }

    private String healthMessage(int score) {
        if (score >= 80) return "활력과 회복력이 좋은 편입니다. 적당한 유산소 운동이나 야외 활동으로 기운을 순환하되 과도한 운동으로 피로를 남기지는 마세요.";
        if (score >= 65) return "전반적인 컨디션은 안정적입니다. 수분 섭취와 규칙적인 식사, 짧은 스트레칭을 유지하면 집중력도 오래 이어집니다.";
        if (score >= 50) return "컨디션의 기복을 관리하는 것이 중요합니다. 긴 작업 사이에 눈과 어깨를 쉬고 늦은 카페인과 야식을 줄여 수면의 질을 지키세요.";
        if (score >= 35) return "피로가 평소보다 쉽게 누적될 수 있습니다. 강도 높은 운동보다 가벼운 걷기와 충분한 수면을 우선하고 몸의 신호를 무시하지 마세요.";
        return "회복을 최우선으로 둘 날입니다. 무리한 일정은 줄이고 불편한 증상이 지속되면 운세 해석이 아닌 의료 전문가의 진료를 받으세요.";
    }

    private String moneyMessage(int score) {
        if (score >= 80) return "수입·협상·절약 기회를 발견하기 좋은 날입니다. 다만 기대감만으로 결정하지 말고 비용, 수익, 위험 조건을 숫자로 비교한 뒤 실행하세요.";
        if (score >= 65) return "재정 흐름이 비교적 안정적입니다. 정기 지출을 점검하고 남는 자금을 목적별로 나누면 안정성과 성취감을 함께 높일 수 있습니다.";
        if (score >= 50) return "수익 확대보다 지출 균형에 집중하기 좋습니다. 즉흥 구매는 하루 보류하고 구독·수수료처럼 반복되는 비용을 점검하세요.";
        if (score >= 35) return "예상하지 못한 지출이나 조건 변경에 유의하세요. 대출·투자·고액 구매는 총비용과 해지 조건을 확인하고 여유 자금을 보존하세요.";
        return "재정 방어가 우선인 날입니다. 충동적인 투자와 보증은 피하고 중요한 금융 판단은 객관적인 자료와 자격 있는 전문가의 조언을 함께 확인하세요.";
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
            return month + "월은 " + zodiacName + "에게 확장과 성취의 흐름이 강한 달입니다. "
                    + "새로운 제안은 목표와 자원 조건을 확인한 뒤 적극적으로 검토하고, 성과는 기록과 공유를 통해 다음 기회로 연결하세요.";
        } else if (score >= 50) {
            return month + "월은 " + zodiacName + "에게 기반을 다지고 완성도를 높이기 좋은 달입니다. "
                    + "진행 중인 일의 우선순위를 정리하고 관계·건강·재정의 균형을 유지하면 꾸준한 결과를 만들 수 있습니다.";
        } else {
            return month + "월은 " + zodiacName + "에게 속도보다 점검과 회복이 중요한 달입니다. "
                    + "새로운 부담을 늘리기보다 일정과 지출을 보수적으로 관리하고, 중요한 결정에는 충분한 검토 시간을 확보하세요.";
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
            return "급한 결정과 과도한 일정 확장을 피하세요. 합의 조건·예산·마감일을 문서로 다시 확인하고 회복 시간을 먼저 확보하는 편이 좋습니다.";
        } else if (score < 70) {
            return "큰 변화를 한꺼번에 추진하기보다 현재 상황을 안정화하세요. 익숙함 때문에 놓친 반복 비용이나 관계의 작은 불편도 점검해 보세요.";
        } else {
            return "흐름이 좋더라도 과신은 피하세요. 약속한 범위와 자원 한계를 지키고 성과를 주변과 나누는 태도가 좋은 흐름을 오래 유지합니다.";
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
            return "새로운 프로젝트 제안, 역할 확장, 협력 관계를 구체화하기 좋습니다. 관심 있는 기회에는 먼저 질문하고 작은 실행으로 가능성을 확인하세요.";
        } else if (score >= 50) {
            return "기존 관계와 업무의 개선 지점에서 기회가 생깁니다. 반복 작업을 정리하거나 미뤄 둔 대화를 마치는 작은 변화가 다음 단계의 기반이 됩니다.";
        } else {
            return "위험이 낮고 되돌릴 수 있는 작은 변화부터 시작하세요. 생활 리듬과 고정비, 업무 우선순위를 정비하는 과정 자체가 다음 기회를 준비합니다.";
        }
    }
}
