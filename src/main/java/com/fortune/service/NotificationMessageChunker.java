package com.fortune.service;

import java.util.ArrayList;
import java.util.List;

/**
 * 채널 길이 제한에 맞춰 운세 알림을 문단/줄 경계에서 나눈다.
 */
public final class NotificationMessageChunker {

    private NotificationMessageChunker() {
    }

    public static List<String> split(String message, int maxLength) {
        if (message == null || message.isBlank()) {
            return List.of();
        }
        if (maxLength < 16) {
            throw new IllegalArgumentException("메시지 최대 길이는 16자 이상이어야 합니다.");
        }

        String normalized = message.strip();
        List<String> chunks = new ArrayList<>();
        int offset = 0;
        while (offset < normalized.length()) {
            int end = Math.min(offset + maxLength, normalized.length());
            if (end < normalized.length()) {
                int paragraph = normalized.lastIndexOf("\n\n", end);
                int line = normalized.lastIndexOf('\n', end);
                int boundary = paragraph >= offset ? paragraph : line;
                if (boundary > offset) {
                    end = boundary;
                }
            }
            String chunk = normalized.substring(offset, end).strip();
            if (!chunk.isEmpty()) {
                chunks.add(chunk);
            }
            offset = end;
            while (offset < normalized.length() && Character.isWhitespace(normalized.charAt(offset))) {
                offset++;
            }
        }
        return List.copyOf(chunks);
    }
}
