package com.fortune.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 대상일 행성과 출생 차트 기준점 사이의 주요 각(aspect).
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AstrologyTransit {
    private String transitingBody;
    private String natalPoint;
    private String aspect;
    private double orb;
    private int scoreAdjustment;
    private String interpretation;
}
