package com.fortune.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.fortune.dto.GanjiCalendarDay;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Arrays;
import java.util.HashMap;

@Service
public class GanjiCalendarService {

    @Autowired
    private GanjiCalculatorService ganjiCalculator;

    @Autowired
    private SinsalService sinsalService;

    /**
     * 월별 간지달력 생성
     */
    public List<GanjiCalendarDay> generateMonthlyCalendar(int year, int month) {
        List<GanjiCalendarDay> calendarDays = new ArrayList<>();

        YearMonth yearMonth = YearMonth.of(year, month);
        int daysInMonth = yearMonth.lengthOfMonth();

        // 해당 월의 모든 날짜 처리
        for (int day = 1; day <= daysInMonth; day++) {
            LocalDate date = LocalDate.of(year, month, day);

            // 간지 계산
            String dayPillar = ganjiCalculator.calculateDayPillar(date);

            // 절기 확인
            String solarTerm = getSolarTerm(date);

            // 길방위 계산
            String luckyDirection = calculateLuckyDirection(dayPillar);

            // 길한 색깔
            List<String> luckyColors = calculateLuckyColors(dayPillar.substring(0, 1));

            // 간단한 길흉 판단
            String dailyLuck = calculateDailyLuck(dayPillar);

            GanjiCalendarDay calendarDay = GanjiCalendarDay.builder()
                    .date(date)
                    .dayPillar(dayPillar)
                    .solarTerm(solarTerm)
                    .luckyDirection(luckyDirection)
                    .luckyColors(luckyColors)
                    .dailyLuck(dailyLuck)
                    .isGoodDay(isGoodDay(dayPillar))
                    .specialNote(getSpecialNote(date))
                    .build();

            calendarDays.add(calendarDay);
        }

        return calendarDays;
    }

    /**
     * 절기 확인
     */
    private String getSolarTerm(LocalDate date) {
        // 24절기 계산 로직 (간단화된 버전)
        int month = date.getMonthValue();
        int day = date.getDayOfMonth();

        Map<String, String> solarTerms = new HashMap<>();
        solarTerms.put("0204", "입춘"); solarTerms.put("0219", "우수");
        solarTerms.put("0305", "경칩"); solarTerms.put("0320", "춘분");
        solarTerms.put("0404", "청명"); solarTerms.put("0420", "곡우");
        solarTerms.put("0505", "입하"); solarTerms.put("0521", "소만");
        solarTerms.put("0605", "망종"); solarTerms.put("0621", "하지");
        solarTerms.put("0707", "소서"); solarTerms.put("0723", "대서");
        solarTerms.put("0807", "입추"); solarTerms.put("0823", "처서");
        solarTerms.put("0907", "백로"); solarTerms.put("0923", "추분");
        solarTerms.put("1008", "한로"); solarTerms.put("1023", "상강");
        solarTerms.put("1107", "입동"); solarTerms.put("1122", "소설");
        solarTerms.put("1207", "대설"); solarTerms.put("1222", "동지");
        solarTerms.put("0105", "소한"); solarTerms.put("0120", "대한");

        String dateKey = String.format("%02d%02d", month, day);
        return solarTerms.getOrDefault(dateKey, "");
    }

    private String calculateDailyLuck(String dayPillar) {
        String dayStem = dayPillar.substring(0, 1);
        String dayBranch = dayPillar.substring(1, 2);

        // 간단한 길흉 판단 로직
        List<String> luckyStem = Arrays.asList("갑", "을", "무", "기");
        List<String> luckyBranch = Arrays.asList("인", "묘", "오", "신");

        if (luckyStem.contains(dayStem) && luckyBranch.contains(dayBranch)) {
            return "대길";
        } else if (luckyStem.contains(dayStem) || luckyBranch.contains(dayBranch)) {
            return "길";
        } else {
            return "평";
        }
    }

    private boolean isGoodDay(String dayPillar) {
        return calculateDailyLuck(dayPillar).equals("대길") || calculateDailyLuck(dayPillar).equals("길");
    }

    private String getSpecialNote(LocalDate date) {
        // 특별한 날 표시 (명절, 기념일 등)
        int month = date.getMonthValue();
        int day = date.getDayOfMonth();

        if (month == 1 && day == 1) return "신정";
        if (month == 3 && day == 1) return "삼일절";
        if (month == 5 && day == 5) return "어린이날";
        if (month == 6 && day == 6) return "현충일";
        if (month == 8 && day == 15) return "광복절";
        if (month == 10 && day == 3) return "개천절";
        if (month == 10 && day == 9) return "한글날";
        if (month == 12 && day == 25) return "성탄절";

        return "";
    }

    private String calculateLuckyDirection(String dayPillar) {
        String dayStem = dayPillar.substring(0, 1);
        Map<String, String> directionMap = new HashMap<>();
        directionMap.put("갑", "동쪽"); directionMap.put("을", "동남쪽");
        directionMap.put("병", "남쪽"); directionMap.put("정", "남서쪽");
        directionMap.put("무", "중앙"); directionMap.put("기", "중앙");
        directionMap.put("경", "서쪽"); directionMap.put("신", "서북쪽");
        directionMap.put("임", "북쪽"); directionMap.put("계", "북동쪽");
        return directionMap.getOrDefault(dayStem, "중앙");
    }

    private List<String> calculateLuckyColors(String dayStem) {
        Map<String, List<String>> colorMap = new HashMap<>();
        colorMap.put("갑", Arrays.asList("녹색", "청색"));
        colorMap.put("을", Arrays.asList("녹색", "청색"));
        colorMap.put("병", Arrays.asList("빨간색", "주황색"));
        colorMap.put("정", Arrays.asList("빨간색", "주황색"));
        colorMap.put("무", Arrays.asList("노란색", "갈색"));
        colorMap.put("기", Arrays.asList("노란색", "갈색"));
        colorMap.put("경", Arrays.asList("흰색", "금색"));
        colorMap.put("신", Arrays.asList("흰색", "금색"));
        colorMap.put("임", Arrays.asList("검정색", "파란색"));
        colorMap.put("계", Arrays.asList("검정색", "파란색"));
        return colorMap.getOrDefault(dayStem, Arrays.asList("흰색"));
    }
}
