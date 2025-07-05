package com.fortune.dto;
import lombok.Builder;
import lombok.Data;
/**
 * 별자리 월별 정보 DTO
 * 
 * @author 하진영
 * @version 2.5.0
 * @since 2025-06-24
 */
@Data
@Builder
public class ZodiacMonthInfo {
    private int month;
    private int overallScore;
    private String theme;
    private String advice;
    private String keyEvent;
}
