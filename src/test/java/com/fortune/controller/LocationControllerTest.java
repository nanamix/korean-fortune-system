package com.fortune.controller;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.fortune.dto.ApiResponse;
import com.fortune.dto.LocationSearchResult;
import com.fortune.exception.LocationSearchUnavailableException;
import com.fortune.service.LocationSearchService;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

class LocationControllerTest {

    private final LocationSearchService service = mock(LocationSearchService.class);
    private final LocationController controller = new LocationController(service);

    @Test
    void returnsSearchResults() {
        List<LocationSearchResult> expected = List.of(
                new LocationSearchResult("서울특별시, 대한민국", "KR",
                        37.566, 126.9784, "Asia/Seoul"));
        when(service.search("서울")).thenReturn(expected);

        ResponseEntity<ApiResponse<List<LocationSearchResult>>> response =
                controller.search("서울");

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().isSuccess()).isTrue();
        assertThat(response.getBody().getData()).isEqualTo(expected);
    }

    @Test
    void returnsBadRequestForInvalidQuery() {
        when(service.search("가")).thenThrow(new IllegalArgumentException("위치 검색어는 2~80자로 입력하세요."));

        ResponseEntity<ApiResponse<List<LocationSearchResult>>> response =
                controller.search("가");

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getErrorCode()).isEqualTo("INVALID_LOCATION_QUERY");
    }

    @Test
    void returnsBadGatewayWhenProviderIsUnavailable() {
        when(service.search("서울")).thenThrow(
                new LocationSearchUnavailableException("provider unavailable", new RuntimeException()));

        ResponseEntity<ApiResponse<List<LocationSearchResult>>> response =
                controller.search("서울");

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_GATEWAY);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getErrorCode()).isEqualTo("LOCATION_SEARCH_UNAVAILABLE");
    }
}
