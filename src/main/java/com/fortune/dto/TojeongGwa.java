package com.fortune.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 토정과 정보 DTO
 * 토정과 이름
 * 토정과 기호
 * 토정과 요약
 * 토정과 상세 운세
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class TojeongGwa {
    private String name;  // 토정과 이름
    private String symbol;  // 토정과 기호
    private String summary;  // 토정과 요약
    private String detailedFortune;  // 토정과 상세 운세
} 