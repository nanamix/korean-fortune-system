package com.fortune.controller;

import com.fortune.service.DailyFortuneService;
import com.fortune.service.GanjiCalculatorService;
import com.fortune.service.GanjiCalendarService;
import com.fortune.service.TojeongBigyeolService;
import com.fortune.service.ZodiacFortuneService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.ArrayList;
import java.util.List;

/**
 * 운세 컨트롤러 테스트
 * @author 하진영
 * @version 1.0
 * @since 2025-06-21
 */
@WebMvcTest(FortuneController.class)
@WithMockUser // Spring Security 인증을 Mock으로 설정
public class FortuneControllerTest {
    
    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private GanjiCalculatorService ganjiCalculatorService;

    @MockBean
    private GanjiCalendarService ganjiCalendarService;

    @MockBean
    private DailyFortuneService dailyFortuneService;

    @MockBean
    private TojeongBigyeolService tojeongBigyeolService;

    @MockBean
    private ZodiacFortuneService zodiacFortuneService;

    @Test
    public void testCalculateFortune() throws Exception {
        // 원인: WebMvcTest는 웹 계층만 로드하므로 서비스 빈들이 없어서 실패
        // 결과: @MockBean으로 필요한 서비스들을 Mock으로 설정하여 테스트 가능
        
        // Mock 서비스의 동작 설정
        when(ganjiCalendarService.generateMonthlyCalendar(2024, 1))
                .thenReturn(new ArrayList<>());
        
        mockMvc.perform(get("/api/fortune/calendar/ganji/2024/1"))
                .andExpect(status().isOk());
    }
}
