package com.fortune.controller;

import com.fortune.dto.GanjiCalendarResponse;
import com.fortune.service.GanjiCalendarService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

/**
 * 📅 간지달력 뷰잉 컨트롤러
 * 
 * <p>간지달력을 실제 달력 형태로 보여주는 컨트롤러입니다.</p>
 * 
 * @author 하진영
 * @version 2.6.0
 * @since 2025-01-05
 */
@Slf4j
@RestController
@RequestMapping("/api/calendar")
@RequiredArgsConstructor
public class CalendarViewController {

    private final GanjiCalendarService ganjiCalendarService;

    /**
     * 간지달력 HTML 뷰 제공
     * 
     * @param year 연도
     * @param month 월
     * @return 간지달력 HTML 페이지
     */
    @GetMapping("/view/{year}/{month}")
    public ResponseEntity<String> getCalendarView(
            @PathVariable int year,
            @PathVariable int month) {

        log.info("📅 간지달력 뷰 요청: {}년 {}월", year, month);

        try {
            GanjiCalendarResponse calendarData = ganjiCalendarService.generateMonthlyCalendar(year, month);
            String html = generateCalendarHtml(calendarData);
            
            return ResponseEntity.ok()
                    .header("Content-Type", "text/html; charset=utf-8")
                    .body(html);
        } catch (Exception e) {
            log.error("❌ 간지달력 뷰 생성 실패: {}", e.getMessage(), e);
            return ResponseEntity.badRequest()
                    .header("Content-Type", "text/html; charset=utf-8")
                    .body(generateErrorHtml("간지달력 뷰 생성에 실패했습니다: " + e.getMessage()));
        }
    }

    /**
     * 현재 월 간지달력 뷰 제공
     * 
     * @return 현재 월 간지달력 HTML 페이지
     */
    @GetMapping("/view/current")
    public ResponseEntity<String> getCurrentMonthCalendarView() {
        LocalDate now = LocalDate.now();
        return getCalendarView(now.getYear(), now.getMonthValue());
    }

    /**
     * 간지달력 HTML 생성
     */
    private String generateCalendarHtml(GanjiCalendarResponse calendarData) {
        return String.format("""
            <!DOCTYPE html>
            <html lang="ko">
            <head>
                <meta charset="UTF-8">
                <meta name="viewport" content="width=device-width, initial-scale=1.0">
                <title>📅 %d년 %d월 간지달력 - 한국형 만세력 운세 시스템</title>
                <style>
                    * { margin: 0; padding: 0; box-sizing: border-box; }
                    body { 
                        font-family: 'Segoe UI', Tahoma, Geneva, Verdana, sans-serif; 
                        background: linear-gradient(135deg, #667eea 0%%, #764ba2 100%%);
                        color: white;
                        min-height: 100vh;
                        padding: 20px;
                    }
                    .container { 
                        max-width: 1200px; 
                        margin: 0 auto; 
                        background: rgba(255,255,255,0.1);
                        border-radius: 15px;
                        padding: 30px;
                        backdrop-filter: blur(10px);
                    }
                    .header { 
                        text-align: center; 
                        margin-bottom: 30px; 
                        padding: 20px;
                        background: rgba(255,255,255,0.1);
                        border-radius: 10px;
                    }
                    .header h1 { 
                        font-size: 2.5em; 
                        margin-bottom: 10px;
                        text-shadow: 2px 2px 4px rgba(0,0,0,0.3);
                    }
                    .header .subtitle { 
                        font-size: 1.2em; 
                        opacity: 0.9; 
                    }
                    .navigation { 
                        display: flex; 
                        justify-content: space-between; 
                        align-items: center; 
                        margin-bottom: 20px; 
                        padding: 15px;
                        background: rgba(255,255,255,0.1);
                        border-radius: 10px;
                    }
                    .nav-btn { 
                        background: rgba(255,255,255,0.2); 
                        border: none; 
                        color: white; 
                        padding: 10px 20px; 
                        border-radius: 5px; 
                        cursor: pointer; 
                        font-size: 1.1em;
                        transition: all 0.3s ease;
                    }
                    .nav-btn:hover { 
                        background: rgba(255,255,255,0.3); 
                        transform: translateY(-2px);
                    }
                    .calendar { 
                        background: rgba(255,255,255,0.1); 
                        border-radius: 10px; 
                        overflow: hidden; 
                        box-shadow: 0 8px 32px rgba(0,0,0,0.3);
                    }
                    .calendar-header { 
                        display: grid; 
                        grid-template-columns: repeat(7, 1fr); 
                        background: rgba(255,255,255,0.2); 
                        padding: 15px 0;
                    }
                    .calendar-header div { 
                        text-align: center; 
                        font-weight: bold; 
                        font-size: 1.1em;
                        padding: 10px;
                    }
                    .calendar-body { 
                        display: grid; 
                        grid-template-columns: repeat(7, 1fr); 
                    }
                    .calendar-day { 
                        border: 1px solid rgba(255,255,255,0.1); 
                        padding: 15px; 
                        min-height: 120px; 
                        position: relative;
                        transition: all 0.3s ease;
                    }
                    .calendar-day:hover { 
                        background: rgba(255,255,255,0.1); 
                        transform: scale(1.02);
                    }
                    .day-number { 
                        font-size: 1.3em; 
                        font-weight: bold; 
                        margin-bottom: 5px;
                        text-align: center;
                    }
                    .ganji { 
                        font-size: 0.9em; 
                        text-align: center; 
                        margin-bottom: 5px;
                        padding: 3px;
                        border-radius: 3px;
                        background: rgba(255,255,255,0.1);
                    }
                    .fortune-score { 
                        font-size: 0.8em; 
                        text-align: center; 
                        margin-bottom: 5px;
                    }
                    .lucky-day { 
                        background: rgba(255, 215, 0, 0.3); 
                        border: 2px solid gold;
                    }
                    .lucky-day .day-number { 
                        color: gold; 
                        text-shadow: 1px 1px 2px rgba(0,0,0,0.5);
                    }
                    .solar-term { 
                        font-size: 0.7em; 
                        text-align: center; 
                        color: #ffd700;
                        margin-top: 5px;
                    }
                    .month-info { 
                        margin-top: 30px; 
                        padding: 20px;
                        background: rgba(255,255,255,0.1);
                        border-radius: 10px;
                    }
                    .month-info h3 { 
                        margin-bottom: 15px; 
                        color: #ffd700;
                    }
                    .info-grid { 
                        display: grid; 
                        grid-template-columns: repeat(auto-fit, minmax(250px, 1fr)); 
                        gap: 20px; 
                    }
                    .info-item { 
                        background: rgba(255,255,255,0.1); 
                        padding: 15px; 
                        border-radius: 8px;
                    }
                    .info-item h4 { 
                        margin-bottom: 10px; 
                        color: #ffd700;
                    }
                    .lucky-days-list { 
                        display: flex; 
                        flex-wrap: wrap; 
                        gap: 5px; 
                    }
                    .lucky-day-tag { 
                        background: rgba(255, 215, 0, 0.3); 
                        padding: 3px 8px; 
                        border-radius: 15px; 
                        font-size: 0.8em;
                        border: 1px solid gold;
                    }
                    .caution-day-tag { 
                        background: rgba(255, 0, 0, 0.3); 
                        padding: 3px 8px; 
                        border-radius: 15px; 
                        font-size: 0.8em;
                        border: 1px solid #ff4444;
                    }
                    .empty-day { 
                        background: rgba(255,255,255,0.05); 
                    }
                    @media (max-width: 768px) {
                        .container { padding: 15px; }
                        .header h1 { font-size: 2em; }
                        .calendar-day { padding: 10px; min-height: 100px; }
                        .day-number { font-size: 1.1em; }
                        .ganji { font-size: 0.8em; }
                    }
                </style>
            </head>
            <body>
                <div class="container">
                    <div class="header">
                        <h1>📅 %d년 %d월 간지달력</h1>
                        <div class="subtitle">한국형 만세력 운세 시스템</div>
                    </div>
                    
                    <div class="navigation">
                        <button class="nav-btn" onclick="navigateMonth(%d, %d)">← 이전 달</button>
                        <div style="font-size: 1.3em; font-weight: bold;">%d년 %d월</div>
                        <button class="nav-btn" onclick="navigateMonth(%d, %d)">다음 달 →</button>
                    </div>
                    
                    <div class="calendar">
                        <div class="calendar-header">
                            <div>일요일</div>
                            <div>월요일</div>
                            <div>화요일</div>
                            <div>수요일</div>
                            <div>목요일</div>
                            <div>금요일</div>
                            <div>토요일</div>
                        </div>
                        <div class="calendar-body">
                            %s
                        </div>
                    </div>
                    
                    <div class="month-info">
                        <h3>📊 %d년 %d월 운세 정보</h3>
                        <div class="info-grid">
                            <div class="info-item">
                                <h4>🎯 월간 테마</h4>
                                <p>%s</p>
                            </div>
                            <div class="info-item">
                                <h4>💡 월간 조언</h4>
                                <p>%s</p>
                            </div>
                            <div class="info-item">
                                <h4>🍀 길한 날</h4>
                                <div class="lucky-days-list">
                                    %s
                                </div>
                            </div>
                            <div class="info-item">
                                <h4>⚠️ 조심할 날</h4>
                                <div class="lucky-days-list">
                                    %s
                                </div>
                            </div>
                            <div class="info-item">
                                <h4>🌿 절기</h4>
                                <p>%s</p>
                            </div>
                        </div>
                    </div>
                </div>
                
                <script>
                    function navigateMonth(year, month) {
                        window.location.href = `/api/calendar/view/${year}/${month}`;
                    }
                    
                    // 현재 날짜 하이라이트
                    const today = new Date();
                    const currentYear = today.getFullYear();
                    const currentMonth = today.getMonth() + 1;
                    const currentDay = today.getDate();
                    
                    if (currentYear === %d && currentMonth === %d) {
                        const dayElements = document.querySelectorAll('.calendar-day');
                        dayElements.forEach(day => {
                            const dayNumber = day.querySelector('.day-number');
                            if (dayNumber && parseInt(dayNumber.textContent) === currentDay) {
                                day.style.border = '3px solid #ffd700';
                                day.style.boxShadow = '0 0 15px rgba(255, 215, 0, 0.5)';
                            }
                        });
                    }
                </script>
            </body>
            </html>
            """,
            calendarData.getYear(), calendarData.getMonth(),
            calendarData.getYear(), calendarData.getMonth() - 1,
            calendarData.getYear(), calendarData.getMonth(),
            calendarData.getYear(), calendarData.getMonth() + 1,
            calendarData.getYear(), calendarData.getMonth(),
            generateCalendarDaysHtml(calendarData),
            calendarData.getYear(), calendarData.getMonth(),
            calendarData.getMonthlyTheme(),
            calendarData.getMonthlyAdvice(),
            generateLuckyDaysHtml(calendarData.getLuckyDays()),
            generateCautionDaysHtml(calendarData.getCautionDays()),
            String.join(", ", calendarData.getSolarTerms()),
            calendarData.getYear(), calendarData.getMonth()
        );
    }

    /**
     * 달력 일자 HTML 생성
     */
    private String generateCalendarDaysHtml(GanjiCalendarResponse calendarData) {
        StringBuilder html = new StringBuilder();
        
        // 달력 시작 전 빈 칸 추가
        LocalDate firstDay = LocalDate.of(calendarData.getYear(), calendarData.getMonth(), 1);
        int dayOfWeek = firstDay.getDayOfWeek().getValue() % 7; // 0=일요일, 1=월요일, ...
        
        for (int i = 0; i < dayOfWeek; i++) {
            html.append("<div class='calendar-day empty-day'></div>");
        }
        
        // 달력 일자 추가
        for (var day : calendarData.getDays()) {
            String dayClass = "calendar-day";
            if (day.isLuckyDay()) {
                dayClass += " lucky-day";
            }
            
            html.append(String.format("""
                <div class="%s">
                    <div class="day-number">%d</div>
                    <div class="ganji">%s</div>
                    <div class="fortune-score">%d점</div>
                    %s
                </div>
                """,
                dayClass,
                day.getDate().getDayOfMonth(),
                day.getDayPillar(),
                day.getFortuneScore(),
                day.getSolarTerm() != null && !day.getSolarTerm().isEmpty() 
                    ? "<div class='solar-term'>" + day.getSolarTerm() + "</div>" 
                    : ""
            ));
        }
        
        return html.toString();
    }

    /**
     * 길한 날 HTML 생성
     */
    private String generateLuckyDaysHtml(java.util.List<Integer> luckyDays) {
        if (luckyDays == null || luckyDays.isEmpty()) {
            return "<span style='opacity: 0.7;'>길한 날이 없습니다</span>";
        }
        
        StringBuilder html = new StringBuilder();
        for (Integer day : luckyDays) {
            html.append(String.format("<span class='lucky-day-tag'>%d일</span>", day));
        }
        return html.toString();
    }

    /**
     * 조심할 날 HTML 생성
     */
    private String generateCautionDaysHtml(java.util.List<Integer> cautionDays) {
        if (cautionDays == null || cautionDays.isEmpty()) {
            return "<span style='opacity: 0.7;'>조심할 날이 없습니다</span>";
        }
        
        StringBuilder html = new StringBuilder();
        for (Integer day : cautionDays) {
            html.append(String.format("<span class='caution-day-tag'>%d일</span>", day));
        }
        return html.toString();
    }

    /**
     * 에러 HTML 생성
     */
    private String generateErrorHtml(String errorMessage) {
        return String.format("""
            <!DOCTYPE html>
            <html lang="ko">
            <head>
                <meta charset="UTF-8">
                <meta name="viewport" content="width=device-width, initial-scale=1.0">
                <title>오류 - 간지달력</title>
                <style>
                    body { 
                        font-family: 'Segoe UI', Tahoma, Geneva, Verdana, sans-serif; 
                        background: linear-gradient(135deg, #667eea 0%%, #764ba2 100%%);
                        color: white;
                        display: flex;
                        justify-content: center;
                        align-items: center;
                        min-height: 100vh;
                        margin: 0;
                        padding: 20px;
                    }
                    .error-container { 
                        background: rgba(255,255,255,0.1);
                        border-radius: 15px;
                        padding: 40px;
                        text-align: center;
                        backdrop-filter: blur(10px);
                        max-width: 500px;
                    }
                    .error-icon { 
                        font-size: 4em; 
                        margin-bottom: 20px; 
                    }
                    .error-title { 
                        font-size: 1.5em; 
                        margin-bottom: 15px; 
                        color: #ff6b6b;
                    }
                    .error-message { 
                        font-size: 1.1em; 
                        opacity: 0.9; 
                        margin-bottom: 30px;
                    }
                    .back-btn { 
                        background: rgba(255,255,255,0.2); 
                        border: none; 
                        color: white; 
                        padding: 12px 24px; 
                        border-radius: 5px; 
                        cursor: pointer; 
                        font-size: 1.1em;
                        text-decoration: none;
                        display: inline-block;
                        transition: all 0.3s ease;
                    }
                    .back-btn:hover { 
                        background: rgba(255,255,255,0.3); 
                        transform: translateY(-2px);
                    }
                </style>
            </head>
            <body>
                <div class="error-container">
                    <div class="error-icon">❌</div>
                    <div class="error-title">오류가 발생했습니다</div>
                    <div class="error-message">%s</div>
                    <a href="/" class="back-btn">홈으로 돌아가기</a>
                </div>
            </body>
            </html>
            """, errorMessage);
    }
} 