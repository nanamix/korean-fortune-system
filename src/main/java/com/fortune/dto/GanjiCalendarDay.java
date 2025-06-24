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
 * 간지달력 일별 정보 DTO
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "간지달력 일별 정보")
public class GanjiCalendarDay {

    @Schema(description = "날짜")
    private LocalDate date;

    @Schema(description = "일주")
    private String dayPillar;

    @Schema(description = "운세 점수")
    private int fortuneScore;

    @Schema(description = "길일 여부")
    private boolean luckyDay;

    @Schema(description = "길방위")
    private String luckyDirection;

    @Schema(description = "길한 색깔")
    private List<String> luckyColors;

    @Schema(description = "24절기")
    private String solarTerm;

    @Schema(description = "간단한 조언")
    private String briefAdvice;
}
