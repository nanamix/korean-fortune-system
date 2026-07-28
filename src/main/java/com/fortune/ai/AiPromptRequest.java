package com.fortune.ai;

public record AiPromptRequest(
        String model,
        String system,
        String user,
        double temperature,
        AiFactPacket factPacket
) {
    public AiPromptRequest {
        factPacket = factPacket == null ? AiFactPacket.empty() : factPacket;
    }

    public AiPromptRequest(String model, String system, String user, double temperature) {
        this(model, system, user, temperature, AiFactPacket.empty());
    }
}
