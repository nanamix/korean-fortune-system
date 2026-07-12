package com.fortune.config;

import com.github.benmanes.caffeine.cache.Caffeine;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cache.caffeine.CaffeineCache;
import org.springframework.cache.support.SimpleCacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.cache.RedisCacheConfiguration;
import org.springframework.data.redis.cache.RedisCacheManager;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.RedisSerializationContext;

import java.time.Duration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * 🗄️ 캐시 설정 클래스
 *
 * <p>캐시 백엔드는 {@code spring.cache.type} 으로 선택한다:</p>
 * <ul>
 *   <li><b>caffeine</b> (기본) — 인메모리, 이름별 TTL + 최대 크기.</li>
 *   <li><b>redis</b> — Redis/Valkey(와이어 호환), 이름별 TTL + JSON 직렬화.</li>
 * </ul>
 *
 * <p>캐시 이름·TTL 은 {@link #CACHES} 한 곳에서 정의해 두 백엔드가 공유한다.
 * ({@code maxSize} 는 Caffeine 에서만 의미, Redis 는 TTL 기반.)</p>
 *
 * @author 하진영
 * @version 3.1.0
 * @since 2025-06-24
 */
@Configuration
@EnableCaching
public class CacheConfig {

    /** 캐시 스펙: 이름 / TTL(초) / 최대 엔트리(Caffeine 전용). */
    private record CacheSpec(String name, int ttlSeconds, int maxSize) {}

    private static final List<CacheSpec> CACHES = List.of(
            new CacheSpec("users",                  1800,   500),
            new CacheSpec("daily-fortune",          3600,  1000),
            new CacheSpec("year-pillar",            86400,  200),
            new CacheSpec("day-pillar",             86400,  500),
            new CacheSpec("blacklist",              3600, 10000),
            new CacheSpec("ai-saju-interpretation", 86400,  100),
            new CacheSpec("ai-daily-advice",        86400,  100),
            new CacheSpec("ai-zodiac-advice",       86400,  100),
            new CacheSpec("ai-tojeong-advice",      86400,  100),
            new CacheSpec("fortune-data",           3600,   500),
            new CacheSpec("zodiac-fortune",         3600,   300)
    );

    /**
     * 기본: Caffeine 인메모리 CacheManager. (spring.cache.type 미지정 또는 caffeine)
     */
    @Bean
    @ConditionalOnProperty(name = "spring.cache.type", havingValue = "caffeine", matchIfMissing = true)
    public CacheManager caffeineCacheManager() {
        SimpleCacheManager cacheManager = new SimpleCacheManager();
        cacheManager.setCaches(CACHES.stream().map(this::caffeineCache).toList());
        return cacheManager;
    }

    /**
     * Redis/Valkey CacheManager. (spring.cache.type=redis)
     * Valkey 는 Redis 프로토콜 호환이라 동일 스타터·설정으로 동작한다.
     * 값은 JSON 직렬화, 이름별 TTL 적용.
     */
    @Bean
    @ConditionalOnProperty(name = "spring.cache.type", havingValue = "redis")
    public CacheManager redisCacheManager(RedisConnectionFactory connectionFactory) {
        RedisCacheConfiguration base = RedisCacheConfiguration.defaultCacheConfig()
                .serializeValuesWith(RedisSerializationContext.SerializationPair.fromSerializer(
                        new GenericJackson2JsonRedisSerializer()))
                .disableCachingNullValues();

        Map<String, RedisCacheConfiguration> perCache = new HashMap<>();
        for (CacheSpec c : CACHES) {
            perCache.put(c.name(), base.entryTtl(Duration.ofSeconds(c.ttlSeconds())));
        }
        return RedisCacheManager.builder(connectionFactory)
                .cacheDefaults(base)
                .withInitialCacheConfigurations(perCache)
                .build();
    }

    private CaffeineCache caffeineCache(CacheSpec c) {
        return new CaffeineCache(c.name(),
                Caffeine.newBuilder()
                        .maximumSize(c.maxSize())
                        .expireAfterWrite(c.ttlSeconds(), TimeUnit.SECONDS)
                        .recordStats()
                        .build());
    }
}
