package com.fortune.enums;

/**
 * 별자리
 * 한글 이름
 * 요소
 */
public enum Zodiac {
    ARIES("양자리", "불"),
    TAURUS("황소자리", "땅"),
    GEMINI("쌍둥이자리", "공기"),
    CANCER("게자리", "물"),
    LEO("사자자리", "불"),
    VIRGO("처녀자리", "땅"),
    LIBRA("천칭자리", "공기"),
    SCORPIO("전갈자리", "물"),
    SAGITTARIUS("사수자리", "불"),
    CAPRICORN("염소자리", "땅"),
    AQUARIUS("물병자리", "공기"),
    PISCES("물고기자리", "물");

    private final String koreanName; // 한글 이름
    private final String element; // 요소

    Zodiac(String koreanName, String element) {
        this.koreanName = koreanName; // 한글 이름
        this.element = element;
    }

    public String getKoreanName() { return koreanName; } // 한글 이름 반환
    public String getElement() { return element; } // 요소 반환
}
