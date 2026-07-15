package com.fortune.service;
import com.fortune.dto.TojeongGwa;
import com.fortune.dto.TojeongRequest;
import com.fortune.dto.TojeongResult;
import com.fortune.dto.MonthlyFortune;
import com.fortune.service.LunarSolarConverter.LunarInfo;
import org.springframework.stereotype.Service;
import lombok.extern.slf4j.Slf4j;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.*;
/**
 * 토정비결 서비스 (정통 144괘 산식)
 *
 * <p>정통 토정비결은 <b>상괘(8) × 중괘(6) × 하괘(3) = 144괘</b>로 구성된다.
 * 세 자리(상·중·하)를 결정론적으로 산출하고, 팔괘(건태리진손감간곤)·중괘·하괘의 의미
 * 상수를 조합해 괘사·점수·행운달/주의달을 규칙 기반으로 생성한다. Random 은 사용하지 않는다.</p>
 *
 * <p><b>산식</b> (같은 입력 → 항상 같은 결과)
 * <ul>
 *   <li>상괘 = (그 해 세는나이 + 태세수) mod 8, 0이면 8</li>
 *   <li>중괘 = (음력 생월 일수[평달29/큰달30] + 월건수) mod 6, 0이면 6</li>
 *   <li>하괘 = (음력 생일 + 일진수) mod 3, 0이면 3</li>
 * </ul>
 * 태세수 = 그 해(대상년) 간지의 수, 월건수 = 생월 간지의 수, 일진수 = 생일 간지의 수.
 * 간지→수 조견표는 유파별로 상이하므로, 여기서는 널리 쓰이는
 * <b>천간 순서수(갑1…계10) + 지지 순서수(자1…해12)</b> 합을 채택한다(코드 상수).
 * 간지는 {@link GanjiCalculatorService}(절기 기반 정통 계산)로 구한다.</p>
 *
 * <p>괘사(summary/detailedFortune)는 완역 고전 원문이 아니라 상·중·하 의미 상수를 조합한
 * <b>대표 해석(알고리즘 생성)</b>이다.</p>
 *
 * @author 하진영
 * @version 3.0.0
 * @since 2025-06-24
 */
@Slf4j
@Service
public class TojeongBigyeolService {

    /** 간지 계산기 (Spring 빈 주입 — @Cacheable 등 프록시 유지). */
    private final GanjiCalculatorService ganji;

    public TojeongBigyeolService(GanjiCalculatorService ganji) {
        this.ganji = ganji;
    }

    /** 천간 (0=갑 … 9=계). 순서수 = index+1. */
    private static final String[] STEMS = {"갑","을","병","정","무","기","경","신","임","계"};
    /** 지지 (0=자 … 11=해). 순서수 = index+1. */
    private static final String[] BRANCHES = {"자","축","인","묘","진","사","오","미","신","유","술","해"};

    /** 상괘(팔괘) 이름. index 1..8 = 건태리진손감간곤 (선천 팔괘 순서). */
    private static final String[] UPPER_NAME = {"", "건", "태", "리", "진", "손", "감", "간", "곤"};
    /** 상괘 기호(유니코드 팔괘). */
    private static final String[] UPPER_SYMBOL = {"", "☰", "☱", "☲", "☳", "☴", "☵", "☶", "☷"};
    /** 상괘 오행 (0목 1화 2토 3금 4수). 건금 태금 리화 진목 손목 감수 간토 곤토. */
    private static final int[] UPPER_ELEM = {-1, 3, 3, 1, 0, 0, 4, 2, 2};
    /** 상괘 기본 점수. */
    private static final int[] UPPER_BASE = {0, 85, 75, 78, 72, 68, 55, 62, 66};
    /** 상괘 대표 해석(핵심 구절). */
    private static final String[] UPPER_MEANING = {"",
            "강건한 하늘의 기운이 크게 형통하니 뜻을 세워 나아갈 때입니다.",
            "연못처럼 기쁨과 소통이 따르나 구설과 말조심이 필요합니다.",
            "밝은 불의 기운으로 명예와 문서가 빛나는 해입니다.",
            "우레처럼 움직임이 크니 놀라움 뒤에 성취가 따릅니다.",
            "바람처럼 두루 통하나 흔들림 속에 중심을 지켜야 합니다.",
            "험한 물을 건너는 형국이니 인내로 위기를 넘겨야 합니다.",
            "산처럼 멈추어 때를 기다리면 안정 속에 발전이 있습니다.",
            "대지처럼 포용하고 순응하면 결실이 따르는 해입니다."};

    /** 중괘(1..6) 전개 뉘앙스. */
    private static final String[] MIDDLE_NUANCE = {"",
            "기초가 굳건하여",
            "귀인의 도움을 받아",
            "노력한 만큼",
            "변화의 흐름 속에서",
            "구설과 시비를 다스리면",
            "때를 기다린 끝에"};
    /** 중괘 점수 가감. */
    private static final int[] MIDDLE_DELTA = {0, 6, 8, 2, 0, -4, -2};

    /** 하괘(1..3) 결말 구절. */
    private static final String[] LOWER_CLAUSE = {"",
            "매사가 순조롭게 풀립니다.",
            "굴곡은 있으나 무난히 넘깁니다.",
            "조심하면 큰 탈은 없습니다."};
    /** 하괘 점수 가감. */
    private static final int[] LOWER_DELTA = {0, 6, 0, -6};

    /**
     * 토정비결 계산 메인 메서드
     *
     * @param request 토정비결 요청 정보(생년월일 + 대상년)
     * @return 토정비결 결과
     */
    public TojeongResult calculateTojeong(TojeongRequest request) {
        log.info("📜 토정비결 계산 시작: {}년생 -> {}년 운세",
                request.getBirthYear(), request.getTargetYear());
        try {
            /* 1. 생일(양력 입력 가정) — 존재하지 않는 일자는 그 달 말일로 클램프 */
            int clampedDay = Math.min(request.getBirthDay(),
                    YearMonth.of(request.getBirthYear(), request.getBirthMonth()).lengthOfMonth());
            LocalDate birth = LocalDate.of(request.getBirthYear(), request.getBirthMonth(), clampedDay);

            /* 2. 간지: 태세(대상년) / 생월 월건 / 생일 일진 */
            int taeseoNum = ganjiNumber(ganji.calculateYearPillar(request.getTargetYear()));
            int monthGeonNum = ganjiNumber(ganji.calculateMonthPillar(birth));
            int iljinNum = ganjiNumber(ganji.calculateDayPillar(birth));

            /* 3. 음력 생일/생월 일수 */
            LunarInfo lunar = LunarSolarConverter.solarToLunar(birth);
            int lunarMonthDays = lunarMonthLength(birth, lunar.day());

            /* 4. 상·중·하 괘 (결정론적) */
            int age = request.getTargetYear() - request.getBirthYear() + 1; // 세는나이
            int upper = mod(age + taeseoNum, 8);
            int middle = mod(lunarMonthDays + monthGeonNum, 6);
            int lower = mod(lunar.day() + iljinNum, 3);

            /* 5. 괘 구성 */
            TojeongGwa gwa = buildGwa(upper, middle, lower);

            /* 6. 월별 운세 (월지 오행 vs 상괘 오행 상생상극) */
            List<MonthlyFortune> monthlyFortune =
                    generateMonthlyFortune(upper, gwa.getScore(), request.getTargetYear());

            TojeongResult result = TojeongResult.builder()
                    .targetYear(request.getTargetYear())
                    .gwaNumber(gwa.getNumber())
                    .gwaName(gwa.getName())
                    .gwaSymbol(gwa.getSymbol())
                    .summary(gwa.getSummary())
                    .detailedFortune(gwa.getDetailedFortune())
                    .overallScore(gwa.getScore())
                    .advice(generateAdvice(gwa))
                    .luckyMonths(summarizeMonths(monthlyFortune, 70, true, "뚜렷한 길월 없음"))
                    .cautionMonths(summarizeMonths(monthlyFortune, 50, false, "특별 주의월 없음"))
                    .monthlyFortune(monthlyFortune)
                    .build();
            log.info("✅ 토정비결 계산 완료: {} ({}점, 상{}중{}하{})",
                    gwa.getName(), gwa.getScore(), upper, middle, lower);
            return result;
        } catch (Exception e) {
            log.error("❌ 토정비결 계산 중 오류가 발생했습니다: {}", e.getMessage(), e);
            throw new RuntimeException("토정비결 계산 중 오류가 발생했습니다: " + e.getMessage(), e);
        }
    }

    /** 상·중·하 괘 코드 → 144괘 정보 조립. */
    private TojeongGwa buildGwa(int upper, int middle, int lower) {
        int number = (upper - 1) * 18 + (middle - 1) * 3 + lower; // 1..144
        int score = clamp(UPPER_BASE[upper] + MIDDLE_DELTA[middle] + LOWER_DELTA[lower]);
        String name = UPPER_NAME[upper] + "괘 [" + upper + middle + lower + "]";
        String summary = UPPER_NAME[upper] + "괘: " + UPPER_MEANING[upper];
        String detailed = UPPER_MEANING[upper] + " " + MIDDLE_NUANCE[middle] + " "
                + LOWER_CLAUSE[lower] + " (상·중·하 괘 의미를 조합한 대표 해석)";
        return TojeongGwa.builder()
                .number(number)
                .name(name)
                .symbol(UPPER_SYMBOL[upper])
                .summary(summary)
                .detailedFortune(detailed)
                .score(score)
                .build();
    }

    /** 실제 월별 점수를 기준으로 기준점 이상/미만인 달을 표시한다. */
    private String summarizeMonths(List<MonthlyFortune> months, int threshold,
                                   boolean atLeast, String emptyMessage) {
        String joined = months.stream()
                .filter(month -> atLeast ? month.getScore() >= threshold : month.getScore() < threshold)
                .map(month -> month.getMonth() + "월(" + month.getScore() + "점)")
                .collect(java.util.stream.Collectors.joining(", "));
        return joined.isEmpty() ? emptyMessage : joined;
    }

    /** 간지(2글자) → 수 = 천간 순서수(1..10) + 지지 순서수(1..12). */
    private int ganjiNumber(String pillar) {
        return (indexOf(STEMS, pillar.substring(0, 1)) + 1)
                + (indexOf(BRANCHES, pillar.substring(1, 2)) + 1);
    }

    /** x mod n, 0이면 n (1..n). */
    private int mod(int x, int n) {
        int r = Math.floorMod(x, n);
        return r == 0 ? n : r;
    }

    /** 음력 월의 대소(평달 29 / 큰달 30) 판정. */
    private int lunarMonthLength(LocalDate solarOfLunarDay, int lunarDay) {
        LocalDate firstOfMonth = solarOfLunarDay.minusDays(lunarDay - 1L); // 그 음력월 1일의 양력
        // 1일 + 29일이 다음달 1일이면 작은달(29), 아니면 큰달(30)
        return LunarSolarConverter.solarToLunar(firstOfMonth.plusDays(29)).day() == 1 ? 29 : 30;
    }

    private int indexOf(String[] arr, String v) {
        for (int i = 0; i < arr.length; i++) if (arr[i].equals(v)) return i;
        return 0;
    }

    private int clamp(int s) {
        return Math.max(0, Math.min(100, s));
    }

    /**
     * 점수대별 조언 생성
     *
     * @param gwa 토정비결 괘
     * @return 점수대별 조언
     */
    private String generateAdvice(TojeongGwa gwa) {
        String advice = "올해 당신의 운세는 '" + gwa.getName() + "'입니다. ";
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
     * 월별 상세 운세 생성 (월지 오행 vs 상괘 오행 상생상극 — 결정론적)
     *
     * @param upper 상괘 index(1..8)
     * @param guaScore 괘 종합 점수
     * @param targetYear 대상년
     * @return 월별 상세 운세 리스트
     */
    private List<MonthlyFortune> generateMonthlyFortune(int upper, int guaScore, int targetYear) {
        List<MonthlyFortune> monthlyList = new ArrayList<>();
        int upperElem = UPPER_ELEM[upper];
        for (int month = 1; month <= 12; month++) {
            /* 해당 월의 월지(절기 기준) 오행을 상괘 오행과 대조 */
            LocalDate rep = LocalDate.of(targetYear, month, 15);
            String monthPillar = ganji.calculateMonthPillar(rep);
            int monthElem = branchElement(monthPillar.substring(1, 2));
            int monthScore = clamp(guaScore + relationDelta(upperElem, monthElem));
            monthlyList.add(MonthlyFortune.builder()
                    .month(month)
                    .score(monthScore)
                    .message(generateMonthlyMessage(month, monthScore))
                    .keywords(generateMonthlyKeywords(month, monthScore))
                    .build());
        }
        return monthlyList;
    }

    /** 상괘 오행(u)과 월 오행(m)의 관계 점수 가감. */
    private int relationDelta(int u, int m) {
        if (u == m) return 8;              // 비화
        if (gen(m) == u) return 12;        // 월이 상괘를 생 (생입)
        if (gen(u) == m) return -6;        // 상괘가 월을 생 (설기)
        if (overcome(m) == u) return -12;  // 월이 상괘를 극 (극입)
        return 4;                          // 상괘가 월을 극 (재/극출)
    }

    /** 오행 상생: 목0→화1→토2→금3→수4→목0. */
    private int gen(int e) { return (e + 1) % 5; }
    /** 오행 상극: 목0→토2→수4→화1→금3→목0. */
    private int overcome(int e) { return (e + 2) % 5; }

    /** 지지 → 오행 index (0목 1화 2토 3금 4수). */
    private int branchElement(String branch) {
        return switch (branch) {
            case "인", "묘" -> 0;
            case "사", "오" -> 1;
            case "진", "술", "축", "미" -> 2;
            case "신", "유" -> 3;
            case "해", "자" -> 4;
            default -> 2;
        };
    }

    /**
     * 월별 키워드 생성 (계절 + 점수대)
     *
     * @param month 월 번호 (1-12)
     * @param score 월별 점수 (0-100)
     * @return 월별 키워드 리스트
     */
    private List<String> generateMonthlyKeywords(int month, int score) {
        List<String> keywords = new ArrayList<>();
        switch ((month - 1) / 3) {
            case 0 -> keywords.addAll(Arrays.asList("새시작", "희망", "성장")); // 봄
            case 1 -> keywords.addAll(Arrays.asList("활동", "에너지", "도전")); // 여름
            case 2 -> keywords.addAll(Arrays.asList("수확", "성과", "감사")); // 가을
            case 3 -> keywords.addAll(Arrays.asList("정리", "휴식", "계획")); // 겨울
        }
        if (score >= 80) {
            keywords.addAll(Arrays.asList("행운", "성공", "기회"));
        } else if (score >= 60) {
            keywords.addAll(Arrays.asList("안정", "발전", "조화"));
        } else if (score >= 40) {
            keywords.addAll(Arrays.asList("인내", "노력", "꾸준함"));
        } else {
            keywords.addAll(Arrays.asList("주의", "신중", "극복"));
        }
        return keywords;
    }

    /**
     * 월별 메시지 생성
     *
     * @param month 월 번호 (1-12)
     * @param score 월별 점수 (0-100)
     * @return 월별 운세 메시지
     */
    private String generateMonthlyMessage(int month, int score) {
        String monthName = month + "월";
        if (score >= 80) {
            return monthName + "은 매우 좋은 운세입니다. 새로운 도전을 해보세요.";
        } else if (score >= 60) {
            return monthName + "은 안정적인 운세입니다. 계획을 차근차근 실행하세요.";
        } else if (score >= 40) {
            return monthName + "은 평범한 운세입니다. 꾸준한 노력이 필요합니다.";
        } else {
            return monthName + "은 조심스러운 운세입니다. 신중하게 행동하세요.";
        }
    }
} // END TojeongBigyeolService
