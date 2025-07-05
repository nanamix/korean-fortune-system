package com.fortune.dto;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.util.List;
/**
 * 토정비결 결과 DTO
 * 
 * @author 하진영
 * @version 2.5.0
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TojeongResult {
    private int targetYear;
    private int gwaNumber;
    private String gwaName;
    private String gwaSymbol;
    private String summary;
    private String detailedFortune;
    private int overallScore;
    private String advice;
    private String luckyMonths;
    private String cautionMonths;
    private List<MonthlyFortune> monthlyFortune;
}
