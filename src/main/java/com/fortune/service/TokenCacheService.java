package com.fortune.service;

import com.fortune.entity.User;
import com.fortune.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

/**
 * 🔒 JWT 토큰 캐시 서비스
 */
@Slf4j
@Service
@RequiredArgsConstructor
class TokenCacheService {

    /**
     * 토큰 블랙리스트 추가
     */
    @CachePut(value = "blacklist", key = "#token", cacheManager = "tokenCacheManager")
    public boolean addToBlacklist(String token) {
        log.info("🚫 토큰 블랙리스트 추가");
        return true;
    }

    /**
     * 토큰 블랙리스트 확인
     */
    @Cacheable(value = "blacklist", key = "#token", cacheManager = "tokenCacheManager")
    public boolean isTokenBlacklisted(String token) {
        log.info("🔍 토큰 블랙리스트 확인");
        // 실제로는 데이터베이스에서 확인
        return false;
    }

    /**
     * 블랙리스트 캐시 제거
     */
    @CacheEvict(value = "blacklist", allEntries = true, cacheManager = "tokenCacheManager")
    public void clearBlacklist() {
        log.info("🧹 토큰 블랙리스트 캐시 제거");
    }
}