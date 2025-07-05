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
    /**
     * 천간별 길신 매핑
     */
    private static final Map<String, List<String>> LUCKY_SINSALS = new HashMap<>();
    /**
     * 천간별 흉신 매핑
     */
    private static final Map<String, List<String>> UNLUCKY_SINSALS = new HashMap<>();
    /**
     * 신살 설명 매핑
     */
    private static final Map<String, String> SINSAL_DESCRIPTIONS = new HashMap<>();
    /**
     * 정적 초기화 메서드들
     */
    static {
        initializeLuckySinsals();
        initializeUnluckySinsals();
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
        /* 신살 정보 리스트 생성 */
        List<SinsalInfo> sinsals = new ArrayList<>();
        /* 예외 처리 */
        try {
            String dayMaster = saju.getDayMaster();
            /* 1. 일간 기반 길신 계산 */
            List<String> luckySinsalNames = LUCKY_SINSALS.getOrDefault(dayMaster, new ArrayList<>());
            /* 길신 계산 */
            for (String sinsalName : luckySinsalNames) {
                /* 신살 활성화 여부 확인 */
                if (isActiveSinsal(sinsalName, targetDate, saju)) {
                    /* 신살 정보 추가 */
                    sinsals.add(new SinsalInfo(
                            sinsalName,
                            SINSAL_DESCRIPTIONS.getOrDefault(sinsalName, sinsalName + " 신살"),
                            true,
                            calculateInfluence(sinsalName, true)
                    ));
                }
            }
            /* 2. 일간 기반 흉신 계산 */
            List<String> unluckySinsalNames = UNLUCKY_SINSALS.getOrDefault(dayMaster, new ArrayList<>());
            /* 흉신 계산 */
            for (String sinsalName : unluckySinsalNames) {
                /* 신살 활성화 여부 확인 */
                if (isActiveSinsal(sinsalName, targetDate, saju)) {
                    /* 신살 정보 추가 */
                    sinsals.add(new SinsalInfo(
                            sinsalName,
                            SINSAL_DESCRIPTIONS.getOrDefault(sinsalName, sinsalName + " 신살"),
                            false,
                            calculateInfluence(sinsalName, false)
                    ));
                }
            }
            /* 3. 특수 신살 계산 (날짜 기반) */
            addDateBasedSinsals(sinsals, targetDate);
            log.info("✅ 신살 계산 완료: {} 개 발견", sinsals.size());
            return sinsals;
        } catch (Exception e) {
            log.error("❌ 신살 계산 중 오류 발생: {}", e.getMessage(), e);
            return new ArrayList<>();
        }
    }
    /**
     * 신살 활성화 여부 확인
     * SQL: SELECT * FROM sinsals WHERE name = ? AND date = ?;
     * @param sinsalName 신살 이름
     * @param targetDate 날짜
     * @param saju 사주 결과
     * @return 신살 활성화 여부
     */
    private boolean isActiveSinsal(String sinsalName, LocalDate targetDate, SajuResult saju) {
        /* 간단한 활성화 로직 (실제로는 더 복잡한 계산 필요) */
        int dayOfYear = targetDate.getDayOfYear();
        /* 해시 계산 */
        int hash = (sinsalName.hashCode() + saju.getDayMaster().hashCode()) % 100;
        /* 대략 1/3 확률로 활성화 */
        return (dayOfYear + hash) % 3 == 0;
    }
    /**
     * 신살 영향도 계산
     * SQL: SELECT * FROM sinsals WHERE name = ? AND is_lucky = ?;
     * @param sinsalName 신살 이름
     * @param isLucky 길신 여부
     * @return 신살 영향도
     */
    private int calculateInfluence(String sinsalName, boolean isLucky) {
        /* 기본 영향도 */
        int baseInfluence = sinsalName.length() * 2;
        /* 길신 여부에 따라 영향도 조정 */
        if (isLucky) {
            return Math.min(20, baseInfluence + 5);
        } else {
            return Math.max(1, baseInfluence);
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
     * 길신 초기화
     * SQL: SELECT * FROM sinsals WHERE is_lucky = true;
     * @param LUCKY_SINSALS 길신 매핑
     * @param UNLUCKY_SINSALS 흉신 매핑
     * @param SINSAL_DESCRIPTIONS 신살 설명 매핑
     */
    private static void initializeLuckySinsals() {
        LUCKY_SINSALS.put("갑", Arrays.asList("천을귀인", "월덕합", "복성귀인"));
        LUCKY_SINSALS.put("을", Arrays.asList("천을귀인", "문창귀인", "학당"));
        LUCKY_SINSALS.put("병", Arrays.asList("천을귀인", "금여", "옥당"));
        LUCKY_SINSALS.put("정", Arrays.asList("천을귀인", "홍란", "함지"));
        LUCKY_SINSALS.put("무", Arrays.asList("천을귀인", "국인", "건록"));
        LUCKY_SINSALS.put("기", Arrays.asList("천을귀인", "태극", "화개"));
        LUCKY_SINSALS.put("경", Arrays.asList("천을귀인", "학당", "진신"));
        LUCKY_SINSALS.put("신", Arrays.asList("천을귀인", "문창", "역마"));
        LUCKY_SINSALS.put("임", Arrays.asList("천을귀인", "천의", "천덕"));
        LUCKY_SINSALS.put("계", Arrays.asList("천을귀인", "괴강", "양인"));
    }
    /**
     * 흉신 초기화
     * SQL: SELECT * FROM sinsals WHERE is_lucky = false;
     * @param LUCKY_SINSALS 길신 매핑
     * @param UNLUCKY_SINSALS 흉신 매핑
     * @param SINSAL_DESCRIPTIONS 신살 설명 매핑
     */
    private static void initializeUnluckySinsals() {
        UNLUCKY_SINSALS.put("갑", Arrays.asList("겁살", "망신", "재살"));
        UNLUCKY_SINSALS.put("을", Arrays.asList("겫인", "고신", "혈인"));
        UNLUCKY_SINSALS.put("병", Arrays.asList("백호", "상문", "육해"));
        UNLUCKY_SINSALS.put("정", Arrays.asList("현침", "구신", "조신"));
        UNLUCKY_SINSALS.put("무", Arrays.asList("토귀", "월형", "일파"));
        UNLUCKY_SINSALS.put("기", Arrays.asList("고란", "재앙", "삼형"));
        UNLUCKY_SINSALS.put("경", Arrays.asList("백호", "금신", "철마"));
        UNLUCKY_SINSALS.put("신", Arrays.asList("현침", "도화", "음차"));
        UNLUCKY_SINSALS.put("임", Arrays.asList("천라", "지망", "원진"));
        UNLUCKY_SINSALS.put("계", Arrays.asList("고신", "혈광", "삼재"));
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
