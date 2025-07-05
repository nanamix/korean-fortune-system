package com.fortune.dto;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
/**
 * 별자리 운세 DTO
 * 
 * @author 하진영
 * @version 2.5.0
 * @since 2025-06-24
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ZodiacFortune {
    private String overall;
    private String love;
    private String career;
    private String health;
    private String money;
    private int loveScore;
    private int healthScore;
    private int moneyScore;
    private int overallScore;
}
