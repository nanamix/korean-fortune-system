package com.fortune.dto;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.util.List;
/**
 * 월별 운세 DTO
 * 
 * @author 하진영
 * @version 2.5.0
 * @since 2025-06-24
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MonthlyFortune {
    private int month;
    private int score;
    private String message;
    private List<String> keywords;
}
