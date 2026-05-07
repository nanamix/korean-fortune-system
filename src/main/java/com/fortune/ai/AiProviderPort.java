package com.fortune.ai;

public interface AiProviderPort {
    AiPromptResponse complete(AiPromptRequest request);
}
