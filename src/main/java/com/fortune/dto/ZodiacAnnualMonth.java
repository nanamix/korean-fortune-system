package com.fortune.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 점성술 연간 운세의 월별 요약 DTO
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ZodiacAnnualMonth {
    private int month;
    private int overallScore;
    private String theme;
    private String summary;
    private String opportunity;
    private String caution;
}
