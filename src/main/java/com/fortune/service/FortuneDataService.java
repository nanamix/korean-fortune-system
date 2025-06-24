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

@Slf4j
@Service
@RequiredArgsConstructor
class FortuneDataService {

    /**
     * 일일 운세 조회 (24시간 캐시)
     */
    @Cacheable(value = "daily-fortune", key = "#date + '_' + #zodiacSign")
    public String getDailyFortune(String date, String zodiacSign) {
        log.info("🔮 일일 운세 생성: {} - {}", date, zodiacSign);
        
        // 실제로는 복잡한 운세 계산 로직이 들어갈 것
        return generateFortuneData(date, zodiacSign);
    }

    /**
     * 운세 데이터 강제 갱신
     */
    @CachePut(value = "daily-fortune", key = "#date + '_' + #zodiacSign")
    public String refreshDailyFortune(String date, String zodiacSign) {
        log.info("🔄 일일 운세 강제 갱신: {} - {}", date, zodiacSign);
        return generateFortuneData(date, zodiacSign);
    }

    /**
     * 모든 운세 캐시 제거 (자정에 실행)
     */
    @CacheEvict(value = {"daily-fortune", "fortune-data"}, allEntries = true)
    public void clearFortuneCache() {
        log.info("🌅 일일 운세 캐시 초기화");
    }

    private String generateFortuneData(String date, String zodiacSign) {
        // 실제 운세 생성 로직
        return String.format("%s의 %s 운세: 좋은 일이 생길 것입니다!", date, zodiacSign);
    }
}
