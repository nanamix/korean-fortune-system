package com.fortune.service;

import com.fortune.dto.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import lombok.extern.slf4j.Slf4j;

import java.time.LocalDate;
import java.time.YearMonth;
import java.util.*;

/**
 * 간지달력 서비스
 *
 * <p>이 서비스는 월별 간지달력을 생성하고, 각 날짜의 간지, 길흉, 길방위, 길한 색깔 등을 계산합니다.
 * 또한 24절기 정보를 포함하여 달력 데이터를 제공합니다.</p>
 *
 * <h3>주요 기능</h3>
 * <ul>
 *   <li>월별 간지달력 생성</li>
 *   <li>일별 간지달력 데이터 생성</li>
 *   <li>길흉 수준 계산</li>
 *   <li>길방위 및 길한 색깔 조회</li>
 *   <li>길일 여부 판단</li>
 *   <li>24절기 정보 조회</li>
 * </ul>
 *
 * @author 하진영
 * @version 2.5.0
 * @since 2025-06-24
 */
@Slf4j
@Service
public class GanjiCalendarService {

    @Autowired
    private GanjiCalculatorService ganjiCalculatorService;

    // 24절기 데이터 (월별 -> 일별 매핑)
    private static final Map<Integer, Map<Integer, String>> SOLAR_TERMS = new HashMap<>();

    // 천간별 오행 매핑
    private static final Map<String, String> HEAVENLY_STEM_WUXING = new HashMap<>();

    // 길방위 매핑 (천간별)
    private static final Map<String, String> LUCKY_DIRECTIONS = new HashMap<>();

    // 길한 색깔 매핑 (천간별)
    private static final Map<String, List<String>> LUCKY_COLORS = new HashMap<>();

    static {
        initializeSolarTerms();
        initializeWuxingMapping();
        initializeLuckyDirections();
        initializeLuckyColors();
    }

    /**
     * 월별 간지달력 생성
     *
     * @param year 년도
     * @param month 월
     * @return 간지달력 응답 객체
     */
    public GanjiCalendarResponse generateMonthlyCalendar(int year, int month) {
        log.info("📆 간지달력 생성 시작: {}년 {}월", year, month);

        try {
            YearMonth yearMonth = YearMonth.of(year, month);
            LocalDate firstDay = yearMonth.atDay(1);
            LocalDate lastDay = yearMonth.atEndOfMonth();

            List<GanjiCalendarDay> days = new ArrayList<>();
            List<Integer> luckyDays = new ArrayList<>();
            List<Integer> cautionDays = new ArrayList<>();

            // 각 날짜별 정보 생성
            for (LocalDate date = firstDay; !date.isAfter(lastDay); date = date.plusDays(1)) {
                GanjiCalendarDay day = createGanjiCalendarDay(date);
                days.add(day);

                // 길일/흉일 분류
                if (day.isLuckyDay() || day.getFortuneScore() >= 80) {
                    luckyDays.add(date.getDayOfMonth());
                } else if (day.getFortuneScore() <= 40) {
                    cautionDays.add(date.getDayOfMonth());
                }
            }

            GanjiCalendarResponse result = GanjiCalendarResponse.builder()
                    .year(year)
                    .month(month)
                    .monthName(month + "월")
                    .days(days)
                    .solarTerms(getSolarTermsForMonth(year, month))
                    .monthlyTheme(generateMonthlyTheme(year, month))
                    .monthlyAdvice(generateMonthlyAdvice(year, month))
                    .luckyDays(luckyDays)
                    .cautionDays(cautionDays)
                    .totalDays(days.size())
                    .build();

            log.info("✅ 간지달력 생성 완료: {}일", days.size());
            return result;

        } catch (Exception e) {
            log.error("❌ 간지달력 생성 중 오류 발생: {}", e.getMessage(), e);
            throw new RuntimeException("간지달력 생성 중 오류가 발생했습니다: " + e.getMessage(), e);
        }
    }

    /**
     * 간지달력 일별 데이터 생성
     *
     * <p>이 메서드는 주어진 날짜에 대한 간지 정보를 계산하고, 길흉 수준, 길방위, 길한 색깔 등을 포함한
     * GanjiCalendarDay 객체를 생성합니다.</p>
     *
     * @param date 계산할 날짜
     * @return 간지달력 일별 정보
     */
    private GanjiCalendarDay createGanjiCalendarDay(LocalDate date) {
        try {
            // 1. 일주 계산
            String dayPillar = ganjiCalculatorService.calculateDayPillar(date);
            String dayStem = dayPillar.substring(0, 1);

            // 2. 운세 점수 계산
            int fortuneScore = calculateDayFortuneScore(date, dayPillar);

            // 3. 길일 여부 판단
            boolean isLuckyDay = isLuckyDay(date, fortuneScore);

            // 4. 길방위 및 길한 색깔
            String luckyDirection = LUCKY_DIRECTIONS.getOrDefault(dayStem, "동쪽");
            List<String> luckyColors = LUCKY_COLORS.getOrDefault(dayStem, Arrays.asList("흰색"));

            // 5. 24절기 확인
            String solarTerm = getSolarTermForDate(date);

            // 6. 간단한 조언 생성
            String briefAdvice = generateBriefAdvice(fortuneScore, isLuckyDay);

            return GanjiCalendarDay.builder()
                    .date(date)
                    .dayPillar(dayPillar)
                    .fortuneScore(fortuneScore)
                    .luckyDay(isLuckyDay)
                    .luckyDirection(luckyDirection)
                    .luckyColors(luckyColors)
                    .solarTerm(solarTerm)
                    .briefAdvice(briefAdvice)
                    .build();

        } catch (Exception e) {
            log.warn("⚠️ 일별 데이터 생성 중 오류: {} - {}", date, e.getMessage());
            // 기본값 반환
            return GanjiCalendarDay.builder()
                    .date(date)
                    .dayPillar("갑자")
                    .fortuneScore(50)
                    .luckyDay(false)
                    .luckyDirection("동쪽")
                    .luckyColors(Arrays.asList("흰색"))
                    .solarTerm("")
                    .briefAdvice("평범한 하루입니다")
                    .build();
        }
    }

    /**
     * 일별 운세 점수 계산
     *
     * @param date 계산할 날짜
     * @param dayPillar 해당 날짜의 일주
     * @return 운세 점수 (0-100)
     */
    private int calculateDayFortuneScore(LocalDate date, String dayPillar) {
        Random random = new Random(date.toEpochDay());

        // 기본 점수 (30-80점)
        int baseScore = 30 + random.nextInt(51);

        // 날짜별 보정
        int dayOfMonth = date.getDayOfMonth();
        if (dayOfMonth == 1 || dayOfMonth == 15) { // 초하루, 보름
            baseScore += 15;
        } else if (dayOfMonth % 7 == 0) { // 7의 배수일
            baseScore -= 10;
        }

        // 요일별 보정
        switch (date.getDayOfWeek()) {
            case SUNDAY, SATURDAY -> baseScore += 5; // 주말 보너스
            case FRIDAY -> baseScore += 10; // 금요일 보너스
            case MONDAY -> baseScore -= 5; // 월요일 페널티
        }

        // 월별 보정
        int month = date.getMonthValue();
        if (month == 3 || month == 4 || month == 5) { // 봄
            baseScore += 5;
        } else if (month == 9 || month == 10 || month == 11) { // 가을
            baseScore += 3;
        }

        // 일주별 보정
        String dayStem = dayPillar.substring(0, 1);
        switch (dayStem) {
            case "갑", "을" -> baseScore += 3; // 목의 기운
            case "병", "정" -> baseScore += 2; // 화의 기운
            case "무", "기" -> baseScore += 1; // 토의 기운
            case "경", "신" -> baseScore += 4; // 금의 기운
            case "임", "계" -> baseScore += 2; // 수의 기운
        }

        return Math.max(0, Math.min(100, baseScore));
    }

    /**
     * 길일 여부 판단
     *
     * @param date 판단할 날짜
     * @param fortuneScore 운세 점수
     * @return 길일 여부
     */
    private boolean isLuckyDay(LocalDate date, int fortuneScore) {
        // 1. 점수 기준
        if (fortuneScore >= 75) {
            return true;
        }

        // 2. 특별한 날짜 기준
        int dayOfMonth = date.getDayOfMonth();
        if (dayOfMonth == 1 || dayOfMonth == 8 || dayOfMonth == 15 || dayOfMonth == 22) {
            return true;
        }

        // 3. 24절기 기준
        String solarTerm = getSolarTermForDate(date);
        if (!solarTerm.isEmpty()) {
            return true;
        }

        // 4. 요일 기준
        if (date.getDayOfWeek().getValue() == 6) { // 토요일
            return true;
        }

        return false;
    }

    /**
     * 24절기 조회 (특정 날짜)
     *
     * @param date 확인할 날짜
     * @return 24절기 이름 (없으면 빈 문자열)
     */
    private String getSolarTermForDate(LocalDate date) {
        Map<Integer, String> monthTerms = SOLAR_TERMS.get(date.getMonthValue());
        if (monthTerms != null) {
            return monthTerms.getOrDefault(date.getDayOfMonth(), "");
        }
        return "";
    }

    /**
     * 월별 24절기 목록 조회
     *
     * @param year 년도
     * @param month 월
     * @return 해당 월의 24절기 목록
     */
    private List<String> getSolarTermsForMonth(int year, int month) {
        Map<Integer, String> monthTerms = SOLAR_TERMS.get(month);
        if (monthTerms != null) {
            List<String> terms = new ArrayList<>();
            for (Map.Entry<Integer, String> entry : monthTerms.entrySet()) {
                terms.add(entry.getKey() + "일 " + entry.getValue());
            }
            return terms;
        }
        return new ArrayList<>();
    }

    /**
     * 월별 테마 생성
     *
     * @param year 년도
     * @param month 월
     * @return 월별 테마
     */
    private String generateMonthlyTheme(int year, int month) {
        return switch (month) {
            case 1 -> "새해의 시작, 희망찬 출발";
            case 2 -> "겨울의 마지막, 봄의 전령";
            case 3 -> "봄의 시작, 만물의 소생";
            case 4 -> "봄꽃이 만발하는 화사한 계절";
            case 5 -> "신록이 우거진 생명력 넘치는 달";
            case 6 -> "초여름의 활기찬 에너지";
            case 7 -> "무더운 여름, 역동적인 시기";
            case 8 -> "여름의 절정, 풍요로운 달";
            case 9 -> "가을의 시작, 결실의 계절";
            case 10 -> "단풍이 아름다운 성숙의 달";
            case 11 -> "늦가을의 깊이, 성찰의 시간";
            case 12 -> "한해의 마무리, 정리의 달";
            default -> "특별한 의미가 있는 달";
        };
    }

    /**
     * 월별 조언 생성
     *
     * @param year 년도
     * @param month 월
     * @return 월별 조언
     */
    private String generateMonthlyAdvice(int year, int month) {
        return switch (month) {
            case 1 -> "새해 계획을 세우고 목표를 명확히 하세요. 새로운 시작의 에너지를 활용하세요.";
            case 2 -> "인내심을 갖고 기다리는 지혜가 필요한 달입니다. 준비하는 시간으로 활용하세요.";
            case 3 -> "새로운 도전을 시작하기 좋은 달입니다. 적극적으로 행동하세요.";
            case 4 -> "인간관계에 신경쓰고 협력하는 자세가 중요합니다.";
            case 5 -> "건강 관리와 자기 계발에 투자하기 좋은 달입니다.";
            case 6 -> "활발한 활동과 소통이 좋은 결과를 가져올 것입니다.";
            case 7 -> "휴식과 재충전의 시간을 갖는 것이 중요합니다.";
            case 8 -> "성과를 거두고 결실을 맺을 수 있는 달입니다.";
            case 9 -> "정리와 점검의 시간입니다. 계획을 재검토하세요.";
            case 10 -> "안정과 균형을 추구하는 것이 좋겠습니다.";
            case 11 -> "내면을 돌아보고 성찰하는 시간을 가져보세요.";
            case 12 -> "한해를 마무리하고 새해를 준비하는 달입니다.";
            default -> "매일을 소중히 여기며 최선을 다하세요.";
        };
    }

    /**
     * 간단한 조언 생성
     *
     * @param fortuneScore 운세 점수
     * @param isLuckyDay 길일 여부
     * @return 간단한 조언
     */
    private String generateBriefAdvice(int fortuneScore, boolean isLuckyDay) {
        if (isLuckyDay) {
            return "길한 날입니다. 중요한 일을 진행하세요!";
        } else if (fortuneScore >= 70) {
            return "좋은 하루입니다. 적극적으로 행동하세요.";
        } else if (fortuneScore >= 50) {
            return "무난한 하루입니다. 꾸준히 노력하세요.";
        } else if (fortuneScore >= 30) {
            return "조심스러운 하루입니다. 신중하게 행동하세요.";
        } else {
            return "특별히 주의가 필요한 날입니다.";
        }
    }

    // ==================== 정적 초기화 메서드들 ====================

    /**
     * 24절기 정보 초기화
     */
    private static void initializeSolarTerms() {
        // 1월
        Map<Integer, String> january = new HashMap<>();
        january.put(5, "소한");
        january.put(20, "대한");
        SOLAR_TERMS.put(1, january);

        // 2월
        Map<Integer, String> february = new HashMap<>();
        february.put(4, "입춘");
        february.put(19, "우수");
        SOLAR_TERMS.put(2, february);

        // 3월
        Map<Integer, String> march = new HashMap<>();
        march.put(6, "경칩");
        march.put(21, "춘분");
        SOLAR_TERMS.put(3, march);

        // 4월
        Map<Integer, String> april = new HashMap<>();
        april.put(5, "청명");
        april.put(20, "곡우");
        SOLAR_TERMS.put(4, april);

        // 5월
        Map<Integer, String> may = new HashMap<>();
        may.put(6, "입하");
        may.put(21, "소만");
        SOLAR_TERMS.put(5, may);

        // 6월
        Map<Integer, String> june = new HashMap<>();
        june.put(6, "망종");
        june.put(21, "하지");
        SOLAR_TERMS.put(6, june);

        // 7월
        Map<Integer, String> july = new HashMap<>();
        july.put(7, "소서");
        july.put(23, "대서");
        SOLAR_TERMS.put(7, july);

        // 8월
        Map<Integer, String> august = new HashMap<>();
        august.put(8, "입추");
        august.put(23, "처서");
        SOLAR_TERMS.put(8, august);

        // 9월
        Map<Integer, String> september = new HashMap<>();
        september.put(8, "백로");
        september.put(23, "추분");
        SOLAR_TERMS.put(9, september);

        // 10월
        Map<Integer, String> october = new HashMap<>();
        october.put(8, "한로");
        october.put(24, "상강");
        SOLAR_TERMS.put(10, october);

        // 11월
        Map<Integer, String> november = new HashMap<>();
        november.put(7, "입동");
        november.put(22, "소설");
        SOLAR_TERMS.put(11, november);

        // 12월
        Map<Integer, String> december = new HashMap<>();
        december.put(7, "대설");
        december.put(22, "동지");
        SOLAR_TERMS.put(12, december);
    }

    /**
     * 천간별 오행 매핑 초기화
     */
    private static void initializeWuxingMapping() {
        HEAVENLY_STEM_WUXING.put("갑", "목");
        HEAVENLY_STEM_WUXING.put("을", "목");
        HEAVENLY_STEM_WUXING.put("병", "화");
        HEAVENLY_STEM_WUXING.put("정", "화");
        HEAVENLY_STEM_WUXING.put("무", "토");
        HEAVENLY_STEM_WUXING.put("기", "토");
        HEAVENLY_STEM_WUXING.put("경", "금");
        HEAVENLY_STEM_WUXING.put("신", "금");
        HEAVENLY_STEM_WUXING.put("임", "수");
        HEAVENLY_STEM_WUXING.put("계", "수");
    }

    /**
     * 천간별 길방위 초기화
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
     * 천간별 길한 색깔 초기화
     */
    private static void initializeLuckyColors() {
        LUCKY_COLORS.put("갑", Arrays.asList("녹색", "청색"));
        LUCKY_COLORS.put("을", Arrays.asList("연두색", "초록색"));
        LUCKY_COLORS.put("병", Arrays.asList("빨간색", "주황색"));
        LUCKY_COLORS.put("정", Arrays.asList("자주색", "분홍색"));
        LUCKY_COLORS.put("무", Arrays.asList("노란색", "갈색"));
        LUCKY_COLORS.put("기", Arrays.asList("베이지색", "아이보리"));
        LUCKY_COLORS.put("경", Arrays.asList("흰색", "은색"));
        LUCKY_COLORS.put("신", Arrays.asList("금색", "백금색"));
        LUCKY_COLORS.put("임", Arrays.asList("검은색", "짙은 파랑"));
        LUCKY_COLORS.put("계", Arrays.asList("하늘색", "연파랑"));
    }

    // ==================== 공개 헬퍼 메서드들 ====================

    /**
     * 특정 날짜의 간지 정보 조회
     *
     * @param date 조회할 날짜
     * @return 간지 정보
     */
    public GanjiInfo getGanjiInfo(LocalDate date) {
        String dayPillar = ganjiCalculatorService.calculateDayPillar(date);
        String heavenlyStem = dayPillar.substring(0, 1);
        String earthlyBranch = dayPillar.substring(1, 2);
        String wuxing = HEAVENLY_STEM_WUXING.getOrDefault(heavenlyStem, "알 수 없음");

        // 음양 판별 (간단한 규칙)
        String yinYang = switch (heavenlyStem) {
            case "갑", "병", "무", "경", "임" -> "양";
            case "을", "정", "기", "신", "계" -> "음";
            default -> "알 수 없음";
        };

        return GanjiInfo.builder()
                .heavenlyStem(heavenlyStem)
                .earthlyBranch(earthlyBranch)
                .ganji(dayPillar)
                .wuxing(wuxing)
                .yinYang(yinYang)
                .build();
    }

    /**
     * 특정 월의 길일 목록 조회
     *
     * @param year 년도
     * @param month 월
     * @return 길일 목록
     */
    public List<LocalDate> getLuckyDatesOfMonth(int year, int month) {
        List<LocalDate> luckyDates = new ArrayList<>();
        YearMonth yearMonth = YearMonth.of(year, month);
        LocalDate firstDay = yearMonth.atDay(1);
        LocalDate lastDay = yearMonth.atEndOfMonth();

        for (LocalDate date = firstDay; !date.isAfter(lastDay); date = date.plusDays(1)) {
            int fortuneScore = calculateDayFortuneScore(date, ganjiCalculatorService.calculateDayPillar(date));
            if (isLuckyDay(date, fortuneScore)) {
                luckyDates.add(date);
            }
        }

        return luckyDates;
    }

    /**
     * 년간 주요 길일 조회
     *
     * @param year 년도
     * @return 년간 주요 길일 목록
     */
    public Map<Integer, List<LocalDate>> getYearlyMajorLuckyDates(int year) {
        Map<Integer, List<LocalDate>> yearlyLuckyDates = new HashMap<>();

        for (int month = 1; month <= 12; month++) {
            List<LocalDate> monthlyLuckyDates = getLuckyDatesOfMonth(year, month);
            // 각 월에서 점수가 높은 상위 3일만 선별
            monthlyLuckyDates.sort((d1, d2) -> {
                int score1 = calculateDayFortuneScore(d1, ganjiCalculatorService.calculateDayPillar(d1));
                int score2 = calculateDayFortuneScore(d2, ganjiCalculatorService.calculateDayPillar(d2));
                return Integer.compare(score2, score1); // 내림차순
            });

            List<LocalDate> topLuckyDates = monthlyLuckyDates.stream()
                    .limit(3)
                    .toList();

            yearlyLuckyDates.put(month, topLuckyDates);
        }

        return yearlyLuckyDates;
    }
}
