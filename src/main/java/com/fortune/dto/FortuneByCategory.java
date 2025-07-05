package com.fortune.dto;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
/**
 * 구체적인 분야별 운세 DTO
 * 연애운, 직장운, 건강운, 재물운을 포함
 * 
 * @author 하진영
 * @version 2.5.0
 * @since 2025-06-24
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FortuneByCategory {
    private int loveScore;
    private String loveMessage;
    private int careerScore;
    private String careerMessage;
    private int healthScore;
    private String healthMessage;
    private int wealthScore;
    private String wealthMessage;
}
