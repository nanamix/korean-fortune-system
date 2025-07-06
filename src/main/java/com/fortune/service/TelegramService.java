package com.fortune.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.Map;

@Slf4j
@Service
public class TelegramService {
    @Value("${app.fortune.telegram.bot-token}")
    private String botToken;

    @Value("${app.fortune.telegram.chat-id}")
    private String chatId;

    private final RestTemplate restTemplate = new RestTemplate();

    public TelegramService() {
        log.info("📱 TelegramService 초기화");
    }

    public void sendMessage(String message, String chatId) {
        log.info("📱 텔레그램 메시지 전송 시도");
        log.info("📱 Bot Token: {}", botToken != null && !botToken.isEmpty() ? "설정됨" : "설정되지 않음");
        log.info("📱 Chat ID: {}", chatId != null && !chatId.isEmpty() ? "설정됨" : "설정되지 않음");
        
        if (botToken == null || botToken.isEmpty() || chatId == null || chatId.isEmpty()) {
            log.warn("⚠️ 텔레그램 설정이 완료되지 않았습니다. 메시지 전송을 건너뜁니다.");
            return;
        }

        try {
            String url = "https://api.telegram.org/bot" + botToken + "/sendMessage";
            Map<String, String> params = new HashMap<>();
            params.put("chat_id", chatId);
            params.put("text", message);
            
            log.info("📱 텔레그램 API 호출: {}", url);
            String response = restTemplate.postForObject(url, params, String.class);
            log.info("📱 텔레그램 메시지 전송 완료: {}", response);
        } catch (Exception e) {
            log.error("❌ 텔레그램 메시지 전송 실패", e);
        }
    }

    // 기존 메서드는 기본 chatId 사용
    public void sendMessage(String message) {
        sendMessage(message, chatId);
    }
} 