package com.fortune.ai;

import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.springframework.stereotype.Component;

/**
 * LLM 서술이 결정론적 fact packet을 뒤집지 않는지 검사한다.
 */
@Component
public class AiNarrationValidator {
    private static final int MAX_RESPONSE_LENGTH = 12_000;
    private static final Pattern DANGEROUS_HTML =
            Pattern.compile("<\\s*/?\\s*(script|iframe|object|embed|style|link|meta)\\b", Pattern.CASE_INSENSITIVE);
    private static final Pattern DAY_MASTER =
            Pattern.compile("([갑을병정무기경신임계])(?:목|화|토|금|수)?(?:\\([^)]*\\))?\\s*일간");
    private static final Pattern DAY_PILLAR_SUFFIX =
            Pattern.compile("([갑을병정무기경신임계][자축인묘진사오미신유술해])\\s*일주");
    private static final Pattern DAY_PILLAR_PREFIX =
            Pattern.compile("일주(?:는|가|:)?\\s*([갑을병정무기경신임계][자축인묘진사오미신유술해])");
    private static final Pattern TOTAL_SCORE =
            Pattern.compile("(?:종합|전체|총)\\s*점수(?:는|가|:)?\\s*(\\d{1,3})\\s*점");
    private static final Pattern GWA_NUMBER =
            Pattern.compile("괘\\s*(?:번호)?(?:는|가|:)?\\s*(\\d{1,3})\\s*번?");

    public ValidationResult validate(AiPromptRequest request, String content) {
        if (content == null || content.isBlank()) {
            return ValidationResult.invalid("EMPTY_RESPONSE", "외부 AI가 빈 응답을 반환했습니다.");
        }
        if (content.length() > MAX_RESPONSE_LENGTH) {
            return ValidationResult.invalid("RESPONSE_TOO_LONG", "외부 AI 응답이 허용 길이를 초과했습니다.");
        }
        if (DANGEROUS_HTML.matcher(content).find()) {
            return ValidationResult.invalid("UNSAFE_HTML", "외부 AI 응답에 허용하지 않은 HTML이 포함됐습니다.");
        }

        AiFactPacket packet = request.factPacket();
        if ("saju".equals(packet.domain())) {
            ValidationResult dayMaster = matchesExpected(content, DAY_MASTER, packet.fact("day_master"), "일간");
            if (!dayMaster.valid()) {
                return dayMaster;
            }
            ValidationResult suffix = matchesExpected(content, DAY_PILLAR_SUFFIX, packet.fact("day_pillar"), "일주");
            if (!suffix.valid()) {
                return suffix;
            }
            ValidationResult prefix = matchesExpected(content, DAY_PILLAR_PREFIX, packet.fact("day_pillar"), "일주");
            if (!prefix.valid()) {
                return prefix;
            }
        }

        String totalScore = packet.fact("total_score");
        if (!totalScore.isBlank()) {
            ValidationResult score = matchesExpected(content, TOTAL_SCORE, totalScore, "종합 점수");
            if (!score.valid()) {
                return score;
            }
        }

        String gwaNumber = packet.fact("gwa_number");
        if (!gwaNumber.isBlank()) {
            ValidationResult gwa = matchesExpected(content, GWA_NUMBER, gwaNumber, "괘 번호");
            if (!gwa.valid()) {
                return gwa;
            }
        }
        return ValidationResult.success();
    }

    private ValidationResult matchesExpected(
            String content,
            Pattern pattern,
            String expected,
            String label) {
        if (expected == null || expected.isBlank() || "정보 없음".equals(expected)) {
            return ValidationResult.success();
        }
        Matcher matcher = pattern.matcher(content);
        while (matcher.find()) {
            if (!expected.equals(matcher.group(1))) {
                return ValidationResult.invalid(
                        "FACT_ALIGNMENT_FAILED",
                        "외부 AI가 엔진의 %s 결과와 다른 내용을 반환했습니다.".formatted(label));
            }
        }
        return ValidationResult.success();
    }

    public record ValidationResult(boolean valid, String code, String reason) {
        private static ValidationResult success() {
            return new ValidationResult(true, "OK", "fact packet과 일치합니다.");
        }

        private static ValidationResult invalid(String code, String reason) {
            return new ValidationResult(false, code, reason);
        }
    }
}
