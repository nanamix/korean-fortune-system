package com.fortune.service;

import com.fortune.dto.SajuRequest;
import com.fortune.dto.SajuResult;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.cache.annotation.Cacheable;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;

/**
 * 간지 계산 서비스
 *
 * <p>사주팔자의 핵심인 간지(干支) 계산을 담당하는 서비스 클래스입니다.</p>
 *
 * @author 하진영
 * @version 2.5.0
 * @since 2025-06-24
 */
@Slf4j
@Service
public class GanjiCalculatorService {

    /**
     * 천간 (10개)
     */
    private static final String[] HEAVENLY_STEMS = {
            "갑", "을", "병", "정", "무", "기", "경", "신", "임", "계"
    };

    /**
     * 지지 (12개)
     */
    private static final String[] EARTHLY_BRANCHES = {
            "자", "축", "인", "묘", "진", "사", "오", "미", "신", "유", "술", "해"
    };

    /**
     * 시간대별 지지 매핑
     */
    private static final String[] TIME_BRANCHES = {
            "자", "축", "인", "묘", "진", "사", "오", "미", "신", "유", "술", "해"
    };

    /**
     * 월별 지지 매핑 (입춘 기준)
     */
    private static final String[] MONTH_BRANCHES = {
            "인", "묘", "진", "사", "오", "미", "신", "유", "술", "해", "자", "축"
    };

    /**
     * 기준일자 (1900년 1월 1일 = 경자일)
     */
    private static final LocalDate BASE_DATE = LocalDate.of(1900, 1, 1);

    /**
     * 기준 천간
     */
    private static final int BASE_DAY_STEM = 6; // 경
    private static final int BASE_DAY_BRANCH = 0; // 자

    /**
     * 사주팔자 계산 메인 메서드
     * 
     * @param request 사주 요청 정보
     * @return 사주 결과
     */
    public SajuResult calculateSaju(SajuRequest request) {
        log.info("🔮 사주팔자 계산 시작: {}년 {}월 {}일 {}시 {}분",
                request.getBirthYear(), request.getBirthMonth(), request.getBirthDay(),
                request.getBirthHour(), request.getBirthMinute());

        try {
            /* 1. 날짜 변환 (음력 → 양력 처리) */
            LocalDateTime adjustedDateTime = adjustDateTime(request);
            LocalDate birthDate = adjustedDateTime.toLocalDate();

            /* 2. 연주 계산 */
            String yearPillar = calculateYearPillar(request.getBirthYear());

            /* 3. 월주 계산 */
            String monthPillar = calculateMonthPillar(birthDate, yearPillar);

            /* 4. 일주 계산 */
            String dayPillar = calculateDayPillar(birthDate);

            /* 5. 시주 계산 */
            String timePillar = calculateTimePillar(request.getBirthHour(), dayPillar);

            /* 6. 일간 추출 */
            String dayMaster = dayPillar.substring(0, 1);

            /* 7. 오행 분석 */
            SajuResult.WuxingAnalysis wuxingAnalysis = analyzeWuxing(yearPillar, monthPillar, dayPillar, timePillar);

            /* 8. 결과 생성 */
            SajuResult result = SajuResult.builder()
                    .yearPillar(yearPillar) // 연주
                    .monthPillar(monthPillar) // 월주
                    .dayPillar(dayPillar) // 일주
                    .timePillar(timePillar) // 시주
                    .dayMaster(dayMaster) // 일간
                    .birthDate(birthDate) // 출생 날짜
                    .adjustedDateTime(adjustedDateTime) // 조정된 날짜/시간
                    .calendarType(request.getCalendarType()) // 달력 유형
                    .gender(request.getGender()) // 성별
                    .wuxingAnalysis(wuxingAnalysis) // 오행 분석
                    .fortuneSummary(generateFortuneSummary(dayMaster)) // 운세 요약
                    .build(); // 사주 결과 반환

            /* 사주 결과 로깅 */
            log.info("✅ 사주팔자 계산 완료: {}", result.getFormattedSaju());

            /* 사주 결과 반환 */
            return result;

            /* 사주 계산 중 오류 발생 시 예외 처리 */
        } catch (Exception e) {
            log.error("❌ 사주팔자 계산 중 오류 발생: {}", e.getMessage(), e);
            throw new RuntimeException("사주팔자 계산 중 오류가 발생했습니다: " + e.getMessage(), e);
        }
    }

    /**
     * 완전한 사주 계산 (테스트용)
     * 
     * @param year 출생 년도
     * @param month 출생 월
     * @param day 출생 일
     * @param hour 출생 시간
     * @param minute 출생 분
     * @param gender 성별
     * @param calendarType 달력 유형
     * @return 사주 결과
     */
    public SajuResult calculateCompleteSaju(int year, int month, int day, int hour, int minute, String gender, String calendarType) {
        /* 사주 요청 정보 생성 */
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

    /**
     * 날짜/시간 조정 (음력→양력 변환 등)
     * 
     * @param request 사주 요청 정보
     * @return 조정된 날짜/시간
     */
    private LocalDateTime adjustDateTime(SajuRequest request) {
        /* 날짜/시간 생성 */
        LocalDateTime dateTime = LocalDateTime.of(
                request.getBirthYear(), request.getBirthMonth(), request.getBirthDay(),
                request.getBirthHour(), request.getBirthMinute()
        );

        /* 음력인 경우 양력으로 변환 (간단한 근사치 계산) */
        if ("LUNAR".equals(request.getCalendarType())) {
            /* 실제 구현에서는 정확한 음력→양력 변환 알고리즘 필요 */
            dateTime = dateTime.plusDays(30); // 임시 처리
        }

        return dateTime;
    }

    /**
     * 연주 계산
     * 
     * @param year 출생 년도
     * @return 연주
     */
    @Cacheable(value = "year-pillar", key = "#year")
    public String calculateYearPillar(int year) {
        /* 1981년 = 신유년을 기준으로 계산 */
        int baseYear = 1981;
        int yearDiff = year - baseYear;

        /* 천간 계산 (10개) = 1981년 신유년을 기준으로 계산 */
        int stemIndex = (yearDiff % 10 + 10) % 10;

        /* 지지 계산 (12개) = 1981년 신유년을 기준으로 계산 */
        int branchIndex = (yearDiff % 12 + 12) % 12;

        // 연주 반환 (천간 + 지지)
        return HEAVENLY_STEMS[stemIndex] + EARTHLY_BRANCHES[branchIndex];
    }

    /**
     * 월주 계산
     * 
     * @param date 출생 날짜
     * @param yearPillar 연주
     * @return 월주
     */
    public String calculateMonthPillar(LocalDate date, String yearPillar) {
        /* 연간의 천간에 따른 월간 계산 (천간 * 2 + 월 - 1) % 10 */
        String yearStem = yearPillar.substring(0, 1);

        /* 천간 인덱스 계산 (천간 배열에서 천간 인덱스 찾기) */
        int yearStemIndex = java.util.Arrays.asList(HEAVENLY_STEMS).indexOf(yearStem);

        /* 월 계산 (월 인덱스 계산) */
        int month = date.getMonthValue();

        /* 월지는 입춘 기준으로 결정 (단순화) (12개) */
        String monthBranch = MONTH_BRANCHES[month - 1];

        /* 월간 계산 공식 (천간 * 2 + 월 - 1) % 10 */
        int monthStemIndex = (yearStemIndex * 2 + month - 1) % 10;

        /* 월간 계산 결과 (천간) */
        String monthStem = HEAVENLY_STEMS[monthStemIndex];

        /* 월간 계산 결과 (천간 + 월지) */
        return monthStem + monthBranch;
    }

    /**
     * 일주 계산
     * 
     * @param date 출생 날짜
     * @return 일주
     */
    @Cacheable(value = "day-pillar", key = "#date")
    public String calculateDayPillar(LocalDate date) {
        /* 기준일자와 출생일자의 일수 차이 계산 */
        long daysBetween = ChronoUnit.DAYS.between(BASE_DATE, date);

        /* 천간 계산 (10개) = 기준일자 경자일을 기준으로 계산 */
        int stemIndex = (int) ((BASE_DAY_STEM + daysBetween) % 10);

        /* 지지 계산 (12개) = 기준일자 경자일을 기준으로 계산 */
        int branchIndex = (int) ((BASE_DAY_BRANCH + daysBetween) % 12);

        /* 천간 인덱스 조정 (음수 값 처리) */
        if (stemIndex < 0) stemIndex += 10;

        /* 지지 인덱스 조정 (음수 값 처리) */
        if (branchIndex < 0) branchIndex += 12;

        /* 일주 계산 결과 (천간 + 지지) */
        return HEAVENLY_STEMS[stemIndex] + EARTHLY_BRANCHES[branchIndex];
    }

    /**
     * 시주 계산
     * 
     * @param hour 출생 시간
     * @param dayPillar 일주
     * @return 시주
     */
    public String calculateTimePillar(int hour, String dayPillar) {
        /* 시지 계산 (2시간씩 12시진) (2시간씩 12시간 매핑) */
        int timeIndex = (hour + 1) / 2 % 12;

        /* 시지 계산 결과 (2시간씩 12시간 매핑) */
        String timeBranch = TIME_BRANCHES[timeIndex];

        /* 일간 천간 추출 */ 
        String dayStem = dayPillar.substring(0, 1);

        /* 일간 천간 인덱스 계산 (천간 배열에서 일간 천간 인덱스 찾기) */
        int dayStemIndex = java.util.Arrays.asList(HEAVENLY_STEMS).indexOf(dayStem);

        /* 시간 천간 인덱스 계산 (일간 천간 * 2 + 시간 인덱스) % 10 */
        int timeStemIndex = (dayStemIndex * 2 + timeIndex) % 10;

        /* 시간 천간 계산 결과 (천간) */
        String timeStem = HEAVENLY_STEMS[timeStemIndex];

        /* 시주 계산 결과 (천간 + 시지) */
        return timeStem + timeBranch;
    }

    /**
     * 오행 분석
     * 
     * @param yearPillar 연주
     * @param monthPillar 월주
     * @param dayPillar 일주
     * @param timePillar 시주
     * @return 오행 분석
     */
    private SajuResult.WuxingAnalysis analyzeWuxing(String yearPillar, String monthPillar, String dayPillar, String timePillar) {
        /* 목, 화, 토, 금, 수 오행 카운트 */
        int[] wuxingCount = new int[5]; 

        /* 각 기둥의 천간과 지지를 오행으로 변환하여 카운트 */
        /* 연주, 월주, 일주, 시주 오행 카운트 */
        String[] pillars = {yearPillar, monthPillar, dayPillar, timePillar};

        /* 각 기둥의 천간과 지지를 오행으로 변환하여 카운트 */
        for (String pillar : pillars) {
            String stem = pillar.substring(0, 1);
            String branch = pillar.substring(1, 2);

            /* 천간 오행 */
            wuxingCount[getStemWuxing(stem)]++;
            /* 지지 오행 */
            wuxingCount[getBranchWuxing(branch)]++;
        }

        /* 가장 강한/약한 오행 찾기 */
        int maxIndex = 0, minIndex = 0;

        /* 가장 강한/약한 오행 찾기 */
        for (int i = 1; i < 5; i++) {
            if (wuxingCount[i] > wuxingCount[maxIndex]) maxIndex = i;
            if (wuxingCount[i] < wuxingCount[minIndex]) minIndex = i;
        }

        /* 목, 화, 토, 금, 수 오행 이름 */
        String[] wuxingNames = {"목", "화", "토", "금", "수"};

        /* 균형도 계산 (편차가 적을수록 높은 점수) */
        double avg = java.util.Arrays.stream(wuxingCount).average().orElse(0);

        /* 균형도 계산 (편차가 적을수록 높은 점수) */
        double variance = java.util.Arrays.stream(wuxingCount)
                .mapToDouble(x -> Math.pow(x - avg, 2))
                .average().orElse(0);

        /* 균형도 계산 (편차가 적을수록 높은 점수) */
        int balance = Math.max(0, 100 - (int)(variance * 10));

        return SajuResult.WuxingAnalysis.builder()
                .woodCount(wuxingCount[0])
                .fireCount(wuxingCount[1])
                .earthCount(wuxingCount[2])
                .metalCount(wuxingCount[3])
                .waterCount(wuxingCount[4])
                .strongestElement(wuxingNames[maxIndex])
                .weakestElement(wuxingNames[minIndex])
                .balance(balance)
                .build();
    }

    /**
     * 천간 오행 매핑
     * 
     * @param stem 천간
     * @return 천간 오행
     */
    private int getStemWuxing(String stem) {
        /* 천간 오행 매핑 */
        return switch (stem) {
            case "갑", "을" -> 0; // 목
            case "병", "정" -> 1; // 화
            case "무", "기" -> 2; // 토
            case "경", "신" -> 3; // 금
            case "임", "계" -> 4; // 수
            default -> 0;
        };
    }

    /**
     * 지지 오행 매핑
     * 
     * @param branch 지지
     * @return 지지 오행
     */
    private int getBranchWuxing(String branch) {
        /* 지지 오행 매핑 */
        return switch (branch) {
            case "인", "묘" -> 0; // 목
            case "사", "오" -> 1; // 화
            case "진", "술", "축", "미" -> 2; // 토
            case "신", "유" -> 3; // 금
            case "해", "자" -> 4; // 수
            default -> 0;
        };
    }

    /**
     * 운세 요약 생성
     * 
     * @param dayMaster 일간
     * @return 운세 요약
     */
    private String generateFortuneSummary(String dayMaster) {
        /* 일간별 운세 요약 생성 */
        return switch (dayMaster) {
            case "갑" -> "큰 나무처럼 웅장하고 정직한 성품의 소유자입니다.";
            case "을" -> "꽃처럼 아름답고 섬세한 감성을 지닌 분입니다.";
            case "병" -> "태양처럼 밝고 활동적인 에너지를 가진 분입니다.";
            case "정" -> "촛불처럼 따뜻하고 정성스러운 마음을 지닌 분입니다.";
            case "무" -> "산처럼 든든하고 포용력이 큰 분입니다.";
            case "기" -> "대지처럼 너그럽고 실용적인 지혜를 가진 분입니다.";
            case "경" -> "쇠처럼 강인하고 원칙을 중시하는 분입니다.";
            case "신" -> "보석처럼 예리하고 세련된 감각을 지닌 분입니다.";
            case "임" -> "바다처럼 깊고 포용력이 넓은 분입니다.";
            case "계" -> "이슬처럼 순수하고 지혜로운 분입니다.";
            default -> "균형 잡힌 성품을 지닌 분입니다.";
        };
    }
}
