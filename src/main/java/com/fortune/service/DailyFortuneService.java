package com.fortune.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.fortune.dto.SajuResult;
import com.fortune.dto.DailyFortuneResult;
import com.fortune.dto.FortuneByCategory;
import com.fortune.dto.SinsalInfo;
import java.time.LocalDate;
import java.util.*;

@Service
public class DailyFortuneService {

    @Autowired
    private GanjiCalculatorService ganjiCalculatorService;

    @Autowired
    private SinsalService sinsalService;

    // 오행 색상 매핑
    private static final Map<String, List<String>> WUXING_COLORS = new HashMap<>();

    // 길방위 매핑 (일간별)
    private static final Map<String, String> LUCKY_DIRECTIONS = new HashMap<>();

    static {
        // 오행별 길한 색깔
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

        // 일간별 길방위
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
     * 일일 운세 계산 메인 메서드
     * 이 메서드는 사주 결과와 대상 날짜를 기반으로 일일 운세를 계산합니다.
     * 1. 대상 날짜의 일주를 계산합니다.
     * 2. 길신/흉신을 계산합니다.
     * 3. 종합 점수를 계산합니다.
     * 4. 분야별 운세를 계산합니다.
     * 5. 조언을 생성합니다.
     * 6. 길방위 및 길한 색깔을 설정합니다.
     * @param saju 사주 결과
     * @param targetDate 대상 날짜
     * @return DailyFortuneResult 일일 운세 결과
     */
    public DailyFortuneResult calculateDailyFortune(SajuResult saju, LocalDate targetDate) {

        try {
            if (ganjiCalculatorService == null) {
                throw new RuntimeException("GanjiCalculatorService is not initialized");
            }

            // 1. 대상 날짜의 일주 계산
            String dayPillar = ganjiCalculatorService.calculateDayPillar(targetDate);

            // 2. 길신/흉신 계산
            List<SinsalInfo> sinsals = sinsalService.calculateDailySinsals(targetDate, saju);

            // 3. 종합 점수 계산
            int totalScore = calculateTotalScore(saju, dayPillar, sinsals);

            // 4. 분야별 운세 계산
            FortuneByCategory categoryFortune = calculateCategoryFortune(saju, dayPillar, totalScore);

            // 5. 조언 생성
            String advice = generateAdvice(totalScore, sinsals);

            // 6. 길방위 및 길한 색깔
            String luckyDirection = LUCKY_DIRECTIONS.get(saju.getDayMaster());
            List<String> luckyColors = WUXING_COLORS.get(saju.getDayMaster());

            return DailyFortuneResult.builder()
                    .date(targetDate)
                    .dayPillar(dayPillar)
                    .sinsals(sinsals)
                    .totalScore(totalScore)
                    .categoryFortune(categoryFortune)
                    .advice(advice)
                    .luckyDirection(luckyDirection)
                    .luckyColors(luckyColors)
                    .build();

        } catch (Exception e) {
            throw new RuntimeException("일일 운세 계산 중 오류가 발생했습니다: " + e.getMessage(), e);
        }
    }

    /**
     * 종합 점수 계산
     * 이 메서드는 사주 결과와 길신/흉신 정보를 기반으로 종합 점수를 계산합니다.
     * 기본 점수는 50점이며, 길신/흉신의 영향과 오행 상생상극 관계를 반영하여 최종 점수를 계산합니다.
     * @param saju 사주 결과
     * @param dayPillar 대상 날짜의 일주
     * @param sinsals 길신/흉신 정보 리스트
     * @return 종합 점수 (0-100 범위)
     */
    private int calculateTotalScore(SajuResult saju, String dayPillar, List<SinsalInfo> sinsals) {
        int baseScore = 50; // 기본 점수

        // 길신/흉신 점수 반영
        for (SinsalInfo sinsal : sinsals) {
            if (sinsal.isLucky()) {
                baseScore += sinsal.getInfluence();
            } else {
                baseScore -= sinsal.getInfluence();
            }
        }

        // 오행 상생상극 점수 반영
        baseScore += calculateWuxingScore(saju.getDayMaster(), dayPillar.substring(0, 1));

        // 점수 범위 조정 (0-100)
        return Math.max(0, Math.min(100, baseScore));
    }

    /**
     * 오행 상생상극 점수 계산
     * 이 메서드는 일간과 일주 간의 오행 상생상극 관계를 계산하여 점수를 반환합니다.
     * 일간과 일주가 같은 오행이면 10점, 상생 관계면 15점, 상극 관계면 -10점을 반환합니다.
     * @param dayMaster 일간 (천간)
     * @param dayGan 일주 (천간)
     * @return 오행 상생상극 점수
     */
    private int calculateWuxingScore(String dayMaster, String dayGan) {
        // 간단화된 오행 계산
        Map<String, String> wuxingMap = new HashMap<>();
        wuxingMap.put("갑", "목"); wuxingMap.put("을", "목");
        wuxingMap.put("병", "화"); wuxingMap.put("정", "화");
        wuxingMap.put("무", "토"); wuxingMap.put("기", "토");
        wuxingMap.put("경", "금"); wuxingMap.put("신", "금");
        wuxingMap.put("임", "수"); wuxingMap.put("계", "수");

        String dayMasterWuxing = wuxingMap.get(dayMaster);
        String dayGanWuxing = wuxingMap.get(dayGan);

        if (dayMasterWuxing.equals(dayGanWuxing)) {
            return 10; // 같은 오행 - 도움
        } else if (isShengRelation(dayMasterWuxing, dayGanWuxing)) {
            return 15; // 상생 관계
        } else if (isKeRelation(dayMasterWuxing, dayGanWuxing)) {
            return -10; // 상극 관계
        }

        return 0; // 중립
    }

    /**
     * 오행 상생 관계 확인
     * 이 메서드는 두 오행이 상생 관계인지 확인합니다.
     * 예를 들어, 목은 화를 상생하고, 화는 토를 상생하는 관계입니다.
     * @param element1 첫 번째 오행
     * @param element2 두 번째 오행
     * @return 상생 관계 여부
     */
    private boolean isShengRelation(String element1, String element2) {
        Map<String, String> shengMap = new HashMap<>();
        shengMap.put("목", "화");
        shengMap.put("화", "토");
        shengMap.put("토", "금");
        shengMap.put("금", "수");
        shengMap.put("수", "목");

        return element2.equals(shengMap.get(element1));
    }

    /**
     * 오행 상극 관계 확인
     * 이 메서드는 두 오행이 상극 관계인지 확인합니다.
     * 예를 들어, 목은 토를 상극하고, 토는 수를 상극하는 관계입니다.
     * @param element1 첫 번째 오행
     * @param element2 두 번째 오행
     * @return 상극 관계 여부
     */
    private boolean isKeRelation(String element1, String element2) {
        Map<String, String> keMap = new HashMap<>();
        keMap.put("목", "토");
        keMap.put("토", "수");
        keMap.put("수", "화");
        keMap.put("화", "금");
        keMap.put("금", "목");

        return element2.equals(keMap.get(element1));
    }

    /**
     * 분야별 운세 계산
     * 이 메서드는 종합 점수와 일주를 기반으로 각 분야별 운세를 계산합니다.
     * 각 분야별 운세는 종합 점수에 약간의 변동을 주어 생성됩니다.
     * @param saju 사주 결과
     * @param dayPillar 대상 날짜의 일주
     * @param totalScore 종합 점수
     * @return 분야별 운세 정보
     */
    private FortuneByCategory calculateCategoryFortune(SajuResult saju, String dayPillar, int totalScore) {
        Random random = new Random();
        int baseVariation = 10;

        return FortuneByCategory.builder()
                .overall(Math.max(0, Math.min(100, totalScore + random.nextInt(baseVariation) - 5)))
                .love(Math.max(0, Math.min(100, totalScore + random.nextInt(baseVariation) - 5)))
                .money(Math.max(0, Math.min(100, totalScore + random.nextInt(baseVariation) - 5)))
                .health(Math.max(0, Math.min(100, totalScore + random.nextInt(baseVariation) - 5)))
                .career(Math.max(0, Math.min(100, totalScore + random.nextInt(baseVariation) - 5)))
                .build();
    }

    /**
     * 조언 생성
     * 이 메서드는 종합 점수와 길신 정보를 기반으로 조언을 생성합니다.
     * 종합 점수에 따라 하루의 전반적인 운세를 평가하고, 길신이 있는 경우 추가적인 긍정적인 조언을 제공합니다.
     * @param totalScore 종합 점수
     * @param sinsals 길신 정보 리스트
     * @return 조언 문자열
     */
    private String generateAdvice(int totalScore, List<SinsalInfo> sinsals) {
        StringBuilder advice = new StringBuilder();

        if (totalScore >= 80) {
            advice.append("매우 좋은 하루입니다. 적극적으로 행동하세요. ");
        } else if (totalScore >= 70) {
            advice.append("좋은 하루가 될 것입니다. 기회를 놓치지 마세요. ");
        } else if (totalScore >= 60) {
            advice.append("평균적인 하루입니다. 꾸준히 노력하세요. ");
        } else if (totalScore >= 50) {
            advice.append("보통의 하루입니다. 신중하게 행동하세요. ");
        } else {
            advice.append("주의가 필요한 하루입니다. 조심스럽게 행동하세요. ");
        }

        // 길신이 있는 경우
        for (SinsalInfo sinsal : sinsals) {
            if (sinsal.isLucky() && sinsal.getInfluence() > 10) {
                advice.append(sinsal.getName()).append("의 좋은 기운이 함께합니다. ");
                break;
            }
        }

        return advice.toString().trim();
    }
}
