package com.fortune.dto;

import lombok.Builder;
import lombok.Data;

/**
 * 분야별 운세
 * 종합운
 * 애정운
 * 재물운
 * 건강운
 * 직업운
 */
@Data
@Builder
public class FortuneCategory {
    private int score;        // 점수
    private String description; // 설명
    private String advice;     // 조언
}
