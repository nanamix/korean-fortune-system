package com.fortune.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import io.swagger.v3.oas.annotations.media.Schema;

/**
 * 별자리 월간 운세 DTO
 * 
 * @author 하진영
 * @version 2.5.0
 * @since 2025-06-24
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "별자리 월간 운세")
public class ZodiacMonthlyFortune {

    @Schema(description = "월")
    private int month;

    @Schema(description = "종합 점수")
    private int overallScore;

    @Schema(description = "주요 테마")
    private String theme;

    @Schema(description = "상세 메시지")
    private String detailedMessage;

    @Schema(description = "주의사항")
    private String caution;

    @Schema(description = "기회 요소")
    private String opportunity;
}
