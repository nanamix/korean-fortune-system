package com.fortune.ai;

public record AiPromptResponse(
        String content,
        String provider,
        boolean fallback
) {
}
