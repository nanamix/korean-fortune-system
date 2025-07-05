package com.fortune.dto;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
/**
 * 별자리 일일 운세 DTO
 * 
 * @author 하진영
 * @version 2.5.0
 * @since 2025-06-24
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ZodiacDailyFortune {
    private String overallMessage;
    private int loveScore;
    private String loveMessage;
    private int careerScore;
    private String careerMessage;
    private int healthScore;
    private String healthMessage;
    private int moneyScore;
    private String moneyMessage;
}
