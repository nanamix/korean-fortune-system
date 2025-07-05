package com.fortune.config;

import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.cache.CacheManager;
import org.springframework.cache.caffeine.CaffeineCacheManager;
import com.github.benmanes.caffeine.cache.Caffeine;
import java.util.concurrent.TimeUnit;

/**
 * 🗄️ 캐시 설정 클래스
 *
 * Caffeine을 사용하여 캐시를 설정합니다.
 * - 최대 크기: 1000
 * - 만료 시간: 1시간
 * 
 * @author 하진영
 * @version 2.5.0
 * @since 2025-06-24
 */
@Configuration
@EnableCaching
public class CacheConfig {

    /**
     * 캐시 관리자 설정
     * 
     * @return 캐시 관리자
     */
    @Bean
    public CacheManager cacheManager() {
        // 캐시 관리자 생성
        CaffeineCacheManager cacheManager = new CaffeineCacheManager();

        // 캐시 설정
        cacheManager.setCaffeine(Caffeine.newBuilder()
                .maximumSize(1000)
                .expireAfterWrite(1, TimeUnit.HOURS)
                .recordStats());
        return cacheManager;
    }
}
