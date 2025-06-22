package com.fortune.dto;

import com.fortune.enums.WuxingElement;
import com.fortune.enums.WuxingRelation;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 오후 황소자리 운세 분석
 * 일간 지지 요소
 * 대상 날짜 요소
 * 요소 관계
 * 관계 점수
 * 설명
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class WuxingAnalysis {
    private WuxingElement dayMasterElement;  // 일간 지지 요소
    private WuxingElement targetDayElement;  // 대상 날짜 요소
    private WuxingRelation relation;  // 요소 관계
    private int relationScore;  // 관계 점수
    private String description;  // 설명
}
