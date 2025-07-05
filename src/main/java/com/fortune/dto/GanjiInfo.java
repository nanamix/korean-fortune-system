package com.fortune.dto;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
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
public class GanjiInfo {
    private String heavenlyStem;
    private String earthlyBranch;
    private String ganji;
    private String wuxing;
    private String yinYang;
}
