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
        String pace;
        if (gwa.getScore() >= 85) {
            pace = "전체 흐름이 강하게 열려 있으므로 오래 준비한 목표 한두 가지를 정해 주도적으로 추진하기 좋습니다. "
                    + "다만 기회가 많아 보여도 사람·시간·비용의 한도를 먼저 정하고, 성과가 생기면 기록과 관계 관리로 다음 기회까지 연결하세요.";
        } else if (gwa.getScore() >= 70) {
            pace = "안정적인 성장 가능성이 있으므로 새로운 시도와 기존 기반의 보강을 함께 가져가는 편이 좋습니다. "
                    + "한 번에 범위를 넓히기보다 월별 우선순위를 정하고, 완료한 일의 품질과 협력 관계를 차근차근 쌓아 가세요.";
        } else if (gwa.getScore() >= 55) {
            pace = "큰 승부보다 꾸준한 실행과 균형 관리가 결과를 만드는 해입니다. "
                    + "진행 중인 일의 마감 기준을 명확히 하고, 중요한 결정은 기대 효과뿐 아니라 비용·일정·되돌릴 방법까지 확인한 뒤 선택하세요.";
        } else {
            pace = "속도를 높이기보다 손실을 줄이고 기반을 정비하는 태도가 필요한 해입니다. "
                    + "새로운 부담은 작게 시험한 뒤 확대하고, 계약·재정·건강처럼 영향이 큰 문제는 충분한 자료와 관련 전문가의 의견을 함께 확인하세요.";
        }
        return "올해의 괘는 '" + gwa.getName() + "'이며 종합 점수는 " + gwa.getScore() + "점입니다. "
                + gwa.getSummary() + " " + pace
                + " 분기 초에는 목표와 가용 자원을 다시 적고, 매달 말에는 실제 성과·지출·컨디션·관계의 변화를 점검해 다음 달 계획을 조정하세요. "
                + "점수가 높은 달은 중요한 실행과 협의를 배치하는 참고 시기로, 낮은 달은 일정 여유와 검토 단계를 늘리는 시기로 활용하면 한 해의 흐름을 보다 현실적으로 관리할 수 있습니다. "
                + "운세는 선택을 대신하는 결론이 아니라 계획을 돌아보게 하는 문화적 참고 정보이므로, 실제 결정에서는 현재 상황과 객관적인 근거를 우선하세요.";
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
            int delta = relationDelta(upperElem, monthElem);
            int monthScore = clamp(guaScore + delta);
            monthlyList.add(MonthlyFortune.builder()
                    .month(month)
                    .score(monthScore)
                    .message(generateMonthlyMessage(
                            month, guaScore, monthScore, upperElem, monthElem, delta))
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
     * @param guaScore 연간 괘 점수
     * @param score 월별 점수 (0-100)
     * @return 월별 운세 메시지
     */
    private String generateMonthlyMessage(int month, int guaScore, int score,
                                          int upperElem, int monthElem, int delta) {
        String relation = elementRelationDescription(upperElem, monthElem);
        String flow;
        if (score >= 80) {
            flow = "추진력과 주변의 호응을 함께 얻기 쉬운 구간입니다. 오래 준비한 제안이나 중요한 협의는 목표·예산·완료 조건을 명확히 한 뒤 실행하고, 좋은 반응은 문서와 후속 일정으로 연결하세요.";
        } else if (score >= 65) {
            flow = "안정적인 진전이 기대되는 구간입니다. 새 일을 지나치게 늘리기보다 핵심 과제를 먼저 완성하고, 관계에서는 약속한 일정과 역할을 지켜 신뢰를 쌓는 데 집중하세요.";
        } else if (score >= 50) {
            flow = "성과와 부담이 함께 나타날 수 있어 우선순위 조정이 중요합니다. 해야 할 일과 미뤄도 되는 일을 구분하고, 재정·일정·체력에 여유분을 남기면 작은 변수에도 흐름을 유지할 수 있습니다.";
        } else {
            flow = "외부 변수와 피로가 겹치기 쉬우므로 확장보다 점검과 회복을 앞세우는 편이 좋습니다. 큰 계약이나 지출은 조건을 다시 확인하고, 갈등이 생기면 즉답보다 사실관계와 자신의 감정을 정리한 뒤 대화하세요.";
        }
        return month + "월은 " + seasonalFocus(month) + " "
                + "연간 괘 점수 " + guaScore + "점에 상괘의 " + elementName(upperElem)
                + " 기운과 이달의 " + elementName(monthElem) + " 기운 관계를 반영해 "
                + signed(delta) + "점이 가감되었고, 월 점수는 " + score + "점입니다. "
                + relation + " " + flow + " "
                + "월 초에는 가장 중요한 목표 하나와 피해야 할 위험 하나를 정하고, 중순에는 진행 상황과 지출·컨디션을 확인하며, 월말에는 결과를 기록해 다음 달 계획에 반영해 보세요.";
    }

    private String seasonalFocus(int month) {
        return switch ((month - 1) / 3) {
            case 0 -> "새로운 계획의 씨앗을 고르고 생활 리듬을 세우는 시기입니다.";
            case 1 -> "활동 범위가 넓어지는 만큼 체력과 일정의 균형을 함께 살필 시기입니다.";
            case 2 -> "진행한 일의 성과를 확인하고 관계와 자원을 정돈할 시기입니다.";
            default -> "한 해의 결과를 정리하고 다음 선택을 준비할 시기입니다.";
        };
    }

    private String elementRelationDescription(int upperElem, int monthElem) {
        if (upperElem == monthElem) {
            return "두 기운이 같은 방향으로 겹치는 비화 관계라 익숙한 강점과 기존 기반을 활용하기 좋지만, 한 방식만 고집하지 않도록 주변 의견도 확인해야 합니다.";
        }
        if (gen(monthElem) == upperElem) {
            return "이달의 기운이 상괘를 북돋는 생입 관계라 도움과 자원이 들어오기 쉬우므로, 제안과 협력의 조건을 구체화하면 흐름을 실질적인 성과로 바꾸기 좋습니다.";
        }
        if (gen(upperElem) == monthElem) {
            return "상괘의 기운이 이달로 빠져나가는 설기 관계라 활동은 많아질 수 있으나 소모도 커지므로, 일의 범위와 휴식 시간을 함께 관리해야 합니다.";
        }
        if (overcome(monthElem) == upperElem) {
            return "이달의 기운이 상괘를 누르는 극입 관계라 예상 밖의 제약이나 지연에 대비해 검토 단계와 일정 여유를 두는 편이 안전합니다.";
        }
        return "상괘가 이달의 기운을 다스리는 극출 관계라 노력으로 결과를 만들 수 있지만, 통제에 힘을 너무 쓰지 않도록 역할을 나누고 비용 대비 효과를 점검해야 합니다.";
    }

    private String elementName(int element) {
        return switch (element) {
            case 0 -> "목(木)";
            case 1 -> "화(火)";
            case 2 -> "토(土)";
            case 3 -> "금(金)";
            case 4 -> "수(水)";
            default -> "중립";
        };
    }

    private String signed(int value) {
        return value >= 0 ? "+" + value : Integer.toString(value);
    }
} // END TojeongBigyeolService
