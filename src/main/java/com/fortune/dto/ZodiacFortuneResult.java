package com.fortune.dto;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDate;
import java.util.List;
/**
 * 별자리 운세 결과 DTO
 * 
 * @author 하진영
 * @version 2.5.0
 * @since 2025-06-24
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ZodiacFortuneResult {
    private com.fortune.enums.Zodiac zodiac;
    private String zodiacKoreanName;
    private LocalDate targetDate;
    private ZodiacDailyFortune todayFortune;
    private ZodiacMonthlyFortune monthlyFortune;
    private List<com.fortune.enums.Zodiac> compatibleZodiacs;
    private List<Integer> luckyNumbers;
    private String luckyColor;
    private String luckyStone;
    private String personality;
}
