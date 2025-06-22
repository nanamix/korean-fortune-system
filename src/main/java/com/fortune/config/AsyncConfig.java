package com.fortune.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.util.concurrent.Executor;

/*
 * 비동기 설정
 * 비동기 태스크 실행자 설정
 */
@Configuration
public class AsyncConfig {

    /*
     * 비동기 태스크 실행자 설정
     * 스레드 풀 태스크 실행기 생성
     * 기본 스레드 수
     * 최대 스레드 수
     * 큐 용량
     * 스레드 이름 접두사
     * 초기화
     * 실행기 반환
     */
    @Bean(name = "fortuneTaskExecutor")
    public Executor fortuneTaskExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();         // 스레드 풀 태스크 실행기 생성
        executor.setCorePoolSize(5);                                        // 기본 스레드 수
        executor.setMaxPoolSize(10);                                        // 최대 스레드 수
        executor.setQueueCapacity(100);                                     // 큐 용량
        executor.setThreadNamePrefix("Fortune-");                           // 스레드 이름 접두사
        executor.initialize();                                              // 초기화
        return executor;                                                    // 실행기 반환
    }
}
