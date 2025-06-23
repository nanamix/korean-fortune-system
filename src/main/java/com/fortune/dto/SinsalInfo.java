package com.fortune.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 신살 정보 DTO
 * 신살의 이름, 설명, 길신 여부, 영향도를 포함합니다.
 * @author 하진영
 * @version 1.0.0
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class SinsalInfo {
    private String name;        // 신살 이름
    private String description; // 설명
    private boolean lucky;      // 길신 여부
    private int influence;      // 영향도 (1-20)
}
