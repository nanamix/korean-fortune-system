package com.fortune.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * 점성술 연간 운세 DTO
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ZodiacAnnualFortune {
    private int year;
    private int overallScore;
    private String scoreBasis;
    private String overview;
    private int bestMonth;
    private String bestMonthReason;
    private int cautionMonth;
    private String caution;
    private List<ZodiacAnnualMonth> months;
}
