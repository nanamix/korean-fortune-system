package com.fortune.service;

import com.fortune.dto.SajuResult;
import com.fortune.dto.SinsalInfo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.time.LocalDate;
import java.util.*;

/**
 * 신살 계산 서비스
 * 길신/흉신 계산
 * 천덕 계산
 * 월덕 계산
 * 천희 계산
 * 월살 계산
 * 일살 계산
 */
@Service
public class SinsalService {

    @Autowired
    private GanjiCalculatorService ganjiCalculatorService; // 간지 계산 서비스

    /**
     * 특정 날짜의 길신/흉신 계산
     * @param date 날짜
     * @param saju 사주 결과
     * @return 신살 정보 리스트
     */
    public List<SinsalInfo> calculateDailySinsals(LocalDate date, SajuResult saju) {
        List<SinsalInfo> sinsals = new ArrayList<>();

        String dayPillar = ganjiCalculatorService.calculateDayPillar(date);
        String dayStem = dayPillar.substring(0, 1);
        String dayBranch = dayPillar.substring(1, 2);

        // 천덕 계산
        if (isCheondeok(date, dayStem)) {
            sinsals.add(SinsalInfo.builder()
                    .name("천덕")
                    .description("하늘의 덕을 받는 날입니다. 길한 일을 시작하기 좋습니다.")
                    .isLucky(true)
                    .impact(85)
                    .build());
        }

        // 월덕 계산
        if (isWoldeok(date, dayStem)) {
            sinsals.add(SinsalInfo.builder()
                    .name("월덕")
                    .description("달의 덕을 받는 날입니다. 인간관계가 좋아집니다.")
                    .isLucky(true)
                    .impact(80)
                    .build());
        }

        // 천희 계산
        if (isCheonhee(date, dayBranch)) {
            sinsals.add(SinsalInfo.builder()
                    .name("천희")
                    .description("하늘의 기쁨이 있는 날입니다. 경사스러운 일이 있을 수 있습니다.")
                    .isLucky(true)
                    .impact(75)
                    .build());
        }

        // 월살 계산
        if (isWolsal(date, saju.getDayMaster())) {
            sinsals.add(SinsalInfo.builder()
                    .name("월살")
                    .description("조심스러운 날입니다. 중요한 결정은 피하는 것이 좋습니다.")
                    .isLucky(false)
                    .impact(-60)
                    .build());
        }

        // 일살 계산
        if (isIlsal(date, dayBranch)) {
            sinsals.add(SinsalInfo.builder()
                    .name("일살")
                    .description("하루 종일 조심해야 할 날입니다. 안전에 유의하세요.")
                    .isLucky(false)
                    .impact(-50)
                    .build());
        }

        return sinsals;
    }

    /**
     * 천덕 판단 로직 (월덕과 동일) - 천덕과 월덕은 동일한 로직으로 계산
     * @param date 날짜
     * @param dayStem 일주 첫 번째 글자
     * @return 천덕 여부
     */
    private boolean isCheondeok(LocalDate date, String dayStem) {
        int month = date.getMonthValue();
        Map<Integer, List<String>> cheondeokMap = new HashMap<>();
        cheondeokMap.put(1, Arrays.asList("정", "무"));   // 정월 천덕일
        cheondeokMap.put(2, Arrays.asList("갑", "기"));   // 이월 천덕일
        cheondeokMap.put(3, Arrays.asList("을", "경"));   // 삼월 천덕일
        cheondeokMap.put(4, Arrays.asList("병", "신"));   // 사월 천덕일
        cheondeokMap.put(5, Arrays.asList("정", "임"));   // 오월 천덕일
        cheondeokMap.put(6, Arrays.asList("무", "계"));   // 유월 천덕일
        cheondeokMap.put(7, Arrays.asList("기", "갑"));   // 칠월 천덕일
        cheondeokMap.put(8, Arrays.asList("경", "을"));   // 팔월 천덕일
        cheondeokMap.put(9, Arrays.asList("신", "병"));   // 구월 천덕일
        cheondeokMap.put(10, Arrays.asList("임", "정"));  // 시월 천덕일
        cheondeokMap.put(11, Arrays.asList("계", "무"));  // 동월 천덕일
        cheondeokMap.put(12, Arrays.asList("갑", "기"));  // 섣달 천덕일

        return cheondeokMap.getOrDefault(month, Collections.emptyList()).contains(dayStem);
    }

    // 월덕 판단 로직
    private boolean isWoldeok(LocalDate date, String dayStem) {
        int month = date.getMonthValue();
        Map<Integer, String> woldeokMap = new HashMap<>();
        woldeokMap.put(1, "정"); woldeokMap.put(2, "신"); woldeokMap.put(3, "임"); woldeokMap.put(4, "계");
        woldeokMap.put(5, "정"); woldeokMap.put(6, "을"); woldeokMap.put(7, "경"); woldeokMap.put(8, "정");
        woldeokMap.put(9, "무"); woldeokMap.put(10, "계"); woldeokMap.put(11, "임"); woldeokMap.put(12, "기");

        return woldeokMap.get(month).equals(dayStem);
    }

    /**
     * 천희 계산 로직 (월덕과 동일) - 천희과 월덕은 동일한 로직으로 계산
     * @param date 날짜
     * @param dayBranch 일주 두 번째 글자
     * @return 천희 여부
     */
    private boolean isCheonhee(LocalDate date, String dayBranch) {
        int month = date.getMonthValue();
        Map<Integer, List<String>> cheonheeMap = new HashMap<>();
        cheonheeMap.put(1, Arrays.asList("정", "무"));
        cheonheeMap.put(2, Arrays.asList("갑", "기"));
        cheonheeMap.put(3, Arrays.asList("을", "경"));
        cheonheeMap.put(4, Arrays.asList("병", "신"));
        cheonheeMap.put(5, Arrays.asList("정", "임"));
        cheonheeMap.put(6, Arrays.asList("무", "계"));
        cheonheeMap.put(7, Arrays.asList("기", "갑"));
        cheonheeMap.put(8, Arrays.asList("경", "을"));
        cheonheeMap.put(9, Arrays.asList("신", "병"));
        cheonheeMap.put(10, Arrays.asList("임", "정"));
        cheonheeMap.put(11, Arrays.asList("계", "무"));
        cheonheeMap.put(12, Arrays.asList("갑", "기"));

        return cheonheeMap.getOrDefault(month, Collections.emptyList()).contains(dayBranch);
    }

    /**
     * 월살 계산 로직 (일살과 동일) - 월살과 일살은 동일한 로직으로 계산
     * @param date 날짜
     * @param dayMaster 일간 성향
     * @return 월살 여부
     */
    private boolean isWolsal(LocalDate date, String dayMaster) {
        int month = date.getMonthValue();
        Map<Integer, String> wolsalMap = new HashMap<>();
        wolsalMap.put(1, "정");
        wolsalMap.put(2, "신");
        wolsalMap.put(3, "임");
        wolsalMap.put(4, "계");
        wolsalMap.put(5, "정");
        wolsalMap.put(6, "을");
        wolsalMap.put(7, "경");
        wolsalMap.put(8, "정");
        wolsalMap.put(9, "무");
        wolsalMap.put(10, "계");
        wolsalMap.put(11, "임");
        wolsalMap.put(12, "기");

        return wolsalMap.get(month).equals(dayMaster);
    }

    /**
     * 일살 계산 로직 (월살과 동일) - 일살과 월살은 동일한 로직으로 계산
     * @param date 날짜
     * @param dayBranch 일주 두 번째 글자
     * @return 일살 여부
     */
    private boolean isIlsal(LocalDate date, String dayBranch) {
        int month = date.getMonthValue();
        Map<Integer, String> ilsalMap = new HashMap<>();
        ilsalMap.put(1, "정");
        ilsalMap.put(2, "신");
        ilsalMap.put(3, "임");
        ilsalMap.put(4, "계");
        ilsalMap.put(5, "정");
        ilsalMap.put(6, "을");
        ilsalMap.put(7, "경");
        ilsalMap.put(8, "정");
        ilsalMap.put(9, "무");
        ilsalMap.put(10, "계");
        ilsalMap.put(11, "임");
        ilsalMap.put(12, "기");

        return ilsalMap.get(month).equals(dayBranch);
    }
}
