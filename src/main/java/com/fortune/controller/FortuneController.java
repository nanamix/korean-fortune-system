package com.fortune.controller;

import com.fortune.dto.*;
import com.fortune.dto.SajuResult;
import com.fortune.service.DailyFortuneService;
import com.fortune.service.GanjiCalculatorService;
import com.fortune.service.GanjiCalendarService;
import com.fortune.service.TojeongBigyeolService;
import com.fortune.service.ZodiacFortuneService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import io.swagger.v3.oas.annotations.Parameter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.time.LocalDate;
import java.util.List;

/*
 * 운세 컨트롤러
 * 사주팔자 계산
 * 오늘의 운세
 * 내일의 운세
 * 특정 날짜 운세
 */
@RestController
@RequestMapping("/api/fortune")
@CrossOrigin(origins = "*")
@Tag(name = "운세 API", description = "한국형 만세력 운세 시스템의 모든 API를 제공합니다")
public class FortuneController {

    @Autowired
    private GanjiCalculatorService ganjiCalculator;

    @Autowired
    private DailyFortuneService dailyFortuneService;

    @Autowired
    private TojeongBigyeolService tojeongService;

    @Autowired
    private ZodiacFortuneService zodiacService;

    @Autowired
    private GanjiCalendarService ganjiCalendarService;

    /**
     * 사주팔자 계산
     */
    @Operation(
            summary = "사주팔자 계산",
            description = "생년월일시와 성별, 달력 구분을 입력하여 정확한 사주팔자를 계산합니다.",
            tags = {"사주팔자"}
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "사주팔자 계산 성공",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = SajuResult.class),
                            examples = @ExampleObject(
                                    name = "사주팔자 계산 결과",
                                    value = """
                    {
                      "success": true,
                      "message": "성공",
                      "data": {
                        "yearPillar": "신유",
                        "monthPillar": "신묘",
                        "dayPillar": "을미",
                        "timePillar": "정축",
                        "dayMaster": "을",
                        "birthDate": "1981-03-20",
                        "adjustedDateTime": "1981-03-20T01:27:00",
                        "calendarType": "SOLAR",
                        "gender": "M"
                      }
                    }
                    """
                            )
                    )
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "잘못된 요청 (입력값 오류)",
                    content = @Content(
                            mediaType = "application/json",
                            examples = @ExampleObject(
                                    value = """
                    {
                      "success": false,
                      "message": "사주 계산 중 오류가 발생했습니다: 올바르지 않은 날짜입니다",
                      "data": null
                    }
                    """
                            )
                    )
            )
    })
    /**
     * 사주팔자 계산
     */
    @PostMapping("/saju/calculate")
    public ResponseEntity<com.fortune.dto.ApiResponse<SajuResult>> calculateSaju(
            @Parameter(description = "사주 계산 요청 정보", required = true)
            @RequestBody @Valid SajuRequest request) {
        try {
            SajuResult result = ganjiCalculator.calculateCompleteSaju(
                    request.getBirthYear(),
                    request.getBirthMonth(),
                    request.getBirthDay(),
                    request.getBirthHour(),
                    request.getBirthMinute(),
                    request.getGender(),
                    request.getCalendarType()
            );

            return ResponseEntity.ok(com.fortune.dto.ApiResponse.success(result));
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(com.fortune.dto.ApiResponse.error("사주 계산 중 오류가 발생했습니다: " + e.getMessage()));
        }
    }

    /**
     * 오늘의 운세
     */
    @Operation(
            summary = "오늘의 운세",
            description = "생년월일시 정보를 바탕으로 오늘의 종합 운세를 제공합니다.",
            tags = {"일일운세"}
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "오늘 운세 조회 성공",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = DailyFortuneResult.class)
                    )
            )
    })
    @PostMapping("/daily/today")
    public ResponseEntity<com.fortune.dto.ApiResponse<DailyFortuneResult>> getTodayFortune(
            @Parameter(description = "사주 정보", required = true)
            @RequestBody @Valid SajuRequest request) {
        try {
            SajuResult saju = ganjiCalculator.calculateCompleteSaju(
                    request.getBirthYear(), request.getBirthMonth(),
                    request.getBirthDay(), request.getBirthHour(), request.getBirthMinute(),
                    request.getGender(), request.getCalendarType()
            );

            DailyFortuneResult result = dailyFortuneService.calculateDailyFortune(saju, LocalDate.now());
            return ResponseEntity.ok(com.fortune.dto.ApiResponse.success(result));
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(com.fortune.dto.ApiResponse.error("오늘 운세 계산 중 오류가 발생했습니다: " + e.getMessage()));
        }
    }


    /**
     * 내일의 운세
     */
    @Operation(
            summary = "내일의 운세",
            description = "생년월일시 정보를 바탕으로 내일의 종합 운세를 제공합니다.",
            tags = {"일일운세"}
    )
    @PostMapping("/daily/tomorrow")
    public ResponseEntity<com.fortune.dto.ApiResponse<DailyFortuneResult>> getTomorrowFortune(
            @Parameter(description = "사주 정보", required = true)
            @RequestBody @Valid SajuRequest request) {
        try {
            SajuResult saju = ganjiCalculator.calculateCompleteSaju(
                    request.getBirthYear(), request.getBirthMonth(),
                    request.getBirthDay(), request.getBirthHour(), request.getBirthMinute(),
                    request.getGender(), request.getCalendarType()
            );

            DailyFortuneResult result = dailyFortuneService.calculateDailyFortune(saju, LocalDate.now().plusDays(1));
            return ResponseEntity.ok(com.fortune.dto.ApiResponse.success(result));
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(com.fortune.dto.ApiResponse.error("내일 운세 계산 중 오류가 발생했습니다: " + e.getMessage()));
        }
    }


    /**
     * 특정 날짜 운세
     */
    @Operation(
            summary = "특정 날짜 운세",
            description = "지정한 날짜의 종합 운세를 제공합니다.",
            tags = {"일일운세"}
    )
    @PostMapping("/daily/{date}")
    public ResponseEntity<com.fortune.dto.ApiResponse<DailyFortuneResult>> getSpecificDateFortune(
            @Parameter(description = "조회할 날짜 (YYYY-MM-DD 형식)", example = "2025-06-22", required = true)
            @PathVariable String date,
            @Parameter(description = "사주 정보", required = true)
            @RequestBody @Valid SajuRequest request) {
        try {
            LocalDate targetDate = LocalDate.parse(date);

            SajuResult saju = ganjiCalculator.calculateCompleteSaju(
                    request.getBirthYear(), request.getBirthMonth(),
                    request.getBirthDay(), request.getBirthHour(), request.getBirthMinute(),
                    request.getGender(), request.getCalendarType()
            );

            DailyFortuneResult result = dailyFortuneService.calculateDailyFortune(saju, targetDate);
            return ResponseEntity.ok(com.fortune.dto.ApiResponse.success(result));
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(com.fortune.dto.ApiResponse.error("지정일 운세 계산 중 오류가 발생했습니다: " + e.getMessage()));
        }
    }


    /**
     * 토정비결
     * - 캐시 적용 (토정비결 계산 결과를 캐시에 저장하고, 토정비결 계산 시 캐시에서 가져옴)
     * @param request 토정비결 계산 요청
     * @return 토정비결 계산 결과
     */
    @Operation(
            summary = "토정비결 64괘 연간 운세",
            description = "생년월일과 대상 년도를 입력하여 토정비결 64괘 기반의 연간 운세를 제공합니다.",
            tags = {"토정비결"}
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "토정비결 계산 성공",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = TojeongResult.class),
                            examples = @ExampleObject(
                                    name = "토정비결 결과",
                                    value = """
                    {
                      "success": true,
                      "data": {
                        "targetYear": 2025,
                        "gwaNumber": 3,
                        "gwaName": "산수몽",
                        "gwaSymbol": "☶☵",
                        "summary": "배움을 통해 지혜를 얻는다",
                        "overallScore": 82,
                        "advice": "올해 당신의 운세는 '산수몽'괘입니다. 매우 좋은 운세입니다.",
                        "luckyMonths": [2, 5, 8, 11],
                        "cautionMonths": []
                      }
                    }
                    """
                            )
                    )
            )
    })
    @PostMapping("/tojeong")
    public ResponseEntity<com.fortune.dto.ApiResponse<TojeongResult>> getTojeongFortune(
            @Parameter(description = "토정비결 요청 정보", required = true)
            @RequestBody @Valid TojeongRequest request) {
        try {
            TojeongResult result = tojeongService.calculateTojeong(
                    request.getBirthYear(),
                    request.getBirthMonth(),
                    request.getBirthDay(),
                    request.getTargetYear()
            );

            return ResponseEntity.ok(com.fortune.dto.ApiResponse.success(result));
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(com.fortune.dto.ApiResponse.error("토정비결 계산 중 오류가 발생했습니다: " + e.getMessage()));
        }
    }

    /**
     * 간지달력 조회
     * - 캐시 적용 (간지달력 계산 결과를 캐시에 저장하고, 간지달력 계산 시 캐시에서 가져옴)
     * @param year 년도
     * @param month 월
     * @return 간지달력 계산 결과
     */
    @Operation(
            summary = "간지달력 조회",
            description = "지정한 년월의 간지달력 정보를 제공합니다.",
            tags = {"간지달력"}
    )
    @GetMapping("/calendar/ganji/{year}/{month}")
    public ResponseEntity<com.fortune.dto.ApiResponse<List<GanjiCalendarDay>>> getGanjiCalendar(
            @Parameter(description = "년도", example = "2025", required = true)
            @PathVariable int year,
            @Parameter(description = "월", example = "6", required = true)
            @PathVariable int month) {
        try {
            List<GanjiCalendarDay> result = ganjiCalendarService.generateMonthlyCalendar(year, month);
            return ResponseEntity.ok(com.fortune.dto.ApiResponse.success(result));
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(com.fortune.dto.ApiResponse.error("간지달력 조회 중 오류가 발생했습니다: " + e.getMessage()));
        }
    }

    /**
     * 별자리 운세
     * - 캐시 적용 (별자리 운세 계산 결과를 캐시에 저장하고, 별자리 운세 계산 시 캐시에서 가져옴)
     * @param request 별자리 운세 계산 요청
     * @return 별자리 운세 계산 결과
     */
    @Operation(
            summary = "별자리 운세",
            description = "생일을 바탕으로 서양 별자리 운세를 제공합니다.",
            tags = {"별자리운세"}
    )
    @PostMapping("/zodiac")
    public ResponseEntity<com.fortune.dto.ApiResponse<ZodiacFortuneResult>> getZodiacFortune(
            @Parameter(description = "별자리 운세 요청 정보", required = true)
            @RequestBody @Valid ZodiacRequest request) {
        try {
            ZodiacFortuneResult result = zodiacService.calculateZodiacFortune(
                    request.getBirthDate(),
                    request.getTargetDate() != null ? request.getTargetDate() : LocalDate.now()
            );

            return ResponseEntity.ok(com.fortune.dto.ApiResponse.success(result));
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(com.fortune.dto.ApiResponse.error("별자리 운세 계산 중 오류가 발생했습니다: " + e.getMessage()));
        }
    }
}
