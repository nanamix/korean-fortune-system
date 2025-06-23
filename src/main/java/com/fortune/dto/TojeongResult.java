package com.fortune.dto;

import lombok.Builder;
import lombok.Data;
import java.util.List;
import java.util.Map;

/**
 * 토정 결과 DTO
 * 토정과의 결과를 담는 객체로, 대상 년도, 괘 번호, 이름, 상징, 요약, 상세 운세, 종합 점수, 조언,
 * 길한 달, 주의할 달, 월별 상세 운세를 포함합니다.
 * @author 하진영
 * @version 1.0.0
 */
@Data
@Builder
public class TojeongResult {
    private int targetYear;             // 대상 년도
    private int gwaNumber;              // 괘 번호
    private String gwaName;             // 괘 이름
    private String gwaSymbol;           // 괘 상징
    private String summary;             // 요약
    private String detailedFortune;     // 상세 운세
    private int overallScore;           // 종합 점수
    private String advice;              // 조언
    private List<Integer> luckyMonths;  // 길한 달
    private List<Integer> cautionMonths; // 주의할 달
    private Map<Integer, String> monthlyFortune; // 월별 상세 운세
}
