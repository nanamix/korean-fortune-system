package com.fortune.enums;

/**
 * 오후 황소자리 운세 분석 요소
 * 목
 * 화
 * 토
 * 금
 * 수
 */
public enum WuxingElement {
    WOOD("목", "나무"),
    FIRE("화", "불"),
    EARTH("토", "흙"),
    METAL("금", "금속"),
    WATER("수", "물");

    private final String korean; // 한글 이름
    private final String description; // 설명

    WuxingElement(String korean, String description) {
        this.korean = korean; // 한글 이름
        this.description = description; // 설명
    }

    public String getKorean() { return korean; } // 한글 이름 반환
    public String getDescription() { return description; } // 설명 반환
}
