package com.fortune.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 신살 정보 DTO
 * 신살의 이름, 설명, 길신 여부, 영향도를 포함합니다.
 * 
 * @author 하진영
 * @version 2.5.0
 * @since 2025-06-24
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Schema(description = "신살 정보")
public class SinsalInfo {
    @Schema(description = "신살 이름")
    private String name;

    @Schema(description = "설명")
    private String description;

    @Schema(description = "길신 여부")
    private boolean lucky;

    @Schema(description = "영향도 (1-20)")
    private int influence;
}
