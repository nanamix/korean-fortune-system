package com.fortune.dto;

import java.time.LocalDate;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 선택한 날짜가 포함된 월요일부터 일요일까지의 서양 점성술 주간 운세.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ZodiacWeeklyFortune {
    private LocalDate startDate;
    private LocalDate endDate;
    private int overallScore;
    private String scoreBasis;
    private String overview;
    private LocalDate bestDate;
    private String bestDayReason;
    private String caution;
    private List<ZodiacWeeklyDay> days;
}
