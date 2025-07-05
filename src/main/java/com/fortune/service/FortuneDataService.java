package com.fortune.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;


/**
 * 운세 데이터 서비스
 * 
 * @author 하진영
 * @version 2.5.0
 * @since 2025-06-24
 */
@Slf4j
@Service
@RequiredArgsConstructor
class FortuneDataService {

    /**
     * 일일 운세 조회 (24시간 캐시)
     * 
     * @param date 날짜
     * @param zodiacSign 별자리
     * @return 일일 운세
     */
    @Cacheable(value = "daily-fortune", key = "#date + '_' + #zodiacSign")
    public String getDailyFortune(String date, String zodiacSign) {
        log.info("🔮 일일 운세 생성: {} - {}", date, zodiacSign);
        
        // 실제로는 복잡한 운세 계산 로직이 들어갈 것
        return generateFortuneData(date, zodiacSign);
    }

    /**
     * 운세 데이터 강제 갱신
     * 
     * @param date 날짜
     * @param zodiacSign 별자리
     * @return 일일 운세
     */
    @CachePut(value = "daily-fortune", key = "#date + '_' + #zodiacSign")
    public String refreshDailyFortune(String date, String zodiacSign) {
        log.info("🔄 일일 운세 강제 갱신: {} - {}", date, zodiacSign);
        return generateFortuneData(date, zodiacSign);
    }

    /**
     * 모든 운세 캐시 제거 (자정에 실행)
     * 
     * @author 하진영
     * @version 2.5.0
     * @since 2025-06-24
     */
    @CacheEvict(value = {"daily-fortune", "fortune-data"}, allEntries = true)
    public void clearFortuneCache() {
        log.info("🌅 일일 운세 캐시 초기화");
    }

    /**
     * 운세 데이터 생성
     * 
     * @param date 날짜
     * @param zodiacSign 별자리
     * @return 운세 데이터
     */
    private String generateFortuneData(String date, String zodiacSign) {
        /* 실제 운세 생성 로직 */
        return String.format("%s의 %s 운세: 좋은 일이 생길 것입니다!", date, zodiacSign);
    }
}
