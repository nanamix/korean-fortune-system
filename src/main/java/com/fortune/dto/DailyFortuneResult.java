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
 * 일일 운세 결과 DTO
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "일일 운세 결과")
public class DailyFortuneResult {

    @Schema(description = "대상 날짜")
    private LocalDate date;

    @Schema(description = "해당 날짜의 일주")
    private String dayPillar;

    @Schema(description = "종합 점수 (0-100)")
    private int totalScore;

    @Schema(description = "분야별 운세")
    private FortuneByCategory categoryFortune;

    @Schema(description = "길신/흉신 정보")
    private List<SinsalInfo> sinsals;

    @Schema(description = "오늘의 조언")
    private String advice;

    @Schema(description = "길방위")
    private String luckyDirection;

    @Schema(description = "길한 색깔")
    private List<String> luckyColors;

    @Schema(description = "주의사항")
    private String caution;
}
