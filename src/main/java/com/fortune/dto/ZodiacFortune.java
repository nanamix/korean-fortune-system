package com.fortune.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 별자리 운세 DTO
 * 
 * @author 하진영
 * @version 2.5.0
 * @since 2025-06-24
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "별자리 운세")
public class ZodiacFortune {
    @Schema(description = "전체 운세")
    private String overall;

    @Schema(description = "애정 운세")
    private String love;

    @Schema(description = "직업 운세")
    private String career;

    @Schema(description = "건강 운세")
    private String health;

    @Schema(description = "재물 운세")
    private String money;

    @Schema(description = "애정 점수")
    private int loveScore;

    @Schema(description = "직업 점수")
    private int healthScore;

    @Schema(description = "재물 점수")
    private int moneyScore;

    @Schema(description = "전체 점수")
    private int overallScore;
}
