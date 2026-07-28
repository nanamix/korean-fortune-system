package com.fortune.ai;

/**
 * AI provider 시도의 개인정보 비포함 실행 영수증.
 *
 * <p>fact packet 원문, 사용자 질문, 모델 응답은 저장하지 않는다.</p>
 */
public record AiNarrationReceipt(
        String schemaVersion,
        String engineVersion,
        String domain,
        String factHash,
        String provider,
        String model,
        boolean providerCalled,
        boolean accepted,
        boolean fallbackUsed,
        String validationCode
) {
    public static AiNarrationReceipt from(
            AiPromptRequest request,
            AiFortuneProperties properties,
            boolean providerCalled,
            boolean accepted,
            boolean fallbackUsed,
            String validationCode) {
        AiFactPacket packet = request.factPacket();
        return new AiNarrationReceipt(
                packet.schemaVersion(),
                packet.engineVersion(),
                packet.domain(),
                packet.factHash(),
                properties.provider(),
                request.model(),
                providerCalled,
                accepted,
                fallbackUsed,
                validationCode);
    }
}
