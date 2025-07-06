package com.fortune.service;

import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.bots.DefaultAbsSender;
import org.telegram.telegrambots.bots.DefaultBotOptions;
import org.telegram.telegrambots.meta.TelegramBotsApi;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import org.telegram.telegrambots.updatesreceivers.DefaultBotSession;

@Service
public class TelegramService {
    @Value("${app.fortune.telegram.bot-token}")
    private String botToken;

    @Value("${app.fortune.telegram.chat-id}")
    private String chatId;

    private DefaultAbsSender sender;

    @PostConstruct
    public void init() {
        sender = new DefaultAbsSender(new DefaultBotOptions()) {
            @Override
            public String getBotToken() {
                return botToken;
            }
        };
    }

    /**
     * 텔레그램 메시지 전송
     * @param message 전송할 메시지
     * @throws TelegramApiException 전송 실패시 예외
     */
    public void sendMessage(String message) throws TelegramApiException {
        SendMessage sendMessage = new SendMessage();
        sendMessage.setChatId(chatId);
        sendMessage.setText(message);
        sender.execute(sendMessage);
    }
} 