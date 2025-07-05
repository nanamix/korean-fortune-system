package com.fortune.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 분야별 운세 DTO
 * 
 * @author 하진영
 * @version 2.5.0
 * @since 2025-06-24
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "분야별 운세")
public class FortuneCategory {
    
    @Schema(description = "운세 점수 (0-100)", example = "85")
    private int score;

    @Schema(description = "운세 설명", example = "오늘은 매우 좋은 날입니다.")
    private String description;

    @Schema(description = "운세 조언", example = "새로운 시도를 해보세요.")
    private String advice;
}
