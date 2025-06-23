package com.fortune.enums;

/**
 * 별자리 Enum
 * 각 별자리를 한글 이름으로 표현합니다.
 * @author 하진영
 * @version 1.0.0
 */
public enum Zodiac {
    ARIES("양자리"),
    TAURUS("황소자리"),
    GEMINI("쌍둥이자리"),
    CANCER("게자리"),
    LEO("사자자리"),
    VIRGO("처녀자리"),
    LIBRA("천칭자리"),
    SCORPIO("전갈자리"),
    SAGITTARIUS("사수자리"),
    CAPRICORN("염소자리"),
    AQUARIUS("물병자리"),
    PISCES("물고기자리");

    private final String koreanName;

    Zodiac(String koreanName) {
        this.koreanName = koreanName;
    }

    public String getKoreanName() {
        return koreanName;
    }
}
