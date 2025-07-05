package com.fortune.dto;
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
public class SinsalInfo {
    private String name;
    private String description;
    private boolean lucky;
    private int influence;
}
