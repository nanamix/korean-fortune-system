package com.fortune.dto;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDate;
import java.time.LocalDateTime;
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
}
