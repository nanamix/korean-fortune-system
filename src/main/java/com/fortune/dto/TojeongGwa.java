package com.fortune.dto;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
/**
 * 토정비결 괘 정보 DTO
 * 
 * @author 하진영
 * @version 2.5.0
 * @since 2025-06-24
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TojeongGwa {
    private int number;
    private String name;
    private String symbol;
    private String summary;
    private String detailedFortune;
    private int score;
    private String luckyMonths;
    private String cautionMonths;
}
