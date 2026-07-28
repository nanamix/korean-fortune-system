package com.fortune.dto;

import java.time.LocalDate;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 서양 점성술 주간 운세의 날짜별 요약.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ZodiacWeeklyDay {
    private LocalDate date;
    private int overallScore;
    private String headline;
    private String transitSummary;
}
