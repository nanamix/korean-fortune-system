package com.fortune.repository;
import com.fortune.entity.SecurityAuditLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.time.LocalDateTime;
import java.util.List;
/**
 * 보안 감사 로그 JPA 리포지토리
 * 원인: SecurityAuditLog 엔티티의 DB CRUD 지원
 * 결과: 감사 로그 저장/조회/삭제 등 JPA 기본 기능 제공
 * 
 * @author 하진영
 * @version 2.5.0
 * @since 2025-06-24
 */
@Repository
public interface SecurityAuditLogRepository extends JpaRepository<SecurityAuditLog, Long> {
    /**
     * 시간 이후 전체 이벤트 수
     * - 시간 이후 전체 이벤트 수를 조회합니다.
     * SQL: SELECT COUNT(*) FROM security_audit_log WHERE timestamp > :fromDate
     * @param fromDate 시간
     * @return 시간 이후 전체 이벤트 수
     */
    long countByTimestampAfter(LocalDateTime fromDate);
    /**
     * 액션별, 시간 이후 이벤트 수
     * SQL: SELECT COUNT(*) FROM security_audit_log WHERE action = :action AND timestamp > :fromDate
     * @param action 액션
     * @param fromDate 시간
     * @return 액션별, 시간 이후 이벤트 수
     */
    long countByActionAndTimestampAfter(String action, LocalDateTime fromDate);
    /**
     * 성공 여부별, 시간 이후 이벤트 수
     * SQL: SELECT COUNT(*) FROM security_audit_log WHERE success = :success AND timestamp > :fromDate
     * @param success 성공 여부
     * @param fromDate 시간
     * @return 성공 여부별, 시간 이후 이벤트 수
     */
    long countBySuccessAndTimestampAfter(Boolean success, LocalDateTime fromDate);
    /**
     * 클라이언트 IP, 액션, 시간 이후 이벤트 수
     * SQL: SELECT COUNT(*) FROM security_audit_log WHERE ip_address = :ipAddress AND action = :action AND timestamp > :fromDate
     * @param ipAddress 클라이언트 IP
     * @param action 액션
     * @param fromDate 시간
     * @return 클라이언트 IP, 액션, 시간 이후 이벤트 수
     */
    long countByIpAddressAndActionAndTimestampAfter(String ipAddress, String action, LocalDateTime fromDate);
    /**
     * 사용자 ID, 액션, 시간 이후 이벤트 수
     * SQL: SELECT COUNT(*) FROM security_audit_log WHERE user_id = :userId AND action = :action AND timestamp > :fromDate
     * @param userId 사용자 ID
     * @param action 액션
     * @param fromDate 시간
     * @return 사용자 ID, 액션, 시간 이후 이벤트 수
     */
    long countByUserIdAndActionAndTimestampAfter(Long userId, String action, LocalDateTime fromDate);
    /**
     * 시간 이전 이벤트 삭제
     * SQL: DELETE FROM security_audit_log WHERE timestamp < :cutoffDate
     * @param cutoffDate 시간
     * @return 시간 이전 이벤트 삭제
     */
    long deleteByTimestampBefore(LocalDateTime cutoffDate);
    /**
     * 사용자 ID, 시간 이후 접속 IP 목록
     * SQL: SELECT DISTINCT ip_address FROM security_audit_log WHERE user_id = :userId AND timestamp > :fromDate
     * @param userId 사용자 ID
     * @param fromDate 시간
     * @return 사용자 ID, 시간 이후 접속 IP 목록
     */
    @Query("SELECT DISTINCT s.ipAddress FROM SecurityAuditLog s WHERE s.user.id = :userId AND s.timestamp > :fromDate")
    List<String> findDistinctIpAddressesByUserIdAndTimestampAfter(@Param("userId") Long userId, @Param("fromDate") LocalDateTime fromDate);
    /**
     * 실패 로그인 IP TOP N
     * SQL: SELECT ip_address, COUNT(*) as cnt FROM security_audit_log WHERE action = :action AND success = false AND timestamp > :fromDate GROUP BY ip_address ORDER BY cnt DESC
     * @param action 액션
     * @param fromDate 시간
     * @return 실패 로그인 IP TOP N
     */
    @Query("SELECT s.ipAddress, COUNT(s) as cnt FROM SecurityAuditLog s WHERE s.action = :action AND s.success = false AND s.timestamp > :fromDate GROUP BY s.ipAddress ORDER BY cnt DESC")
    List<Object[]> findTopFailedLoginIps(@Param("action") String action, @Param("fromDate") LocalDateTime fromDate);
    /**
     * 사용자별 최근 로그 조회
     * SQL: SELECT s FROM SecurityAuditLog s WHERE s.user.id = :userId ORDER BY s.timestamp DESC
     * @param userId 사용자 ID
     * @return 사용자별 최근 로그
     */
    @Query("SELECT s FROM SecurityAuditLog s WHERE s.user.id = :userId ORDER BY s.timestamp DESC")
    List<SecurityAuditLog> findByUserIdOrderByTimestampDesc(@Param("userId") Long userId);
} 