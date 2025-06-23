package com.fortune.security;

import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * 🔑 JWT 토큰 유틸리티 클래스
 * 
 * <p>JWT 토큰 생성, 검증, 정보 추출을 담당합니다.</p>
 */
@Slf4j
@Component
public class JwtTokenUtil {

    @Value("${jwt.secret:mySecretKey123456789012345678901234567890}")
    private String jwtSecret;

    @Value("${jwt.expiration:86400}") // 24시간 (초 단위)
    private int jwtExpirationInSeconds;

    @Value("${jwt.refresh-expiration:604800}") // 7일 (초 단위)
    private int jwtRefreshExpirationInSeconds;

    /**
     * JWT 토큰에서 사용자명 추출
     */
    public String getUsernameFromToken(String token) {
        return getClaimFromToken(token, Claims::getSubject);
    }

    /**
     * JWT 토큰에서 만료일 추출
     */
    public Date getExpirationDateFromToken(String token) {
        return getClaimFromToken(token, Claims::getExpiration);
    }

    /**
     * JWT 토큰에서 권한 목록 추출
     */
    @SuppressWarnings("unchecked")
    public Collection<String> getAuthoritiesFromToken(String token) {
        Claims claims = getAllClaimsFromToken(token);
        List<String> authorities = (List<String>) claims.get("authorities");
        return authorities != null ? authorities : Collections.emptyList();
    }

    /**
     * JWT 토큰에서 특정 클레임 추출
     */
    public <T> T getClaimFromToken(String token, Function<Claims, T> claimsResolver) {
        final Claims claims = getAllClaimsFromToken(token);
        return claimsResolver.apply(claims);
    }

    /**
     * JWT 토큰에서 모든 클레임 추출
     */
    private Claims getAllClaimsFromToken(String token) {
        try {
            return Jwts.parserBuilder()
                    .setSigningKey(getSigningKey())
                    .build()
                    .parseClaimsJws(token)
                    .getBody();
        } catch (JwtException e) {
            log.error("❌ JWT 토큰 파싱 오류: {}", e.getMessage());
            throw e;
        }
    }

    /**
     * JWT 토큰 만료 여부 확인
     */
    private Boolean isTokenExpired(String token) {
        final Date expiration = getExpirationDateFromToken(token);
        return expiration.before(new Date());
    }

    /**
     * 사용자 정보로 JWT 토큰 생성
     */
    public String generateToken(UserDetails userDetails) {
        Map<String, Object> claims = new HashMap<>();
        
        // 권한 정보를 토큰에 포함
        Collection<String> authorities = userDetails.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .collect(Collectors.toList());
        claims.put("authorities", authorities);
        
        return createToken(claims, userDetails.getUsername(), jwtExpirationInSeconds);
    }

    /**
     * 리프레시 토큰 생성
     */
    public String generateRefreshToken(UserDetails userDetails) {
        Map<String, Object> claims = new HashMap<>();
        claims.put("type", "refresh");
        return createToken(claims, userDetails.getUsername(), jwtRefreshExpirationInSeconds);
    }

    /**
     * JWT 토큰 생성 (내부 메서드)
     */
    private String createToken(Map<String, Object> claims, String subject, int expirationSeconds) {
        Date now = new Date();
        Date expiryDate = new Date(now.getTime() + expirationSeconds * 1000L);

        return Jwts.builder()
                .setClaims(claims)
                .setSubject(subject)
                .setIssuedAt(now)
                .setExpiration(expiryDate)
                .signWith(getSigningKey(), SignatureAlgorithm.HS512)
                .compact();
    }

    /**
     * JWT 토큰 유효성 검증
     */
    public Boolean validateToken(String token, UserDetails userDetails) {
        try {
            final String username = getUsernameFromToken(token);
            return (username.equals(userDetails.getUsername()) && !isTokenExpired(token));
        } catch (JwtException e) {
            log.error("❌ JWT 토큰 검증 실패: {}", e.getMessage());
            return false;
        }
    }

    /**
     * 리프레시 토큰 유효성 검증
     */
    public Boolean validateRefreshToken(String token) {
        try {
            Claims claims = getAllClaimsFromToken(token);
            String tokenType = (String) claims.get("type");
            return "refresh".equals(tokenType) && !isTokenExpired(token);
        } catch (JwtException e) {
            log.error("❌ 리프레시 토큰 검증 실패: {}", e.getMessage());
            return false;
        }
    }

    /**
     * 토큰에서 남은 만료 시간 반환 (밀리초)
     */
    public Long getExpirationTime(String token) {
        Date expiration = getExpirationDateFromToken(token);
        return expiration.getTime() - System.currentTimeMillis();
    }

    /**
     * 서명 키 생성
     */
    private SecretKey getSigningKey() {
        byte[] keyBytes = jwtSecret.getBytes();
        return Keys.hmacShaKeyFor(keyBytes);
    }

    /**
     * 토큰이 곧 만료되는지 확인 (30분 이내)
     */
    public Boolean isTokenNearExpiry(String token) {
        try {
            Long remainingTime = getExpirationTime(token);
            return remainingTime < 30 * 60 * 1000; // 30분
        } catch (Exception e) {
            return true;
        }
    }
}