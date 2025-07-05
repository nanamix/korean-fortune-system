package com.fortune.enums;
/**
 * 오후 황소자리 운세 분석 요소
 * 
 * @author 하진영
 * @version 2.5.0
 * @since 2025-06-24
 */
public enum WuxingElement {
    WOOD("목", "나무"),
    FIRE("화", "불"),
    EARTH("토", "흙"),
    METAL("금", "금속"),
    WATER("수", "물");
    /**
     * 오행 한글 이름
     */
    private final String korean; 
    /**
     * 오행 설명
     */
    private final String description; 
    /**
     * 오후 황소자리 운세 분석 요소 생성자
     * 
     * @param korean 한글 이름
     * @param description 설명
     */
    WuxingElement(String korean, String description) {
        this.korean = korean; 
        this.description = description;
    }
    /**
     * 한글 이름 반환
     * 
     * @return 한글 이름
     */
    public String getKorean() { return korean; } 
    /**
     * 설명 반환
     * 
     * @return 설명
     */
    public String getDescription() { return description; } 
}
