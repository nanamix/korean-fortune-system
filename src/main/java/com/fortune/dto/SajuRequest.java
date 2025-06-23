package com.fortune.dto;

public class SajuRequest {
    private int birthYear;
    private int birthMonth;
    private int birthDay;
    private int birthHour;
    private int birthMinute;
    private String gender;
    private String calendarType;

    // 기본 생성자
    public SajuRequest() {
    }

    // 모든 필드를 포함한 생성자
    public SajuRequest(int birthYear, int birthMonth, int birthDay, int birthHour, int birthMinute, String gender, String calendarType) {
        this.birthYear = birthYear;
        this.birthMonth = birthMonth;
        this.birthDay = birthDay;
        this.birthHour = birthHour;
        this.birthMinute = birthMinute;
        this.gender = gender;
        this.calendarType = calendarType;
    }

    // Getter 및 Setter
    public int getBirthYear() {
        return birthYear;
    }

    public void setBirthYear(int birthYear) {
        this.birthYear = birthYear;
    }

    public int getBirthMonth() {
        return birthMonth;
    }

    public void setBirthMonth(int birthMonth) {
        this.birthMonth = birthMonth;
    }

    public int getBirthDay() {
        return birthDay;
    }

    public void setBirthDay(int birthDay) {
        this.birthDay = birthDay;
    }

    public int getBirthHour() {
        return birthHour;
    }

    public void setBirthHour(int birthHour) {
        this.birthHour = birthHour;
    }

    public int getBirthMinute() {
        return birthMinute;
    }

    public void setBirthMinute(int birthMinute) {
        this.birthMinute = birthMinute;
    }

    public String getGender() {
        return gender;
    }

    public void setGender(String gender) {
        this.gender = gender;
    }

    public String getCalendarType() {
        return calendarType;
    }

    public void setCalendarType(String calendarType) {
        this.calendarType = calendarType;
    }

    // Builder 클래스
    public static class Builder {
        private int birthYear;
        private int birthMonth;
        private int birthDay;
        private int birthHour;
        private int birthMinute;
        private String gender;
        private String calendarType;

        public Builder birthYear(int birthYear) {
            this.birthYear = birthYear;
            return this;
        }

        public Builder birthMonth(int birthMonth) {
            this.birthMonth = birthMonth;
            return this;
        }

        public Builder birthDay(int birthDay) {
            this.birthDay = birthDay;
            return this;
        }

        public Builder birthHour(int birthHour) {
            this.birthHour = birthHour;
            return this;
        }

        public Builder birthMinute(int birthMinute) {
            this.birthMinute = birthMinute;
            return this;
        }

        public Builder gender(String gender) {
            this.gender = gender;
            return this;
        }

        public Builder calendarType(String calendarType) {
            this.calendarType = calendarType;
            return this;
        }

        public SajuRequest build() {
            return new SajuRequest(birthYear, birthMonth, birthDay, birthHour, birthMinute, gender, calendarType);
        }
    }

    public static Builder builder() {
        return new Builder();
    }
}
