package com.fortune.service;

import org.springframework.stereotype.Service;
import com.fortune.dto.SajuResult;
import com.fortune.dto.SinsalInfo;
import java.time.LocalDate;
import java.util.*;

@Service
public class SinsalService {

    /**
     * 일일 길신/흉신 계산
     * 이 메서드는 주어진 날짜와 사주 결과를 기반으로 일일 길신과 흉신을 계산합니다.
     * 각 길신과 흉신은 SinsalInfo 객체로 표현되며, 이 객체는 이름, 설명, 길한지 여부, 점수 등을 포함합니다.
     * @param date 대상 날짜
     * @param saju 사주 결과
     * @return 길신/흉신 정보 리스트
     */
    public List<SinsalInfo> calculateDailySinsals(LocalDate date, SajuResult saju) {
        List<SinsalInfo> sinsals = new ArrayList<>();

        // 월덕 계산
        // 월덕은 사주와 날짜를 기반으로 계산됩니다.
        if (isWoldeok(date, saju)) {
            sinsals.add(new SinsalInfo("월덕", "달의 덕이 있는 날", true, 15));
        }

        // 천덕 계산
        // 천덕은 날짜와 사주를 기반으로 계산됩니다.
        if (isCheondeok(date, saju)) {
            sinsals.add(new SinsalInfo("천덕", "하늘의 덕이 있는 날", true, 20));
        }

        // 천희 계산
        // 천희는 날짜만을 기반으로 계산됩니다.
        if (isCheonhee(date)) {
            sinsals.add(new SinsalInfo("천희", "하늘의 기쁨이 있는 날", true, 12));
        }

        // 월살 계산
        // 월살은 사주와 날짜를 기반으로 계산됩니다.
        if (isWolsal(date, saju)) {
            sinsals.add(new SinsalInfo("월살", "달의 살기가 있는 날", false, 10));
        }

        // 일살 계산
        // 일살은 날짜만을 기반으로 계산됩니다.
        if (isIlsal(date)) {
            sinsals.add(new SinsalInfo("일살", "일진이 좋지 않은 날", false, 8));
        }

        return sinsals;
    }

    /**
     * 월덕 계산 (간단화)
     * 월덕은 특정 월에 해당하는 날의 덕을 계산합니다.
     * 예를 들어, 1월은 병정, 2월은 갑을 등으로 간단화하여 계산합니다.
     * @param date 대상 날짜
     * @param saju 사주 결과
     * @return 월덕 여부
     */
    private boolean isWoldeok(LocalDate date, SajuResult saju) {
        int month = date.getMonthValue();
        String dayMaster = saju.getDayMaster();

        // 월별 월덕 간지 (간단화)
        Map<Integer, List<String>> woldeokMap = new HashMap<>();
        woldeokMap.put(1, Arrays.asList("병", "정"));
        woldeokMap.put(2, Arrays.asList("갑", "을"));
        woldeokMap.put(3, Arrays.asList("임", "계"));
        woldeokMap.put(4, Arrays.asList("무", "기"));
        woldeokMap.put(5, Arrays.asList("경", "신"));
        woldeokMap.put(6, Arrays.asList("신", "유"));
        woldeokMap.put(7, Arrays.asList("유", "술"));
        woldeokMap.put(8, Arrays.asList("술", "해"));
        woldeokMap.put(9, Arrays.asList("해", "자"));
        woldeokMap.put(10, Arrays.asList("자", "축"));
        woldeokMap.put(11, Arrays.asList("축", "인"));
        woldeokMap.put(12, Arrays.asList("인", "묘"));

        return woldeokMap.getOrDefault(month, Collections.emptyList()).contains(dayMaster);
    }

    /**
     * 천덕 계산
     * 천덕은 특정 날짜의 덕을 계산합니다.
     * 예를 들어, 날짜가 5의 배수인 날은 천덕으로 간주합니다.
     * @param date 대상 날짜
     * @param saju 사주 결과
     * @return 천덕 여부
     */
    private boolean isCheondeok(LocalDate date, SajuResult saju) {
        // 간단화된 천덕 계산
        return date.getDayOfMonth() % 5 == 0;
    }

    /**
     * 천희 계산
     * 천희는 특정 날짜의 기쁨을 계산합니다.
     * 예를 들어, 일요일은 천희로 간주합니다.
     * @param date 대상 날짜
     * @return 천희 여부
     */
    private boolean isCheonhee(LocalDate date) {
        // 간단화된 천희 계산
        return date.getDayOfWeek().getValue() == 7; // 일요일
    }

    /**
     * 월살 계산
     * 월살은 특정 날짜와 사주를 기반으로 계산됩니다.
     * 예를 들어, 날짜가 13일인 날은 월살로 간주합니다.
     * @param date 대상 날짜
     * @param saju 사주 결과
     */
    private boolean isWolsal(LocalDate date, SajuResult saju) {
        // 간단화된 월살 계산
        return date.getDayOfMonth() == 13;
    }

    /**
     * 일살 계산
     * 일살은 특정 날짜만을 기반으로 계산됩니다.
     * 예를 들어, 화요일은 일살로 간주합니다.
     * @param date 대상 날짜
     * @return 일살 여부
     */
    private boolean isIlsal(LocalDate date) {
        // 간단화된 일살 계산
        return date.getDayOfWeek().getValue() == 2; // 화요일
    }
}
