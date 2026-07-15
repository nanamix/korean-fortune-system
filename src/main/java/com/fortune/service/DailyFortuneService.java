package com.fortune.service;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.fortune.dto.SajuResult;
import com.fortune.dto.DailyFortuneResult;
import com.fortune.dto.FortuneByCategory;
import com.fortune.dto.SinsalInfo;
import lombok.extern.slf4j.Slf4j;
import java.time.LocalDate;
import java.util.*;
/**
 * 일일 운세 계산 서비스
 *
 * <p>사주를 기반으로 특정 날짜의 일일 운세를 계산하는 서비스입니다.</p>
 *
 * @author 하진영
 * @version 2.5.0
 * @since 2025-06-24
 */
@Slf4j
@Service
public class DailyFortuneService {
    /**
     * 일주 계산 서비스
     * - Autowired 어노테이션을 사용하여 일주 계산 서비스를 주입합니다.
     * - GanjiCalculatorService 클래스를 사용하여 일주 계산을 합니다.
     */
    @Autowired
    private GanjiCalculatorService ganjiCalculatorService;
    /**
     * 신살 계산 서비스
     * - Autowired 어노테이션을 사용하여 신살 계산 서비스를 주입합니다.
     * - SinsalService 클래스를 사용하여 신살 계산을 합니다.
     */
    @Autowired
    private SinsalService sinsalService;
    /**
     * 오행 색상 매핑
     * - 오행 색상 매핑을 정의합니다.
     */
    private static final Map<String, List<String>> WUXING_COLORS = new HashMap<>();
    /**
     * 길방위 매핑 (일간별)
     * - 길방위 매핑을 정의합니다.
     */
    private static final Map<String, String> LUCKY_DIRECTIONS = new HashMap<>();
    /**
     * 일간별 기본 운세 점수
     * - 일간별 기본 운세 점수를 정의합니다.
     */
    private static final Map<String, Integer> BASE_FORTUNE_SCORES = new HashMap<>();
    /**
     * 십신별 분야 가중 [연애, 직장, 건강, 재물].
     * 재성→재물·연애, 관성→직장, 인성→건강/문서, 식상→활동/건강, 비겁→탈재.
     * 통용되는 십신-십성 성정을 근거로 한 결정론적 가중치(대표값).
     */
    private static final Map<String, int[]> SIPSIN_WEIGHTS = Map.ofEntries(
            Map.entry("비견", new int[]{ 0, -3,  5, -5}),
            Map.entry("겁재", new int[]{-3, -3,  3, -8}),
            Map.entry("식신", new int[]{ 5,  2,  8,  5}),
            Map.entry("상관", new int[]{ 6, -5,  3,  4}),
            Map.entry("편재", new int[]{ 8,  5, -3, 12}),
            Map.entry("정재", new int[]{ 6,  5, -3, 14}),
            Map.entry("편관", new int[]{ 3, 10, -8, -3}),
            Map.entry("정관", new int[]{ 6, 14, -5,  2}),
            Map.entry("편인", new int[]{-3,  4,  8, -4}),
            Map.entry("정인", new int[]{-2,  6, 12, -3})
    );
    private static final int[] NEUTRAL_WEIGHT = {0, 0, 0, 0};
    /**
     * 초기화 메서드
     * - 오행 색상 매핑, 길방위 매핑, 일간별 기본 운세 점수를 초기화합니다.
     */
    static {
        initializeWuxingColors();
        initializeLuckyDirections();
        initializeBaseFortuneScores();
    }
    /**
     * 일일 운세 계산 메인 메서드
     * 
     * @param saju 사주 결과 객체
     * @param targetDate 대상 날짜
     * @return 일일 운세 결과
     */
    public DailyFortuneResult calculateDailyFortune(SajuResult saju, LocalDate targetDate) {
        log.info("🔮 일일 운세 계산 시작: {} - {}", saju.getDayMaster(), targetDate);
        try {
            /* 1. 대상 날짜의 일주 계산 */
            String dayPillar = ganjiCalculatorService.calculateDayPillar(targetDate);
            /* 2. 길신/흉신 계산 */
            List<SinsalInfo> sinsals = sinsalService.calculateDailySinsals(targetDate, saju);
            /* 3. 종합 점수 계산 */
            int totalScore = calculateTotalScore(saju, dayPillar, sinsals);
            /* 4. 분야별 운세 계산 */
            FortuneByCategory categoryFortune = calculateCategoryFortune(saju, dayPillar, totalScore);
            /* 5. 조언 생성 */
            String advice = generateAdvice(saju, totalScore, sinsals);
            /* 6. 길방위 설정 */
            String luckyDirection = LUCKY_DIRECTIONS.getOrDefault(saju.getDayMaster(), "동쪽");
            /* 7. 길한 색깔 설정 */
            List<String> luckyColors = WUXING_COLORS.getOrDefault(saju.getDayMaster(),
                    Arrays.asList("흰색", "검은색"));
            /* 8. 주의사항 생성 */
            String caution = generateCaution(sinsals, totalScore);
            /* 9. 결과 생성 */
            DailyFortuneResult result = DailyFortuneResult.builder()
                    .date(targetDate)
                    .dayPillar(dayPillar)
                    .totalScore(totalScore)
                    .categoryFortune(categoryFortune)
                    .sinsals(sinsals)
                    .advice(advice)
                    .luckyDirection(luckyDirection)
                    .luckyColors(luckyColors)
                    .caution(caution)
                    .build();
            log.info("✅ 일일 운세 계산 완료: 종합점수 {}", totalScore);
            return result;
        } catch (Exception e) {
            log.error("❌ 일일 운세 계산 중 오류 발생: {}", e.getMessage(), e);
            throw new RuntimeException("일일 운세 계산 중 오류가 발생했습니다: " + e.getMessage(), e);
        }
    }
    /**
     * 종합 점수 계산
     * 
     * @param saju 사주 결과 객체
     * @param dayPillar 일주
     * @param sinsals 신살 정보 리스트
     * @return 종합 점수
     */
    private int calculateTotalScore(SajuResult saju, String dayPillar, List<SinsalInfo> sinsals) {
        /* 1. 기본 점수 (일간 기반) */
        int baseScore = BASE_FORTUNE_SCORES.getOrDefault(saju.getDayMaster(), 60);
        /* 2. 일주 상성 점수 */
        int pillarScore = calculatePillarCompatibility(saju.getDayPillar(), dayPillar);
        /* 3. 신살 점수 */
        int sinsalScore = calculateSinsalScore(sinsals);
        /* 4. 오행 균형 점수 */
        int balanceScore = saju.getWuxingAnalysis() != null ?
                saju.getWuxingAnalysis().getBalance() / 10 : 5;
        /* 5. 종합 계산 */
        int totalScore = baseScore + pillarScore + sinsalScore + balanceScore;
        /* 6. 0-100 범위로 조정 */
        return Math.max(0, Math.min(100, totalScore));
    }
    /**
     * 일주 상성 점수 계산
     * 
     * @param birthDayPillar 출생 일주
     * @param targetDayPillar 대상 일주
     * @return 일주 상성 점수
     */
    private int calculatePillarCompatibility(String birthDayPillar, String targetDayPillar) {
        /* 같은 일주는 매우 좋음 */
        if (birthDayPillar.equals(targetDayPillar)) {
            return 20;
        }
        /* 천간 상성 체크 */
        String birthStem = birthDayPillar.substring(0, 1);
        /* 대상 일주 천간 */
        String targetStem = targetDayPillar.substring(0, 1);
        /* 출생 일주 지지 */
        String birthBranch = birthDayPillar.substring(1, 2);
        /* 대상 일주 지지 */
        String targetBranch = targetDayPillar.substring(1, 2);
        /* 일주 상성 점수 */
        int compatibility = 0;
        // 천간 상성 체크
        if (isCompatibleStems(birthStem, targetStem)) {
            compatibility += 10;
        }
        /* 지지 상성 체크 */
        if (isCompatibleBranches(birthBranch, targetBranch)) {
            compatibility += 10;
        }
        return compatibility;
    }
    /**
     * 천간 상성 체크
     * 
     * @param stem1 천간1
     * @param stem2 천간2
     * @return 천간 상성 여부
     */
    private boolean isCompatibleStems(String stem1, String stem2) {
        // 간단한 상성 로직 (실제로는 더 복잡)
        // 천간 상성 매핑
        Map<String, List<String>> compatibleStems = Map.of(
                "갑", Arrays.asList("기", "을"),
                "을", Arrays.asList("경", "갑"),
                "병", Arrays.asList("신", "정"),
                "정", Arrays.asList("임", "병"),
                "무", Arrays.asList("계", "기"),
                "기", Arrays.asList("갑", "무"),
                "경", Arrays.asList("을", "신"),
                "신", Arrays.asList("병", "경"),
                "임", Arrays.asList("정", "계"),
                "계", Arrays.asList("무", "임")
        );
        return compatibleStems.getOrDefault(stem1, Collections.emptyList()).contains(stem2);
    }
    /**
     * 지지 상성 체크
     * 
     * @param branch1 지지1
     * @param branch2 지지2
     * @return 지지 상성 여부
     */
    private boolean isCompatibleBranches(String branch1, String branch2) {
        /* 삼합, 육합 등의 상성 체크 */
        /* 지지 상성 매핑 */
        Map<String, List<String>> compatibleBranches = Map.ofEntries(
                Map.entry("자", Arrays.asList("축", "진", "신")),
                Map.entry("축", Arrays.asList("자", "사", "유")),
                Map.entry("인", Arrays.asList("해", "오", "술")),
                Map.entry("묘", Arrays.asList("술", "미", "해")),
                Map.entry("진", Arrays.asList("유", "자", "신")),
                Map.entry("사", Arrays.asList("신", "축", "유")),
                Map.entry("오", Arrays.asList("미", "인", "술")),
                Map.entry("미", Arrays.asList("오", "묘", "해")),
                Map.entry("신", Arrays.asList("사", "자", "진")),
                Map.entry("유", Arrays.asList("진", "축", "사")),
                Map.entry("술", Arrays.asList("묘", "인", "오")),
                Map.entry("해", Arrays.asList("인", "미", "묘"))
        );
        return compatibleBranches.getOrDefault(branch1, Collections.emptyList()).contains(branch2);
    }
    /**
     * 신살 점수 계산
     * 
     * @param sinsals 신살 정보 리스트
     * @return 신살 점수
     */
    private int calculateSinsalScore(List<SinsalInfo> sinsals) {
        /* 신살 점수 */
        int score = 0;
        /* 신살 점수 계산 */
        for (SinsalInfo sinsal : sinsals) {
            if (sinsal.isLucky()) {
                score += sinsal.getInfluence();
            } else {
                score -= sinsal.getInfluence();
            }
        }
        return score;
    }
    /**
     * 분야별 운세 계산
     * 
     * @param saju 사주 결과 객체
     * @param dayPillar 일주
     * @param totalScore 종합 점수
     * @return 분야별 운세 결과
     */
    private FortuneByCategory calculateCategoryFortune(SajuResult saju, String dayPillar, int totalScore) {
        /* 오늘 일진 간지와 사주 일간의 십신 관계로 분야별 점수를 결정론적으로 산출.
         * 천간 십신은 전체 가중, 지지 본기 십신은 절반 가중. 십신별 분야 가중은 SIPSIN_WEIGHTS 참고. */
        String dayMaster = saju.getDayMaster();
        String todayStem = dayPillar.substring(0, 1);
        String todayBranch = dayPillar.substring(1, 2);
        int[] stemW = weightOf(ganjiCalculatorService.sipsinOf(dayMaster, todayStem));
        int[] branchW = weightOf(ganjiCalculatorService.branchMainSipsin(dayMaster, todayBranch));
        /* [연애, 직장, 건강, 재물] */
        int loveScore   = Math.max(0, Math.min(100, totalScore + stemW[0] + branchW[0] / 2));
        int careerScore = Math.max(0, Math.min(100, totalScore + stemW[1] + branchW[1] / 2));
        int healthScore = Math.max(0, Math.min(100, totalScore + stemW[2] + branchW[2] / 2));
        int wealthScore = Math.max(0, Math.min(100, totalScore + stemW[3] + branchW[3] / 2));
        return buildCategory(loveScore, careerScore, healthScore, wealthScore);
    }
    /** 십신 이름 → 분야 가중치 (null·미정의는 중립). 불변맵 null 키 조회 회피. */
    private int[] weightOf(String sipsin) {
        int[] w = sipsin == null ? null : SIPSIN_WEIGHTS.get(sipsin);
        return w != null ? w : NEUTRAL_WEIGHT;
    }
    private FortuneByCategory buildCategory(int loveScore, int careerScore, int healthScore, int wealthScore) {
        return FortuneByCategory.builder()
                .loveScore(loveScore)
                .loveMessage(generateCategoryMessage("연애", loveScore))
                .careerScore(careerScore)
                .careerMessage(generateCategoryMessage("직장", careerScore))
                .healthScore(healthScore)
                .healthMessage(generateCategoryMessage("건강", healthScore))
                .wealthScore(wealthScore)
                .wealthMessage(generateCategoryMessage("재물", wealthScore))
                .build();
    }
    /**
     * 분야별 메시지 생성
     * 
     * @param category 분야
     * @param score 점수
     * @return 분야별 메시지
     */
    private String generateCategoryMessage(String category, int score) {
        int level = score >= 80 ? 3 : score >= 60 ? 2 : score >= 40 ? 1 : 0;
        return switch (category) {
            case "연애" -> switch (level) {
                case 3 -> "마음을 표현하기 좋은 날입니다. 먼저 안부를 묻거나 약속을 제안하면 관계가 자연스럽게 진전될 수 있습니다.";
                case 2 -> "대화의 흐름이 좋은 편입니다. 상대의 말을 충분히 듣고 솔직한 칭찬을 건네 보세요.";
                case 1 -> "큰 결론보다 편안한 대화에 집중하세요. 익숙한 관계를 세심하게 돌보는 것이 좋습니다.";
                default -> "감정적인 판단이나 성급한 고백은 피하세요. 오해가 생기면 즉시 반박하기보다 시간을 두고 확인하는 편이 좋습니다.";
            };
            case "직장" -> switch (level) {
                case 3 -> "주도적으로 제안하고 어려운 업무를 정리하기 좋습니다. 중요한 보고와 협의는 핵심 근거를 갖춰 오늘 진행해 보세요.";
                case 2 -> "협업과 실무 추진이 원활한 날입니다. 우선순위를 분명히 하고 동료와 진행 상황을 공유하면 성과가 커집니다.";
                case 1 -> "새 일을 벌이기보다 진행 중인 업무를 점검하세요. 일정과 요구사항을 문서로 확인하면 실수를 줄일 수 있습니다.";
                default -> "충동적인 결정이나 강한 표현을 삼가세요. 중요한 승인과 계약은 조건을 다시 검토한 뒤 진행하는 것이 안전합니다.";
            };
            case "건강" -> switch (level) {
                case 3 -> "활력이 좋은 편입니다. 가벼운 유산소 운동과 충분한 수분 섭취로 좋은 리듬을 유지하세요.";
                case 2 -> "컨디션은 무난합니다. 오래 앉아 있다면 틈틈이 몸을 풀고 식사와 수면 시간을 일정하게 유지하세요.";
                case 1 -> "피로가 쌓이기 쉬우니 무리한 일정을 줄이세요. 목·어깨 스트레칭과 짧은 휴식이 도움이 됩니다.";
                default -> "회복을 우선해야 하는 날입니다. 과음·과식과 격한 운동을 피하고 불편한 증상이 지속되면 전문가의 진료를 받으세요.";
            };
            case "재물" -> switch (level) {
                case 3 -> "계획한 거래나 예산 조정을 검토하기 좋은 날입니다. 수익만 보지 말고 조건과 위험을 함께 확인하세요.";
                case 2 -> "안정적인 흐름입니다. 필요한 지출과 원하는 지출을 구분하면 여유 자금을 지키는 데 도움이 됩니다.";
                case 1 -> "수입 확대보다 지출 관리에 집중하세요. 자동결제와 반복 비용을 점검하면 작은 누수를 막을 수 있습니다.";
                default -> "충동구매, 보증, 단기 투자를 피하세요. 큰 금액의 결정은 하루 이상 숙고하고 반드시 근거를 재확인하세요.";
            };
            default -> category + "운을 차분히 살피고 중요한 일은 한 번 더 확인하세요.";
        };
    }
    /**
     * 조언 생성
     *  
     * @param saju 사주 결과 객체
     * @param totalScore 종합 점수
     * @param sinsals 신살 정보 리스트
     * @return 조언
     */
    private String generateAdvice(SajuResult saju, int totalScore, List<SinsalInfo> sinsals) {
        // 조언 생성
        StringBuilder advice = new StringBuilder();
        // 종합 점수에 따른 조언 추가
        if (totalScore >= 80) {
            advice.append("오늘은 매우 좋은 날입니다! ");
        } else if (totalScore >= 60) {
            advice.append("오늘은 좋은 하루가 될 것입니다. ");
        } else if (totalScore >= 40) {
            advice.append("오늘은 평범한 하루입니다. ");
        } else {
            advice.append("오늘은 조심스러운 하루입니다. ");
        }
        // 일간별 특성 추가
        advice.append(getDayMasterAdvice(saju.getDayMaster()));
        // 길신이 있으면 추가 조언
        long luckyCount = sinsals.stream().filter(SinsalInfo::isLucky).count();
        if (luckyCount > 0) {
            advice.append(" 길신의 도움을 받아 좋은 결과를 얻을 수 있습니다.");
        }
        return advice.toString();
    }
    /**
     * 일간별 조언
     * 
     * @param dayMaster 일간
     * @return 일간별 조언
     */
    private String getDayMasterAdvice(String dayMaster) {
        /* 일간별 조언 생성 */
        return switch (dayMaster) {
            case "갑" -> "정직하고 진실한 마음으로 행동하면 좋은 결과가 있을 것입니다.";
            case "을" -> "섬세함과 배려로 주변 사람들과 좋은 관계를 유지하세요.";
            case "병" -> "밝고 활기찬 에너지로 주변을 이끌어 나가세요.";
            case "정" -> "따뜻한 마음과 정성으로 일을 처리하면 성공할 수 있습니다.";
            case "무" -> "든든하고 신뢰할 수 있는 모습으로 주변의 지지를 받으세요.";
            case "기" -> "실용적이고 현실적인 접근으로 문제를 해결하세요.";
            case "경" -> "원칙과 규칙을 지키며 공정하게 행동하세요.";
            case "신" -> "예리한 판단력과 세련된 감각을 발휘하세요.";
            case "임" -> "깊은 사고와 넓은 포용력으로 상황을 이끌어 나가세요.";
            case "계" -> "순수한 마음과 지혜로운 판단을 하세요.";
            default -> "균형 잡힌 마음으로 하루를 보내세요.";
        };
    }
    /**
     * 주의사항 생성
     * 
     * @param sinsals 신살 정보 리스트
     * @param totalScore 종합 점수
     * @return 주의사항
     */
    private String generateCaution(List<SinsalInfo> sinsals, int totalScore) {
        /* 불운 신살 개수 계산 */
        long unluckyCount = sinsals.stream().filter(sinsal -> !sinsal.isLucky()).count();
        /* 주의사항 생성 */
        if (unluckyCount >= 3) {
            return "흉신이 많아 특별히 조심해야 하는 날입니다. 중요한 결정은 미루는 것이 좋겠습니다.";
        } else if (unluckyCount >= 1) {
            return "일부 흉신이 있으니 신중하게 행동하세요.";
        } else if (totalScore < 40) {
            return "운세가 다소 저조하니 무리하지 마시고 휴식을 취하세요.";
        } else {
            return "특별한 주의사항은 없습니다. 평상시처럼 행동하세요.";
        }
    }
    /**
     * 정적 초기화 메서드들
     * 
     * @author 하진영
     * @version 2.5.0
     * @since 2025-06-24
     */
    private static void initializeWuxingColors() {
        WUXING_COLORS.put("갑", Arrays.asList("녹색", "청색", "남색"));
        WUXING_COLORS.put("을", Arrays.asList("연두색", "초록색", "청록색"));
        WUXING_COLORS.put("병", Arrays.asList("빨간색", "주황색", "분홍색"));
        WUXING_COLORS.put("정", Arrays.asList("자주색", "보라색", "연분홍색"));
        WUXING_COLORS.put("무", Arrays.asList("노란색", "갈색", "황토색"));
        WUXING_COLORS.put("기", Arrays.asList("베이지색", "아이보리", "크림색"));
        WUXING_COLORS.put("경", Arrays.asList("흰색", "은색", "회색"));
        WUXING_COLORS.put("신", Arrays.asList("금색", "백금색", "진주색"));
        WUXING_COLORS.put("임", Arrays.asList("검은색", "짙은 파랑", "남색"));
        WUXING_COLORS.put("계", Arrays.asList("물색", "하늘색", "연파랑"));
    }
    /**
     * 길방위 매핑 (일간별)
     */
    private static void initializeLuckyDirections() {
        LUCKY_DIRECTIONS.put("갑", "동쪽");
        LUCKY_DIRECTIONS.put("을", "동남쪽");
        LUCKY_DIRECTIONS.put("병", "남쪽");
        LUCKY_DIRECTIONS.put("정", "남서쪽");
        LUCKY_DIRECTIONS.put("무", "중앙");
        LUCKY_DIRECTIONS.put("기", "중앙");
        LUCKY_DIRECTIONS.put("경", "서쪽");
        LUCKY_DIRECTIONS.put("신", "서북쪽");
        LUCKY_DIRECTIONS.put("임", "북쪽");
        LUCKY_DIRECTIONS.put("계", "북동쪽");
    }
    /**
     * 일간별 기본 운세 점수
     */
    private static void initializeBaseFortuneScores() {
        BASE_FORTUNE_SCORES.put("갑", 65);
        BASE_FORTUNE_SCORES.put("을", 60);
        BASE_FORTUNE_SCORES.put("병", 70);
        BASE_FORTUNE_SCORES.put("정", 65);
        BASE_FORTUNE_SCORES.put("무", 60);
        BASE_FORTUNE_SCORES.put("기", 55);
        BASE_FORTUNE_SCORES.put("경", 65);
        BASE_FORTUNE_SCORES.put("신", 70);
        BASE_FORTUNE_SCORES.put("임", 60);
        BASE_FORTUNE_SCORES.put("계", 55);
    }
}
