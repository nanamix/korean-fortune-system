package com.fortune.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;
import java.util.Set;

/**
 * 👤 사용자 엔티티
 * 
 * <p>시스템의 사용자 정보를 담는 핵심 엔티티입니다.</p>
 * 
 * <h3>주요 기능</h3>
 * <ul>
 *   <li>일반 회원가입 사용자 정보 관리</li>
 *   <li>OAuth2 소셜 로그인 사용자 정보 관리</li>
 *   <li>계정 상태 및 보안 정보 관리</li>
 *   <li>사주 운세 관련 개인 정보 저장</li>
 * </ul>
 * 
 * @author 데이터팀
 * @version 2.0.0
 * @since 2025-06-23
 */
@Entity
@Table(name = "users", indexes = {
    @Index(name = "idx_users_email", columnList = "email"),
    @Index(name = "idx_users_username", columnList = "username"),
    @Index(name = "idx_users_provider", columnList = "auth_provider, provider_id"),
    @Index(name = "idx_users_created_at", columnList = "created_at"),
    @Index(name = "idx_users_birth_date", columnList = "birth_year, birth_month, birth_day")
})
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // ========== 기본 정보 ==========
    
    @Column(nullable = false, length = 50)
    private String name;

    @Column(unique = true, length = 100)
    private String username;

    @Column(nullable = false, unique = true, length = 100)
    private String email;

    @Column(length = 255)
    private String password;

    @Column(name = "profile_image_url", length = 500)
    private String profileImageUrl;

    // ========== 사주 관련 생년월일 정보 ==========
    
    @Column(name = "birth_year")
    private Integer birthYear;

    @Column(name = "birth_month")
    private Integer birthMonth;

    @Column(name = "birth_day")
    private Integer birthDay;

    @Column(name = "birth_hour")
    private Integer birthHour;

    @Column(name = "birth_minute")
    private Integer birthMinute;

    @Enumerated(EnumType.STRING)
    @Column(length = 10)
    private Gender gender;

    @Enumerated(EnumType.STRING)
    @Column(name = "calendar_type", length = 10)
    private CalendarType calendarType;

    @Column(name = "birth_location", length = 100)
    private String birthLocation; // 출생지 (태양시 보정용)

    // ========== OAuth2 관련 정보 ==========
    
    @Enumerated(EnumType.STRING)
    @Column(name = "auth_provider", nullable = false, length = 20)
    @Builder.Default
    private AuthProvider authProvider = AuthProvider.LOCAL;

    @Column(name = "provider_id", length = 100)
    private String providerId;

    // ========== 계정 상태 정보 ==========
    
    @ElementCollection(fetch = FetchType.EAGER)
    @Enumerated(EnumType.STRING)
    @CollectionTable(name = "user_roles", joinColumns = @JoinColumn(name = "user_id"))
    @Column(name = "role")
    private Set<Role> roles;

    @Column(name = "enabled")
    @Builder.Default
    private boolean enabled = true;

    @Column(name = "account_locked")
    @Builder.Default
    private Boolean accountLocked = false;

    @Column(name = "email_verified")
    @Builder.Default
    private Boolean emailVerified = false;

    @Column(name = "email_verified_at")
    private LocalDateTime emailVerifiedAt;

    @Column(name = "last_login_at")
    private LocalDateTime lastLoginAt;

    @Column(name = "login_count")
    @Builder.Default
    private Integer loginCount = 0;

    @Column(name = "failed_login_attempts")
    @Builder.Default
    private Integer failedLoginAttempts = 0;

    @Column(name = "password_changed_at")
    private LocalDateTime passwordChangedAt;

    // ========== 사용자 설정 ==========
    
    @Enumerated(EnumType.STRING)
    @Column(name = "preferred_language", length = 10)
    @Builder.Default
    private Language preferredLanguage = Language.KOREAN;

    @Column(name = "timezone", length = 50)
    @Builder.Default
    private String timezone = "Asia/Seoul";

    @Column(name = "ai_features_enabled")
    @Builder.Default
    private Boolean aiFeaturesEnabled = true;

    @Column(name = "notification_enabled")
    @Builder.Default
    private Boolean notificationEnabled = true;

    @Column(name = "marketing_emails_enabled")
    @Builder.Default
    private Boolean marketingEmailsEnabled = false;

    // ========== 타임스탬프 ==========
    
    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    @Column(name = "deleted_at")
    private LocalDateTime deletedAt;

    // ========== Enum 정의 ==========
    
    /**
     * 사용자 역할 Enum
     * 
     * @author 하진영
     * @version 2.5.0
     * @since 2025-06-24
     */
    public enum Role {
        USER("일반 사용자"),
        ADMIN("관리자"),
        MODERATOR("모더레이터"),
        PREMIUM("프리미엄 사용자");

        /**
         * 사용자 역할 설명
         */
        private final String description;

        /**
         * 사용자 역할 생성자
         *
         * @param description 사용자 역할 설명
         */
        Role(String description) {
            this.description = description;
        }

        /**
         * 사용자 역할 설명 반환
         * 
         * @return 사용자 역할 설명
         */
        public String getDescription() {
            return description;
        }
    }

    /**
     * 성별 Enum
     * 
     * @author 하진영
     * @version 2.5.0
     * @since 2025-06-24
     */
    public enum Gender {
        M("남성"), 
        F("여성");

        private final String description;

        Gender(String description) {
            this.description = description;
        }

        public String getDescription() {
            return description;
        }
    }

    /**
     * 달력 타입 Enum
     * 
     * @author 하진영
     * @version 2.5.0
     * @since 2025-06-24
     */
    public enum CalendarType {
        SOLAR("양력"), 
        LUNAR("음력");

        /**
         * 달력 타입 설명
         */
        private final String description;

        /**
         * 달력 타입 생성자
         * 
         * @param description 달력 타입 설명
         */
        CalendarType(String description) {
            this.description = description;
        }

        /**
         * 달력 타입 설명 반환
         * 
         * @return 달력 타입 설명
         */
        public String getDescription() {
            return description;
        }
    }

    /**
     * 인증 제공자 Enum
     * 
     * @author 하진영
     * @version 2.5.0
     * @since 2025-06-24
     */
    public enum AuthProvider {
        LOCAL("로컬"),
        GOOGLE("구글"),
        KAKAO("카카오"),
        NAVER("네이버"),
        FACEBOOK("페이스북");

        /**
         * 인증 제공자 설명
         */
        private final String description;

        /**
         * 인증 제공자 생성자
         * 
         * @param description 인증 제공자 설명
         */
        AuthProvider(String description) {
            this.description = description;
        }

        /**
         * 인증 제공자 설명 반환
         * 
         * @return 인증 제공자 설명
         */
        public String getDescription() {
            return description;
        }
    }

    /**
     * 언어 설정 Enum
     * 
     * @author 하진영
     * @version 2.5.0
     * @since 2025-06-24
     */
    public enum Language {
        KOREAN("ko", "한국어"),
        ENGLISH("en", "English"),
        JAPANESE("ja", "日本語"),
        CHINESE("zh", "中文");

        private final String code;
        private final String description;

        /**
         * 언어 생성자
         * 
         * @param code 언어 코드
         * @param description 언어 설명
         */
        Language(String code, String description) {
            this.code = code;
            this.description = description;
        }

        /**
         * 언어 코드 반환
         * 
         * @return 언어 코드
         */
        public String getCode() {
            return code;
        }

        /**
         * 언어 설명 반환
         * 
         * @return 언어 설명
         */
        public String getDescription() {
            return description;
        }
    }

    // ========== 비즈니스 메서드 ==========
    
    /**
     * 📅 출생일 정보 완전성 확인
     * 
     * @return 출생일 정보가 모두 입력되었는지 여부
     */
    public boolean hasBirthDateInfo() {
        return birthYear != null && birthMonth != null && birthDay != null;
    }

    /**
     * ⏰ 출생시간 정보 완전성 확인
     * 
     * @return 출생시간 정보가 모두 입력되었는지 여부
     */
    public boolean hasBirthTimeInfo() {
        return birthHour != null && birthMinute != null;
    }

    /**
     * 🔒 OAuth2 사용자 여부 확인
     * 
     * @return OAuth2 로그인 사용자인지 여부
     */
    public boolean isOAuth2User() {
        return authProvider != AuthProvider.LOCAL;
    }

    /**
     * ✅ 이메일 인증 완료 여부
     * 
     * @return 이메일 인증 상태
     */
    public boolean isEmailVerified() {
        return Boolean.TRUE.equals(emailVerified);
    }

    /**
     * 🔐 계정 활성화 상태 확인
     * 
     * @return 계정 사용 가능 여부
     */
    public boolean isAccountActive() {
        return Boolean.TRUE.equals(enabled) && 
               !Boolean.TRUE.equals(accountLocked) && 
               deletedAt == null;
    }

    /**
     * 🚨 로그인 실패 횟수 증가
     * 
     * @author 하진영
     * @version 2.5.0
     * @since 2025-06-24
     */
    public void incrementFailedLoginAttempts() {
        this.failedLoginAttempts = (this.failedLoginAttempts == null) ? 1 : this.failedLoginAttempts + 1;
        
        // 5회 실패 시 계정 잠금
        if (this.failedLoginAttempts >= 5) {
            this.accountLocked = true;
        }
    }

    /**
     * ✅ 로그인 성공 처리
     *  
     * @author 하진영
     * @version 2.5.0
     * @since 2025-06-24
     */
    public void handleSuccessfulLogin() {
        this.lastLoginAt = LocalDateTime.now();
        this.loginCount = (this.loginCount == null) ? 1 : this.loginCount + 1;
        this.failedLoginAttempts = 0;
        this.accountLocked = false;
    }

    /**
     * 📧 이메일 인증 완료 처리
     * 
     * @author 하진영
     * @version 2.5.0
     * @since 2025-06-24
     */
    public void verifyEmail() {
        this.emailVerified = true;
        this.emailVerifiedAt = LocalDateTime.now();
    }

    /**
     * 🔑 비밀번호 변경 처리
     * 
     * @author 하진영
     * @version 2.5.0
     * @since 2025-06-24
     * @param newPassword 새 비밀번호 (암호화된)
     */
    public void changePassword(String newPassword) {
        this.password = newPassword;
        this.passwordChangedAt = LocalDateTime.now();
    }

    /**
     * 🗑️ 소프트 삭제 처리
     */
    public void softDelete() {
        this.deletedAt = LocalDateTime.now();
        this.enabled = false;
    }

    /**
     * 🔄 OAuth2 정보 업데이트
     * 
     * @param provider 인증 제공자
     * @param providerId 제공자 ID
     * @param name 이름
     * @param email 이메일
     * @param profileImageUrl 프로필 이미지 URL
     */
    public void updateOAuth2Info(AuthProvider provider, String providerId, 
                                String name, String email, String profileImageUrl) {
        this.authProvider    = provider;
        this.providerId      = providerId;
        this.name            = name; 
        this.email           = email;
        this.profileImageUrl = profileImageUrl; 
        this.emailVerified   = true; // OAuth2는 이메일 인증 완료로 간주
        this.emailVerifiedAt = LocalDateTime.now();
    }

    /**
     * 사용자 역할 조회
     * 
     * @return 사용자 역할
     */
    public Set<Role> getRoles() {
        return roles;
    }

    /**
     * 사용자 활성화 상태 확인
     * 
     * @return 사용자 활성화 상태
     */
    public boolean isEnabled() {
        return enabled;
    }

    // ========== toString, equals, hashCode 최적화 ==========
    
    /**
     * 사용자 정보 문자열 표현
     * 
     * @return 사용자 정보 문자열
     */
    @Override
    public String toString() {
        return "User{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", email='" + email + '\'' +
                ", authProvider=" + authProvider +
                ", enabled=" + enabled +
                ", emailVerified=" + emailVerified +
                ", createdAt=" + createdAt +
                '}';
    }

    /**
     * 사용자 정보 비교
     * 
     * @param o 비교 대상
     * @return 동일한 사용자인지 여부
     */
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof User)) return false;
        User user = (User) o;
        return id != null && id.equals(user.getId());
    }

    /**
     * 사용자 정보 해시 코드 생성
     * 
     * @return 사용자 정보 해시 코드
     */
    @Override
    public int hashCode() {
        return getClass().hashCode();
    }
}