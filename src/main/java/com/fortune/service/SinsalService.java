package com.fortune.service;
import com.fortune.dto.SajuResult;
import com.fortune.dto.SinsalInfo;
import org.springframework.stereotype.Service;
import lombok.extern.slf4j.Slf4j;
import java.time.LocalDate;
import java.util.*;
/**
 * 신살(神殺) 계산 서비스
 *
 * <p>사주팔자에서 길신과 흉신을 계산하는 서비스입니다.</p>
 *
 * @author 하진영
 * @version 2.5.0
 * @since 2025-06-24
 */
@Slf4j
@Service
public class SinsalService {
    /** 간지 계산기 (Spring 빈 주입 — @Cacheable 등 프록시 유지). */
    private final GanjiCalculatorService ganji;

    public SinsalService(GanjiCalculatorService ganji) {
        this.ganji = ganji;
    }

    /**
     * 신살 설명 매핑
     */
    private static final Map<String, String> SINSAL_DESCRIPTIONS = new HashMap<>();

    /* ── 지지 삼합국 기반 신살 조견표 ────────────────────────────────
     * 삼합국 index: 0=신자진(水), 1=해묘미(木), 2=인오술(火), 3=사유축(金).
     * 일지(또는 년지)의 삼합국으로 해당 신살의 성립 지지를 결정한다.
     * (통용되는 신살 정위 조견표) */
    private static final String[] DOHWA   = {"유", "자", "묘", "오"}; // 도화(년살)
    private static final String[] YEOKMA  = {"인", "사", "신", "해"}; // 역마
    private static final String[] HWAGAE  = {"진", "미", "술", "축"}; // 화개
    private static final String[] GEOPSAL = {"사", "신", "해", "인"}; // 겁살
    private static final String[] MANGSIN = {"해", "인", "사", "신"}; // 망신
    private static final String[] JAESAL  = {"오", "유", "자", "묘"}; // 재살(수옥살)

    /* ── 일간(천간) 기반 신살 조견표 ─────────────────────────────── */
    /** 천을귀인: 일간 → 성립 지지 2개. (갑무경-축미 / 을기-자신 / 병정-해유 / 임계-사묘 / 신-인오) */
    private static final Map<String, String[]> CHEONEUL = Map.ofEntries(
            Map.entry("갑", new String[]{"축", "미"}), Map.entry("무", new String[]{"축", "미"}),
            Map.entry("경", new String[]{"축", "미"}), Map.entry("을", new String[]{"자", "신"}),
            Map.entry("기", new String[]{"자", "신"}), Map.entry("병", new String[]{"해", "유"}),
            Map.entry("정", new String[]{"해", "유"}), Map.entry("임", new String[]{"사", "묘"}),
            Map.entry("계", new String[]{"사", "묘"}), Map.entry("신", new String[]{"인", "오"})
    );
    /** 문창귀인: 일간 → 성립 지지. */
    private static final Map<String, String> MUNCHANG = Map.ofEntries(
            Map.entry("갑", "사"), Map.entry("을", "오"), Map.entry("병", "신"),
            Map.entry("정", "유"), Map.entry("무", "신"), Map.entry("기", "유"),
            Map.entry("경", "해"), Map.entry("신", "자"), Map.entry("임", "인"), Map.entry("계", "묘")
    );

    /**
     * 정적 초기화 메서드들
     */
    static {
        initializeSinsalDescriptions();
    }
    /**
     * 일일 신살 계산
     * SQL: SELECT * FROM sinsals WHERE day_master = ? AND date = ?;
     * @param targetDate 날짜
     * @param saju 사주 결과
     * @return 신살 정보 리스트
     */
    public List<SinsalInfo> calculateDailySinsals(LocalDate targetDate, SajuResult saju) {
        /* 신살은 사주 일간(천간)·일지(지지)를 기준으로, 해당일 일진의 지지가
         * 조견표상 성립 지지와 일치할 때 성립한다. 근거 없는 해시 판정은 제거. */
        List<SinsalInfo> sinsals = new ArrayList<>();
        try {
            String dayStem = saju.getDayMaster();
            String dayBranch = (saju.getDayPillar() != null && saju.getDayPillar().length() >= 2)
                    ? saju.getDayPillar().substring(1, 2) : null;
            String targetBranch = ganji.calculateDayPillar(targetDate).substring(1, 2);

            /* 1. 지지 삼합국 기반 신살 (일지 기준) */
            if (dayBranch != null) {
                int g = branchGroup(dayBranch);
                if (g >= 0) {
                    addIf(sinsals, "역마", true, 12, targetBranch, YEOKMA[g]);
                    addIf(sinsals, "화개", true, 10, targetBranch, HWAGAE[g]);
                    addIf(sinsals, "도화", false, 12, targetBranch, DOHWA[g]);
                    addIf(sinsals, "겁살", false, 12, targetBranch, GEOPSAL[g]);
                    addIf(sinsals, "망신", false, 10, targetBranch, MANGSIN[g]);
                    addIf(sinsals, "재살", false, 11, targetBranch, JAESAL[g]);
                }
            }
            /* 2. 일간(천간) 기반 길신 */
            String[] cheoneul = CHEONEUL.get(dayStem);
            if (cheoneul != null) {
                addIf(sinsals, "천을귀인", true, 18, targetBranch, cheoneul);
            }
            addIf(sinsals, "문창귀인", true, 15, targetBranch, MUNCHANG.get(dayStem));

            /* 3. 특수 신살 계산 (날짜 기반, 결정론적) */
            addDateBasedSinsals(sinsals, targetDate);
            log.info("✅ 신살 계산 완료: {} 개 발견", sinsals.size());
            return sinsals;
        } catch (Exception e) {
            log.error("❌ 신살 계산 중 오류 발생: {}", e.getMessage(), e);
            return new ArrayList<>();
        }
    }
    /**
     * 일지의 삼합국 index 반환. 0=신자진 1=해묘미 2=인오술 3=사유축, 미매칭 -1.
     */
    private int branchGroup(String branch) {
        return switch (branch) {
            case "신", "자", "진" -> 0;
            case "해", "묘", "미" -> 1;
            case "인", "오", "술" -> 2;
            case "사", "유", "축" -> 3;
            default -> -1;
        };
    }
    /**
     * 대상일 지지가 성립 지지 중 하나면 신살 추가.
     * @param targets 가변 성립 지지 목록(1개 이상), null 이면 미추가
     */
    private void addIf(List<SinsalInfo> sinsals, String name, boolean lucky, int influence,
                       String targetBranch, String... targets) {
        if (targets == null || targetBranch == null) return;
        for (String t : targets) {
            if (targetBranch.equals(t)) {
                sinsals.add(new SinsalInfo(
                        name,
                        SINSAL_DESCRIPTIONS.getOrDefault(name, name + " 신살"),
                        lucky,
                        influence));
                return;
            }
        }
    }
    /**
     * 날짜 기반 특수 신살 추가
     * SQL: SELECT * FROM sinsals WHERE date = ?;
     * @param sinsals 신살 정보 리스트
     * @param targetDate 날짜
     */
    private void addDateBasedSinsals(List<SinsalInfo> sinsals, LocalDate targetDate) {
        /* 월의 날짜 */
        int dayOfMonth = targetDate.getDayOfMonth();
        /* 1일 또는 15일에 활성화되는 신살 */
        if (dayOfMonth == 1 || dayOfMonth == 15) {
            sinsals.add(new SinsalInfo("월건", "월건일로 길한 기운이 있습니다", true, 15));
        }
        /* 7일마다 활성화되는 신살 */
        if (dayOfMonth % 7 == 0) {
            sinsals.add(new SinsalInfo("칠살", "조심스러운 날입니다", false, 10));
        }
        /* 요일별 신살 추가 */
        switch (targetDate.getDayOfWeek()) {
            case SUNDAY -> sinsals.add(new SinsalInfo("일요길신", "일요일의 좋은 기운", true, 12));
            case FRIDAY -> sinsals.add(new SinsalInfo("금요복신", "금요일의 복된 기운", true, 10));
            default -> {} // 다른 요일은 특별한 신살 없음
        }
    }
    /**
     * 신살 설명 초기화
     * SQL: SELECT * FROM sinsals WHERE is_lucky = true;
     * @param SINSAL_DESCRIPTIONS 신살 설명 매핑
     */
    private static void initializeSinsalDescriptions() {
        // 길신 설명
        SINSAL_DESCRIPTIONS.put("천을귀인", "하늘의 도움을 받는 길한 신살입니다");
        SINSAL_DESCRIPTIONS.put("월덕합", "달의 덕을 받아 순조로운 날입니다");
        SINSAL_DESCRIPTIONS.put("복성귀인", "복을 가져다주는 길한 별입니다");
        SINSAL_DESCRIPTIONS.put("문창귀인", "학문과 문예에 도움이 되는 신살입니다");
        SINSAL_DESCRIPTIONS.put("학당", "학업과 지혜에 좋은 영향을 미칩니다");
        SINSAL_DESCRIPTIONS.put("금여", "금전운과 재물운이 좋은 날입니다");
        SINSAL_DESCRIPTIONS.put("옥당", "명예와 지위에 도움이 되는 신살입니다");
        SINSAL_DESCRIPTIONS.put("홍란", "인간관계에서 좋은 기운을 받습니다");
        SINSAL_DESCRIPTIONS.put("함지", "깊은 지혜와 통찰력을 얻는 날입니다");
        SINSAL_DESCRIPTIONS.put("국인", "국가나 조직에서 인정받는 기운입니다");
        SINSAL_DESCRIPTIONS.put("건록", "건강하고 녹이 풍부한 길한 신살입니다");
        SINSAL_DESCRIPTIONS.put("태극", "균형과 조화를 이루는 날입니다");
        SINSAL_DESCRIPTIONS.put("화개", "예술적 재능이 빛나는 신살입니다");
        SINSAL_DESCRIPTIONS.put("진신", "진귀한 것을 얻는 길한 날입니다");
        SINSAL_DESCRIPTIONS.put("문창", "창의력과 표현력이 뛰어난 날입니다");
        SINSAL_DESCRIPTIONS.put("역마", "변화와 이동에 좋은 기운입니다");
        SINSAL_DESCRIPTIONS.put("천의", "하늘의 뜻을 받는 신성한 신살입니다");
        SINSAL_DESCRIPTIONS.put("천덕", "하늘의 덕을 받는 길한 날입니다");
        SINSAL_DESCRIPTIONS.put("괴강", "특별한 능력을 발휘하는 신살입니다");
        SINSAL_DESCRIPTIONS.put("양인", "강한 기운을 받는 날입니다");
        // 흉신 설명
        SINSAL_DESCRIPTIONS.put("겁살", "재물 손실에 주의해야 하는 날입니다");
        SINSAL_DESCRIPTIONS.put("망신", "명예나 체면에 손상이 올 수 있습니다");
        SINSAL_DESCRIPTIONS.put("재살", "재정적 어려움에 주의하세요");
        SINSAL_DESCRIPTIONS.put("겫인", "인간관계에서 갈등이 생길 수 있습니다");
        SINSAL_DESCRIPTIONS.put("고신", "고독하거나 외로움을 느낄 수 있습니다");
        SINSAL_DESCRIPTIONS.put("혈인", "건강에 특별히 주의해야 합니다");
        SINSAL_DESCRIPTIONS.put("백호", "예상치 못한 사고에 조심하세요");
        SINSAL_DESCRIPTIONS.put("상문", "슬프거나 우울한 소식이 있을 수 있습니다");
        SINSAL_DESCRIPTIONS.put("육해", "계획이 틀어지거나 방해받을 수 있습니다");
        SINSAL_DESCRIPTIONS.put("현침", "날카로운 말이나 비판에 주의하세요");
        SINSAL_DESCRIPTIONS.put("구신", "구설수나 험담에 조심해야 합니다");
        SINSAL_DESCRIPTIONS.put("조신", "조급함을 버리고 신중하게 행동하세요");
        SINSAL_DESCRIPTIONS.put("토귀", "토지나 부동산 관련 문제에 주의하세요");
        SINSAL_DESCRIPTIONS.put("월형", "형벌이나 법적 문제에 조심하세요");
        SINSAL_DESCRIPTIONS.put("일파", "하루 종일 파란만장한 일이 생길 수 있습니다");
        SINSAL_DESCRIPTIONS.put("고란", "고생스럽고 어려운 일이 생길 수 있습니다");
        SINSAL_DESCRIPTIONS.put("재앙", "뜻하지 않은 재앙에 주의하세요");
        SINSAL_DESCRIPTIONS.put("삼형", "형제나 친구와의 갈등에 주의하세요");
        SINSAL_DESCRIPTIONS.put("금신", "금전 관련 신중한 판단이 필요합니다");
        SINSAL_DESCRIPTIONS.put("철마", "교통수단 이용 시 특별히 조심하세요");
        SINSAL_DESCRIPTIONS.put("도화", "이성관계에서 혼란이 올 수 있습니다");
        SINSAL_DESCRIPTIONS.put("음차", "음흉한 일이나 뒤에서 방해하는 일에 주의하세요");
        SINSAL_DESCRIPTIONS.put("천라", "하늘의 그물에 걸린 듯 답답할 수 있습니다");
        SINSAL_DESCRIPTIONS.put("지망", "땅의 그물에 얽혀 진전이 어려울 수 있습니다");
        SINSAL_DESCRIPTIONS.put("원진", "원한이나 진노를 사는 일에 조심하세요");
        SINSAL_DESCRIPTIONS.put("혈광", "피를 보는 일이나 사고에 특별히 주의하세요");
        SINSAL_DESCRIPTIONS.put("삼재", "3년간의 재앙 중 하나로 매우 조심해야 합니다");
        // 특수 신살 설명
        SINSAL_DESCRIPTIONS.put("월건", "월건일로 길한 기운이 강한 날입니다");
        SINSAL_DESCRIPTIONS.put("칠살", "7일마다 돌아오는 조심스러운 기운입니다");
        SINSAL_DESCRIPTIONS.put("일요길신", "일요일의 편안하고 좋은 기운입니다");
        SINSAL_DESCRIPTIONS.put("금요복신", "금요일의 복된 기운을 받는 날입니다");
    }
}
