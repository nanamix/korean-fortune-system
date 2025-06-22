package com.fortune;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.scheduling.annotation.EnableAsync;

/**
 * 한국 운세 애플리케이션
 * @author 하진영
 * @version 1.0
 * @since 2025-06-21
 */
@SpringBootApplication
@EnableCaching
@EnableAsync
public class KoreanFortuneApplication {

    /**
     * 메인 메서드
     * @param args 인자
     */
    public static void main(String[] args) {
        SpringApplication.run(KoreanFortuneApplication.class, args);
    }

}
