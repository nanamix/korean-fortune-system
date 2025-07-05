package com.fortune.dto;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import jakarta.validation.constraints.*;
import java.time.LocalDate;
import java.util.List;
/**
 * 간지달력 응답 DTO
 * 
 * @author 하진영
 * @version 2.5.0
 * @since 2025-06-24
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class GanjiCalendarResponse {
    private int year;
    private int month;
    private String monthName;
    private List<GanjiCalendarDay> days;
    private List<String> solarTerms;
    private String monthlyTheme;
    private String monthlyAdvice;
    private List<Integer> luckyDays;
    private List<Integer> cautionDays;
    private int totalDays;
}
