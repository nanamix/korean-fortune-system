package com.fortune.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.fortune.dto.SajuResult;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;

@Service
public class GanjiCalculatorService {

    @Autowired
    private LunarCalendarService lunarCalendarService;

    // 천간 (10개)
    private static final String[] HEAVENLY_STEMS = {
            "갑", "을", "병", "정", "무", "기", "경", "신", "임", "계"
    };

    // 지지 (12개)
    private static final String[] EARTHLY_BRANCHES = {
            "자", "축", "인", "묘", "진", "사", "오", "미", "신", "유", "술", "해"
    };

    // 기준년도 - 갑자년 (1984년)
    private static final int BASE_YEAR = 1984;

    // 기준일 - 갑자일 (1900년 1월 1일)
    private static final LocalDate BASE_DATE = LocalDate.of(1900, 1, 1);

    /**
     * 완전한 사주팔자 계산 (양력/음력 구분)
     */
    public SajuResult calculateCompleteSaju(int year, int month, int day, int hour, int minute,
                                            String gender, String calendarType) {
        LocalDate actualBirthDate;

        // 음력/양력 구분하여 실제 생일 계산
        if ("LUNAR".equals(calendarType)) {
            actualBirthDate = lunarCalendarService.convertLunarToSolar(year, month, day);
        } else {
            actualBirthDate = LocalDate.of(year, month, day);
        }

        // 태양시 보정 (한국 표준시 -> 태양시)
        LocalDateTime adjustedDateTime = adjustToSolarTime(actualBirthDate, hour, minute);

        String yearPillar = calculateYearPillar(actualBirthDate.getYear());
        String monthPillar = calculateMonthPillar(actualBirthDate.getYear(), actualBirthDate.getMonthValue());
        String dayPillar = calculateDayPillar(actualBirthDate);
        String timePillar = calculateTimePillar(actualBirthDate, adjustedDateTime.getHour());

        // 일간(일주의 천간)이 본인의 성향을 나타냄
        String dayMaster = dayPillar.substring(0, 1);

        return SajuResult.builder()
                .yearPillar(yearPillar)
                .monthPillar(monthPillar)
                .dayPillar(dayPillar)
                .timePillar(timePillar)
                .dayMaster(dayMaster)
                .birthDate(actualBirthDate)
                .adjustedDateTime(adjustedDateTime)
                .calendarType(calendarType)
                .gender(gender)
                .lunarBirthDate(calendarType.equals("LUNAR") ?
                        String.format("%d년 %d월 %d일", year, month, day) : null)
                .build();
    }

    /**
     * 태양시 보정 (표준시 -> 태양시)
     */
    private LocalDateTime adjustToSolarTime(LocalDate date, int hour, int minute) {
        LocalDateTime standardTime = LocalDateTime.of(date, java.time.LocalTime.of(hour, minute));

        // 한국 위치: 동경 127도 (서울 기준)
        // 표준시 기준: 동경 135도
        // 경도 차이: 8도 = 32분 (4분/도)

        // 한국은 표준시보다 서쪽에 위치하므로 태양시가 32분 늦음
        LocalDateTime solarTime = standardTime.minusMinutes(32);

        // 서머타임 적용 기간 확인 (1948-1988년)
        if (isSummerTimePeriod(date)) {
            solarTime = solarTime.minusHours(1);
        }

        return solarTime;
    }

    /**
     * 서머타임 적용 기간 확인
     */
    private boolean isSummerTimePeriod(LocalDate date) {
        int year = date.getYear();

        // 한국 서머타임 실시 기간 (1948-1988년 중 일부)
        if (year < 1948 || year > 1988) {
            return false;
        }

        // 간단화된 서머타임 기간 (실제로는 년도별로 다름)
        // 대략 5월~9월 기간
        int month = date.getMonthValue();
        return month >= 5 && month <= 9;
    }

    /**
     * 년주 계산
     */
    public String calculateYearPillar(int year) {
        int diff = year - BASE_YEAR;

        int stemIndex = ((diff % 10) + 10) % 10;
        int branchIndex = ((diff % 12) + 12) % 12;

        return HEAVENLY_STEMS[stemIndex] + EARTHLY_BRANCHES[branchIndex];
    }

    /**
     * 월주 계산 - 년간에 따라 달라짐
     */
    public String calculateMonthPillar(int year, int month) {
        String yearPillar = calculateYearPillar(year);
        String yearStem = yearPillar.substring(0, 1);

        // 년간별 정월 월간 시작점
        int baseMonthStemIndex = getBaseMonthStemIndex(yearStem);

        // 월지는 고정: 인(1월), 묘(2월), 진(3월)...
        int monthBranchIndex = (month + 1) % 12; // 인월부터 시작 (인=2)
        int monthStemIndex = (baseMonthStemIndex + month - 1) % 10;

        return HEAVENLY_STEMS[monthStemIndex] + EARTHLY_BRANCHES[monthBranchIndex];
    }

    /**
     * 일주 계산
     */
    public String calculateDayPillar(LocalDate date) {
        long daysDiff = ChronoUnit.DAYS.between(BASE_DATE, date);

        int stemIndex = (int)((daysDiff % 10 + 10) % 10);
        int branchIndex = (int)((daysDiff % 12 + 12) % 12);

        return HEAVENLY_STEMS[stemIndex] + EARTHLY_BRANCHES[branchIndex];
    }

    /**
     * 시주 계산 - 일간에 따라 달라짐
     */
    public String calculateTimePillar(LocalDate date, int hour) {
        String dayPillar = calculateDayPillar(date);
        String dayStem = dayPillar.substring(0, 1);

        int baseTimeStemIndex = getBaseTimeStemIndex(dayStem);

        // 시지 계산 (자시=23-1시, 축시=1-3시...)
        int timeBranchIndex = getTimeBranchIndex(hour);
        int timeStemIndex = (baseTimeStemIndex + timeBranchIndex) % 10;

        return HEAVENLY_STEMS[timeStemIndex] + EARTHLY_BRANCHES[timeBranchIndex];
    }

    // 보조 메서드들
    private int getBaseMonthStemIndex(String yearStem) {
        // 갑기년 = 병인월, 을경년 = 무인월, 병신년 = 경인월...
        switch (yearStem) {
            case "갑": case "기": return 2; // 병
            case "을": case "경": return 4; // 무
            case "병": case "신": return 6; // 경
            case "정": case "임": return 8; // 임
            case "무": case "계": return 0; // 갑
            default: return 0;
        }
    }

    private int getBaseTimeStemIndex(String dayStem) {
        // 갑기일 = 갑자시, 을경일 = 병자시...
        switch (dayStem) {
            case "갑": case "기": return 0; // 갑
            case "을": case "경": return 2; // 병
            case "병": case "신": return 4; // 무
            case "정": case "임": return 6; // 경
            case "무": case "계": return 8; // 임
            default: return 0;
        }
    }

    private int getTimeBranchIndex(int hour) {
        if (hour >= 23 || hour < 1) return 0;  // 자시
        if (hour >= 1 && hour < 3) return 1;   // 축시
        if (hour >= 3 && hour < 5) return 2;   // 인시
        if (hour >= 5 && hour < 7) return 3;   // 묘시
        if (hour >= 7 && hour < 9) return 4;   // 진시
        if (hour >= 9 && hour < 11) return 5;  // 사시
        if (hour >= 11 && hour < 13) return 6; // 오시
        if (hour >= 13 && hour < 15) return 7; // 미시
        if (hour >= 15 && hour < 17) return 8; // 신시
        if (hour >= 17 && hour < 19) return 9; // 유시
        if (hour >= 19 && hour < 21) return 10; // 술시
        if (hour >= 21 && hour < 23) return 11; // 해시
        return 0;
    }
}
