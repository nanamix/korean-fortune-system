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
 * 월별 운세 DTO
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "월별 운세")
public class MonthlyFortune {

    @Schema(description = "월")
    private int month;

    @Schema(description = "점수")
    private int score;

    @Schema(description = "운세 메시지")
    private String message;

    @Schema(description = "주요 키워드")
    private List<String> keywords;
}
