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
 * 토정비결 결과 DTO
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "토정비결 결과")
public class TojeongResult {

    @Schema(description = "대상 연도")
    private int targetYear;

    @Schema(description = "괘 번호")
    private int gwaNumber;

    @Schema(description = "괘 이름")
    private String gwaName;

    @Schema(description = "괘 상징")
    private String gwaSymbol;

    @Schema(description = "운세 요약")
    private String summary;

    @Schema(description = "상세 운세")
    private String detailedFortune;

    @Schema(description = "종합 점수")
    private int overallScore;

    @Schema(description = "조언")
    private String advice;

    @Schema(description = "길한 달")
    private String luckyMonths;

    @Schema(description = "주의할 달")
    private String cautionMonths;

    @Schema(description = "월별 운세")
    private List<MonthlyFortune> monthlyFortune;
}
