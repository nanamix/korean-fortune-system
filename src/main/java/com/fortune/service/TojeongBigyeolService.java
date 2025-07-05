package com.fortune.service;

import com.fortune.dto.TojeongGwa;
import com.fortune.dto.TojeongRequest;
import com.fortune.dto.TojeongResult;
import com.fortune.dto.MonthlyFortune;
import org.springframework.stereotype.Service;
import lombok.extern.slf4j.Slf4j;

import java.util.*;

/**
 * 토정비결 서비스
 *
 * <p>전통 토정비결 64괘를 계산하고 해석하는 서비스입니다.</p>
 *
 * @author 하진영
 * @version 2.5.0
 * @since 2025-06-24
 */
@Slf4j
@Service
public class TojeongBigyeolService {

    /**
     * 토정비결 괘 정보 맵
     */
    private static final Map<Integer, TojeongGwa> TOJEONG_GWA_MAP = new HashMap<>();

    /**
     * 정적 초기화 메서드들
     */
    static {
        initializeAllGwa();
    }

    /**
     * 토정비결 계산 메인 메서드
     * SQL: SELECT * FROM tojeong_gwa;
     * @param request 토정비결 요청 정보
     * @return 토정비결 결과
     */
    public TojeongResult calculateTojeong(TojeongRequest request) {
        log.info("📜 토정비결 계산 시작: {}년생 -> {}년 운세",
                request.getBirthYear(), request.getTargetYear());

        try {
            /* 1. 기본 계산 */
            int sum = request.getBirthYear() + request.getBirthMonth() +
                    request.getBirthDay() + request.getTargetYear();

            /* 2. 토정비결 공식 적용 */
            int step1 = (sum % 60) + 1;
            int step2 = (step1 * 20) % 100;
            int step3 = (step2 + request.getBirthMonth()) % 64;
            if (step3 == 0) step3 = 64;

            /* 3. 해당 괘 조회 */
            TojeongGwa gwa = TOJEONG_GWA_MAP.get(step3);
            if (gwa == null) {
                throw new RuntimeException("토정비결 괘를 찾을 수 없습니다: " + step3);
            }

            /* 4. 월별 운세 생성 */
            List<MonthlyFortune> monthlyFortune = generateMonthlyFortune(gwa, request.getTargetYear());

            /* 5. 결과 생성 */
            TojeongResult result = TojeongResult.builder()
                    .targetYear(request.getTargetYear())
                    .gwaNumber(gwa.getNumber())
                    .gwaName(gwa.getName())
                    .gwaSymbol(gwa.getSymbol())
                    .summary(gwa.getSummary())
                    .detailedFortune(gwa.getDetailedFortune())
                    .overallScore(gwa.getScore())
                    .advice(generateAdvice(gwa))
                    .luckyMonths(gwa.getLuckyMonths())
                    .cautionMonths(gwa.getCautionMonths())
                    .monthlyFortune(monthlyFortune)
                    .build();

            log.info("✅ 토정비결 계산 완료: {} 괘 ({}점)", gwa.getName(), gwa.getScore());
            return result;

        } catch (Exception e) {
            log.error("❌ 토정비결 계산 중 오류가 발생했습니다: {}", e.getMessage(), e);
            throw new RuntimeException("토정비결 계산 중 오류가 발생했습니다: " + e.getMessage(), e);
        }
    }

    /**
     * 점수대별 조언 생성
     * SQL: SELECT * FROM tojeong_gwa;
     * @param gwa 토정비결 괘
     * @return 점수대별 조언
     */
    private String generateAdvice(TojeongGwa gwa) {
        /* 점수대별 조언 생성 */
        String advice = "올해 당신의 운세는 '" + gwa.getName() + "'괘입니다. ";

        /* 점수대별 조언 생성 */
        if (gwa.getScore() >= 90) {
            advice += "매우 좋은 운세입니다. 적극적으로 행동하세요.";
        } else if (gwa.getScore() >= 80) {
            advice += "좋은 운세입니다. 기회를 놓치지 마세요.";
        } else if (gwa.getScore() >= 70) {
            advice += "평균 이상의 운세입니다. 꾸준히 노력하세요.";
        } else if (gwa.getScore() >= 60) {
            advice += "보통의 운세입니다. 신중하게 행동하세요.";
        } else if (gwa.getScore() >= 50) {
            advice += "주의가 필요한 해입니다. 조심스럽게 행동하세요.";
        } else {
            advice += "어려운 해가 될 수 있습니다. 인내심을 갖고 극복하세요.";
        }

        return advice;
    }

    /**
     * 월별 상세 운세 생성
     * SQL: SELECT * FROM tojeong_gwa;
     * @param gwa 토정비결 괘
     * @param targetYear 년도
     * @return 월별 상세 운세 리스트
     */
    private List<MonthlyFortune> generateMonthlyFortune(TojeongGwa gwa, int targetYear) {
        /* 월별 상세 운세 생성 */
        List<MonthlyFortune> monthlyList = new ArrayList<>();
        /* 랜덤 객체 생성 */
        Random random = new Random(gwa.getNumber() + targetYear);

        /* 월별 상세 운세 생성 */
        for (int month = 1; month <= 12; month++) {
            int baseScore = gwa.getScore();
            int variation = random.nextInt(21) - 10;
            int monthScore = Math.max(0, Math.min(100, baseScore + variation));

            /* 월별 키워드 생성 */
            List<String> keywords = generateMonthlyKeywords(month, monthScore);
            /* 월별 메시지 생성 */
            String message = generateMonthlyMessage(month, monthScore);

            /* 월별 상세 운세 생성 */
            MonthlyFortune monthlyFortune = MonthlyFortune.builder()
                    .month(month)
                    .score(monthScore)
                    .message(message)
                    .keywords(keywords)
                    .build();

            monthlyList.add(monthlyFortune);
        }

        return monthlyList;
    }

    /**
     * 월별 키워드 생성
     * SQL: SELECT * FROM tojeong_gwa;
     * 메서드는 각 월별 운세에 맞는 키워드를 생성합니다.
     * @param month 월 번호 (1-12)
     * @param score 월별 점수 (0-100)
     * @return 월별 키워드 리스트
     */
    private List<String> generateMonthlyKeywords(int month, int score) {
        List<String> keywords = new ArrayList<>();

        /* 계절별 기본 키워드 */
        switch ((month - 1) / 3) {
            case 0 -> keywords.addAll(Arrays.asList("새시작", "희망", "성장")); // 봄
            case 1 -> keywords.addAll(Arrays.asList("활동", "에너지", "도전")); // 여름
            case 2 -> keywords.addAll(Arrays.asList("수확", "성과", "감사")); // 가을
            case 3 -> keywords.addAll(Arrays.asList("정리", "휴식", "계획")); // 겨울
        }

        /* 점수별 추가 키워드 */
        if (score >= 80) {
            keywords.addAll(Arrays.asList("행운", "성공", "기회"));
        } else if (score >= 60) {
            keywords.addAll(Arrays.asList("안정", "발전", "조화"));
        } else if (score >= 40) {
            keywords.addAll(Arrays.asList("인내", "노력", "꾸준함"));
        } else {
            keywords.addAll(Arrays.asList("주의", "신중", "극복"));
        }

        return keywords;
    }

    /**
     * 월별 메시지 생성
     * SQL: SELECT * FROM tojeong_gwa;
     * 이 메서드는 각 월별 운세 메시지를 생성합니다.
     * @param month 월 번호 (1-12)
     * @param score 월별 점수 (0-100)
     *@return 월별 운세 메시지
     */
    private String generateMonthlyMessage(int month, int score) {
        /* 월별 메시지 생성 */
        String monthName = month + "월";

        /* 월별 메시지 생성 */
        if (score >= 80) {
            return monthName + "은 매우 좋은 운세입니다. 새로운 도전을 해보세요.";
        } else if (score >= 60) {
            return monthName + "은 안정적인 운세입니다. 계획을 차근차근 실행하세요.";
        } else if (score >= 40) {
            return monthName + "은 평범한 운세입니다. 꾸준한 노력이 필요합니다.";
        } else {
            return monthName + "은 조심스러운 운세입니다. 신중하게 행동하세요.";
        }
    }

    /**
     * 모든 괘 정보 초기화
     * SQL: SELECT * FROM tojeong_gwa;
     * 이 메서드는 토정비결의 모든 괘 정보를 초기화합니다.
     * @param
     * @return void
     */
    private static void initializeAllGwa() {
        /* 1-16번 괘 */
        TOJEONG_GWA_MAP.put(1, createGwa(1, "건위천", "☰☰", "창조와 리더십의 해",
                "강건하고 창조적인 기운이 넘치는 해입니다. 새로운 일을 시작하기에 매우 좋은 시기입니다.",
                85, "1,6,11월", "3,9월"));

        /* 2번 괘 생성 */
        TOJEONG_GWA_MAP.put(2, createGwa(2, "곤위지", "☷☷", "포용과 인내의 해",
                "인내심을 갖고 기다리는 것이 중요한 해입니다. 서두르지 말고 차근차근 준비하세요.",
                65, "2,7,12월", "4,10월"));

        /* 3번 괘 생성 */
        TOJEONG_GWA_MAP.put(3, createGwa(3, "수뢰둔", "☵☳", "어려움 속의 성장",
                "초기에는 어려움이 있지만 점차 상황이 좋아질 것입니다. 포기하지 마세요.",
                55, "5,8월", "1,6월"));

        /* 4번 괘 생성 */
        TOJEONG_GWA_MAP.put(4, createGwa(4, "산수몽", "☶☵", "학습과 깨달음의 해",
                "배움과 깨달음을 통해 성장하는 해입니다. 겸손한 마음으로 임하세요.",
                70, "3,9월", "7,11월"));

        /* 5번 괘 생성 */
        TOJEONG_GWA_MAP.put(5, createGwa(5, "수천수", "☵☰", "기다림의 지혜",
                "때를 기다리는 지혜가 필요한 해입니다. 급하게 서두르지 마세요.",
                60, "4,10월", "2,8월"));

        /* 6-10번 괘 */
        TOJEONG_GWA_MAP.put(6, createGwa(6, "천수송", "☰☵", "갈등과 해결",
                "갈등이 있을 수 있지만 지혜롭게 해결할 수 있는 해입니다.",
                50, "6,12월", "3,9월"));

        /* 7번 괘 생성 */
        TOJEONG_GWA_MAP.put(7, createGwa(7, "지수사", "☷☵", "조직과 협력",
                "많은 사람들과 협력하여 큰 일을 이룰 수 있는 해입니다.",
                75, "1,7월", "4,10월"));

        /* 8번 괘 생성 */
        TOJEONG_GWA_MAP.put(8, createGwa(8, "수지비", "☵☷", "화합과 단결",
                "주변 사람들과의 화합이 중요한 해입니다. 서로 도우며 나아가세요.",
                80, "2,8월", "5,11월"));

        /* 9번 괘 생성 */
        TOJEONG_GWA_MAP.put(9, createGwa(9, "풍천소축", "☴☰", "작은 성과의 축적",
                "작은 것부터 차근차근 쌓아가는 것이 중요한 해입니다.",
                65, "3,9월", "6,12월"));

        /* 10번 괘 생성 */
        TOJEONG_GWA_MAP.put(10, createGwa(10, "천택리", "☰☱", "예의와 도리",
                "예의와 도리를 지키며 행동하면 좋은 결과가 있을 것입니다.",
                70, "4,10월", "1,7월"));

        /* 11-20번 괘 (간략화) */
        for (int i = 11; i <= 20; i++) {
            TOJEONG_GWA_MAP.put(i, createDefaultGwa(i));
        }

        /* 21-64번 괘도 유사하게 초기화 (간략화) */
        for (int i = 21; i <= 64; i++) {
            TOJEONG_GWA_MAP.put(i, createDefaultGwa(i));
        }
    }

    /**
     * 기본 괘 생성
     * SQL: SELECT * FROM tojeong_gwa;
     * 이 메서드는 기본 괘 정보를 생성합니다.
     * @param number 괘 번호
     * @return TojeongGwa 객체
     */
    private static TojeongGwa createDefaultGwa(int number) {
        String[] gwaNames = {
                "지천태", "천지비", "천화동인", "화천대유", "지산겸", "뢰지예", "택지수", "풍지관", "화뢰서합",
                "산지박", "지뢰복", "천뢰무망", "산천대축", "지산이", "택뢰수", "산택손", "택풍대과", "감위수",
                "이위화", "산택손", "뢰화풍", "산화비", "지풍승", "택천쾌", "천풍구", "산지박", "뢰지예",
                "대과", "감위수", "이위화", "택산함", "뢰풍항", "천산둔", "뢰천대장", "화지진", "지화명이",
                "풍화가인", "화택규", "수산건", "뢰수해", "산풍고", "풍산점", "뢰택귀매", "택천쾌", "천풍구",
                "택지취", "지풍승", "수풍정", "풍수환", "택산함", "뢰풍항", "산화비", "뢰화풍", "산택손",
                "손위풍", "택뢰수", "뢰산소과", "감위수", "산택손", "중뢰진", "산뢰이", "풍택중부", "뢰화풍",
                "화택규", "풍화가인", "택화혁", "화풍정", "뢰산소과", "산지박"
        };

        /* 괘 이름 생성 */
        String name = (number <= gwaNames.length) ? gwaNames[number - 1] : "미정의괘" + number;
        /* 랜덤 객체 생성 */
        Random random = new Random(number);

        return TojeongGwa.builder()
                .number(number)
                .name(name)
                .symbol(String.valueOf(random.nextInt(10) + 1))
                .summary(random.nextInt(10) + 1 + "년의 성장을 기록한 괘입니다.")
                .detailedFortune(random.nextInt(10) + 1 + "년의 성장을 기록한 괘입니다.")
                .score(random.nextInt(100) + 1)
                .luckyMonths("1,2,3,4,5,6,7,8,9,10,11,12")
                .cautionMonths("1,3,5,7,8,10,12")
                .build();
    }

    /**
     * 상세 괘 생성
     * SQL: SELECT * FROM tojeong_gwa;
     * 이 메서드는 각 괘의 상세 정보를 생성합니다.
     * @param number 괘 번호
     * @param name 괘 이름
     * @param symbol 괘 기호
     * @param summary 괘 요약
     * @param detailedFortune 괘 상세 운세
     * @param score 괘 점수
     * @param luckyMonths 행운의 달
     * @param cautionMonths 주의해야 할 달
     * @return TojeongGwa 객체
     */
    private static TojeongGwa createGwa(int number, String name, String symbol, String summary,
                                        String detailedFortune, int score, String luckyMonths, String cautionMonths) {
        return TojeongGwa.builder()
                .number(number)
                .name(name)
                .symbol(symbol)
                .summary(summary)
                .detailedFortune(detailedFortune)
                .score(score)
                .luckyMonths(luckyMonths)
                .cautionMonths(cautionMonths)
                .build();
    }

} // END TojeongBigyeolService
