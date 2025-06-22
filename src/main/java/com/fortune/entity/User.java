package com.fortune.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import java.time.LocalDateTime;

/**
 * 사용자 정보
 * 이름
 * 생년월일
 * 생시
 * 생분
 * 성별
 * 양력/음력 구분
 */
@Entity
@Table(name = "users")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id; // 사용자 ID (자동 생성)

    @Column(nullable = false, length = 50)
    private String name; // 이름 / 필수 / 50자 이하

    @Column(nullable = false)
    private Integer birthYear; // 생년 / 필수 / 1900 ~ 2100 사이의 값

    @Column(nullable = false)
    private Integer birthMonth; // 생월 / 필수 / 1 ~ 12 사이의 값

    @Column(nullable = false)
    private Integer birthDay; // 생일 / 필수 / 1 ~ 31 사이의 값

    @Column(nullable = false)
    private Integer birthHour;      // 생시 - 필수

    @Column(nullable = false)
    private Integer birthMinute;    // 생분 - 필수

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Gender gender;          // 성별 - 필수

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private CalendarType calendarType; // 양력/음력 구분 - 필수

    @Column(name = "created_at")
    private LocalDateTime createdAt = LocalDateTime.now(); // 생성 일시 (기본값: 현재 시간)

    public enum Gender {
        M("남성"), F("여성"); // 성별

        private final String description; // 성별 설명

        Gender(String description) {
            this.description = description;
        }

        public String getDescription() {
            return description;
        }
    } // 성별

    public enum CalendarType {
        SOLAR("양력"), LUNAR("음력");

        private final String description;

        CalendarType(String description) {
            this.description = description;
        } // 성별 설명

        public String getDescription() {
            return description;
        }
    } // 양력/음력 구분 
}
