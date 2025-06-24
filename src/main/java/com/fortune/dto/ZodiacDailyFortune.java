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
 * 별자리 일일 운세 DTO
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "별자리 일일 운세")
public class ZodiacDailyFortune {

    @Schema(description = "종합 메시지")
    private String overallMessage;

    @Schema(description = "연애운 점수")
    private int loveScore;

    @Schema(description = "연애운 메시지")
    private String loveMessage;

    @Schema(description = "직업운 점수")
    private int careerScore;

    @Schema(description = "직업운 메시지")
    private String careerMessage;

    @Schema(description = "건강운 점수")
    private int healthScore;

    @Schema(description = "건강운 메시지")
    private String healthMessage;

    @Schema(description = "금전운 점수")
    private int moneyScore;

    @Schema(description = "금전운 메시지")
    private String moneyMessage;
}
