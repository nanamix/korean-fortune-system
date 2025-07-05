package com.fortune.enums;

/**
 * 오후 황소자리 운세 분석 관계
 * 
 * @author 하진영
 * @version 2.5.0
 * @since 2025-06-24
 */
public enum WuxingRelation {
    SUPPORT("상생", "서로 돕는 관계"),
    CONFLICT("상극", "서로 대립하는 관계"),
    SAME("비화", "같은 성질"),
    DRAIN("설기", "기운을 빼앗는 관계"),
    WEAK("약", "약한 관계"); // 약한 관계

    /**
     * 오후 황소자리 운세 분석 관계 한글 이름
     */
    private final String korean; 

    /**
     * 오후 황소자리 운세 분석 관계 설명
     */
    private final String description; 

    /**
     * 오후 황소자리 운세 분석 관계 생성자
     * 
     * @param korean 한글 이름
     * @param description 설명
     */
    WuxingRelation(String korean, String description) {
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
