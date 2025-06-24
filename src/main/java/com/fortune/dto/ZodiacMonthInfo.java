package com.fortune.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class ZodiacMonthInfo {
    private int month;              // 월 (1-12)
    private int overallScore;       // 종합 점수
    private String theme;           // 월 테마
    private String advice;          // 조언
    private String keyEvent;        // 주요 이벤트
}
