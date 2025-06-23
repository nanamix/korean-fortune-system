package com.fortune.service;

import com.fortune.dto.GanjiInfo;
import org.springframework.stereotype.Service;
import com.fortune.dto.SajuRequest;
import com.fortune.dto.SajuResult;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;

@Service
public class GanjiCalculatorService {

    // 천간 (天干) - 10개
    private static final String[] HEAVENLY_STEMS = {
            "갑", "을", "병", "정", "무", "기", "경", "신", "임", "계"
    };

    // 지지 (地支) - 12개
    private static final String[] EARTHLY_BRANCHES = {
            "자", "축", "인", "묘", "진", "사", "오", "미", "신", "유", "술", "해"
    };

    // 기준년도 (1984년 = 갑자년)
    private static final int BASE_YEAR = 1984;

    // 기준일 (1985년 2월 4일 = 갑자일)
    private static final LocalDate BASE_DATE = LocalDate.of(1985, 2, 4);

    /**
     * 사주팔자 계산 메인 메서드
     */
    public SajuResult calculateSaju(SajuRequest request) {
        try {
            // 1. 생년월일시 검증 및 변환
            LocalDateTime birthDateTime = createBirthDateTime(request);

            // 2. 태양시 보정
            LocalDateTime adjustedDateTime = adjustForSolarTime(birthDateTime);

            // 3. 사주 계산
            String yearPillar = calculateYearPillar(adjustedDateTime.getYear());
            String monthPillar = calculateMonthPillar(adjustedDateTime);
            String dayPillar = calculateDayPillar(adjustedDateTime.toLocalDate());
            String timePillar = calculateTimePillar(adjustedDateTime, dayPillar);

            // 4. 일간 (日主) 결정
            String dayMaster = dayPillar.substring(0, 1);

            return SajuResult.builder()
                    .yearPillar(yearPillar)
                    .monthPillar(monthPillar)
                    .dayPillar(dayPillar)
                    .timePillar(timePillar)
                    .dayMaster(dayMaster)
                    .birthDate(adjustedDateTime.toLocalDate())
                    .adjustedDateTime(adjustedDateTime)
                    .calendarType(request.getCalendarType())
                    .gender(request.getGender())
                    .build();

        } catch (Exception e) {
            throw new RuntimeException("사주 계산 중 오류가 발생했습니다: " + e.getMessage(), e);
        }
    }

    /**
     * 간지 정보 상세 계산 (GanjiCalendarService에서 사용)
     */
    public GanjiInfo calculateGanjiInfo(LocalDate date) {
        // 기준일로부터 일수 차이 계산
        long daysDiff = ChronoUnit.DAYS.between(BASE_DATE, date);

        int heavenlyIndex = (int) ((daysDiff + 10) % 10);
        int earthlyIndex = (int) ((daysDiff + 12) % 12);

        if (heavenlyIndex < 0) heavenlyIndex += 10;
        if (earthlyIndex < 0) earthlyIndex += 12;

        String heavenlyStem = HEAVENLY_STEMS[heavenlyIndex];
        String earthlyBranch = EARTHLY_BRANCHES[earthlyIndex];
        String dayPillar = heavenlyStem + earthlyBranch;

        return GanjiInfo.builder()
                .dayPillar(dayPillar)
                .heavenlyStem(heavenlyStem)
                .earthlyBranch(earthlyBranch)
                .heavenlyStemIndex(heavenlyIndex)
                .earthlyBranchIndex(earthlyIndex)
                .build();
    }

    /**
     * 일주 계산 (공개 메서드)
     */
    public String calculateDayPillar(LocalDate date) {
        long daysDiff = ChronoUnit.DAYS.between(BASE_DATE, date);

        int heavenlyIndex = (int) ((daysDiff + 10) % 10);
        int earthlyIndex = (int) ((daysDiff + 12) % 12);

        if (heavenlyIndex < 0) heavenlyIndex += 10;
        if (earthlyIndex < 0) earthlyIndex += 12;

        return HEAVENLY_STEMS[heavenlyIndex] + EARTHLY_BRANCHES[earthlyIndex];
    }

    /**
     * 천간만 계산
     */
    public String calculateHeavenlyStem(LocalDate date) {
        long daysDiff = ChronoUnit.DAYS.between(BASE_DATE, date);
        int heavenlyIndex = (int) ((daysDiff + 10) % 10);
        if (heavenlyIndex < 0) heavenlyIndex += 10;
        return HEAVENLY_STEMS[heavenlyIndex];
    }

    /**
     * 지지만 계산
     */
    public String calculateEarthlyBranch(LocalDate date) {
        long daysDiff = ChronoUnit.DAYS.between(BASE_DATE, date);
        int earthlyIndex = (int) ((daysDiff + 12) % 12);
        if (earthlyIndex < 0) earthlyIndex += 12;
        return EARTHLY_BRANCHES[earthlyIndex];
    }

    // ==================== Private 메서드들 ====================

    /**
     * 년주 계산
     */
    private String calculateYearPillar(int year) {
        int yearDiff = year - BASE_YEAR;
        int heavenlyIndex = (yearDiff + 10) % 10;
        int earthlyIndex = (yearDiff + 12) % 12;

        if (heavenlyIndex < 0) heavenlyIndex += 10;
        if (earthlyIndex < 0) earthlyIndex += 12;

        return HEAVENLY_STEMS[heavenlyIndex] + EARTHLY_BRANCHES[earthlyIndex];
    }

    /**
     * 월주 계산
     */
    private String calculateMonthPillar(LocalDateTime dateTime) {
        int year = dateTime.getYear();
        int month = dateTime.getMonthValue();
        int day = dateTime.getDayOfMonth();

        // 절기 날짜 계산 (간단화)
        int adjustedMonth = month;
        if (day < getSolarTermDay(year, month)) {
            adjustedMonth = month - 1;
            if (adjustedMonth == 0) {
                adjustedMonth = 12;
                year--;
            }
        }

        // 년간에 따른 월간 계산
        String yearPillar = calculateYearPillar(year);
        int yearStemIndex = getHeavenlyStemIndex(yearPillar.substring(0, 1));

        // 월간 공식: (년간 × 2 + 월수) % 10
        int monthStemIndex = (yearStemIndex * 2 + adjustedMonth + 1) % 10;
        int monthBranchIndex = (adjustedMonth + 1) % 12;

        return HEAVENLY_STEMS[monthStemIndex] + EARTHLY_BRANCHES[monthBranchIndex];
    }

    /**
     * 시주 계산
     */
    private String calculateTimePillar(LocalDateTime dateTime, String dayPillar) {
        int hour = dateTime.getHour();

        // 시지 결정
        int timeBranchIndex = (hour + 1) / 2 % 12;
        String timeBranch = EARTHLY_BRANCHES[timeBranchIndex];

        // 시간 계산 공식
        int dayStemIndex = getHeavenlyStemIndex(dayPillar.substring(0, 1));
        int timeStemIndex = (dayStemIndex * 2 + timeBranchIndex) % 10;

        return HEAVENLY_STEMS[timeStemIndex] + timeBranch;
    }

    /**
     * 태양시 보정
     */
    private LocalDateTime adjustForSolarTime(LocalDateTime dateTime) {
        return dateTime.minusMinutes(30);
    }

    /**
     * 생년월일시 LocalDateTime 변환
     */
    private LocalDateTime createBirthDateTime(SajuRequest request) {
        LocalDate birthDate = LocalDate.of(
                request.getBirthYear(),
                request.getBirthMonth(),
                request.getBirthDay()
        );
        return birthDate.atTime(request.getBirthHour(), request.getBirthMinute());
    }

    /**
     * 천간 인덱스 조회
     */
    private int getHeavenlyStemIndex(String stem) {
        for (int i = 0; i < HEAVENLY_STEMS.length; i++) {
            if (HEAVENLY_STEMS[i].equals(stem)) {
                return i;
            }
        }
        return 0;
    }

    /**
     * 절기 날짜 계산
     */
    private int getSolarTermDay(int year, int month) {
        int[] solarTermDays = {6, 4, 6, 5, 6, 6, 7, 8, 8, 8, 7, 7};
        return solarTermDays[month - 1];
    }

    /**
     * 사주 전체 계산 메서드
     * 이 메서드는 생년월일시와 성별, 달력 유형을 기반으로 사주를 계산합니다.
     * @param year 생년
     * @param month 월
     * @param day 일
     * @param hour 시
     * @param minute 분
     * @return SajuResult 사주 계산 결과
     */

    public SajuResult calculateCompleteSaju(int year, int month, int day, int hour, int minute, String gender, String calendarType) {
        SajuRequest request = SajuRequest.builder()
                .birthYear(year)
                .birthMonth(month)
                .birthDay(day)
                .birthHour(hour)
                .birthMinute(minute)
                .gender(gender)
                .calendarType(calendarType)
                .build();
        return calculateSaju(request);
    }

}
