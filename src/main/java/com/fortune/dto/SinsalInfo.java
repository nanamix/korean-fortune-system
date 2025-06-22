package com.fortune.dto;

import lombok.Builder;
import lombok.Data;

/**
 * 신살 정보 DTO
 * 신살명
 * 설명
 * 길신/흉신 여부
 * 영향도 (-100 ~ +100)
 * 조언
 */
@Data
@Builder
public class SinsalInfo {
    private String name;          // 신살명
    private String description;   // 설명
    private boolean isLucky;      // 길신/흉신 여부
    private int impact;          // 영향도 (-100 ~ +100)
    private String advice;       // 조언
}
