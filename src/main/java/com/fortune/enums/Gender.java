package com.fortune.enums;
/**
 * 성별 ENUM
 * 
 * @author 하진영
 * @version 2.5.0
 * @since 2025-06-24
 */
enum Gender {
    MALE("남성", "M"),
    FEMALE("여성", "F");
    private final String korean;
    private final String code;
    /**
     * 성별 생성자
     * 
     * @param korean 성별 한글 이름
     * @param code 성별 코드
     */
    Gender(String korean, String code) {
        this.korean = korean;
        this.code = code;
    }
    /**
     * 성별 한글 이름 반환
     * 
     * @return 성별 한글 이름
     */
    public String getKorean() {
        return korean;
    }
    /**
     * 성별 코드 반환
     * 
     * @return 성별 코드
     */
    public String getCode() {
        return code;
    }
}
