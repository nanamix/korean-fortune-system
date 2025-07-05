package com.fortune.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Data;

/**
 * 별자리 월별 정보 DTO
 * 
 * @author 하진영
 * @version 2.5.0
 * @since 2025-06-24
 */
@Data
@Builder
@Schema(description = "별자리 월별 정보")
public class ZodiacMonthInfo {
    @Schema(description = "월 (1-12)")
    private int month;

    @Schema(description = "종합 점수")
    private int overallScore;

    @Schema(description = "월 테마")
    private String theme;

    @Schema(description = "조언")
    private String advice;

    @Schema(description = "주요 이벤트")
    private String keyEvent;
}
