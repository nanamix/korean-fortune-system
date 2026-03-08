package com.fortune.enums;
/**
 * 오행 ENUM
 * 
 * @author 하진영
 * @version 2.5.0
 * @since 2025-06-24
 */
public enum Wuxing {
    WOOD("목", "木"),
    FIRE("화", "火"),
    EARTH("토", "土"),
    METAL("금", "金"),
    WATER("수", "水");
    /**
     * 오행 한글 이름
     */
    private final String korean;
    /**
     * 오행 한자 이름
     */
    private final String chinese;
    /**
     * 오행 생성자
     * 
     * @param korean 오행 한글 이름
     * @param chinese 오행 한자 이름
     */
    Wuxing(String korean, String chinese) {
        this.korean = korean;
        this.chinese = chinese;
    }
    /**
     * 오행 한글 이름 반환
     * 
     * @return 오행 한글 이름
     */
    public String getKorean() {
        return korean;
    }
    /**
     * 오행 한자 이름 반환
     * 
     * @return 오행 한자 이름
     */
    public String getChinese() {
        return chinese;
    }
}
