package com.fortune.service;

import com.fortune.dto.LocationSearchResult;
import com.fortune.exception.LocationSearchUnavailableException;
import java.time.Duration;
import java.time.ZoneId;
import java.time.zone.ZoneRulesException;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

/**
 * 도시·지역·우편번호를 WGS84 좌표와 IANA 시간대로 변환한다.
 *
 * <p>외부 제공자 URL은 설정에서 고정하며 사용자 입력을 호스트나 경로로 사용하지
 * 않는다. 검색어는 로그나 영속 저장소에 남기지 않는다.</p>
 */
@Service
public class LocationSearchService {

    private static final int MIN_QUERY_LENGTH = 2;
    private static final int MAX_QUERY_LENGTH = 80;
    private static final int MAX_RESULTS = 8;
    private static final ParameterizedTypeReference<Map<String, Object>> RESPONSE_TYPE =
            new ParameterizedTypeReference<>() {
            };

    private final RestClient restClient;

    @Autowired
    public LocationSearchService(
            @Value("${app.location-search.base-url:https://geocoding-api.open-meteo.com}") String baseUrl) {
        this(createRestClient(baseUrl));
    }

    LocationSearchService(RestClient restClient) {
        this.restClient = restClient;
    }

    public List<LocationSearchResult> search(String rawQuery) {
        String query = normalizeQuery(rawQuery);
        try {
            Map<String, Object> response = restClient.get()
                    .uri(uriBuilder -> uriBuilder
                            .path("/v1/search")
                            .queryParam("name", query)
                            .queryParam("count", MAX_RESULTS)
                            .queryParam("language", "ko")
                            .queryParam("format", "json")
                            .build())
                    .retrieve()
                    .body(RESPONSE_TYPE);
            return mapResults(response);
        } catch (RestClientException exception) {
            throw new LocationSearchUnavailableException(
                    "위치 검색 제공자에 연결할 수 없습니다.", exception);
        }
    }

    private static RestClient createRestClient(String baseUrl) {
        SimpleClientHttpRequestFactory requestFactory = new SimpleClientHttpRequestFactory();
        requestFactory.setConnectTimeout(Duration.ofSeconds(3));
        requestFactory.setReadTimeout(Duration.ofSeconds(5));
        return RestClient.builder()
                .baseUrl(baseUrl)
                .requestFactory(requestFactory)
                .defaultHeader("User-Agent",
                        "korean-fortune-system/3.1 (+https://github.com/nanamix/korean-fortune-system)")
                .build();
    }

    private String normalizeQuery(String rawQuery) {
        if (rawQuery == null) {
            throw new IllegalArgumentException("위치 검색어를 입력하세요.");
        }
        String query = rawQuery.strip().replaceAll("\\s+", " ");
        if (query.length() < MIN_QUERY_LENGTH || query.length() > MAX_QUERY_LENGTH) {
            throw new IllegalArgumentException("위치 검색어는 2~80자로 입력하세요.");
        }
        if (query.chars().anyMatch(Character::isISOControl)) {
            throw new IllegalArgumentException("위치 검색어에 제어 문자를 사용할 수 없습니다.");
        }
        return query;
    }

    private List<LocationSearchResult> mapResults(Map<String, Object> response) {
        if (response == null || !(response.get("results") instanceof List<?> results)) {
            return List.of();
        }

        List<LocationSearchResult> mapped = new ArrayList<>();
        Set<String> seen = new LinkedHashSet<>();
        for (Object item : results) {
            if (!(item instanceof Map<?, ?> result)) {
                continue;
            }
            LocationSearchResult location = mapResult(result);
            if (location == null) {
                continue;
            }
            String identity = String.format(Locale.ROOT, "%.5f:%.5f:%s",
                    location.latitude(), location.longitude(), location.timeZone());
            if (seen.add(identity)) {
                mapped.add(location);
            }
            if (mapped.size() == MAX_RESULTS) {
                break;
            }
        }
        return List.copyOf(mapped);
    }

    private LocationSearchResult mapResult(Map<?, ?> result) {
        Double latitude = number(result.get("latitude"));
        Double longitude = number(result.get("longitude"));
        String timeZone = text(result.get("timezone"));
        String name = displayName(result);
        if (latitude == null || longitude == null || name.isBlank()
                || latitude < -90 || latitude > 90
                || longitude < -180 || longitude > 180
                || !isValidTimeZone(timeZone)) {
            return null;
        }
        return new LocationSearchResult(
                name,
                text(result.get("country_code")).toUpperCase(Locale.ROOT),
                latitude,
                longitude,
                timeZone);
    }

    private String displayName(Map<?, ?> result) {
        LinkedHashSet<String> parts = new LinkedHashSet<>();
        addPart(parts, result.get("name"));
        addPart(parts, result.get("admin2"));
        addPart(parts, result.get("admin1"));
        addPart(parts, result.get("country"));
        return String.join(", ", parts);
    }

    private void addPart(Set<String> parts, Object value) {
        String part = text(value);
        if (!part.isBlank()) {
            parts.add(part);
        }
    }

    private boolean isValidTimeZone(String timeZone) {
        if (timeZone.isBlank() || timeZone.length() > 64) {
            return false;
        }
        try {
            ZoneId.of(timeZone);
            return true;
        } catch (ZoneRulesException exception) {
            return false;
        }
    }

    private Double number(Object value) {
        return value instanceof Number number ? number.doubleValue() : null;
    }

    private String text(Object value) {
        return value == null ? "" : value.toString().strip();
    }
}
