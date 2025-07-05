package com.fortune.repository;

import com.fortune.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * 👤 사용자 저장소 인터페이스
 * 
 * <p>사용자 엔티티에 대한 데이터베이스 접근을 담당하는 Repository입니다.</p>
 * 
 * <h3>주요 기능</h3>
 * <ul>
 *   <li>이메일/사용자명으로 사용자 조회</li>
 *   <li>OAuth2 제공자별 사용자 조회</li>
 *   <li>활성/비활성 사용자 관리</li>
 *   <li>사용자 통계 조회</li>
 * </ul>
 * 
 * @author 데이터팀
 * @version 2.0.0
 * @since 2025-06-23
 */
@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    /**
     * 🔍 이메일로 사용자 조회
     * SQL: SELECT * FROM users WHERE email = :email
     * @param email 이메일 주소
     * @return 사용자 정보 (Optional)
     */
    Optional<User> findByEmail(String email);

    /**
     * 🔍 사용자명으로 사용자 조회
     * SQL: SELECT * FROM users WHERE username = :username
     * @param username 사용자명
     * @return 사용자 정보 (Optional)
     */
    Optional<User> findByUsername(String username);

    /**
     * 🔍 OAuth2 제공자와 제공자 ID로 사용자 조회
     * SQL: SELECT u FROM User u WHERE u.authProvider = :provider AND u.providerId = :providerId
     * @param provider OAuth2 제공자 (google, kakao 등)
     * @param providerId 제공자에서의 사용자 ID
     * @return 사용자 정보 (Optional)
     */
    @Query("SELECT u FROM User u WHERE u.authProvider = :provider AND u.providerId = :providerId")
    Optional<User> findByAuthProviderAndProviderId(@Param("provider") String provider, 
                                                   @Param("providerId") String providerId);

    /**
     * ✅ 이메일 중복 확인
     * SQL: SELECT EXISTS(SELECT 1 FROM users WHERE email = :email)
     * @param email 확인할 이메일
     * @return 존재 여부
     */
    boolean existsByEmail(String email);

    /**
     * ✅ 사용자명 중복 확인
     * SQL: SELECT EXISTS(SELECT 1 FROM users WHERE username = :username)
     * @param username 확인할 사용자명
     * @return 존재 여부
     */
    boolean existsByUsername(String username);

    /**
     * 👥 활성 사용자 목록 조회
     * SQL: SELECT u FROM User u WHERE u.enabled = true
     * @return 활성 사용자 목록
     */
    @Query("SELECT u FROM User u WHERE u.enabled = true")
    List<User> findAllActiveUsers();

    /**
     * 📅 특정 기간 내 생성된 사용자 조회
     * SQL: SELECT u FROM User u WHERE u.createdAt BETWEEN :startDate AND :endDate
     * @param startDate 시작 날짜
     * @param endDate 종료 날짜
     * @return 해당 기간 사용자 목록
     */
    @Query("SELECT u FROM User u WHERE u.createdAt BETWEEN :startDate AND :endDate")
    List<User> findUsersCreatedBetween(@Param("startDate") LocalDateTime startDate, 
                                      @Param("endDate") LocalDateTime endDate);

    /**
     * 📊 OAuth2 제공자별 사용자 수 조회
     * SQL: SELECT COUNT(u) FROM User u WHERE u.authProvider = :provider
     * @param provider OAuth2 제공자
     * @return 사용자 수
     */
    @Query("SELECT COUNT(u) FROM User u WHERE u.authProvider = :provider")
    long countByAuthProvider(@Param("provider") String provider);

    /**
     * 📊 전체 활성 사용자 수 조회
     * SQL: SELECT COUNT(u) FROM User u WHERE u.enabled = true
     * @return 활성 사용자 수
     */
    @Query("SELECT COUNT(u) FROM User u WHERE u.enabled = true")
    long countActiveUsers();

    /**
     * 🔍 이메일 패턴으로 사용자 검색
     * SQL: SELECT u FROM User u WHERE u.email LIKE %:emailPattern%
     * @param emailPattern 이메일 패턴 (LIKE 검색)
     * @return 매칭되는 사용자 목록
     */
    @Query("SELECT u FROM User u WHERE u.email LIKE %:emailPattern%")
    List<User> findByEmailContaining(@Param("emailPattern") String emailPattern);

    /**
     * 🔍 이름으로 사용자 검색
     * SQL: SELECT u FROM User u WHERE u.name LIKE %:name%
     * @param name 이름 (부분 검색)
     * @return 매칭되는 사용자 목록
     */
    @Query("SELECT u FROM User u WHERE u.name LIKE %:name%")
    List<User> findByNameContaining(@Param("name") String name);

    /**
     * ⏰ 마지막 로그인 시간 업데이트
     * SQL: UPDATE users SET last_login_at = :lastLoginAt WHERE id = :userId
     * @param userId 사용자 ID
     * @param lastLoginAt 마지막 로그인 시간
     */
    @Query("UPDATE User u SET u.lastLoginAt = :lastLoginAt WHERE u.id = :userId")
    void updateLastLoginAt(@Param("userId") Long userId, 
                          @Param("lastLoginAt") LocalDateTime lastLoginAt);

    /**
     * 🔒 계정 잠금 상태 변경
     * SQL: UPDATE users SET account_locked = :locked WHERE id = :userId
     * @param userId 사용자 ID
     * @param locked 잠금 여부
     */
    @Query("UPDATE User u SET u.accountLocked = :locked WHERE u.id = :userId")
    void updateAccountLocked(@Param("userId") Long userId, @Param("locked") boolean locked);

    /**
     * 🔑 비밀번호 변경
     * SQL: UPDATE users SET password = :newPassword, password_changed_at = CURRENT_TIMESTAMP WHERE id = :userId
     * @param userId 사용자 ID
     * @param newPassword 새 비밀번호 (암호화된)
     */
    @Query("UPDATE User u SET u.password = :newPassword, u.passwordChangedAt = CURRENT_TIMESTAMP WHERE u.id = :userId")
    void updatePassword(@Param("userId") Long userId, @Param("newPassword") String newPassword);

    /**
     * 📧 이메일 인증 상태 업데이트
     * SQL: UPDATE users SET email_verified = :verified, email_verified_at = CURRENT_TIMESTAMP WHERE id = :userId
     * @param userId 사용자 ID
     * @param verified 인증 여부
     */
    @Query("UPDATE User u SET u.emailVerified = :verified, u.emailVerifiedAt = CURRENT_TIMESTAMP WHERE u.id = :userId")
    void updateEmailVerified(@Param("userId") Long userId, @Param("verified") boolean verified);

    /**
     * 🗑️ 비활성화된 사용자 목록 조회 (정리용)
     * SQL: SELECT u FROM User u WHERE u.enabled = false AND u.updatedAt < :cutoffDate
     * @param daysAgo 며칠 전부터
     * @return 비활성화된 사용자 목록
     */
    @Query("SELECT u FROM User u WHERE u.enabled = false AND u.updatedAt < :cutoffDate")
    List<User> findDisabledUsersOlderThan(@Param("cutoffDate") LocalDateTime cutoffDate);

    /**
     * 📊 사용자 통계 조회 (네이티브 쿼리)
     * SQL: SELECT COUNT(*) as total_users, SUM(CASE WHEN enabled = true THEN 1 ELSE 0 END) as active_users, SUM(CASE WHEN auth_provider = 'LOCAL' THEN 1 ELSE 0 END) as local_users, SUM(CASE WHEN auth_provider = 'GOOGLE' THEN 1 ELSE 0 END) as google_users, SUM(CASE WHEN auth_provider = 'KAKAO' THEN 1 ELSE 0 END) as kakao_users, SUM(CASE WHEN DATE(created_at) = CURDATE() THEN 1 ELSE 0 END) as today_signups FROM users
     * @return 사용자 통계 정보
     */
    @Query(value = """
        SELECT 
            COUNT(*) as total_users,
            SUM(CASE WHEN enabled = true THEN 1 ELSE 0 END) as active_users,
            SUM(CASE WHEN auth_provider = 'LOCAL' THEN 1 ELSE 0 END) as local_users,
            SUM(CASE WHEN auth_provider = 'GOOGLE' THEN 1 ELSE 0 END) as google_users,
            SUM(CASE WHEN auth_provider = 'KAKAO' THEN 1 ELSE 0 END) as kakao_users,
            SUM(CASE WHEN DATE(created_at) = CURDATE() THEN 1 ELSE 0 END) as today_signups
        FROM users
        """, nativeQuery = true)
    Object[] getUserStatistics();

    /**
     * 🔍 삭제되지 않은 사용자명으로 조회
     * SQL: SELECT u FROM User u WHERE u.username = :username AND u.deletedAt IS NULL
     * @param username 사용자명
     * @return 삭제되지 않은 사용자명으로 조회
     */
    Optional<User> findByUsernameAndDeletedAtIsNull(String username);

    /**
     * 🔍 삭제되지 않은 이메일로 조회
     * SQL: SELECT u FROM User u WHERE u.email = :email AND u.deletedAt IS NULL
     * @param email 이메일
     * @return 삭제되지 않은 이메일로 조회
     */
    Optional<User> findByEmailAndDeletedAtIsNull(String email);
}