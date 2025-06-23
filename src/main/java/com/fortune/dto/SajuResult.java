package com.fortune.dto;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@Builder
public class SajuResult {
    private String yearPillar;
    private String monthPillar;
    private String dayPillar;
    private String timePillar;
    private String dayMaster;
    private LocalDate birthDate;
    private LocalDateTime adjustedDateTime;
    private String calendarType;
    private String gender;

    // getFormattedSaju 메서드 추가
    public String getFormattedSaju() {
        return String.format("연지: %s, 월지: %s, 일지: %s, 시지: %s",
                yearPillar, monthPillar, dayPillar, timePillar);
    }

    public String toJson(){
        ObjectMapper objectMapper = new ObjectMapper();
        try {
            return objectMapper.writeValueAsString(this);
        } catch (JsonProcessingException e) {
            e.printStackTrace();
            return "{}"; // 오류 발생 시 빈 JSON 반환
        }
    }

    public String getFullSaju() {
        return String.format("연지: %s, 월지: %s, 일지: %s, 시지: %s, 일간: %s",
                yearPillar, monthPillar, dayPillar, timePillar, dayMaster);
    }
}
