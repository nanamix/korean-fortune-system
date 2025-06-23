package com.fortune.service;
import com.fortune.dto.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.YearMonth;
import java.util.*;

/**
 * 간지달력 서비스
 * 이 서비스는 월별 간지달력을 생성하고, 각 날짜의 간지, 길흉, 길방위, 길한 색깔 등을 계산합니다.
 * 또한 24절기 정보를 포함하여 달력 데이터를 제공합니다.
 * 주요 기능:
 * - 월별 간지달력 생성
 * - 일별 간지달력 데이터 생성
 * - 길흉 수준 계산
 * - 길방위 및 길한 색깔 조회
 * * - 길일 여부 판단
 * * - 24절기 정보 조회
 * * 이 서비스는 GanjiCalculatorService를 사용하여 간지 계산을 수행합니다.
 * * @author 하진영
 * * @version 1.0
 */
import com.fortune.dto.GanjiCalendarResponse;
import com.fortune.dto.GanjiCalendarDay;
import com.fortune.dto.GanjiInfo;

/**
 * 간지달력 서비스
 * 이 서비스는 월별 간지달력을 생성하고, 각 날짜의 간지, 길흉, 길방위, 길한 색깔 등을 계산합니다.
 * 또한 24절기 정보를 포함하여 달력 데이터를 제공합니다.
 * 주요 기능:
 * - 월별 간지달력 생성
 * - 일별 간지달력 데이터 생성
 * - 길흉 수준 계산
 * - 길방위 및 길한 색깔 조회
 * - 길일 여부 판단
 * - 24절기 정보 조회
 */
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
     */
    public GanjiCalendarResponse generateMonthlyCalendar(int year, int month) {
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

        return GanjiCalendarResponse.builder()
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
    }

    /**
     * 간지달력 일별 데이터 생성
     * 이 메서드는 주어진 날짜에 대한 간지 정보를 계산하고, 길흉 수준, 길방위, 길한 색깔 등을 포함한 GanjiCalendarDay 객체를 생성합니다.
     * @param date 대상 날짜
     * @return GanjiCalendarDay 일별 간지달력 데이터
     */
    private GanjiCalendarDay createGanjiCalendarDay(LocalDate date) {
        // 간지 정보 상세 계산
        GanjiInfo ganjiInfo = ganjiCalculatorService.calculateGanjiInfo(date);

        // 오행 결정
        String wuxingElement = HEAVENLY_STEM_WUXING.getOrDefault(ganjiInfo.getHeavenlyStem(), "토");

        // 길흉 수준 및 점수 계산
        String fortuneLevel = calculateFortuneLevel(ganjiInfo.getDayPillar(), date);
        int fortuneScore = calculateFortuneScore(ganjiInfo.getDayPillar(), date);

        // 길방위 및 색깔
        String luckyDirection = LUCKY_DIRECTIONS.getOrDefault(ganjiInfo.getHeavenlyStem(), "중앙");
        List<String> luckyColors = LUCKY_COLORS.getOrDefault(ganjiInfo.getHeavenlyStem(), Arrays.asList("흰색"));

        // 절기 확인
        String solarTerm = getSolarTerm(date);

        // 길일 여부
        boolean isLuckyDay = isLuckyDay(ganjiInfo.getDayPillar(), date);

        // 일일 조언 생성
        String dailyAdvice = generateDailyAdvice(fortuneLevel, wuxingElement);

        return GanjiCalendarDay.builder()
                .date(date)
                .dayPillar(ganjiInfo.getDayPillar())
                .dayHeavenlyStem(ganjiInfo.getHeavenlyStem())
                .dayBranch(ganjiInfo.getEarthlyBranch())
                .wuxingElement(wuxingElement)
                .fortuneLevel(fortuneLevel)
                .fortuneScore(fortuneScore)
                .luckyDirection(luckyDirection)
                .luckyColors(luckyColors)
                .solarTerm(solarTerm)
                .isLuckyDay(isLuckyDay)
                .dailyAdvice(dailyAdvice)
                .build();
    }
    /**
     * 길흉 수준 계산
     */
    private String calculateFortuneLevel(String dayPillar, LocalDate date) {
        String heavenlyStem = dayPillar.substring(0, 1);
        String earthlyBranch = dayPillar.substring(1, 2);
        int dayOfMonth = date.getDayOfMonth();
        int dayOfWeek = date.getDayOfWeek().getValue();

        int score = 50; // 기본 점수

        // 천간별 기본 점수
        switch (heavenlyStem) {
            case "갑": case "을": score += 15; break;
            case "병": case "정": score += 10; break;
            case "무": case "기": score += 5; break;
            case "경": case "신": score += 8; break;
            case "임": case "계": score += 12; break;
        }

        // 지지별 보정
        if (Arrays.asList("자", "묘", "오", "유").contains(earthlyBranch)) {
            score += 10;
        }

        // 요일별 보정
        if (dayOfWeek == 7) score += 5;  // 일요일
        if (dayOfWeek == 2) score -= 5;  // 화요일

        // 날짜별 보정
        if (dayOfMonth % 3 == 0) score += 5;
        if (dayOfMonth == 13) score -= 10;

        // 점수에 따른 길흉 수준 결정
        if (score >= 85) return "대길";
        else if (score >= 70) return "길";
        else if (score >= 40) return "평";
        else return "흉";
    }

    /**
     * 길흉 점수 계산 (0-100)
     */
    private int calculateFortuneScore(String dayPillar, LocalDate date) {
        String heavenlyStem = dayPillar.substring(0, 1);
        String earthlyBranch = dayPillar.substring(1, 2);
        int dayOfMonth = date.getDayOfMonth();
        int dayOfWeek = date.getDayOfWeek().getValue();

        int score = 50;

        // 천간별 점수
        switch (heavenlyStem) {
            case "갑": case "을": score += 15; break;
            case "병": case "정": score += 10; break;
            case "무": case "기": score += 5; break;
            case "경": case "신": score += 8; break;
            case "임": case "계": score += 12; break;
        }

        // 지지별 점수
        if (Arrays.asList("자", "묘", "오", "유").contains(earthlyBranch)) {
            score += 10;
        }

        // 기타 보정들
        if (dayOfWeek == 7) score += 5;
        if (dayOfWeek == 2) score -= 5;
        if (dayOfMonth % 3 == 0) score += 5;
        if (dayOfMonth == 13) score -= 10;

        return Math.max(0, Math.min(100, score));
    }

    /**
     * 길일 여부 판단 (확장된 로직)
     */
    private boolean isLuckyDay(String dayPillar, LocalDate date) {
        // 전통적인 길일 판단
        List<String> traditionalLuckyPillars = Arrays.asList(
                "갑자", "을축", "병인", "정묘", "무진", "기사",
                "경오", "신미", "임신", "계유", "갑술", "을해"
        );

        if (traditionalLuckyPillars.contains(dayPillar)) {
            return true;
        }

        // 월별 특별한 날
        int day = date.getDayOfMonth();

        // 각 월의 1일, 15일은 길일로 간주
        if (day == 1 || day == 15) {
            return true;
        }

        // 절기가 있는 날은 길일
        if (getSolarTerm(date) != null) {
            return true;
        }

        return false;
    }

    /**
     * 일일 조언 생성
     */
    private String generateDailyAdvice(String fortuneLevel, String wuxingElement) {
        StringBuilder advice = new StringBuilder();

        // 길흉 수준별 기본 조언
        switch (fortuneLevel) {
            case "대길":
                advice.append("매우 좋은 날입니다. 중요한 일을 추진하기에 최적의 날입니다.");
                break;
            case "길":
                advice.append("좋은 날입니다. 새로운 시작이나 계획 실행에 좋습니다.");
                break;
            case "평":
                advice.append("무난한 날입니다. 꾸준히 노력하며 현상을 유지하세요.");
                break;
            case "흉":
                advice.append("조심스러운 날입니다. 신중하게 행동하며 안전을 우선시하세요.");
                break;
        }

        // 오행별 추가 조언
        switch (wuxingElement) {
            case "목":
                advice.append(" 성장과 발전에 좋은 기운입니다.");
                break;
            case "화":
                advice.append(" 열정과 활력을 발휘하세요.");
                break;
            case "토":
                advice.append(" 안정과 신뢰를 중시하세요.");
                break;
            case "금":
                advice.append(" 정의와 원칙을 지키세요.");
                break;
            case "수":
                advice.append(" 지혜와 직관을 활용하세요.");
                break;
        }

        return advice.toString();
    }

    /**
     * 월별 테마 생성
     */
    private String generateMonthlyTheme(int year, int month) {
        String[] themes = {
                "새해 계획", "정화와 준비", "성장의 시작", "활력과 발전",
                "풍요로운 성장", "조화와 균형", "열정의 계절", "성숙과 결실",
                "변화와 적응", "수확과 감사", "성찰과 준비", "마무리와 정리"
        };
        return themes[month - 1];
    }

    /**
     * 월별 조언 생성
     */
    private String generateMonthlyAdvice(int year, int month) {
        String[] advices = {
                "새로운 해의 시작, 목표를 명확히 세우세요.",
                "마음을 정화하고 봄을 준비하는 달입니다.",
                "새싹이 돋는 시기, 새로운 도전을 시작하세요.",
                "활력이 넘치는 계절, 적극적으로 행동하세요.",
                "모든 것이 풍성해지는 달, 관계를 소중히 하세요.",
                "균형과 조화를 추구하는 시기입니다.",
                "열정을 불태우며 목표를 향해 나아가세요.",
                "노력의 결실을 거두는 소중한 시기입니다.",
                "변화에 적응하며 새로운 기회를 찾으세요.",
                "수확의 계절, 감사하는 마음을 가지세요.",
                "한 해를 돌아보며 내년을 준비하세요.",
                "마무리를 잘하며 새해를 맞을 준비를 하세요."
        };
        return advices[month - 1];
    }



    /**
     * 24절기 초기화 (완전판)
     */
    private static void initializeSolarTerms() {
        Map<Integer, String> january = new HashMap<>();
        january.put(5, "소한"); january.put(20, "대한");
        SOLAR_TERMS.put(1, january);

        Map<Integer, String> february = new HashMap<>();
        february.put(4, "입춘"); february.put(19, "우수");
        SOLAR_TERMS.put(2, february);

        Map<Integer, String> march = new HashMap<>();
        march.put(6, "경칩"); march.put(21, "춘분");
        SOLAR_TERMS.put(3, march);

        Map<Integer, String> april = new HashMap<>();
        april.put(5, "청명"); april.put(20, "곡우");
        SOLAR_TERMS.put(4, april);

        Map<Integer, String> may = new HashMap<>();
        may.put(6, "입하"); may.put(21, "소만");
        SOLAR_TERMS.put(5, may);

        Map<Integer, String> june = new HashMap<>();
        june.put(6, "망종"); june.put(21, "하지");
        SOLAR_TERMS.put(6, june);

        Map<Integer, String> july = new HashMap<>();
        july.put(7, "소서"); july.put(23, "대서");
        SOLAR_TERMS.put(7, july);

        Map<Integer, String> august = new HashMap<>();
        august.put(8, "입추"); august.put(23, "처서");
        SOLAR_TERMS.put(8, august);

        Map<Integer, String> september = new HashMap<>();
        september.put(8, "백로"); september.put(23, "추분");
        SOLAR_TERMS.put(9, september);

        Map<Integer, String> october = new HashMap<>();
        october.put(8, "한로"); october.put(23, "상강");
        SOLAR_TERMS.put(10, october);

        Map<Integer, String> november = new HashMap<>();
        november.put(7, "입동"); november.put(22, "소설");
        SOLAR_TERMS.put(11, november);

        Map<Integer, String> december = new HashMap<>();
        december.put(7, "대설"); december.put(22, "동지");
        SOLAR_TERMS.put(12, december);
    }

    /**
     * 천간별 오행 매핑 초기화
     */
    private static void initializeWuxingMapping() {
        HEAVENLY_STEM_WUXING.put("갑", "목"); HEAVENLY_STEM_WUXING.put("을", "목");
        HEAVENLY_STEM_WUXING.put("병", "화"); HEAVENLY_STEM_WUXING.put("정", "화");
        HEAVENLY_STEM_WUXING.put("무", "토"); HEAVENLY_STEM_WUXING.put("기", "토");
        HEAVENLY_STEM_WUXING.put("경", "금"); HEAVENLY_STEM_WUXING.put("신", "금");
        HEAVENLY_STEM_WUXING.put("임", "수"); HEAVENLY_STEM_WUXING.put("계", "수");
    }

    /**
     * 길방위 초기화
     */
    private static void initializeLuckyDirections() {
        LUCKY_DIRECTIONS.put("갑", "동쪽"); LUCKY_DIRECTIONS.put("을", "동남쪽");
        LUCKY_DIRECTIONS.put("병", "남쪽"); LUCKY_DIRECTIONS.put("정", "남서쪽");
        LUCKY_DIRECTIONS.put("무", "중앙"); LUCKY_DIRECTIONS.put("기", "중앙");
        LUCKY_DIRECTIONS.put("경", "서쪽"); LUCKY_DIRECTIONS.put("신", "서북쪽");
        LUCKY_DIRECTIONS.put("임", "북쪽"); LUCKY_DIRECTIONS.put("계", "북동쪽");
    }

    /**
     * 길한 색깔 초기화
     */
    private static void initializeLuckyColors() {
        LUCKY_COLORS.put("갑", Arrays.asList("녹색", "청색", "남색"));
        LUCKY_COLORS.put("을", Arrays.asList("연두색", "초록색", "청록색"));
        LUCKY_COLORS.put("병", Arrays.asList("빨간색", "주황색", "분홍색"));
        LUCKY_COLORS.put("정", Arrays.asList("자주색", "보라색", "연분홍색"));
        LUCKY_COLORS.put("무", Arrays.asList("노란색", "갈색", "황토색"));
        LUCKY_COLORS.put("기", Arrays.asList("베이지색", "아이보리", "크림색"));
        LUCKY_COLORS.put("경", Arrays.asList("흰색", "은색", "회색"));
        LUCKY_COLORS.put("신", Arrays.asList("금색", "백금색", "진주색"));
        LUCKY_COLORS.put("임", Arrays.asList("검은색", "짙은 파랑", "남색"));
        LUCKY_COLORS.put("계", Arrays.asList("물색", "하늘색", "연파랑"));
    }

    // 기타 유틸리티 메서드들...
    private String getSolarTerm(LocalDate date) {
        return SOLAR_TERMS.getOrDefault(date.getMonthValue(), Collections.emptyMap())
                .get(date.getDayOfMonth());
    }

    private Map<Integer, String> getSolarTermsForMonth(int year, int month) {
        return SOLAR_TERMS.getOrDefault(month, Collections.emptyMap());
    }



}
