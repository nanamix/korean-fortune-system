package com.fortune.service;

import com.fortune.dto.TojeongGwa;
import org.springframework.stereotype.Service;
import com.fortune.dto.TojeongRequest;
import com.fortune.dto.TojeongResult;
import java.util.*;

/**
 * 토정비결 서비스
 * 토정비결 계산 메인 메서드
 * 월별 상세 운세 생성
 * 계절별 운세 생성
 * 연간 종합 조언 생성
 * 길한 달 선별
 * 주의할 달 선별
 */
@Service
public class TojeongBigyeolService {


    /* * 토정비결 괘 정보 맵
     * 키: 괘 번호 (0-63)
     * 값: TojeongGwa 객체
     */
    private static final Map<Integer, TojeongGwa> TOJEONG_GWA_MAP = new HashMap<>();

    static {
        // 초기화 메서드호출
        initializeAllGwa();
    }

    /**
     * 토정비결 계산 메인 메서드
     */
    public TojeongResult calculateTojeong(TojeongRequest request) {
        try {
            // 1. 기본 계산
            int sum = request.getBirthYear() + request.getBirthMonth() +
                    request.getBirthDay() + request.getTargetYear();

            // 2. 토정비결 공식 적용
            int step1 = (sum % 60) + 1;
            int step2 = (step1 * 20) % 100;
            int step3 = (step2 + request.getBirthMonth()) % 64;
            if (step3 == 0) step3 = 64;

            // 3. 해당 괘 조회
            TojeongGwa gwa = TOJEONG_GWA_MAP.get(step3);
            if (gwa == null) {
                throw new RuntimeException("토정비결 괘를 찾을 수 없습니다: " + step3);
            }

            // 4. 결과 생성
            return TojeongResult.builder()
                    .targetYear(request.getTargetYear())
                    .gwaNumber(gwa.getNumber())
                    .gwaName(gwa.getName())
                    .gwaSymbol(gwa.getSymbol())
                    .summary(gwa.getSummary())
                    .detailedFortune(gwa.getDetailedFortune())
                    .overallScore(gwa.getScore())
                    .advice(generateAdvice(gwa))
                    .luckyMonths(gwa.getLuckyMonths())
                    .cautionMonths(gwa.getCautionMonths())
                    .monthlyFortune(generateMonthlyFortune(gwa))
                    .build();

        } catch (Exception e) {
            throw new RuntimeException("토정비결 계산 중 오류가 발생했습니다: " + e.getMessage(), e);
        }
    }

    /**
     * 점수대별 조언 생성
     */
    private String generateAdvice(TojeongGwa gwa) {
        String advice = "올해 당신의 운세는 '" + gwa.getName() + "'괘입니다. ";

        if (gwa.getScore() >= 90) {
            advice += "매우 좋은 운세입니다. 적극적으로 행동하세요.";
        } else if (gwa.getScore() >= 80) {
            advice += "좋은 운세입니다. 기회를 놓치지 마세요.";
        } else if (gwa.getScore() >= 70) {
            advice += "평균 이상의 운세입니다. 꾸준히 노력하세요.";
        } else if (gwa.getScore() >= 60) {
            advice += "보통의 운세입니다. 신중하게 행동하세요.";
        } else if (gwa.getScore() >= 50) {
            advice += "주의가 필요한 해입니다. 조심스럽게 행동하세요.";
        } else {
            advice += "어려운 해가 될 수 있습니다. 인내심을 갖고 극복하세요.";
        }

        return advice;
    }

    /**
     * 월별 운세 생성
     */
    private Map<Integer, String> generateMonthlyFortune(TojeongGwa gwa) {
        Map<Integer, String> monthlyFortune = new HashMap<>();

        for (int month = 1; month <= 12; month++) {
            if (gwa.getLuckyMonths().contains(month)) {
                monthlyFortune.put(month, "길한 달입니다. 새로운 시도를 해보세요.");
            } else if (gwa.getCautionMonths().contains(month)) {
                monthlyFortune.put(month, "주의가 필요한 달입니다. 신중하게 행동하세요.");
            } else {
                monthlyFortune.put(month, "평범한 달입니다. 꾸준히 노력하세요.");
            }
        }

        return monthlyFortune;
    }

    /**
     * 모든 64괘 초기화 (일부만 구현)
     */
    private static void initializeAllGwa() {

        // ========== 상상괘 (90점 이상) ==========
        TOJEONG_GWA_MAP.put(1, new TojeongGwa(1, "건위천", "☰☰",
                "하늘의 기운이 충만하다",
                "천지창조의 기운으로 모든 일이 순조롭게 풀립니다. 리더십을 발휘할 때이며, 큰 성취를 이룰 수 있는 해입니다.",
                95, Arrays.asList(1, 4, 7, 10), Arrays.asList()));

        TOJEONG_GWA_MAP.put(2, new TojeongGwa(2, "곤위지", "☷☷",
                "땅의 포용력으로 기다린다",
                "인내와 포용으로 때를 기다리면 좋은 결과가 있습니다. 협력과 조화를 중시하면 성공할 수 있습니다.",
                92, Arrays.asList(2, 5, 8, 11), Arrays.asList()));

        TOJEONG_GWA_MAP.put(11, new TojeongGwa(11, "지천태", "☷☰",
                "땅과 하늘의 소통",
                "소통과 교류가 활발해지는 평화로운 해입니다. 상하가 잘 통하고 협력이 원활한 시기입니다.",
                94, Arrays.asList(1, 4, 7, 10), Arrays.asList()));

        TOJEONG_GWA_MAP.put(19, new TojeongGwa(19, "지택임", "☷☱",
                "땅 위의 못이 가득하다",
                "풍요로움과 번영의 시기입니다. 노력한 만큼 결실을 거둘 수 있는 좋은 해입니다.",
                91, Arrays.asList(2, 6, 10), Arrays.asList()));

        TOJEONG_GWA_MAP.put(35, new TojeongGwa(35, "화지진", "☲☷",
                "해가 땅 위로 떠오른다",
                "발전과 진보의 상으로 명예와 성공을 얻는 해입니다. 자신의 능력을 마음껏 발휘할 수 있습니다.",
                93, Arrays.asList(3, 7, 11), Arrays.asList()));

        // ========== 상괘 (80-89점) ==========
        TOJEONG_GWA_MAP.put(3, new TojeongGwa(3, "산수몽", "☶☵",
                "배움을 통해 지혜를 얻는다",
                "학습과 수양에 좋은 해입니다. 새로운 지식을 습득하고 실력을 기르세요. 젊은이에게는 특히 좋은 운세입니다.",
                82, Arrays.asList(2, 5, 8, 11), Arrays.asList()));

        TOJEONG_GWA_MAP.put(5, new TojeongGwa(5, "수천수", "☵☰",
                "기다림 끝에 희망이 보인다",
                "인내심을 갖고 기다리면 좋은 소식이 올 것입니다. 조급해하지 말고 때를 기다리는 지혜가 필요합니다.",
                80, Arrays.asList(1, 4, 7, 10), Arrays.asList()));

        TOJEONG_GWA_MAP.put(6, new TojeongGwa(6, "천수송", "☰☵",
                "하늘과 물의 조화",
                "소통과 화합으로 모든 일이 잘 풀리는 해입니다. 분쟁이 있다면 원만하게 해결될 것입니다.",
                85, Arrays.asList(1, 4, 7, 10), Arrays.asList()));

        TOJEONG_GWA_MAP.put(10, new TojeongGwa(10, "천택리", "☰☱",
                "하늘과 못의 예의",
                "예의와 격식을 지키며 행동하면 좋은 결과가 있습니다. 인간관계에서 예절을 중시하세요.",
                83, Arrays.asList(2, 6, 10), Arrays.asList()));

        TOJEONG_GWA_MAP.put(13, new TojeongGwa(13, "천화동인", "☰☲",
                "하늘의 불빛으로 사람이 모인다",
                "협력과 단합의 힘으로 큰 일을 성취하는 해입니다. 팀워크가 중요한 시기입니다.",
                87, Arrays.asList(1, 5, 9), Arrays.asList()));

        TOJEONG_GWA_MAP.put(14, new TojeongGwa(14, "화천대유", "☲☰",
                "하늘의 불이 크게 타오른다",
                "큰 성공과 풍요로움을 얻는 해입니다. 재물운이 좋고 사업이 번창할 것입니다.",
                89, Arrays.asList(3, 7, 11), Arrays.asList()));

        TOJEONG_GWA_MAP.put(25, new TojeongGwa(25, "천뢰무망", "☰☳",
                "하늘의 천둥이 순수하다",
                "순수한 마음으로 행동하면 하늘의 도움을 받는 해입니다. 정직함이 복을 가져다줍니다.",
                84, Arrays.asList(1, 4, 10), Arrays.asList()));

        // ========== 중상괘 (70-79점) ==========
        TOJEONG_GWA_MAP.put(4, new TojeongGwa(4, "수산건", "☵☶",
                "준비하여 때를 기다린다",
                "차근차근 준비하며 기회를 기다리는 해입니다. 성급하게 서두르지 말고 기반을 다지세요.",
                78, Arrays.asList(3, 6, 9, 12), Arrays.asList()));

        TOJEONG_GWA_MAP.put(7, new TojeongGwa(7, "지수사", "☷☵",
                "군대의 규율",
                "조직력과 리더십을 발휘할 때입니다. 질서와 규율을 중시하면 좋은 결과를 얻을 수 있습니다.",
                76, Arrays.asList(3, 6, 9, 12), Arrays.asList()));

        TOJEONG_GWA_MAP.put(8, new TojeongGwa(8, "수지비", "☵☷",
                "물과 땅의 친밀함",
                "인간관계가 좋아지고 협력이 잘 되는 해입니다. 주변 사람들과의 화합을 중시하세요.",
                75, Arrays.asList(2, 5, 8, 11), Arrays.asList()));

        TOJEONG_GWA_MAP.put(9, new TojeongGwa(9, "풍천소축", "☴☰",
                "바람이 하늘을 감싼다",
                "작은 것부터 차근차근 쌓아가는 해입니다. 큰 성과보다는 착실한 노력이 중요합니다.",
                72, Arrays.asList(1, 5, 9), Arrays.asList(3, 7, 11)));

        TOJEONG_GWA_MAP.put(15, new TojeongGwa(15, "지산겸", "☷☶",
                "땅 속의 산이 겸손하다",
                "겸손한 마음가짐으로 임하면 모든 일이 잘 풀리는 해입니다. 자만하지 말고 낮은 자세를 유지하세요.",
                79, Arrays.asList(2, 6, 10), Arrays.asList()));

        TOJEONG_GWA_MAP.put(16, new TojeongGwa(16, "뢰지예", "☳☷",
                "천둥이 땅을 흔든다",
                "기쁨과 환희가 가득한 해입니다. 즐거운 일들이 많이 생기고 축하할 일이 있을 것입니다.",
                77, Arrays.asList(1, 4, 8, 12), Arrays.asList()));

        TOJEONG_GWA_MAP.put(20, new TojeongGwa(20, "풍지관", "☴☷",
                "바람이 땅 위를 돈다",
                "관찰과 성찰의 시기입니다. 주변을 자세히 살펴보고 신중하게 판단해야 할 때입니다.",
                74, Arrays.asList(2, 5, 9), Arrays.asList()));

        TOJEONG_GWA_MAP.put(21, new TojeongGwa(21, "화뢰서합", "☲☳",
                "불과 천둥이 만난다",
                "문제를 해결하고 정의를 실현하는 해입니다. 옳은 일을 위해 용기를 내야 할 때입니다.",
                73, Arrays.asList(3, 7, 11), Arrays.asList()));

        // ========== 중괘 (60-69점) ==========
        TOJEONG_GWA_MAP.put(17, new TojeongGwa(17, "택뢰수", "☱☳",
                "못 아래 천둥이 따른다",
                "변화에 잘 적응하며 흐름을 따라가는 해입니다. 융통성을 발휘하면 좋은 결과를 얻을 수 있습니다.",
                68, Arrays.asList(1, 6, 11), Arrays.asList()));

        TOJEONG_GWA_MAP.put(18, new TojeongGwa(18, "산풍고", "☶☴",
                "산에 바람이 분다",
                "과거의 잘못을 바로잡는 해입니다. 문제점을 개선하고 새로운 출발을 준비하세요.",
                65, Arrays.asList(2, 7, 12), Arrays.asList(4, 8)));

        TOJEONG_GWA_MAP.put(22, new TojeongGwa(22, "산화비", "☶☲",
                "산에 불이 아름답다",
                "외적인 아름다움보다 내적인 충실함이 중요한 해입니다. 실속을 챙기는 것이 좋습니다.",
                66, Arrays.asList(3, 8), Arrays.asList()));

        TOJEONG_GWA_MAP.put(23, new TojeongGwa(23, "산지박", "☶☷",
                "산이 땅을 누른다",
                "어려운 시기이지만 인내하면 반드시 좋은 때가 올 것입니다. 현재는 참고 견디는 것이 중요합니다.",
                62, Arrays.asList(), Arrays.asList(2, 5, 8, 11)));

        TOJEONG_GWA_MAP.put(24, new TojeongGwa(24, "지뢰복", "☷☳",
                "땅 아래 천둥이 돌아온다",
                "회복과 재생의 시기입니다. 잃었던 것을 되찾거나 새로운 기회가 찾아올 것입니다.",
                69, Arrays.asList(1, 5, 9), Arrays.asList()));

        TOJEONG_GWA_MAP.put(26, new TojeongGwa(26, "산천대축", "☶☰",
                "산이 하늘을 받든다",
                "큰 기운을 모아서 성취하는 해입니다. 실력을 기르고 기회를 기다리면 성공할 수 있습니다.",
                67, Arrays.asList(1, 4, 7), Arrays.asList()));

        TOJEONG_GWA_MAP.put(27, new TojeongGwa(27, "산뢰이", "☶☳",
                "산 아래 천둥이 기른다",
                "자기계발과 수양에 힘써야 할 때입니다. 몸과 마음을 건강하게 기르는 것이 중요합니다.",
                64, Arrays.asList(2, 6, 10), Arrays.asList()));

        TOJEONG_GWA_MAP.put(28, new TojeongGwa(28, "택풍대과", "☱☴",
                "못 위의 바람이 크다",
                "과도한 것은 피하고 적당함을 유지해야 할 때입니다. 무리하지 말고 균형을 잡으세요.",
                63, Arrays.asList(3, 9), Arrays.asList(6, 12)));

        // ========== 중하괘 (50-59점) ==========
        TOJEONG_GWA_MAP.put(12, new TojeongGwa(12, "천지비", "☰☷",
                "하늘과 땅이 막힌다",
                "막힌 것이 많은 해이니 인내심을 갖고 기다리세요. 무리하게 추진하지 말고 때를 기다리는 것이 좋습니다.",
                55, Arrays.asList(), Arrays.asList(2, 5, 8, 11)));

        TOJEONG_GWA_MAP.put(29, new TojeongGwa(29, "감위수", "☵☵",
                "물이 거듭 흐른다",
                "위험이 겹치는 시기이니 조심해야 합니다. 신중하게 행동하고 안전을 최우선으로 하세요.",
                52, Arrays.asList(), Arrays.asList(1, 4, 7, 10)));

        TOJEONG_GWA_MAP.put(30, new TojeongGwa(30, "리위화", "☲☲",
                "불이 거듭 타오른다",
                "열정과 활력이 넘치는 해입니다. 밝은 기운으로 주변을 비추면 좋은 일이 생길 것입니다.",
                58, Arrays.asList(3, 7, 11), Arrays.asList()));

        TOJEONG_GWA_MAP.put(31, new TojeongGwa(31, "택산함", "☱☶",
                "못과 산이 감응한다",
                "상호작용과 소통이 중요한 해입니다. 진심으로 대하면 상대방도 마음을 열 것입니다.",
                59, Arrays.asList(2, 6, 10), Arrays.asList()));

        TOJEONG_GWA_MAP.put(32, new TojeongGwa(32, "뢰풍항", "☳☴",
                "천둥과 바람이 계속된다",
                "꾸준한 노력과 지속성이 필요한 해입니다. 포기하지 말고 끝까지 밀고 나가세요.",
                57, Arrays.asList(1, 5, 9), Arrays.asList()));

        TOJEONG_GWA_MAP.put(33, new TojeongGwa(33, "천산둔", "☰☶",
                "하늘 아래 산이 물러난다",
                "때로는 물러서는 것이 지혜입니다. 무리하게 진행하지 말고 적절한 타이밍을 기다리세요.",
                54, Arrays.asList(), Arrays.asList(3, 6, 9)));

        TOJEONG_GWA_MAP.put(34, new TojeongGwa(34, "뢰천대장", "☳☰",
                "천둥이 하늘에 크게 울린다",
                "큰 힘을 발휘할 수 있는 해입니다. 정당한 방법으로 권위와 영향력을 행사하세요.",
                56, Arrays.asList(1, 4, 7), Arrays.asList()));

        TOJEONG_GWA_MAP.put(36, new TojeongGwa(36, "지화명이", "☷☲",
                "땅 속에 불빛이 상한다",
                "어려운 상황에서도 내면의 밝음을 잃지 말아야 할 때입니다. 희망을 버리지 마세요.",
                53, Arrays.asList(), Arrays.asList(2, 5, 8, 11)));

        // ========== 하괘 (40-49점) ==========
        TOJEONG_GWA_MAP.put(37, new TojeongGwa(37, "풍화가인", "☴☲",
                "바람과 불이 집안을 다스린다",
                "가족과 가정에 집중해야 할 해입니다. 내부 결속을 다지고 화목한 관계를 유지하세요.",
                48, Arrays.asList(4, 8, 12), Arrays.asList()));

        TOJEONG_GWA_MAP.put(38, new TojeongGwa(38, "화택규", "☲☱",
                "불과 못이 어긋난다",
                "의견 차이나 갈등이 있을 수 있는 해입니다. 서로 다름을 인정하고 이해하려 노력하세요.",
                45, Arrays.asList(), Arrays.asList(3, 7, 11)));

        TOJEONG_GWA_MAP.put(39, new TojeongGwa(39, "수산건", "☵☶",
                "물이 산을 만나 막힌다",
                "장애물이 많은 해이지만 지혜롭게 대처하면 극복할 수 있습니다. 우회 전략을 사용하세요.",
                47, Arrays.asList(), Arrays.asList(2, 6, 10)));

        TOJEONG_GWA_MAP.put(40, new TojeongGwa(40, "뢰수해", "☳☵",
                "천둥과 물이 풀어준다",
                "문제가 해결되고 긴장이 풀리는 해입니다. 어려움에서 벗어나 새로운 시작을 할 수 있습니다.",
                49, Arrays.asList(1, 5, 9), Arrays.asList()));

        TOJEONG_GWA_MAP.put(41, new TojeongGwa(41, "산택손", "☶☱",
                "산 아래 못이 줄어든다",
                "손실이 있을 수 있지만 그것이 더 큰 이익을 위한 투자일 수 있습니다. 장기적 안목을 가지세요.",
                46, Arrays.asList(), Arrays.asList(4, 8, 12)));

        TOJEONG_GWA_MAP.put(42, new TojeongGwa(42, "풍뢰익", "☴☳",
                "바람과 천둥이 더해진다",
                "이익과 증가가 있는 해입니다. 선한 일에 힘쓰면 복이 돌아올 것입니다.",
                44, Arrays.asList(2, 6, 10), Arrays.asList()));

        TOJEONG_GWA_MAP.put(43, new TojeongGwa(43, "택천쾌", "☱☰",
                "못이 하늘로 올라간다",
                "결단력 있게 행동해야 할 때입니다. 미련을 버리고 과감하게 결정하세요.",
                43, Arrays.asList(1, 7), Arrays.asList()));

        TOJEONG_GWA_MAP.put(44, new TojeongGwa(44, "천풍구", "☰☴",
                "하늘에서 바람이 만난다",
                "예상치 못한 만남이나 기회가 있을 수 있습니다. 하지만 신중하게 판단해야 합니다.",
                41, Arrays.asList(), Arrays.asList(5, 9)));

        // ========== 하하괘 (30-39점) ==========
        TOJEONG_GWA_MAP.put(45, new TojeongGwa(45, "택지취", "☱☷",
                "못이 땅 위에 모인다",
                "여러 사람이 모이는 때이지만 갈등의 소지도 있습니다. 화합을 위해 노력해야 합니다.",
                38, Arrays.asList(), Arrays.asList(3, 6, 9, 12)));

        TOJEONG_GWA_MAP.put(46, new TojeongGwa(46, "지풍승", "☷☴",
                "땅에서 바람이 올라간다",
                "점진적인 발전과 상승의 기운이 있습니다. 급하게 서두르지 말고 차근차근 올라가세요.",
                39, Arrays.asList(2, 8), Arrays.asList()));

        TOJEONG_GWA_MAP.put(47, new TojeongGwa(47, "택수곤", "☱☵",
                "못에 물이 마른다",
                "어려운 상황에 처할 수 있는 해입니다. 절약하고 검소하게 생활하는 것이 좋습니다.",
                35, Arrays.asList(), Arrays.asList(1, 4, 7, 10)));

        TOJEONG_GWA_MAP.put(48, new TojeongGwa(48, "수풍정", "☵☴",
                "물과 바람이 우물을 이룬다",
                "기본으로 돌아가 실력을 기르는 해입니다. 기초를 탄탄히 하면 언젠가 쓸모가 있을 것입니다.",
                37, Arrays.asList(4, 10), Arrays.asList()));

        TOJEONG_GWA_MAP.put(49, new TojeongGwa(49, "택화혁", "☱☲",
                "못에서 불이 혁신한다",
                "큰 변화와 혁신이 필요한 해입니다. 과감하게 바꾸려는 용기가 필요합니다.",
                36, Arrays.asList(3, 9), Arrays.asList()));

        TOJEONG_GWA_MAP.put(50, new TojeongGwa(50, "화풍정", "☲☴",
                "불과 바람이 솥을 이룬다",
                "새로운 문화와 문명을 만드는 해입니다. 창조적인 활동에 힘쓰면 좋은 결과가 있을 것입니다.",
                34, Arrays.asList(5, 11), Arrays.asList()));

        TOJEONG_GWA_MAP.put(51, new TojeongGwa(51, "진위뢰", "☳☳",
                "천둥이 거듭 울린다",
                "놀라운 일이나 급변하는 상황에 직면할 수 있습니다. 침착하게 대응하는 것이 중요합니다.",
                33, Arrays.asList(), Arrays.asList(1, 4, 7, 10)));

        TOJEONG_GWA_MAP.put(52, new TojeongGwa(52, "간위산", "☶☶",
                "산이 거듭 서 있다",
                "멈춤과 안정을 추구해야 할 때입니다. 성급하게 움직이지 말고 현상 유지에 힘쓰세요.",
                32, Arrays.asList(), Arrays.asList(2, 5, 8, 11)));

        // ========== 최하괘 (20-29점) ==========
        TOJEONG_GWA_MAP.put(53, new TojeongGwa(53, "풍산점", "☴☶",
                "바람이 산을 점진적으로 깎는다",
                "느리지만 꾸준한 발전이 있는 해입니다. 조급해하지 말고 단계적으로 접근하세요.",
                29, Arrays.asList(6, 12), Arrays.asList()));

        TOJEONG_GWA_MAP.put(54, new TojeongGwa(54, "뢰택귀매", "☳☱",
                "천둥과 못의 잘못된 결합",
                "성급한 결정이나 행동을 피해야 할 해입니다. 신중하게 생각하고 움직이세요.",
                28, Arrays.asList(), Arrays.asList(3, 7, 11)));

        TOJEONG_GWA_MAP.put(55, new TojeongGwa(55, "뢰화풍", "☳☲",
                "천둥과 불이 풍성하다",
                "일시적으로 좋은 기운이 있지만 오래가지 않을 수 있습니다. 현실을 직시하세요.",
                27, Arrays.asList(5), Arrays.asList()));

        TOJEONG_GWA_MAP.put(56, new TojeongGwa(56, "화산려", "☲☶",
                "불이 산을 여행한다",
                "불안정하고 변화가 많은 해입니다. 한곳에 안주하지 말고 적응력을 기르세요.",
                26, Arrays.asList(), Arrays.asList(4, 8)));

        TOJEONG_GWA_MAP.put(57, new TojeongGwa(57, "손위풍", "☴☴",
                "바람이 거듭 분다",
                "우유부단함을 버리고 확고한 의지를 가져야 할 때입니다. 일관성 있게 행동하세요.",
                25, Arrays.asList(), Arrays.asList(2, 6, 10)));

        TOJEONG_GWA_MAP.put(58, new TojeongGwa(58, "태위택", "☱☱",
                "못이 거듭 기쁘다",
                "표면적인 기쁨에 속지 말고 실속을 챙겨야 할 때입니다. 허영을 경계하세요.",
                24, Arrays.asList(8), Arrays.asList()));

        TOJEONG_GWA_MAP.put(59, new TojeongGwa(59, "풍수환", "☴☵",
                "바람이 물을 흩어놓는다",
                "흩어지고 분산되기 쉬운 해입니다. 집중력을 기르고 목표를 명확히 하세요.",
                23, Arrays.asList(), Arrays.asList(1, 5, 9)));

        TOJEONG_GWA_MAP.put(60, new TojeongGwa(60, "수택절", "☵☱",
                "물이 못에서 절제된다",
                "절제와 규율이 필요한 해입니다. 욕심을 부리지 말고 적당한 선에서 만족하세요.",
                22, Arrays.asList(), Arrays.asList(3, 7, 11)));

        TOJEONG_GWA_MAP.put(61, new TojeongGwa(61, "풍택중부", "☴☱",
                "바람이 못 위에서 중심을 잡는다",
                "진실한 마음으로 임하면 좋은 결과가 있을 것입니다. 정성과 성의를 다하세요.",
                21, Arrays.asList(4, 10), Arrays.asList()));

        TOJEONG_GWA_MAP.put(62, new TojeongGwa(62, "산뢰소과", "☶☳",
                "산 위의 천둥이 작게 지나간다",
                "작은 일에 충실하되 큰 일은 피하는 것이 좋은 해입니다. 분수를 알고 행동하세요.",
                20, Arrays.asList(), Arrays.asList(2, 6, 10)));

        TOJEONG_GWA_MAP.put(63, new TojeongGwa(63, "수화기제", "☵☲",
                "물과 불이 이미 완성되었다",
                "완성의 기쁨이 있지만 방심하면 안 되는 해입니다. 겸손함을 잃지 말고 경계하세요.",
                19, Arrays.asList(7), Arrays.asList()));

        TOJEONG_GWA_MAP.put(64, new TojeongGwa(64, "화수미제", "☲☵",
                "불과 물이 아직 완성되지 않았다",
                "아직 완성되지 않은 상태이니 계속 노력해야 할 해입니다. 포기하지 말고 끝까지 최선을 다하세요.",
                18, Arrays.asList(), Arrays.asList(1, 4, 7, 10)));
    }

    /**
     * 나머지 괘들 채우기 (간략화)
     */
    private static void fillRemainingGwa() {
        for (int i = 13; i <= 64; i++) {
            if (!TOJEONG_GWA_MAP.containsKey(i)) {
                int score = 50 + (int)(Math.random() * 40); // 50-89점 랜덤
                TOJEONG_GWA_MAP.put(i, new TojeongGwa(i, "괘" + i, "☰☰",
                        "기본 해석", "기본적인 운세 해석입니다.", score,
                        Arrays.asList(1, 4, 7), Arrays.asList()));
            }
        }
    }
}
