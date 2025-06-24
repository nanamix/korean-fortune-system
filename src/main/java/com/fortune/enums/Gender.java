package com.fortune.enums;

/**
 * 성별 열거형
 */
enum Gender {
    MALE("남성", "M"),
    FEMALE("여성", "F");

    private final String korean;
    private final String code;

    Gender(String korean, String code) {
        this.korean = korean;
        this.code = code;
    }

    public String getKorean() {
        return korean;
    }

    public String getCode() {
        return code;
    }
}
