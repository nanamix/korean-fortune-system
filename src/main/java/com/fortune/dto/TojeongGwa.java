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
 * 토정비결 괘 정보 DTO
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "토정비결 괘 정보")
public class TojeongGwa {

    @Schema(description = "괘 번호")
    private int number;

    @Schema(description = "괘 이름")
    private String name;

    @Schema(description = "괘 상징")
    private String symbol;

    @Schema(description = "요약")
    private String summary;

    @Schema(description = "상세 운세")
    private String detailedFortune;

    @Schema(description = "점수")
    private int score;

    @Schema(description = "길한 달")
    private String luckyMonths;

    @Schema(description = "주의할 달")
    private String cautionMonths;
}
