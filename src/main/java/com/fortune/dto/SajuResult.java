package com.fortune.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import io.swagger.v3.oas.annotations.media.Schema;
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
@Schema(description = "사주팔자 계산 결과")
public class SajuResult {

    @Schema(description = "연주 (년간+년지)", example = "경오")
    private String yearPillar;

    @Schema(description = "월주 (월간+월지)", example = "신사")
    private String monthPillar;

    @Schema(description = "일주 (일간+일지)", example = "갑자")
    private String dayPillar;

    @Schema(description = "시주 (시간+시지)", example = "신미")
    private String timePillar;

    @Schema(description = "일간 (본인을 나타내는 천간)", example = "갑")
    private String dayMaster;

    @Schema(description = "출생일자")
    private LocalDate birthDate;

    @Schema(description = "조정된 출생일시 (음력→양력 변환 등)")
    private LocalDateTime adjustedDateTime;

    @Schema(description = "달력 타입 (SOLAR/LUNAR)")
    private String calendarType;

    @Schema(description = "성별 (M/F)")
    private String gender;

    @Schema(description = "오행 분석")
    private WuxingAnalysis wuxingAnalysis;

    @Schema(description = "운세 요약")
    private String fortuneSummary;

    /**
     * 사주팔자를 문자열로 포맷팅
     */
    public String getFormattedSaju() {
        return String.format("%s %s %s %s", yearPillar, monthPillar, dayPillar, timePillar);
    }

    /**
     * 내부 클래스: 오행 분석
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @Schema(description = "오행 분석 결과")
    public static class WuxingAnalysis {
        @Schema(description = "목 개수")
        private int woodCount;

        @Schema(description = "화 개수")
        private int fireCount;

        @Schema(description = "토 개수")
        private int earthCount;

        @Schema(description = "금 개수")
        private int metalCount;

        @Schema(description = "수 개수")
        private int waterCount;

        @Schema(description = "가장 강한 오행")
        private String strongestElement;

        @Schema(description = "가장 약한 오행")
        private String weakestElement;

        @Schema(description = "오행 균형도 (0-100)")
        private int balance;
    }
}
