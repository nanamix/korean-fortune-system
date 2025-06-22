package com.fortune.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 별자리 운세
 * 전체 운세
 * 애정 운세
 * 직업 운세
 * 건강 운세
 * 재물 운세
 * 애정 점수
 * 직업 점수
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ZodiacFortune {
    private String overall;  // 전체 운세
    private String love;  // 애정 운세
    private String career;  // 직업 운세
    private String health;  // 건강 운세
    private String money;  // 재물 운세
    private int loveScore;  // 애정 점수
    private int careerScore;  // 직업 점수
    private int healthScore;  // 건강 점수
    private int moneyScore;  // 재물 점수
    private int overallScore;  // 전체 점수
}
