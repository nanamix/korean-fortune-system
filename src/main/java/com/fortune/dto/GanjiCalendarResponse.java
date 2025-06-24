package com.fortune.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.*;
import java.time.LocalDate;
import java.util.List;

/**
 * 간지달력 응답 DTO
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "간지달력 응답")
public class GanjiCalendarResponse {

    @Schema(description = "연도")
    private int year;

    @Schema(description = "월")
    private int month;

    @Schema(description = "월 이름")
    private String monthName;

    @Schema(description = "일별 정보")
    private List<GanjiCalendarDay> days;

    @Schema(description = "24절기 정보")
    private List<String> solarTerms;

    @Schema(description = "이달의 테마")
    private String monthlyTheme;

    @Schema(description = "이달의 조언")
    private String monthlyAdvice;

    @Schema(description = "길한 날들")
    private List<Integer> luckyDays;

    @Schema(description = "주의할 날들")
    private List<Integer> cautionDays;

    @Schema(description = "총 일수")
    private int totalDays;
}
