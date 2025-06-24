package com.fortune.enums;

/**
 * 별자리 Enum
 * 각 별자리를 한글 이름으로 표현합니다.
 * @author 하진영
 * @version 1.0.0
 */
/**
 * 별자리 열거형
 */
public enum Zodiac {
    ARIES("양자리", "Aries"),
    TAURUS("황소자리", "Taurus"),
    GEMINI("쌍둥이자리", "Gemini"),
    CANCER("게자리", "Cancer"),
    LEO("사자자리", "Leo"),
    VIRGO("처녀자리", "Virgo"),
    LIBRA("천칭자리", "Libra"),
    SCORPIO("전갈자리", "Scorpio"),
    SAGITTARIUS("사수자리", "Sagittarius"),
    CAPRICORN("염소자리", "Capricorn"),
    AQUARIUS("물병자리", "Aquarius"),
    PISCES("물고기자리", "Pisces");

    private final String koreanName;
    private final String englishName;

    Zodiac(String koreanName, String englishName) {
        this.koreanName = koreanName;
        this.englishName = englishName;
    }

    public String getKoreanName() {
        return koreanName;
    }

    public String getEnglishName() {
        return englishName;
    }
}
