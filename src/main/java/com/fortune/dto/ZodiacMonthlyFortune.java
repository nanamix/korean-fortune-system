package com.fortune.dto;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
/**
 * 별자리 월간 운세 DTO
 * 
 * @author 하진영
 * @version 2.5.0
 * @since 2025-06-24
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ZodiacMonthlyFortune {
    private int month;
    private int overallScore;
    private String theme;
    private String detailedMessage;
    private String caution;
    private String opportunity;
}
