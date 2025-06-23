package com.fortune.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class ZodiacDailyFortune {
    private String overall;         // 종합 운세 메시지
    private int loveScore;          // 애정 운세 점수 (0-100)
    private int careerScore;        // 직업 운세 점수 (0-100)
    private int healthScore;        // 건강 운세 점수 (0-100)
    private int moneyScore;         // 재물 운세 점수 (0-100)

    // 상세 메시지들
    private String loveMessage;     // 애정 운세 상세
    private String careerMessage;   // 직업 운세 상세
    private String healthMessage;   // 건강 운세 상세
    private String moneyMessage;    // 재물 운세 상세
}
