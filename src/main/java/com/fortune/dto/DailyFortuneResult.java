package com.fortune.dto;

import lombok.Builder;
import lombok.Data;
import java.time.LocalDate;
import java.util.List;

/**
 * 일일 운세 결과
 * 날짜
 * 일간 지지
 * 천덕, 월덕, 천희, 월살, 일살
 * 오늘의 오후 황소자리 운세
 * 종합 점수
 * 분야별 운세
 * 조언
 */
@Data
@Builder
public class DailyFortuneResult {
    private LocalDate date;   // 날짜
    private String dayPillar; // 일간 지지
    private List<SinsalInfo> sinsals; // 천덕, 월덕, 천희, 월살, 일살
    private WuxingAnalysis wuxingAnalysis; // 오늘의 오후 황소자리 운세
    private int totalScore; // 종합 점수
    private FortuneByCategory categoryFortune; // 분야별 운세
    private String advice; // 조언
    private String luckyDirection; // 길방위
    private List<String> luckyColors; // 길한 색깔
}
