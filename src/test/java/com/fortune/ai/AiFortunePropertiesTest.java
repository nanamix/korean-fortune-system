package com.fortune.ai;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

class AiFortunePropertiesTest {

    @Test
    void blankProviderConfigurationUsesCurrentDeepSeekDefaults() {
        AiFortuneProperties properties = new AiFortuneProperties(
                true, "deepseek", "", "", "", null, true);

        assertEquals("deepseek-v4-flash", properties.model());
        assertEquals("https://api.deepseek.com", properties.baseUrl());
    }
}
