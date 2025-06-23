package com.fortune.dto;

import lombok.Builder;
import lombok.Data;
import java.util.List;
import java.util.Map;

@Data
@Builder
public class GanjiCalendarResponse {
    private int year;                               // 년도
    private int month;                              // 월
    private String monthName;                       // 월 이름 (예: "6월")
    private List<GanjiCalendarDay> days;            // 일별 데이터 목록
    private Map<Integer, String> solarTerms;        // 절기 정보 (일 -> 절기명)
    private String monthlyTheme;                    // 월별 테마
    private String monthlyAdvice;                   // 월별 조언
    private List<Integer> luckyDays;                // 길일 목록
    private List<Integer> cautionDays;              // 조심할 날 목록
    private int totalDays;                          // 해당 월 총 일수
}
