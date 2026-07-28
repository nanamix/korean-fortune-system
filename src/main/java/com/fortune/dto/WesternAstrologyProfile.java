package com.fortune.dto;

import com.fortune.enums.Zodiac;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 출생 시각과 장소에서 결정론적으로 계산한 서양 점성술 핵심 프로필.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class WesternAstrologyProfile {
    private String calculationModel;
    private String precision;
    private Zodiac sunSign;
    private double sunDegree;
    private Zodiac moonSign;
    private double moonDegree;
    private Zodiac risingSign;
    private Double risingDegree;
    private String element;
    private String modality;
    private String rulingPlanet;
    private int decan;
    private String natalMoonPhase;
    private String summary;
}
