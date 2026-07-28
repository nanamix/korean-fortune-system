package com.fortune.controller;

import com.fortune.dto.ApiResponse;
import com.fortune.dto.LocationSearchResult;
import com.fortune.exception.LocationSearchUnavailableException;
import com.fortune.service.LocationSearchService;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * 출생 위치 검색 API.
 */
@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/location")
public class LocationController {

    private final LocationSearchService locationSearchService;

    @GetMapping("/search")
    public ResponseEntity<ApiResponse<List<LocationSearchResult>>> search(
            @RequestParam(name = "q") String query) {
        // 출생 장소 검색어는 개인정보가 될 수 있으므로 값은 로그에 남기지 않는다.
        log.info("📍 출생 위치 검색 요청: queryLength={}", query == null ? 0 : query.length());
        try {
            return ResponseEntity.ok(ApiResponse.success(locationSearchService.search(query)));
        } catch (IllegalArgumentException exception) {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error(exception.getMessage(), "INVALID_LOCATION_QUERY"));
        } catch (LocationSearchUnavailableException exception) {
            log.warn("출생 위치 검색 제공자 연결 실패: {}", exception.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_GATEWAY)
                    .body(ApiResponse.error(
                            "위치 검색을 일시적으로 사용할 수 없습니다. 직접 좌표를 입력해 주세요.",
                            "LOCATION_SEARCH_UNAVAILABLE"));
        }
    }
}
