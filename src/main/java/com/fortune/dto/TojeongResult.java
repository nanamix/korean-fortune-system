package com.fortune.dto;

import lombok.Builder;
import lombok.Data;

import java.util.List;
import java.util.Map;

/**
 * 토정과 결과 DTO
 * 대상 년도
 * 괘 번호 (0-63)
 * 괘명
 * 괘 기호
 * 한줄 요약
 * 상세 운세
 * 월별 운세
 */
@Data
@Builder
public class TojeongResult {
    private Integer targetYear;                    // 대상 년도
    private Integer gwaNumber;                     // 괘 번호 (0-63)
    private String gwaName;                        // 괘명
    private String gwaSymbol;                      // 괘 기호
    private String summary;                        // 한줄 요약
    private String detailedFortune;                // 상세 운세
    private Map<Integer, String> monthlyFortune;   // 월별 운세
    private Map<String, String> seasonalFortune;   // 계절별 운세
    private Integer overallScore;                  // 종합 점수 (0-100)
    private String advice;                         // 연간 종합 조언
    private List<Integer> luckyMonths;             // 길한 달들
    private List<Integer> cautionMonths;           // 주의할 달들
}
