package com.fortune.config;

import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.JavaMailSenderImpl;

import java.util.Properties;

/**
 * 테스트용 설정
 * 
 * @author 하진영
 * @version 2.6.0
 * @since 2025-01-05
 */
@TestConfiguration
public class TestConfig {

    /**
     * 테스트용 Mock JavaMailSender 빈
     * 실제 메일 발송 없이 테스트 환경에서 EmailService가 정상 동작하도록 함
     */
    @Bean
    @Primary
    public JavaMailSender mockJavaMailSender() {
        JavaMailSenderImpl mailSender = new JavaMailSenderImpl();
        mailSender.setHost("localhost");
        mailSender.setPort(1025); // 테스트용 포트
        mailSender.setUsername("test@test.com");
        mailSender.setPassword("test");
        
        Properties props = mailSender.getJavaMailProperties();
        props.put("mail.transport.protocol", "smtp");
        props.put("mail.smtp.auth", "false");
        props.put("mail.smtp.starttls.enable", "false");
        props.put("mail.debug", "false");
        
        return mailSender;
    }
} 