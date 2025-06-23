package com.fortune.service;

import com.fortune.entity.SecurityAuditLog;
import com.fortune.entity.User;
import com.fortune.repository.SecurityAuditLogRepository;
import com.fortune.repository.UserRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import jakarta.servlet.http.HttpServletRequest;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * 🔍 보안 감사 서비스
 * 
 * <p>시스템의 모든 보안 관련 이벤트를 기록하고 분석하는 서비스입니다.</p>
 * 
 * <h3>주요 기능</h3>
 * <ul>
 *   <li>🔐 로그인/로그아웃 이벤트 기록</li>
 *   <li>🚨 보안 위반 시도 탐지 및 기록</li>
 *   <li>📊 보안 통계 및 분석</li>
 *   <li>🔔 실시간 보안 알림</li>
 *   <li>📋 감사 보고서 생성</li>
 * </ul>
 * 
 * @author 보안팀
 * @version 2.0.0
 * @since 2025-06-23
 */
@Slf4j
@Service
@Transactional
public class SecurityAuditService {

    private final SecurityAuditLogRepository auditLogRepository;
    private final UserRepository userRepository;
    
    @Autowired
    public SecurityAuditService(SecurityAuditLogRepository auditLogRepository, UserRepository userRepository) {
        this.auditLogRepository = auditLogRepository;
        this.userRepository = userRepository;
    }

    /**
     * 🔐 로그인 성공 이벤트 기록
     * 
     * @param username 사용자명
     * @param request HTTP 요청 정보
     */
    @Async
    public void recordLoginSuccess(String username, HttpServletRequest request) {
        try {
            User user = findUserByUsername(username);
            
            SecurityAuditLog auditLog = SecurityAuditLog.builder()
                    .user(user)
                    .action("LOGIN_SUCCESS")
                    .resource("LOGIN")
                    .ipAddress(getClientIpAddress(request))
                    .userAgent(request.getHeader("User-Agent"))
                    .success(true)
                    .timestamp(LocalDateTime.now())
                    .details(String.format("{\"session_id\":\"%s\",\"description\":\"사용자 로그인 성공\"}", 
                            request.getSession().getId()))
                    .build();
            
            auditLogRepository.save(auditLog);
            log.info("🔐 로그인 성공 기록: {} from {}", username, getClientIpAddress(request));
            
        } catch (Exception e) {
            log.error("로그인 성공 이벤트 기록 실패", e);
        }
    }

    /**
     * ❌ 로그인 실패 이벤트 기록
     * 
     * @param username 사용자명
     * @param reason 실패 사유
     * @param request HTTP 요청 정보
     */
    @Async
    public void recordLoginFailure(String username, String reason, HttpServletRequest request) {
        try {
            User user = findUserByUsername(username);
            String ipAddress = getClientIpAddress(request);
            
            SecurityAuditLog auditLog = SecurityAuditLog.builder()
                    .user(user)
                    .action("LOGIN_FAILURE")
                    .resource("LOGIN")
                    .ipAddress(ipAddress)
                    .userAgent(request.getHeader("User-Agent"))
                    .success(false)
                    .timestamp(LocalDateTime.now())
                    .details(String.format("{\"reason\":\"%s\",\"description\":\"로그인 실패\"}", reason))
                    .build();
            
            auditLogRepository.save(auditLog);
            
            // 연속 실패 횟수 확인
            checkFailedLoginAttempts(username, ipAddress);
            
            log.warn("❌ 로그인 실패 기록: {} from {} - {}", username, ipAddress, reason);
            
        } catch (Exception e) {
            log.error("로그인 실패 이벤트 기록 실패", e);
        }
    }

    /**
     * 👋 로그아웃 이벤트 기록
     * 
     * @param username 사용자명
     * @param request HTTP 요청 정보
     */
    @Async
    public void recordLogout(String username, HttpServletRequest request) {
        try {
            User user = findUserByUsername(username);
            
            SecurityAuditLog auditLog = SecurityAuditLog.builder()
                    .user(user)
                    .action("LOGOUT")
                    .resource("LOGIN")
                    .ipAddress(getClientIpAddress(request))
                    .userAgent(request.getHeader("User-Agent"))
                    .success(true)
                    .timestamp(LocalDateTime.now())
                    .details(String.format("{\"session_id\":\"%s\",\"description\":\"사용자 로그아웃\"}", 
                            request.getSession().getId()))
                    .build();
            
            auditLogRepository.save(auditLog);
            log.info("👋 로그아웃 기록: {} from {}", username, getClientIpAddress(request));
            
        } catch (Exception e) {
            log.error("로그아웃 이벤트 기록 실패", e);
        }
    }

    /**
     * 🚨 보안 위반 시도 기록
     * 
     * @param action 액션 타입
     * @param username 사용자명 (없으면 null)
     * @param description 상세 설명
     * @param request HTTP 요청 정보
     */
    @Async
    public void recordSecurityViolation(String action, String username, String description, HttpServletRequest request) {
        try {
            User user = username != null ? findUserByUsername(username) : null;
            String ipAddress = getClientIpAddress(request);
            
            SecurityAuditLog auditLog = SecurityAuditLog.builder()
                    .user(user)
                    .action(action)
                    .resource(request != null ? request.getRequestURI() : "UNKNOWN")
                    .ipAddress(ipAddress)
                    .userAgent(request != null ? request.getHeader("User-Agent") : "UNKNOWN")
                    .success(false)
                    .timestamp(LocalDateTime.now())
                    .details(String.format("{\"description\":\"%s\",\"risk_level\":\"HIGH\"}", description))
                    .build();
            
            auditLogRepository.save(auditLog);
            
            // 심각한 보안 위반은 즉시 알림
            if ("BRUTE_FORCE_ATTACK".equals(action) || "SUSPICIOUS_ACTIVITY".equals(action)) {
                sendSecurityAlert(auditLog);
            }
            
            log.warn("🚨 보안 위반 기록: {} - {} from {}", action, description, ipAddress);
            
        } catch (Exception e) {
            log.error("보안 위반 이벤트 기록 실패", e);
        }
    }

    /**
     * 🔑 권한 관련 이벤트 기록
     * 
     * @param action 액션 타입
     * @param username 사용자명
     * @param resource 접근 시도한 리소스
     * @param success 성공 여부
     * @param request HTTP 요청 정보
     */
    @Async
    public void recordAccessAttempt(String action, String username, String resource, boolean success, HttpServletRequest request) {
        try {
            User user = findUserByUsername(username);
            
            SecurityAuditLog auditLog = SecurityAuditLog.builder()
                    .user(user)
                    .action(action)
                    .resource(resource)
                    .ipAddress(getClientIpAddress(request))
                    .userAgent(request.getHeader("User-Agent"))
                    .success(success)
                    .timestamp(LocalDateTime.now())
                    .details(String.format("{\"description\":\"%s 리소스 접근 %s\",\"risk_level\":\"%s\"}", 
                            success ? "허가된" : "거부된", 
                            success ? "성공" : "실패",
                            success ? "LOW" : "MEDIUM"))
                    .build();
            
            auditLogRepository.save(auditLog);
            
            if (!success) {
                log.warn("🔒 접근 거부: {} 사용자가 {} 리소스 접근 시도", username, resource);
            }
            
        } catch (Exception e) {
            log.error("접근 시도 이벤트 기록 실패", e);
        }
    }

    /**
     * 📊 보안 통계 조회
     * 
     * @param days 조회할 일수
     * @return 보안 통계 정보
     */
    @Transactional(readOnly = true)
    public Map<String, Object> getSecurityStatistics(int days) {
        LocalDateTime fromDate = LocalDateTime.now().minusDays(days);
        
        long totalEvents = auditLogRepository.countByTimestampAfter(fromDate);
        long loginSuccesses = auditLogRepository.countByActionAndTimestampAfter("LOGIN_SUCCESS", fromDate);
        long loginFailures = auditLogRepository.countByActionAndTimestampAfter("LOGIN_FAILURE", fromDate);
        long securityViolations = auditLogRepository.countBySuccessAndTimestampAfter(false, fromDate);
        
        List<Object[]> topFailedIps = auditLogRepository.findTopFailedLoginIps("LOGIN_FAILURE", fromDate);
        
        return Map.of(
                "totalEvents", totalEvents,
                "loginSuccesses", loginSuccesses,
                "loginFailures", loginFailures,
                "securityViolations", securityViolations,
                "loginSuccessRate", (loginSuccesses + loginFailures) > 0 ? 
                        (double) loginSuccesses / (loginSuccesses + loginFailures) * 100 : 0,
                "topFailedIps", topFailedIps,
                "periodDays", days
        );
    }

    /**
     * 🔍 의심스러운 활동 탐지
     * 
     * @param username 사용자명
     * @param clientIp 클라이언트 IP
     * @return 의심스러운 활동 여부
     */
    @Transactional(readOnly = true)
    public boolean detectSuspiciousActivity(String username, String clientIp) {
        LocalDateTime oneHourAgo = LocalDateTime.now().minusHours(1);
        
        // 1시간 내 같은 IP에서 10회 이상 로그인 실패
        long recentFailures = auditLogRepository.countByIpAddressAndActionAndTimestampAfter(
                clientIp, "LOGIN_FAILURE", oneHourAgo);
        
        if (recentFailures >= 10) {
            recordSecurityViolation(
                    "BRUTE_FORCE_ATTACK",
                    username,
                    String.format("1시간 내 %d회 로그인 실패 시도", recentFailures),
                    null
            );
            return true;
        }
        
        // 동시에 여러 IP에서 로그인 시도
        User user = findUserByUsername(username);
        if (user != null) {
            List<String> recentIps = auditLogRepository.findDistinctIpAddressesByUserIdAndTimestampAfter(
                    user.getId(), oneHourAgo);
            
            if (recentIps.size() >= 5) {
                recordSecurityViolation(
                        "SUSPICIOUS_ACTIVITY",
                        username,
                        String.format("1시간 내 %d개 서로 다른 IP에서 로그인 시도", recentIps.size()),
                        null
                );
                return true;
            }
        }
        
        return false;
    }

    /**
     * 🧹 오래된 감사 로그 정리
     * 
     * @param daysToKeep 보관할 일수
     * @return 삭제된 로그 수
     */
    @Transactional
    public long cleanupOldAuditLogs(int daysToKeep) {
        LocalDateTime cutoffDate = LocalDateTime.now().minusDays(daysToKeep);
        long deletedCount = auditLogRepository.deleteByTimestampBefore(cutoffDate);
        
        log.info("🧹 오래된 감사 로그 정리 완료: {}일 이전 로그 {}건 삭제", daysToKeep, deletedCount);
        
        return deletedCount;
    }

    // ========== Private 메서드 ==========

    /**
     * 사용자명으로 사용자 찾기
     */
    private User findUserByUsername(String username) {
        if (username == null) {
            return null;
        }
        
        Optional<User> userOpt = userRepository.findByUsername(username);
        if (userOpt.isEmpty()) {
            userOpt = userRepository.findByEmail(username);
        }
        
        return userOpt.orElse(null);
    }

    /**
     * 연속 로그인 실패 횟수 확인
     */
    private void checkFailedLoginAttempts(String username, String clientIp) {
        LocalDateTime oneHourAgo = LocalDateTime.now().minusHours(1);
        User user = findUserByUsername(username);
        
        if (user != null) {
            long recentFailures = auditLogRepository.countByUserIdAndActionAndTimestampAfter(
                    user.getId(), "LOGIN_FAILURE", oneHourAgo);
            
            if (recentFailures >= 5) {
                recordSecurityViolation(
                        "BRUTE_FORCE_ATTACK",
                        username,
                        String.format("1시간 내 %d회 로그인 실패", recentFailures),
                        null
                );
            }
        }
    }

    /**
     * 클라이언트 IP 주소 추출
     */
    private String getClientIpAddress(HttpServletRequest request) {
        if (request == null) {
            return "unknown";
        }
        
        String xForwardedFor = request.getHeader("X-Forwarded-For");
        if (xForwardedFor != null && !xForwardedFor.isEmpty() && !"unknown".equalsIgnoreCase(xForwardedFor)) {
            return xForwardedFor.split(",")[0].trim();
        }
        
        String xRealIp = request.getHeader("X-Real-IP");
        if (xRealIp != null && !xRealIp.isEmpty() && !"unknown".equalsIgnoreCase(xRealIp)) {
            return xRealIp;
        }
        
        return request.getRemoteAddr();
    }

    /**
     * 보안 알림 전송
     */
    private void sendSecurityAlert(SecurityAuditLog auditLog) {
        // 실제 구현에서는 이메일, Slack, SMS 등으로 알림 전송
        log.error("🚨 보안 알림: {} - {} from {}", 
                auditLog.getAction(), 
                auditLog.getDetails(), 
                auditLog.getIpAddress());
        
        // TODO: 알림 서비스 연동
        // notificationService.sendSecurityAlert(auditLog);
    }
}