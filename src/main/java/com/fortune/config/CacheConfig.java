package com.fortune.config;

import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cache.concurrent.ConcurrentMapCacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;

import java.util.Arrays;

/*
 * 캐시 설정
 * 캐시 관리자 설정
 * 매일 새벽 3시에 캐시 정리
 * 캐시 정리 스케줄러 설정
 */
@Configuration
@EnableCaching
@EnableScheduling
public class CacheConfig {

    /*
     * 캐시 관리자 설정
     * 캐시 이름 설정
     * 캐시 관리자 반환
     */
    @Bean
    public CacheManager cacheManager() {
        ConcurrentMapCacheManager cacheManager = new ConcurrentMapCacheManager();    // 동시성 맵 캐시 관리자 생성
        cacheManager.setCacheNames(Arrays.asList(
                "dailyFortune",
                "sajuData",
                "ganjiCalendar",
                "tojeongFortune",
                "zodiacFortune"
        ));                                                                         // 캐시 이름 설정
        return cacheManager;                                                        // 캐시 관리자 반환
    }

    /*
     * 매일 새벽 3시에 캐시 정리
     * 캐시 정리 스케줄러 설정
     */
    @Scheduled(cron = "0 0 3 * * *")
    public void clearDailyCache() {
        var dailyFortuneCache = cacheManager().getCache("dailyFortune");            // 일일 운세 캐시 정리
        if (dailyFortuneCache != null) {
            dailyFortuneCache.clear();
        }
        
        var ganjiCalendarCache = cacheManager().getCache("ganjiCalendar");          // 간지달력 캐시 정리
        if (ganjiCalendarCache != null) {
            ganjiCalendarCache.clear();
        }

        var tojeongFortuneCache = cacheManager().getCache("tojeongFortune");        // 토정비결 캐시 정리
        if (tojeongFortuneCache != null) {
            tojeongFortuneCache.clear();
        }

        var zodiacFortuneCache = cacheManager().getCache("zodiacFortune");          // 별자리 운세 캐시 정리
        if (zodiacFortuneCache != null) {
            zodiacFortuneCache.clear();
        }

        var sajuDataCache = cacheManager().getCache("sajuData");                    // 사주 데이터 캐시 정리
        if (sajuDataCache != null) {
            sajuDataCache.clear();
        }

    }
}
