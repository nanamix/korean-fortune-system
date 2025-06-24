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
 * 별자리 운세 결과 DTO
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "별자리 운세 결과")
public class ZodiacFortuneResult {

    @Schema(description = "별자리 (영문)")
    private com.fortune.enums.Zodiac zodiac;

    @Schema(description = "별자리 (한글)")
    private String zodiacKoreanName;

    @Schema(description = "대상 날짜")
    private LocalDate targetDate;

    @Schema(description = "오늘의 운세")
    private ZodiacDailyFortune todayFortune;

    @Schema(description = "이달의 운세")
    private ZodiacMonthlyFortune monthlyFortune;

    @Schema(description = "궁합 좋은 별자리")
    private List<com.fortune.enums.Zodiac> compatibleZodiacs;

    @Schema(description = "행운의 숫자")
    private List<Integer> luckyNumbers;

    @Schema(description = "행운의 색깔")
    private String luckyColor;

    @Schema(description = "행운의 보석")
    private String luckyStone;

    @Schema(description = "성격 특성")
    private String personality;
}
