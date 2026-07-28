package com.fortune.dto;

/**
 * 출생 위치 검색 결과.
 *
 * @param name 표시할 위치명
 * @param countryCode ISO 3166-1 alpha-2 국가 코드
 * @param latitude WGS84 위도
 * @param longitude WGS84 경도
 * @param timeZone IANA 시간대
 */
public record LocationSearchResult(
        String name,
        String countryCode,
        double latitude,
        double longitude,
        String timeZone) {
}
