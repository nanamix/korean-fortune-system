package com.fortune.dto;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import jakarta.validation.constraints.*;
import java.time.LocalDate;
import java.util.List;
/**
 * 간지달력 일별 정보 DTO
 * 
 * @author 하진영
 * @version 2.5.0
 * @since 2025-06-24
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class GanjiCalendarDay {
    private LocalDate date;
    private String dayPillar;
    private int fortuneScore;
    private boolean luckyDay;
    private String luckyDirection;
    private List<String> luckyColors;
    private String solarTerm;
    private String briefAdvice;
}
