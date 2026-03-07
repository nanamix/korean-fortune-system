package com.fortune.config;

import com.github.benmanes.caffeine.cache.Caffeine;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cache.caffeine.CaffeineCache;
import org.springframework.cache.support.SimpleCacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * 🗄️ 캐시 설정 클래스
 *
 * <p>Caffeine을 사용하여 캐시 이름별로 독립된 TTL 과 최대 크기를 설정합니다.</p>
 *
 * <ul>
 *   <li>users           – 사용자 정보 (30분/1800초, 최대 500)</li>
 *   <li>daily-fortune   – 일일 운세   (1시간, 최대 1000)</li>
 *   <li>year-pillar     – 연주 계산   (24시간, 최대 200)</li>
 *   <li>day-pillar      – 일주 계산   (24시간, 최대 500)</li>
 *   <li>blacklist       – JWT 블랙리스트 (1시간, 최대 10000)</li>
 *   <li>ai-*            – AI 응답    (24시간, 최대 100)</li>
 *   <li>fortune-data    – 운세 데이터  (1시간, 최대 500)</li>
 *   <li>zodiac-fortune  – 별자리 운세  (1시간, 최대 300)</li>
 * </ul>
 *
 * @author 하진영
 * @version 2.6.0
 * @since 2025-06-24
 */
@Configuration
@EnableCaching
public class CacheConfig {

    /**
     * 캐시 이름별 독립 설정을 가진 CacheManager 를 빈으로 등록합니다.
     *
     * @return {@link SimpleCacheManager} 인스턴스
     */
    @Bean
    public CacheManager cacheManager() {
        SimpleCacheManager cacheManager = new SimpleCacheManager();
        cacheManager.setCaches(List.of(
                caffeineCache("users",                    1800,       500),
                caffeineCache("daily-fortune",            3600,      1000),
                caffeineCache("year-pillar",              86400,      200),
                caffeineCache("day-pillar",               86400,      500),
                caffeineCache("blacklist",                3600,     10000),
                caffeineCache("ai-saju-interpretation",   86400,      100),
                caffeineCache("ai-daily-advice",          86400,      100),
                caffeineCache("ai-zodiac-advice",         86400,      100),
                caffeineCache("ai-tojeong-advice",        86400,      100),
                caffeineCache("fortune-data",             3600,       500),
                caffeineCache("zodiac-fortune",           3600,       300)
        ));
        return cacheManager;
    }

    /**
     * 지정된 TTL 과 최대 크기로 {@link CaffeineCache} 를 생성합니다.
     *
     * @param name       캐시 이름
     * @param ttlSeconds 만료 시간(초)
     * @param maxSize    최대 엔트리 수
     * @return 설정된 {@link CaffeineCache}
     */
    private CaffeineCache caffeineCache(String name, int ttlSeconds, int maxSize) {
        return new CaffeineCache(name,
                Caffeine.newBuilder()
                        .maximumSize(maxSize)
                        .expireAfterWrite(ttlSeconds, TimeUnit.SECONDS)
                        .recordStats()
                        .build());
    }
}

