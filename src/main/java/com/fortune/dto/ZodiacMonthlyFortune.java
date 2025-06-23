package com.fortune.dto;

import lombok.Builder;
import lombok.Data;
import java.util.List;
import java.util.Map;

@Data
@Builder
public class ZodiacMonthlyFortune {
    private Map<Integer, ZodiacMonthInfo> monthlyInfo; // 월별 정보 (1-12월)
    private String yearlyTrend;                        // 연간 전체 트렌드
    private List<Integer> bestMonths;                  // 최고의 달들
    private List<Integer> cautionMonths;               // 주의할 달들
}
