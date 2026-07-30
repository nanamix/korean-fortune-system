package com.fortune.service;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;
import org.junit.jupiter.api.Test;

class NotificationMessageChunkerTest {

    @Test
    void preservesCompleteMessageAcrossParagraphChunks() {
        String message = "첫 번째 문단입니다.\n\n두 번째 문단은 조금 더 긴 내용입니다.\n세 번째 줄입니다.";

        List<String> chunks = NotificationMessageChunker.split(message, 24);

        assertThat(chunks).hasSizeGreaterThan(1);
        assertThat(chunks).allMatch(chunk -> chunk.length() <= 24);
        assertThat(String.join("", chunks).replaceAll("\\s+", ""))
                .isEqualTo(message.replaceAll("\\s+", ""));
    }
}
