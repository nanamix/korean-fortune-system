package com.fortune.dto;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDate;
import java.util.List;
/**
 * 일일 운세 결과 DTO
 * 
 * @author 하진영
 * @version 2.5.0
 * @since 2025-06-24
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DailyFortuneResult {
    private LocalDate date;
    private String dayPillar;
    private int totalScore;
    private FortuneByCategory categoryFortune;
    private List<SinsalInfo> sinsals;
    private String advice;
    private String luckyDirection;
    private List<String> luckyColors;
    private String caution;
}
