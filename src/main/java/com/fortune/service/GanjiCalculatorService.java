package com.fortune.service;
import com.fortune.dto.SajuRequest;
import com.fortune.dto.SajuResult;
import com.nlf.calendar.EightChar;
import com.nlf.calendar.Solar;
import com.nlf.calendar.eightchar.DaYun;
import com.nlf.calendar.eightchar.Yun;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
/**
 * 간지 계산 서비스 — 사주 엔진 {@code lunar-java}(cn.6tail:lunar) 위임.
 *
 * <p>4주(년/월/일/시)·십신·지장간·12운성·대운·절기를 검증된 lunar-java 라이브러리로 산출한다.
 * 손으로 짠 천문/만세력 산술 대신 라이브러리를 신뢰원으로 쓴다. 음력 입력은 한국 정합
 * {@link LunarSolarConverter}(Time4J)로 양력 변환 후 lunar-java 에 투입한다.</p>
 *
 * <p>결과는 한글 간지("신유")로 매핑해 기존 DTO/서비스 계약을 유지한다.
 * 검증: 양력 1981-03-20 01:59(경도보정 -30분→01:29) 남 → 년 신유·월 신묘·일 정유·시 신축,
 * 십신 편재·12운성 장생, 대운 역행·대운수 5 (전문 만세력 '척척사주'·lunar-java 일치).</p>
 *
 * @author 하진영
 * @version 4.0.0
 * @since 2025-06-24
 */
@Slf4j
@Service
public class GanjiCalculatorService {
    private static final String[] STEMS = {"갑","을","병","정","무","기","경","신","임","계"};
    private static final String[] STEMS_HJ = {"甲","乙","丙","丁","戊","己","庚","辛","壬","癸"};
    private static final String[] BRANCHES = {"자","축","인","묘","진","사","오","미","신","유","술","해"};
    private static final String[] BRANCHES_HJ = {"子","丑","寅","卯","辰","巳","午","未","申","酉","戌","亥"};
    private static final String[] TWELVE_STAGES = {"장생","목욕","관대","건록","제왕","쇠","병","사","묘","절","태","양"};
    /** 지장간(본기=마지막). 공개 십신 헬퍼(sipsinOf/branchMainSipsin)용. */
    private static final int[][] HIDDEN_STEMS = {
            {8,9}, {9,7,5}, {4,2,0}, {0,1}, {1,9,4}, {4,6,2},
            {2,5,3}, {3,1,5}, {4,8,6}, {6,7}, {7,3,4}, {4,0,8}
    };
    /** 일간별 12운성 장생지(대운 12운성 산출용). 갑해 을오 병인 정유 무인 기유 경사 신자 임신 계묘 */
    private static final int[] JANGSAENG_BRANCH = {11, 6, 2, 9, 2, 9, 5, 0, 8, 3};
    /** 한국(동경135°) 진태양시 경도 보정(분). */
    private static final int KOREA_TIME_CORRECTION_MIN = 30;

    // lunar-java 한자(간체) → 한글 매핑
    private static final Map<Character, String> HJ_STEM = buildMap(STEMS_HJ, STEMS);
    private static final Map<Character, String> HJ_BRANCH = buildMap(BRANCHES_HJ, BRANCHES);
    /** lunar-java 십신(간체) → 한글. 七杀=편관, 日主=일간(본원). */
    private static final Map<String, String> SIPSIN_KR = Map.ofEntries(
            Map.entry("比肩","비견"), Map.entry("劫财","겁재"), Map.entry("食神","식신"),
            Map.entry("伤官","상관"), Map.entry("偏财","편재"), Map.entry("正财","정재"),
            Map.entry("七杀","편관"), Map.entry("正官","정관"), Map.entry("偏印","편인"),
            Map.entry("正印","정인"), Map.entry("日主","일간(본원)"));
    /** lunar-java 12운성(地势, 간체) → 한글. 临官=건록. */
    private static final Map<String, String> DISHI_KR = Map.ofEntries(
            Map.entry("长生","장생"), Map.entry("沐浴","목욕"), Map.entry("冠带","관대"),
            Map.entry("临官","건록"), Map.entry("帝旺","제왕"), Map.entry("衰","쇠"),
            Map.entry("病","병"), Map.entry("死","사"), Map.entry("墓","묘"),
            Map.entry("绝","절"), Map.entry("胎","태"), Map.entry("养","양"));

    private static Map<Character, String> buildMap(String[] hj, String[] kr) {
        java.util.HashMap<Character, String> m = new java.util.HashMap<>();
        for (int i = 0; i < hj.length; i++) m.put(hj[i].charAt(0), kr[i]);
        return m;
    }

    /**
     * 사주팔자 계산 메인 메서드 (lunar-java 위임).
     */
    public SajuResult calculateSaju(SajuRequest request) {
        log.info("🔮 사주팔자 계산 시작: {}년 {}월 {}일 {}시 {}분",
                request.getBirthYear(), request.getBirthMonth(), request.getBirthDay(),
                request.getBirthHour(), request.getBirthMinute());
        try {
            LocalDateTime birthKst = toSolarDateTime(request);
            // 진태양시 보정(-30분): 한국 경도. lunar-java 는 입력을 지역시로 취급한다.
            LocalDateTime adj = birthKst.minusMinutes(KOREA_TIME_CORRECTION_MIN);
            EightChar ec = eightChar(adj);

            String yearPillar = pillarKr(ec.getYear());
            String monthPillar = pillarKr(ec.getMonth());
            String dayPillar = pillarKr(ec.getDay());
            String timePillar = pillarKr(ec.getTime());
            String dayMaster = HJ_STEM.get(ec.getDayGan().charAt(0));

            SajuResult.Pillar yearDetail = pillar(ec.getYearGan(), ec.getYearZhi(),
                    ec.getYearShiShenGan(), ec.getYearShiShenZhi(), ec.getYearDiShi(), ec.getYearHideGan(), false);
            SajuResult.Pillar monthDetail = pillar(ec.getMonthGan(), ec.getMonthZhi(),
                    ec.getMonthShiShenGan(), ec.getMonthShiShenZhi(), ec.getMonthDiShi(), ec.getMonthHideGan(), false);
            SajuResult.Pillar dayDetail = pillar(ec.getDayGan(), ec.getDayZhi(),
                    ec.getDayShiShenGan(), ec.getDayShiShenZhi(), ec.getDayDiShi(), ec.getDayHideGan(), true);
            SajuResult.Pillar timeDetail = pillar(ec.getTimeGan(), ec.getTimeZhi(),
                    ec.getTimeShiShenGan(), ec.getTimeShiShenZhi(), ec.getTimeDiShi(), ec.getTimeHideGan(), false);

            List<SajuResult.DaeUn> daeun = new ArrayList<>();
            boolean forward = true;
            int daeunNo = 0;
            Yun yun = ec.getYun("M".equalsIgnoreCase(request.getGender()) ? 1 : 0);
            forward = yun.isForward();
            for (DaYun dy : yun.getDaYun()) {
                String gz = dy.getGanZhi();
                if (gz == null || gz.isEmpty()) continue; // index0 = 출생~기운 이전 구간
                if (daeunNo == 0) daeunNo = dy.getStartAge();
                daeun.add(daeUn(dy.getStartAge(), gz, dayMaster));
            }

            SajuResult.WuxingAnalysis wuxingAnalysis = analyzeWuxing(yearPillar, monthPillar, dayPillar, timePillar);

            SajuResult result = SajuResult.builder()
                    .yearPillar(yearPillar).monthPillar(monthPillar)
                    .dayPillar(dayPillar).timePillar(timePillar)
                    .dayMaster(dayMaster)
                    .birthDate(birthKst.toLocalDate())
                    .adjustedDateTime(adj)
                    .calendarType(request.getCalendarType())
                    .gender(request.getGender())
                    .wuxingAnalysis(wuxingAnalysis)
                    .yearDetail(yearDetail).monthDetail(monthDetail)
                    .dayDetail(dayDetail).timeDetail(timeDetail)
                    .daeun(daeun).daeunForward(forward).daeunNumber(daeunNo)
                    .fortuneSummary(generateFortuneSummary(dayMaster))
                    .build();
            log.info("✅ 사주팔자 계산 완료: {} (대운 {}, 대운수 {})",
                    result.getFormattedSaju(), forward ? "순행" : "역행", daeunNo);
            return result;
        } catch (Exception e) {
            log.error("❌ 사주팔자 계산 중 오류 발생: {}", e.getMessage(), e);
            throw new RuntimeException("사주팔자 계산 중 오류가 발생했습니다: " + e.getMessage(), e);
        }
    }

    /**
     * 완전한 사주 계산 (테스트/편의용)
     */
    public SajuResult calculateCompleteSaju(int year, int month, int day, int hour, int minute, String gender, String calendarType) {
        return calculateSaju(SajuRequest.builder()
                .birthYear(year).birthMonth(month).birthDay(day)
                .birthHour(hour).birthMinute(minute)
                .gender(gender).calendarType(calendarType)
                .build());
    }

    /** 입력 → 양력 LocalDateTime (음력이면 한국 정합 변환). */
    private LocalDateTime toSolarDateTime(SajuRequest request) {
        LocalDate date = "LUNAR".equals(request.getCalendarType())
                ? LunarSolarConverter.lunarToSolar(request.getBirthYear(), request.getBirthMonth(), request.getBirthDay(), false)
                : LocalDate.of(request.getBirthYear(), request.getBirthMonth(), request.getBirthDay());
        return LocalDateTime.of(date, java.time.LocalTime.of(request.getBirthHour(), request.getBirthMinute()));
    }

    /** LocalDateTime → lunar-java EightChar. */
    private EightChar eightChar(LocalDateTime dt) {
        Solar solar = new Solar(dt.getYear(), dt.getMonthValue(), dt.getDayOfMonth(),
                dt.getHour(), dt.getMinute(), 0);
        return solar.getLunar().getEightChar();
    }

    /** lunar-java 간지(한자 2자, 예 "辛酉") → 한글("신유"). */
    private String pillarKr(String ganZhiHanja) {
        return HJ_STEM.get(ganZhiHanja.charAt(0)) + HJ_BRANCH.get(ganZhiHanja.charAt(1));
    }

    /** lunar-java 값으로 기둥 상세 구성. shiShenZhi 는 본기(주기)가 index0. */
    private SajuResult.Pillar pillar(String ganHj, String zhiHj, String shiShenGan,
                                     List<String> shiShenZhi, String diShi, List<String> hideGan, boolean isDay) {
        List<String> hidden = new ArrayList<>();
        for (String g : hideGan) hidden.add(HJ_STEM.get(g.charAt(0)));
        List<String> hiddenSipsin = new ArrayList<>();
        for (String s : shiShenZhi) hiddenSipsin.add(SIPSIN_KR.getOrDefault(s, s));
        char stemHj = ganHj.charAt(0), branchHj = zhiHj.charAt(0);
        return SajuResult.Pillar.builder()
                .stem(HJ_STEM.get(stemHj)).branch(HJ_BRANCH.get(branchHj))
                .stemHanja(ganHj).branchHanja(zhiHj)
                .stemSipsin(isDay ? "일간(본원)" : SIPSIN_KR.getOrDefault(shiShenGan, shiShenGan))
                .branchSipsin(shiShenZhi.isEmpty() ? null : SIPSIN_KR.getOrDefault(shiShenZhi.get(0), shiShenZhi.get(0)))
                .twelveStage(DISHI_KR.getOrDefault(diShi, diShi))
                .hiddenStems(hidden).hiddenStemsSipsin(hiddenSipsin)
                .build();
    }

    /** 대운 한 주기: lunar-java DaYun + 일간 대비 십신·12운성 파생. */
    private SajuResult.DaeUn daeUn(int age, String ganZhiHj, String dayStemKr) {
        String stemKr = HJ_STEM.get(ganZhiHj.charAt(0));
        String branchKr = HJ_BRANCH.get(ganZhiHj.charAt(1));
        return SajuResult.DaeUn.builder()
                .age(age)
                .ganji(stemKr + branchKr)
                .ganjiHanja(ganZhiHj)
                .stemSipsin(sipsinOf(dayStemKr, stemKr))
                .branchSipsin(branchMainSipsin(dayStemKr, branchKr))
                .twelveStage(twelveStage(indexOf(STEMS, dayStemKr), indexOf(BRANCHES, branchKr)))
                .build();
    }

    // ── 하위호환 공개 헬퍼 (다른 서비스가 사용) ─────────────────────

    /** 연주(입춘 무시, 연도만 아는 하위호환용 — 토정비결 태세 등). */
    @Cacheable(value = "year-pillar", key = "#year")
    public String calculateYearPillar(Integer year) {
        int s = Math.floorMod(year - 1984, 10);
        int b = Math.floorMod(year - 1984, 12);
        return STEMS[s] + BRANCHES[b];
    }

    /**
     * 월주 — lunar-java (해당일 정오 기준).
     * @param yearPillar 하위호환용 파라미터. 월간은 lunar-java 가 연간을 자체 산출하므로 <b>사용하지 않는다</b>.
     */
    @SuppressWarnings("unused")
    public String calculateMonthPillar(LocalDate date, String yearPillar) {
        return pillarKr(eightChar(date.atTime(12, 0)).getMonth());
    }

    /** 일주 — lunar-java (해당일 정오 기준, 자시 경계 회피). */
    @Cacheable(value = "day-pillar", key = "#date")
    public String calculateDayPillar(LocalDate date) {
        return pillarKr(eightChar(date.atTime(12, 0)).getDay());
    }

    /** 시주 — 오서둔(시두법). 일간·시각으로 결정. */
    public String calculateTimePillar(Integer hour, String dayPillar) {
        if (hour == null) throw new IllegalArgumentException("출생 시간은 필수입니다");
        int hourBranchIdx = ((hour + 1) / 2) % 12; // 자시=23~01
        int dayStemIdx = indexOf(STEMS, dayPillar.substring(0, 1));
        int hourStemIdx = ((dayStemIdx % 5) * 2 % 10 + hourBranchIdx) % 10;
        return STEMS[hourStemIdx] + BRANCHES[hourBranchIdx];
    }

    /** 십신(공개): 일간(한글) 대비 대상 천간(한글). */
    public String sipsinOf(String dayStemKr, String targetStemKr) {
        return sipsin(indexOf(STEMS, dayStemKr), indexOf(STEMS, targetStemKr));
    }

    /** 지지 본기 기준 십신(공개): 일간(한글) 대비 지지(한글)의 본기 천간. */
    public String branchMainSipsin(String dayStemKr, String branchKr) {
        int b = indexOf(BRANCHES, branchKr);
        int bonki = HIDDEN_STEMS[b][HIDDEN_STEMS[b].length - 1];
        return sipsin(indexOf(STEMS, dayStemKr), bonki);
    }

    /** 십신 규칙: 오행 생극 + 음양. (lunar-java 결과와 동일 규칙, 날짜 불요 헬퍼용) */
    private String sipsin(int dayStemIdx, int targetStemIdx) {
        int dayElem = dayStemIdx / 2, tElem = targetStemIdx / 2;
        boolean samePol = (dayStemIdx % 2) == (targetStemIdx % 2);
        int rel = Math.floorMod(tElem - dayElem, 5);
        return switch (rel) {
            case 0 -> samePol ? "비견" : "겁재";
            case 1 -> samePol ? "식신" : "상관";
            case 2 -> samePol ? "편재" : "정재";
            case 3 -> samePol ? "편관" : "정관";
            default -> samePol ? "편인" : "정인";
        };
    }

    /** 12운성: 일간의 지지 대비 포태. 양간 순행/음간 역행. (대운 파생용) */
    private String twelveStage(int dayStemIdx, int branchIdx) {
        int jangsaeng = JANGSAENG_BRANCH[dayStemIdx];
        boolean yang = dayStemIdx % 2 == 0;
        int stageIdx = yang ? Math.floorMod(branchIdx - jangsaeng, 12)
                            : Math.floorMod(jangsaeng - branchIdx, 12);
        return TWELVE_STAGES[stageIdx];
    }

    private int indexOf(String[] arr, String v) {
        for (int i = 0; i < arr.length; i++) if (arr[i].equals(v)) return i;
        return -1;
    }

    // ── 오행 분석 (천간·지지 본기 기준) ─────────────────────────
    private SajuResult.WuxingAnalysis analyzeWuxing(String yearPillar, String monthPillar, String dayPillar, String timePillar) {
        int[] wuxingCount = new int[5];
        for (String pillar : new String[]{yearPillar, monthPillar, dayPillar, timePillar}) {
            wuxingCount[getStemWuxing(pillar.substring(0, 1))]++;
            wuxingCount[getBranchWuxing(pillar.substring(1, 2))]++;
        }
        int maxIndex = 0, minIndex = 0;
        for (int i = 1; i < 5; i++) {
            if (wuxingCount[i] > wuxingCount[maxIndex]) maxIndex = i;
            if (wuxingCount[i] < wuxingCount[minIndex]) minIndex = i;
        }
        String[] wuxingNames = {"목", "화", "토", "금", "수"};
        double avg = java.util.Arrays.stream(wuxingCount).average().orElse(0);
        double variance = java.util.Arrays.stream(wuxingCount)
                .mapToDouble(x -> Math.pow(x - avg, 2)).average().orElse(0);
        int balance = Math.max(0, 100 - (int) (variance * 10));
        return SajuResult.WuxingAnalysis.builder()
                .woodCount(wuxingCount[0]).fireCount(wuxingCount[1]).earthCount(wuxingCount[2])
                .metalCount(wuxingCount[3]).waterCount(wuxingCount[4])
                .strongestElement(wuxingNames[maxIndex]).weakestElement(wuxingNames[minIndex])
                .balance(balance).build();
    }
    private int getStemWuxing(String stem) {
        return switch (stem) {
            case "갑", "을" -> 0; case "병", "정" -> 1; case "무", "기" -> 2;
            case "경", "신" -> 3; case "임", "계" -> 4; default -> 0;
        };
    }
    private int getBranchWuxing(String branch) {
        return switch (branch) {
            case "인", "묘" -> 0; case "사", "오" -> 1;
            case "진", "술", "축", "미" -> 2; case "신", "유" -> 3;
            case "해", "자" -> 4; default -> 0;
        };
    }
    private String generateFortuneSummary(String dayMaster) {
        return switch (dayMaster) {
            case "갑" -> "큰 나무처럼 웅장하고 정직한 성품의 소유자입니다.";
            case "을" -> "꽃처럼 아름답고 섬세한 감성을 지닌 분입니다.";
            case "병" -> "태양처럼 밝고 활동적인 에너지를 가진 분입니다.";
            case "정" -> "촛불처럼 따뜻하고 정성스러운 마음을 지닌 분입니다.";
            case "무" -> "산처럼 든든하고 포용력이 큰 분입니다.";
            case "기" -> "대지처럼 너그럽고 실용적인 지혜를 가진 분입니다.";
            case "경" -> "쇠처럼 강인하고 원칙을 중시하는 분입니다.";
            case "신" -> "보석처럼 예리하고 세련된 감각을 지닌 분입니다.";
            case "임" -> "바다처럼 깊고 포용력이 넓은 분입니다.";
            case "계" -> "이슬처럼 순수하고 지혜로운 분입니다.";
            default -> "균형 잡힌 성품을 지닌 분입니다.";
        };
    }
}
