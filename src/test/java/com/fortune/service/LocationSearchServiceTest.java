package com.fortune.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.header;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;

import com.fortune.dto.LocationSearchResult;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestClient;

class LocationSearchServiceTest {

    private MockRestServiceServer server;
    private LocationSearchService service;

    @BeforeEach
    void setUp() {
        RestClient.Builder builder = RestClient.builder()
                .baseUrl("https://geocoding.example.test")
                .defaultHeader("User-Agent", "korean-fortune-system/test");
        server = MockRestServiceServer.bindTo(builder).build();
        service = new LocationSearchService(builder.build());
    }

    @Test
    void mapsCoordinatesAndIanaTimeZoneWithoutPersistingQuery() {
        server.expect(requestTo(
                        "https://geocoding.example.test/v1/search?name=%EC%84%9C%EC%9A%B8%ED%8A%B9%EB%B3%84%EC%8B%9C&count=8&language=ko&format=json"))
                .andExpect(header("User-Agent", "korean-fortune-system/test"))
                .andRespond(withSuccess("""
                        {
                          "results": [
                            {
                              "name": "서울특별시",
                              "latitude": 37.566,
                              "longitude": 126.9784,
                              "timezone": "Asia/Seoul",
                              "country_code": "KR",
                              "country": "대한민국",
                              "admin1": "서울특별시"
                            }
                          ]
                        }
                        """, MediaType.APPLICATION_JSON));

        List<LocationSearchResult> results = service.search("  서울특별시  ");

        assertThat(results).containsExactly(new LocationSearchResult(
                "서울특별시, 대한민국", "KR", 37.566, 126.9784, "Asia/Seoul"));
        server.verify();
    }

    @Test
    void filtersInvalidCoordinatesAndTimeZones() {
        server.expect(requestTo(
                        "https://geocoding.example.test/v1/search?name=test&count=8&language=ko&format=json"))
                .andRespond(withSuccess("""
                        {
                          "results": [
                            {"name":"Invalid latitude","latitude":91,"longitude":0,"timezone":"UTC"},
                            {"name":"Invalid zone","latitude":37,"longitude":127,"timezone":"Not/AZone"},
                            {"name":"Valid","latitude":35.1796,"longitude":129.0756,
                             "timezone":"Asia/Seoul","country_code":"KR","country":"대한민국","admin1":"부산광역시"}
                          ]
                        }
                        """, MediaType.APPLICATION_JSON));

        assertThat(service.search("test"))
                .containsExactly(new LocationSearchResult(
                        "Valid, 부산광역시, 대한민국", "KR", 35.1796, 129.0756, "Asia/Seoul"));
    }

    @Test
    void rejectsBlankShortAndOversizedQueriesBeforeCallingProvider() {
        assertThatThrownBy(() -> service.search(" "))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("2~80자");
        assertThatThrownBy(() -> service.search("가"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("2~80자");
        assertThatThrownBy(() -> service.search("가".repeat(81)))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("2~80자");
        server.verify();
    }
}
