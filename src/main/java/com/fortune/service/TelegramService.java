package com.fortune.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.Map;

@Service
public class TelegramService {
    @Value("${app.fortune.telegram.bot-token}")
    private String botToken;

    @Value("${app.fortune.telegram.chat-id}")
    private String chatId;

    private final RestTemplate restTemplate = new RestTemplate();

    /**
     * 텔레그램 메시지 전송 (REST API 직접 호출)
     * @param message 전송할 메시지
     */
    public void sendMessage(String message) {
        String url = "https://api.telegram.org/bot" + botToken + "/sendMessage";
        Map<String, String> params = new HashMap<>();
        params.put("chat_id", chatId);
        params.put("text", message);
        restTemplate.postForObject(url, params, String.class);
    }
} 