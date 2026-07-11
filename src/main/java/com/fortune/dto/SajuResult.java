package com.fortune.dto;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
/**
 * 사주팔자 계산 결과 DTO
 *
 * <p>사주팔자 계산 결과를 담는 클래스입니다.</p>
 *
 * @author 하진영
 * @version 2.0.0
 * @since 2025-06-24
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SajuResult {
    private String yearPillar;
    private String monthPillar;
    private String dayPillar;
    private String timePillar;
    private String dayMaster;
    private LocalDate birthDate;
    private LocalDateTime adjustedDateTime;
    private String calendarType;
    private String gender;
    private WuxingAnalysis wuxingAnalysis;
    private String fortuneSummary;

    // ── 명리 상세 (십신·지장간·12운성·대운) ──────────────────────
    /** 4주 상세: 천간/지지, 십신, 지장간, 12운성. 기존 문자열 pillar 와 별개로 추가. */
    private Pillar yearDetail;
    private Pillar monthDetail;
    private Pillar dayDetail;
    private Pillar timeDetail;
    /** 대운 목록 (5·15·25… 각 10년 운) */
    private List<DaeUn> daeun;
    /** 대운 방향: true=순행(양남음녀), false=역행(음남양녀) */
    private boolean daeunForward;
    /** 대운수(대운 시작 나이) */
    private int daeunNumber;

    /**
     * 사주팔자를 문자열로 포맷팅
     * 
     * <p>사주팔자를 문자열로 포맷팅합니다.</p>
     * 
     * <p>예시: "경오 신사 갑자 신미"</p>
     * 
     * @return 사주팔자 문자열
     */
    public String getFormattedSaju() {
        return String.format("%s %s %s %s", yearPillar, monthPillar, dayPillar, timePillar);
    }
    /**
     * 내부 클래스: 오행 분석
     * 
     * @author 하진영
     * @version 2.5.0
     * @since 2025-06-24
     * 
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class WuxingAnalysis {
        private int woodCount;
        private int fireCount;
        private int earthCount;
        private int metalCount;
        private int waterCount;
        private String strongestElement;
        private String weakestElement;
        private int balance;
    }

    /**
     * 한 기둥(柱)의 명리 상세.
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Pillar {
        private String stem;        // 천간 (한글, 예: 신)
        private String branch;      // 지지 (한글, 예: 유)
        private String stemHanja;   // 천간 한자 (예: 辛)
        private String branchHanja; // 지지 한자 (예: 酉)
        private String stemSipsin;      // 천간 십신 (예: 편재). 일간은 "일간(본원)"
        private String branchSipsin;    // 지지 본기(정기) 십신
        private String twelveStage;     // 12운성 (일간 대비 지지, 예: 장생)
        private List<String> hiddenStems;       // 지장간 (예: [경, 신])
        private List<String> hiddenStemsSipsin; // 지장간 각각의 십신
    }

    /**
     * 대운 한 주기(10년).
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class DaeUn {
        private int age;          // 시작 나이
        private String ganji;     // 간지 (한글, 예: 경인)
        private String ganjiHanja;// 간지 한자 (예: 庚寅)
        private String stemSipsin;   // 천간 십신
        private String branchSipsin; // 지지 십신
        private String twelveStage;  // 12운성
    }
}
