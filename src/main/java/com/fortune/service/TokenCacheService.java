package com.fortune.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

/**
 * 🔒 JWT 토큰 캐시 서비스
 * 
 * @author 하진영
 * @version 2.5.0
 * @since 2025-06-24
 */
@Slf4j
@Service
@RequiredArgsConstructor
class TokenCacheService {

    /**
     * 토큰 블랙리스트 추가
     * SQL: INSERT INTO blacklist (token) VALUES (?);
     * @param token 토큰
     * @return 토큰 블랙리스트 추가 여부
     */
    @CachePut(value = "blacklist", key = "#token")
    public boolean addToBlacklist(String token) {
        log.info("🚫 토큰 블랙리스트 추가");
        return true;
    }

    /**
     * 토큰 블랙리스트 확인
     * SQL: SELECT * FROM blacklist WHERE token = ?;
     * @param token 토큰
     * @return 토큰 블랙리스트 확인 여부
     */
    @Cacheable(value = "blacklist", key = "#token")
    public boolean isTokenBlacklisted(String token) {
        log.info("🔍 토큰 블랙리스트 확인");
        // 실제로는 데이터베이스에서 확인
        return false;
    }

    /**
     * 블랙리스트 캐시 제거
     * SQL: DELETE FROM blacklist;
     * @return 토큰 블랙리스트 캐시 제거 여부
     */
    @CacheEvict(value = "blacklist", allEntries = true)
    public void clearBlacklist() {
        log.info("🧹 토큰 블랙리스트 캐시 제거");
    }
}
