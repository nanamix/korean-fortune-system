package com.fortune.enums;

/**
 * 오후 황소자리 운세 분석 관계
 * 상생
 * 상극
 * 비화
 * 설기
 * 약
 */
public enum WuxingRelation {
    SUPPORT("상생", "서로 돕는 관계"),
    CONFLICT("상극", "서로 대립하는 관계"),
    SAME("비화", "같은 성질"),
    DRAIN("설기", "기운을 빼앗는 관계"),
    WEAK("약", "약한 관계"); // 약한 관계

    private final String korean; // 한글 이름
    private final String description; // 설명

    WuxingRelation(String korean, String description) {
        this.korean = korean; // 한글 이름
        this.description = description; // 설명
    } 

    public String getKorean() { return korean; } // 한글 이름 반환
    public String getDescription() { return description; } // 설명 반환
}
