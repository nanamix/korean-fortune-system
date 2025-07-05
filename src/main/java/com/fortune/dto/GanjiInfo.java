package com.fortune.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import io.swagger.v3.oas.annotations.media.Schema;
/**
 * 간지 정보 DTO
 * 
 * @author 하진영
 * @version 2.5.0
 * @since 2025-06-24
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "간지 정보")
public class GanjiInfo {

    @Schema(description = "천간")
    private String heavenlyStem;

    @Schema(description = "지지")
    private String earthlyBranch;

    @Schema(description = "간지")
    private String ganji;

    @Schema(description = "오행")
    private String wuxing;

    @Schema(description = "음양")
    private String yinYang;
}
