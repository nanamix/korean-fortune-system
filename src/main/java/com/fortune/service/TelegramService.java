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
    @Value("${app.fortune.telegram.bot-token:}")
    private String botToken;

    @Value("${app.fortune.telegram.chat-id:}")
    private String chatId;

    private final RestTemplate restTemplate = new RestTemplate();

    public TelegramService() {
        log.info("📱 TelegramService 초기화");
    }

    public void sendMessage(String message, String chatId) {
        log.info("📱 텔레그램 메시지 전송 시도");
        log.info("📱 Bot Token: {}", botToken != null && !botToken.isEmpty() ? "설정됨" : "설정되지 않음");
        log.info("📱 Chat ID: {}", chatId != null && !chatId.isEmpty() ? "설정됨" : "설정되지 않음");

        try {
            sendTestMessage(message, chatId);
        } catch (Exception e) {
            log.error("❌ 텔레그램 메시지 전송 실패", e);
        }
    }

    /**
     * 설정과 실제 Telegram API 호출을 검증하는 테스트 전송. 실패 시 호출자에게 예외를 전달한다.
     */
    public void sendTestMessage(String message, String targetChatId) {
        if (message == null || message.isBlank()) {
            throw new IllegalArgumentException("텔레그램 테스트 메시지는 필수입니다.");
        }
        if (botToken == null || botToken.isBlank()) {
            throw new IllegalStateException("텔레그램 봇 토큰이 설정되지 않았습니다.");
        }
        String resolvedChatId = targetChatId == null || targetChatId.isBlank() ? chatId : targetChatId;
        if (resolvedChatId == null || resolvedChatId.isBlank()) {
            throw new IllegalStateException("텔레그램 채팅 ID가 설정되지 않았습니다.");
        }

        String url = "https://api.telegram.org/bot" + botToken + "/sendMessage";
        Map<String, String> params = new HashMap<>();
        params.put("chat_id", resolvedChatId);
        params.put("text", message);

        log.info("📱 텔레그램 API 호출");
        String response = restTemplate.postForObject(url, params, String.class);
        log.info("📱 텔레그램 메시지 전송 완료: {}", response);
    }

    // 기존 메서드는 기본 chatId 사용
    public void sendMessage(String message) {
        sendMessage(message, chatId);
    }
}
