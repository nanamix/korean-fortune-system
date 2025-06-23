package com.fortune.dto;

import lombok.Builder;
import lombok.Data;
import java.time.LocalDate;
import java.util.List;

/**
 * 일일 운세 결과 DTO
 * 특정 날짜의 운세 정보를 담는 객체로, 일주, 길신/흉신 목록, 종합 점수, 분야별 운세,
 * 조언, 길방위, 길한 색깔 등을 포함합니다.
 * @author 하진영
 * @version 1.0.0
 */
@Data
@Builder
public class DailyFortuneResult {
    private LocalDate date;                     // 대상 날짜
    private String dayPillar;                   // 일주
    private List<SinsalInfo> sinsals;           // 길신/흉신 목록
    private int totalScore;                     // 종합 점수 (0-100)
    private FortuneByCategory categoryFortune;  // 분야별 운세
    private String advice;                      // 조언
    private String luckyDirection;              // 길방위
    private List<String> luckyColors;           // 길한 색깔
}
