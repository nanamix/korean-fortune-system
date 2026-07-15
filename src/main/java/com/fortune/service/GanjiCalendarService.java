package com.fortune.service;
import com.fortune.dto.*;
import org.springframework.stereotype.Service;
import lombok.extern.slf4j.Slf4j;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.*;
import net.time4j.PlainDate;
import net.time4j.TemporalType;
import net.time4j.calendar.KoreanCalendar;
import net.time4j.calendar.SolarTerm;
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
    /**
     * 간지 계산 서비스
     * - Autowired 어노테이션을 사용하여 간지 계산 서비스를 주입합니다.
     * - GanjiCalculatorService 클래스를 사용하여 간지 계산을 수행합니다.
     */
    private final GanjiCalculatorService ganjiCalculatorService;
    private final LunarCalendarService lunarCalendarService;

    public GanjiCalendarService(
            GanjiCalculatorService ganjiCalculatorService,
            LunarCalendarService lunarCalendarService) {
        this.ganjiCalculatorService = ganjiCalculatorService;
        this.lunarCalendarService = lunarCalendarService;
    }
    /**
     * 천간별 오행 매핑
     * - 천간별 오행 매핑을 저장합니다.
     */
    private static final Map<String, String> HEAVENLY_STEM_WUXING = new HashMap<>();
    /**
     * 길방위 매핑 (천간별)
     * - 길방위 매핑을 저장합니다.
     */
    private static final Map<String, String> LUCKY_DIRECTIONS = new HashMap<>();
    /**
     * 길한 색깔 매핑 (천간별)
     * - 길한 색깔 매핑을 저장합니다.
     */
    private static final Map<String, List<String>> LUCKY_COLORS = new HashMap<>();
    /**
     * 정적 초기화 메서드들
     * - 24절기 데이터, 천간별 오행 매핑, 길방위 매핑, 길한 색깔 매핑을 초기화합니다.
     */
    static {
        initializeWuxingMapping();
        initializeLuckyDirections();
        initializeLuckyColors();
    }
    /**
     * 월별 간지달력 생성
     * - 주어진 년도와 월에 대한 간지달력을 생성합니다.
     * - 각 날짜별 간지, 길흉, 길방위, 길한 색깔 등을 계산합니다.
     * - 24절기 정보를 포함하여 달력 데이터를 제공합니다.
     *
     * @param year 년도
     * @param month 월
     * @return 간지달력 응답 객체
     */
    public GanjiCalendarResponse generateMonthlyCalendar(int year, int month) {
        /* 간지달력 생성 시작 */
        log.info("📆 간지달력 생성 시작: {}년 {}월", year, month);
        /* 간지달력 생성 시작 */
        try {
            YearMonth yearMonth = YearMonth.of(year, month);
            LocalDate firstDay = yearMonth.atDay(1);
            LocalDate lastDay = yearMonth.atEndOfMonth();
            Map<LocalDate, String> solarTerms = calculateSolarTerms(year, month);
            /* 각 날짜별 정보 생성 */
            List<GanjiCalendarDay> days = new ArrayList<>();
            /* 길일/흉일 분류 */
            List<Integer> luckyDays = new ArrayList<>();
            List<Integer> cautionDays = new ArrayList<>();
            /* 각 날짜별 정보 생성 */
            for (LocalDate date = firstDay; !date.isAfter(lastDay); date = date.plusDays(1)) {
                GanjiCalendarDay day = createGanjiCalendarDay(date, solarTerms);
                days.add(day);
                /* 길일/흉일 분류 */
                if (day.isLuckyDay() || day.getFortuneScore() >= 80) {
                    luckyDays.add(date.getDayOfMonth());
                } else if (day.getFortuneScore() <= 45) {
                    cautionDays.add(date.getDayOfMonth());
                }
            }
            /* 간지달력 응답 객체 생성 */
            GanjiCalendarResponse result = GanjiCalendarResponse.builder()
                    .year(year)
                    .month(month)
                    .monthName(month + "월")
                    .calendarBasis("SOLAR_WITH_LUNAR")
                    .solarTermsBasis("TIME4J_ASTRONOMICAL_KOREA")
                    .days(days)
                    .solarTerms(formatSolarTerms(solarTerms))
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
    private GanjiCalendarDay createGanjiCalendarDay(LocalDate date, Map<LocalDate, String> solarTerms) {
        /* 1. 일주 계산 */
        String dayPillar = ganjiCalculatorService.calculateDayPillar(date);
            String dayStem = dayPillar.substring(0, 1);
            /* 2. 운세 점수 계산 */
            int fortuneScore = calculateDayFortuneScore(date, dayPillar);
            /* 3. 길일 여부 판단 */
            boolean isLuckyDay = isLuckyDay(date, fortuneScore);
            /* 4. 길방위 및 길한 색깔 */
            String luckyDirection = LUCKY_DIRECTIONS.getOrDefault(dayStem, "동쪽");
            List<String> luckyColors = LUCKY_COLORS.getOrDefault(dayStem, Arrays.asList("흰색"));
            /* 5. 24절기 확인 */
            String solarTerm = solarTerms.getOrDefault(date, "");
            LunarDate lunarDate = lunarCalendarService.convertSolarToLunar(date);
            /* 6. 간단한 조언 생성 */
            String briefAdvice = generateBriefAdvice(dayPillar, fortuneScore);
        return GanjiCalendarDay.builder()
                    .date(date)
                    .lunarYear(lunarDate.getYear())
                    .lunarMonth(lunarDate.getMonth())
                    .lunarDay(lunarDate.getDay())
                    .leapMonth(lunarDate.isLeapMonth())
                    .dayPillar(dayPillar)
                    .fortuneScore(fortuneScore)
                    .luckyDay(isLuckyDay)
                    .luckyDirection(luckyDirection)
                    .luckyColors(luckyColors)
                    .solarTerm(solarTerm)
                    .briefAdvice(briefAdvice)
                .build();
    }

    private Map<LocalDate, String> calculateSolarTerms(int year, int month) {
        Map<LocalDate, String> terms = new TreeMap<>();
        for (int solarTermYear : List.of(year - 1, year)) {
            for (KoreanCalendar koreanDate : SolarTerm.list(solarTermYear, KoreanCalendar.axis())) {
                PlainDate plainDate = koreanDate.transform(PlainDate.class);
                LocalDate date = TemporalType.LOCAL_DATE.from(plainDate);
                if (date.getYear() == year && date.getMonthValue() == month) {
                    SolarTerm term = koreanDate.get(KoreanCalendar.SOLAR_TERM);
                    terms.put(date, term.getDisplayName(Locale.KOREAN));
                }
            }
        }
        return terms;
    }

    private List<String> formatSolarTerms(Map<LocalDate, String> solarTerms) {
        return solarTerms.entrySet().stream()
                .map(entry -> entry.getKey().getDayOfMonth() + "일 " + entry.getValue())
                .toList();
    }
    /**
     * 일별 운세 점수 계산
     *
     * @param date 계산할 날짜
     * @param dayPillar 해당 날짜의 일주
     * @return 운세 점수 (0-100)
     */
    private int calculateDayFortuneScore(LocalDate date, String dayPillar) {
        return baseScoreFromPillar(dayPillar);
    }
    /**
     * 일진 간지의 오행 균형 기반 기본 점수 (난수 대체, 결정론적).
     *
     * @param dayPillar 일진 간지 (예: "갑자")
     * @return 기본 점수
     */
    private int baseScoreFromPillar(String dayPillar) {
        int stemElem = stemElement(dayPillar.substring(0, 1));
        int branchElem = branchElement(dayPillar.substring(1, 2));
        if (stemElem == branchElem) return 70;           // 간지동기(비화)
        if ((branchElem + 1) % 5 == stemElem) return 82; // 지지생천간(생조)
        if ((stemElem + 1) % 5 == branchElem) return 64; // 천간생지지(설기)
        if ((stemElem + 2) % 5 == branchElem) return 54; // 천간극지지
        return 42;                                       // 지지극천간
    }
    /** 천간 → 오행 index (0목 1화 2토 3금 4수). */
    private int stemElement(String stem) {
        return switch (stem) {
            case "갑", "을" -> 0; case "병", "정" -> 1; case "무", "기" -> 2;
            case "경", "신" -> 3; case "임", "계" -> 4; default -> 2;
        };
    }
    /** 지지 → 오행 index (0목 1화 2토 3금 4수). */
    private int branchElement(String branch) {
        return switch (branch) {
            case "인", "묘" -> 0; case "사", "오" -> 1;
            case "진", "술", "축", "미" -> 2; case "신", "유" -> 3;
            case "해", "자" -> 4; default -> 2;
        };
    }
    /**
     * 길일 여부 판단
     *
     * @param date 판단할 날짜
     * @param fortuneScore 운세 점수
     * @return 길일 여부
     */
    private boolean isLuckyDay(LocalDate date, int fortuneScore) {
        return fortuneScore >= 75;
    }
    /**
     * 월별 테마 생성
     *
     * @param year 년도
     * @param month 월
     * @return 월별 테마
     */
    private String generateMonthlyTheme(int year, int month) {
        /* 월별 테마 생성 */
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
        /* 월별 조언 생성 */
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
    private String generateBriefAdvice(String dayPillar, int fortuneScore) {
        String relation = describeElementRelation(dayPillar);
        if (fortuneScore >= 75) {
            return relation + "로 받쳐 주는 힘이 강한 날입니다. 중요한 일은 우선순위를 정해 추진하고, "
                    + "얻은 도움이나 성과를 주변과 나누면 좋은 흐름을 오래 이어갈 수 있습니다.";
        }
        if (fortuneScore >= 65) {
            return relation + "로 같은 방향의 기운이 모이는 날입니다. 익숙한 일의 완성도를 높이고 "
                    + "협력할 부분을 분명히 하되, 고집으로 다른 의견을 밀어내지 않도록 살피세요.";
        }
        if (fortuneScore >= 60) {
            return relation + "로 에너지가 밖으로 흘러가는 날입니다. 발표·정리·창작처럼 결과물을 만드는 일에 적합하지만, "
                    + "한꺼번에 너무 많은 일을 맡아 체력과 집중력을 소모하지 않도록 범위를 정하세요.";
        }
        if (fortuneScore >= 50) {
            return relation + "로 주도권을 잡기 위해 힘을 써야 하는 날입니다. 작은 과제부터 조건과 책임을 명확히 하며 진행하고, "
                    + "성과를 서두르기보다 비용과 상대의 반응을 함께 확인하세요.";
        }
        return relation + "로 외부 압박이나 일정 충돌을 체감하기 쉬운 날입니다. 중요한 결정은 즉답하지 말고 사실·기한·자원을 다시 확인하며, "
                + "갈등이 예상되는 대화는 감정보다 기록과 기준을 중심으로 진행하세요.";
    }

    private String describeElementRelation(String dayPillar) {
        int stem = stemElement(dayPillar.substring(0, 1));
        int branch = branchElement(dayPillar.substring(1, 2));
        if (stem == branch) return "천간과 지지가 같은 오행인 비화 관계";
        if ((branch + 1) % 5 == stem) return "지지가 천간을 돕는 생조 관계";
        if ((stem + 1) % 5 == branch) return "천간이 지지를 돕는 설기 관계";
        if ((stem + 2) % 5 == branch) return "천간이 지지를 제어하는 극출 관계";
        return "지지가 천간을 제어하는 극입 관계";
    }
    // ==================== 정적 초기화 메서드들 ====================
    /**
     * 천간별 오행 매핑 초기화
     * @param HEAVENLY_STEM_WUXING 천간별 오행 매핑
     * @return 천간별 오행 매핑
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
     * @param LUCKY_DIRECTIONS 길방위 매핑
     * @return 천간별 길방위 매핑
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
     * @param LUCKY_COLORS 길한 색깔 매핑
     * @return 천간별 길한 색깔 매핑
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
    /**
     * 특정 날짜의 간지 정보 조회
     *
     * @param date 조회할 날짜
     * @return 간지 정보
     */
    public GanjiInfo getGanjiInfo(LocalDate date) {
        /* 간지 계산 */
        String dayPillar = ganjiCalculatorService.calculateDayPillar(date);
        /* 천간 */
        String heavenlyStem = dayPillar.substring(0, 1);
        /* 지지 */
        String earthlyBranch = dayPillar.substring(1, 2);
        /* 오행 */
        String wuxing = HEAVENLY_STEM_WUXING.getOrDefault(heavenlyStem, "알 수 없음");
        /* 음양 판별 (간단한 규칙) */
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
        /* 길일 목록 생성 */
        List<LocalDate> luckyDates = new ArrayList<>();
        /* 년월 생성 */
        YearMonth yearMonth = YearMonth.of(year, month);
        /* 첫째날 */
        LocalDate firstDay = yearMonth.atDay(1);
        /* 마지막날 */
        LocalDate lastDay = yearMonth.atEndOfMonth();
        /* 날짜 반복 */
        for (LocalDate date = firstDay; !date.isAfter(lastDay); date = date.plusDays(1)) {
            /* 운세 점수 계산 */
            int fortuneScore = calculateDayFortuneScore(date, ganjiCalculatorService.calculateDayPillar(date));
            /* 길일 여부 판단 */
            if (isLuckyDay(date, fortuneScore)) {
                /* 길일 목록 추가 */
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
        /* 년간 주요 길일 목록 생성 */
        Map<Integer, List<LocalDate>> yearlyLuckyDates = new HashMap<>();
        /* 월별 길일 목록 조회 */
        for (int month = 1; month <= 12; month++) {
            List<LocalDate> monthlyLuckyDates = getLuckyDatesOfMonth(year, month);
            /* 각 월에서 점수가 높은 상위 3일만 선별 */
            monthlyLuckyDates.sort((d1, d2) -> {
                int score1 = calculateDayFortuneScore(d1, ganjiCalculatorService.calculateDayPillar(d1));
                int score2 = calculateDayFortuneScore(d2, ganjiCalculatorService.calculateDayPillar(d2));
                return Integer.compare(score2, score1); /* 내림차순 */
            });
            /* 상위 3일 선별 */
            List<LocalDate> topLuckyDates = monthlyLuckyDates.stream()
                    .limit(3)
                    .toList(); /* 상위 3일 선별 */
            /* 년간 주요 길일 목록 추가 */
            yearlyLuckyDates.put(month, topLuckyDates);
        }
        return yearlyLuckyDates;
    }
}
