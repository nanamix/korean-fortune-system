package com.fortune.ai;

public record AiPromptRequest(
        String model,
        String system,
        String user,
        double temperature
) {
}
