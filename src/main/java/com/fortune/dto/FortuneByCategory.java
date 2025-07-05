package com.fortune.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import io.swagger.v3.oas.annotations.media.Schema;

/**
 * 구체적인 분야별 운세 DTO
 * 연애운, 직장운, 건강운, 재물운을 포함
 * 
 * @author 하진영
 * @version 2.5.0
 * @since 2025-06-24
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "구체적인 분야별 운세")
public class FortuneByCategory {

    @Schema(description = "연애운 점수 (0-100)", example = "85")
    private int loveScore;

    @Schema(description = "연애운 메시지", example = "로맨틱한 만남이 기다리고 있습니다.")
    private String loveMessage;

    @Schema(description = "직장운 점수 (0-100)", example = "90")
    private int careerScore;

    @Schema(description = "직장운 메시지", example = "업무에서 좋은 성과를 거둘 수 있습니다.")
    private String careerMessage;

    @Schema(description = "건강운 점수 (0-100)", example = "75")
    private int healthScore;

    @Schema(description = "건강운 메시지", example = "적절한 운동과 휴식을 취하세요.")
    private String healthMessage;

    @Schema(description = "재물운 점수 (0-100)", example = "80")
    private int wealthScore;

    @Schema(description = "재물운 메시지", example = "재정적 기회가 찾아올 수 있습니다.")
    private String wealthMessage;
}
