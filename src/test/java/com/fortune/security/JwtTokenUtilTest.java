package com.fortune.security;

import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;

class JwtTokenUtilTest {

    @Test
    void rejectsMissingSigningKeyWhenSecurityIsEnabled() {
        JwtTokenUtil tokenUtil = configuredTokenUtil(true, "");

        assertThrows(IllegalStateException.class, tokenUtil::validateSecretConfiguration);
    }

    @Test
    void acceptsSigningKeyFromSecretStore() {
        JwtTokenUtil tokenUtil = configuredTokenUtil(true, "x".repeat(32));

        assertDoesNotThrow(tokenUtil::validateSecretConfiguration);
    }

    @Test
    void allowsMissingSigningKeyWhenSecurityIsDisabled() {
        JwtTokenUtil tokenUtil = configuredTokenUtil(false, "");

        assertDoesNotThrow(tokenUtil::validateSecretConfiguration);
    }

    private JwtTokenUtil configuredTokenUtil(boolean enabled, String value) {
        JwtTokenUtil tokenUtil = new JwtTokenUtil();
        ReflectionTestUtils.setField(tokenUtil, "securityEnabled", enabled);
        ReflectionTestUtils.setField(tokenUtil, "jwtSecret", value);
        return tokenUtil;
    }
}
