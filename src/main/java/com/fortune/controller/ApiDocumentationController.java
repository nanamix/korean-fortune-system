package com.fortune.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import com.fortune.dto.ApiResponse;
import lombok.extern.slf4j.Slf4j;

import java.util.HashMap;
import java.util.Map;
import java.util.List;
import java.util.Arrays;

/**
 * 📚 API 문서화 컨트롤러
 * 
 * <p>Swagger 대신 직접 API 문서를 제공하는 컨트롤러입니다.</p>
 * 
 * @author 하진영
 * @version 2.5.0
 * @since 2025-06-24
 */
@Slf4j
@RestController
@RequestMapping("/api/docs")
public class ApiDocumentationController {

    /**
     * API 문서 목록 조회
     * 
     * @return API 문서 목록
     */
    @GetMapping
    public ResponseEntity<ApiResponse<Map<String, Object>>> getApiDocumentation() {
        Map<String, Object> docs = new HashMap<>();
        
        // API 그룹별 정보
        docs.put("title", "🔮 한국형 만세력 운세 시스템 API 문서");
        docs.put("version", "2.5.0");
        docs.put("description", "전통 사주팔자와 토정비결을 제공하는 한국형 운세 시스템의 REST API 문서");
        docs.put("baseUrl", "http://localhost:8080");
        
        // API 엔드포인트 목록
        Map<String, Object> endpoints = new HashMap<>();
        
        // 사주 관련 API
        Map<String, Object> sajuApis = new HashMap<>();
        sajuApis.put("calculate", Map.of(
            "method", "POST",
            "url", "/api/fortune/saju/calculate",
            "description", "사주팔자 계산",
            "requestBody", Map.of(
                "birthYear", "int (1900-2030)",
                "birthMonth", "int (1-12)",
                "birthDay", "int (1-31)",
                "birthHour", "int (0-23)",
                "birthMinute", "int (0-59)",
                "gender", "String (M/F)",
                "calendarType", "String (SOLAR/LUNAR)"
            ),
            "example", Map.of(
                "birthYear", 1990,
                "birthMonth", 5,
                "birthDay", 15,
                "birthHour", 14,
                "birthMinute", 30,
                "gender", "M",
                "calendarType", "SOLAR"
            )
        ));
        endpoints.put("사주팔자", sajuApis);
        
        // 일일 운세 API
        Map<String, Object> dailyApis = new HashMap<>();
        dailyApis.put("calculate", Map.of(
            "method", "POST",
            "url", "/api/fortune/daily",
            "description", "일일 운세 계산",
            "parameters", Map.of(
                "targetDate", "LocalDate (YYYY-MM-DD)"
            )
        ));
        dailyApis.put("today", Map.of(
            "method", "POST",
            "url", "/api/fortune/daily/today",
            "description", "오늘의 운세"
        ));
        endpoints.put("일일운세", dailyApis);
        
        // 토정비결 API
        Map<String, Object> tojeongApis = new HashMap<>();
        tojeongApis.put("calculate", Map.of(
            "method", "POST",
            "url", "/api/fortune/tojeong",
            "description", "토정비결 계산",
            "requestBody", Map.of(
                "birthYear", "int",
                "targetYear", "int"
            )
        ));
        endpoints.put("토정비결", tojeongApis);
        
        // 별자리 운세 API
        Map<String, Object> zodiacApis = new HashMap<>();
        zodiacApis.put("calculate", Map.of(
            "method", "POST",
            "url", "/api/fortune/zodiac",
            "description", "별자리 운세 계산",
            "requestBody", Map.of(
                "birthDate", "LocalDate",
                "targetDate", "LocalDate"
            )
        ));
        endpoints.put("별자리운세", zodiacApis);
        
        // 간지달력 API
        Map<String, Object> calendarApis = new HashMap<>();
        calendarApis.put("ganji", Map.of(
            "method", "GET",
            "url", "/api/fortune/calendar/ganji/{year}/{month}",
            "description", "간지달력 조회",
            "pathVariables", Map.of(
                "year", "int (1900-2100)",
                "month", "int (1-12)"
            )
        ));
        endpoints.put("간지달력", calendarApis);
        
        // AI 관련 API
        Map<String, Object> aiApis = new HashMap<>();
        aiApis.put("interpretSaju", Map.of(
            "method", "POST",
            "url", "/api/fortune/ai/interpret-saju",
            "description", "AI 사주 해석"
        ));
        aiApis.put("dailyAdvice", Map.of(
            "method", "POST",
            "url", "/api/fortune/ai/daily-advice",
            "description", "AI 일일 운세 조언"
        ));
        aiApis.put("ask", Map.of(
            "method", "POST",
            "url", "/api/fortune/ai/ask",
            "description", "AI 운세 질문 답변",
            "parameters", Map.of(
                "question", "String"
            )
        ));
        endpoints.put("AI운세", aiApis);
        
        // 시스템 API
        Map<String, Object> systemApis = new HashMap<>();
        systemApis.put("status", Map.of(
            "method", "GET",
            "url", "/api/system/status",
            "description", "시스템 상태 확인"
        ));
        systemApis.put("health", Map.of(
            "method", "GET",
            "url", "/api/fortune/health",
            "description", "운세 시스템 상태 확인"
        ));
        endpoints.put("시스템", systemApis);
        
        docs.put("endpoints", endpoints);
        
        // 응답 형식
        docs.put("responseFormat", Map.of(
            "success", "boolean",
            "data", "Object",
            "message", "String",
            "errorCode", "String (에러시에만)",
            "timestamp", "String"
        ));
        
        // 에러 코드
        docs.put("errorCodes", Map.of(
            "SAJU_CALC_ERROR", "사주팔자 계산 오류",
            "DAILY_FORTUNE_ERROR", "일일 운세 계산 오류",
            "TOJEONG_CALC_ERROR", "토정비결 계산 오류",
            "ZODIAC_FORTUNE_ERROR", "별자리 운세 계산 오류",
            "GANJI_CALENDAR_ERROR", "간지달력 조회 오류",
            "AI_SERVICE_DISABLED", "AI 서비스 비활성화",
            "AI_INTERPRETATION_ERROR", "AI 해석 오류",
            "AI_ADVICE_ERROR", "AI 조언 오류",
            "AI_QUESTION_ERROR", "AI 질문 답변 오류"
        ));
        
        return ResponseEntity.ok(ApiResponse.success(docs));
    }

    /**
     * 특정 API 상세 정보 조회
     * 
     * @param category API 카테고리
     * @param endpoint API 엔드포인트
     * @return API 상세 정보
     */
    @GetMapping("/{category}/{endpoint}")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getApiDetail(
            @PathVariable String category,
            @PathVariable String endpoint) {
        
        log.info("📚 API 상세 정보 조회: {}/{}", category, endpoint);
        
        Map<String, Object> detail = new HashMap<>();
        detail.put("category", category);
        detail.put("endpoint", endpoint);
        detail.put("description", "API 상세 정보는 /api/docs에서 전체 목록을 확인하세요.");
        
        return ResponseEntity.ok(ApiResponse.success(detail));
    }

    /**
     * API 테스트 페이지 HTML 제공 (파라미터 입력 가능)
     * 
     * @return API 테스트 페이지
     */
    @GetMapping("/test")
    public ResponseEntity<String> getApiTestPage() {
        String html = """
            <!DOCTYPE html>
            <html lang="ko">
            <head>
                <meta charset="UTF-8">
                <meta name="viewport" content="width=device-width, initial-scale=1.0">
                <title>🔮 한국형 만세력 운세 시스템 API 테스트</title>
                <style>
                    body { font-family: 'Segoe UI', Tahoma, Geneva, Verdana, sans-serif; margin: 0; padding: 20px; background: linear-gradient(135deg, #667eea 0%, #764ba2 100%); color: white; }
                    .container { max-width: 1200px; margin: 0 auto; }
                    h1 { text-align: center; margin-bottom: 30px; }
                    .api-section { background: rgba(255,255,255,0.1); border-radius: 10px; padding: 20px; margin-bottom: 20px; }
                    .api-endpoint { background: rgba(255,255,255,0.05); border-radius: 5px; padding: 15px; margin: 10px 0; }
                    .method { display: inline-block; padding: 5px 10px; border-radius: 3px; font-weight: bold; margin-right: 10px; }
                    .get { background: #61affe; }
                    .post { background: #49cc90; }
                    .url { font-family: monospace; background: rgba(0,0,0,0.3); padding: 5px; border-radius: 3px; }
                    .description { margin: 10px 0; }
                    .form-group { margin: 10px 0; }
                    .form-group label { display: inline-block; width: 120px; font-weight: bold; }
                    .form-group input, .form-group select { padding: 8px; border: none; border-radius: 5px; width: 200px; margin-left: 10px; }
                    .form-group input[type="number"] { width: 100px; }
                    .form-group input[type="date"] { width: 150px; }
                    .test-button { background: #ff6b6b; color: white; border: none; padding: 10px 20px; border-radius: 5px; cursor: pointer; margin: 5px; }
                    .test-button:hover { background: #ff5252; }
                    .response { background: rgba(0,0,0,0.3); padding: 10px; border-radius: 5px; margin-top: 10px; font-family: monospace; white-space: pre-wrap; max-height: 400px; overflow-y: auto; }
                    .error { background: rgba(255,0,0,0.2); border-left: 4px solid #ff0000; }
                    .success { background: rgba(0,255,0,0.1); border-left: 4px solid #00ff00; }
                    .loading { opacity: 0.6; pointer-events: none; }
                    .status { padding: 5px 10px; border-radius: 3px; margin: 5px 0; font-size: 12px; }
                    .status.success { background: rgba(0,255,0,0.2); }
                    .status.error { background: rgba(255,0,0,0.2); }
                </style>
            </head>
            <body>
                <div class="container">
                    <h1>🔮 한국형 만세력 운세 시스템 API 테스트</h1>
                    
                    <div class="api-section">
                        <h2>📊 사주팔자 계산</h2>
                        <div class="api-endpoint">
                            <span class="method post">POST</span>
                            <span class="url">/api/fortune/saju/calculate</span>
                            <div class="description">생년월일시를 입력받아 전통 사주팔자를 계산합니다.</div>
                            
                            <div class="form-group">
                                <label>출생연도:</label>
                                <input type="number" id="saju-birthYear" value="1981" min="1900" max="2030">
                            </div>
                            <div class="form-group">
                                <label>출생월:</label>
                                <input type="number" id="saju-birthMonth" value="3" min="1" max="12">
                            </div>
                            <div class="form-group">
                                <label>출생일:</label>
                                <input type="number" id="saju-birthDay" value="20" min="1" max="31">
                            </div>
                            <div class="form-group">
                                <label>출생시간:</label>
                                <input type="number" id="saju-birthHour" value="1" min="0" max="23">
                            </div>
                            <div class="form-group">
                                <label>출생분:</label>
                                <input type="number" id="saju-birthMinute" value="59" min="0" max="59">
                            </div>
                            <div class="form-group">
                                <label>성별:</label>
                                <select id="saju-gender">
                                    <option value="M">남성 (M)</option>
                                    <option value="F">여성 (F)</option>
                                </select>
                            </div>
                            <div class="form-group">
                                <label>달력타입:</label>
                                <select id="saju-calendarType">
                                    <option value="SOLAR">양력 (SOLAR)</option>
                                    <option value="LUNAR">음력 (LUNAR)</option>
                                </select>
                            </div>
                            
                            <button class="test-button" onclick="testSajuCalculate()">테스트</button>
                            <div id="saju-status" class="status" style="display:none;"></div>
                            <div id="saju-response" class="response" style="display:none;"></div>
                        </div>
                    </div>
                    
                    <div class="api-section">
                        <h2>📅 일일 운세</h2>
                        <div class="api-endpoint">
                            <span class="method post">POST</span>
                            <span class="url">/api/fortune/daily/today</span>
                            <div class="description">오늘의 운세를 계산합니다.</div>
                            
                            <div class="form-group">
                                <label>출생연도:</label>
                                <input type="number" id="daily-birthYear" value="1981" min="1900" max="2030">
                            </div>
                            <div class="form-group">
                                <label>출생월:</label>
                                <input type="number" id="daily-birthMonth" value="3" min="1" max="12">
                            </div>
                            <div class="form-group">
                                <label>출생일:</label>
                                <input type="number" id="daily-birthDay" value="20" min="1" max="31">
                            </div>
                            <div class="form-group">
                                <label>출생시간:</label>
                                <input type="number" id="daily-birthHour" value="1" min="0" max="23">
                            </div>
                            <div class="form-group">
                                <label>출생분:</label>
                                <input type="number" id="daily-birthMinute" value="59" min="0" max="59">
                            </div>
                            <div class="form-group">
                                <label>성별:</label>
                                <select id="daily-gender">
                                    <option value="M">남성 (M)</option>
                                    <option value="F">여성 (F)</option>
                                </select>
                            </div>
                            <div class="form-group">
                                <label>달력타입:</label>
                                <select id="daily-calendarType">
                                    <option value="SOLAR">양력 (SOLAR)</option>
                                    <option value="LUNAR">음력 (LUNAR)</option>
                                </select>
                            </div>
                            
                            <button class="test-button" onclick="testTodayFortune()">테스트</button>
                            <div id="daily-status" class="status" style="display:none;"></div>
                            <div id="daily-response" class="response" style="display:none;"></div>
                        </div>
                    </div>
                    
                    <div class="api-section">
                        <h2>📜 토정비결</h2>
                        <div class="api-endpoint">
                            <span class="method post">POST</span>
                            <span class="url">/api/fortune/tojeong</span>
                            <div class="description">토정비결을 계산합니다.</div>
                            
                            <div class="form-group">
                                <label>출생연도:</label>
                                <input type="number" id="tojeong-birthYear" value="1981" min="1900" max="2030">
                            </div>
                            <div class="form-group">
                                <label>출생월:</label>
                                <input type="number" id="tojeong-birthMonth" value="3" min="1" max="12">
                            </div>
                            <div class="form-group">
                                <label>출생일:</label>
                                <input type="number" id="tojeong-birthDay" value="20" min="1" max="31">
                            </div>

                            <div class="form-group">
                                <label>대상연도:</label>
                                <input type="number" id="tojeong-targetYear" value="2025" min="1900" max="2030">
                            </div>
                            
                            <button class="test-button" onclick="testTojeong()">테스트</button>
                            <div id="tojeong-status" class="status" style="display:none;"></div>
                            <div id="tojeong-response" class="response" style="display:none;"></div>
                        </div>
                    </div>
                    
                    <div class="api-section">
                        <h2>⭐ 별자리 운세</h2>
                        <div class="api-endpoint">
                            <span class="method post">POST</span>
                            <span class="url">/api/fortune/zodiac</span>
                            <div class="description">별자리 운세를 계산합니다.</div>
                            
                            <div class="form-group">
                                <label>출생연도:</label>
                                <input type="number" id="zodiac-birthYear" value="1981" min="1900" max="2030">
                            </div>
                            <div class="form-group">
                                <label>출생월:</label>
                                <input type="number" id="zodiac-birthMonth" value="3" min="1" max="12">
                            </div>
                            <div class="form-group">
                                <label>출생일:</label>
                                <input type="number" id="zodiac-birthDay" value="20" min="1" max="31">
                            </div>

                            <div class="form-group">
                                <label>대상일:</label>
                                <input type="date" id="zodiac-targetDate">
                            </div>
                            
                            <button class="test-button" onclick="testZodiac()">테스트</button>
                            <div id="zodiac-status" class="status" style="display:none;"></div>
                            <div id="zodiac-response" class="response" style="display:none;"></div>
                        </div>
                    </div>
                    
                    <div class="api-section">
                        <h2>📆 간지달력</h2>
                        <div class="api-endpoint">
                            <span class="method get">GET</span>
                            <span class="url">/api/fortune/calendar/ganji/{year}/{month}</span>
                            <div class="description">간지달력을 조회합니다.</div>
                            
                            <div class="form-group">
                                <label>연도:</label>
                                <input type="number" id="calendar-year" value="2025" min="1900" max="2100">
                            </div>
                            <div class="form-group">
                                <label>월:</label>
                                <select id="calendar-month">
                                    <option value="1">1월</option>
                                    <option value="2">2월</option>
                                    <option value="3">3월</option>
                                    <option value="4">4월</option>
                                    <option value="5">5월</option>
                                    <option value="6">6월</option>
                                    <option value="7">7월</option>
                                    <option value="8">8월</option>
                                    <option value="9">9월</option>
                                    <option value="10">10월</option>
                                    <option value="11">11월</option>
                                    <option value="12">12월</option>
                                </select>
                            </div>
                            
                            <button class="test-button" onclick="testGanjiCalendar()">테스트</button>
                            <div id="calendar-status" class="status" style="display:none;"></div>
                            <div id="calendar-response" class="response" style="display:none;"></div>
                        </div>
                    </div>
                    
                    <div class="api-section">
                        <h2>🔍 시스템 상태</h2>
                        <div class="api-endpoint">
                            <span class="method get">GET</span>
                            <span class="url">/api/system/status</span>
                            <div class="description">시스템 상태를 확인합니다.</div>
                            
                            <button class="test-button" onclick="testSystemStatus()">테스트</button>
                            <div id="status-status" class="status" style="display:none;"></div>
                            <div id="status-response" class="response" style="display:none;"></div>
                        </div>
                    </div>
                </div>
                
                <script>
                    // 페이지 로드 시 오늘 날짜를 별자리 운세 대상일로 설정
                    window.addEventListener('load', function() {
                        const today = new Date();
                        const year = today.getFullYear();
                        const month = String(today.getMonth() + 1).padStart(2, '0');
                        const day = String(today.getDate()).padStart(2, '0');
                        const todayString = `${year}-${month}-${day}`;
                        document.getElementById('zodiac-targetDate').value = todayString;
                    });
                    
                    function showStatus(elementId, message, isSuccess) {
                        const statusElement = document.getElementById(elementId);
                        statusElement.textContent = message;
                        statusElement.className = 'status ' + (isSuccess ? 'success' : 'error');
                        statusElement.style.display = 'block';
                    }
                    
                    function showResponse(elementId, data, isSuccess) {
                        const responseElement = document.getElementById(elementId);
                        responseElement.textContent = JSON.stringify(data, null, 2);
                        responseElement.className = 'response ' + (isSuccess ? 'success' : 'error');
                        responseElement.style.display = 'block';
                    }
                    
                    async function testSajuCalculate() {
                        const button = event.target;
                        button.classList.add('loading');
                        
                        try {
                            const requestBody = {
                                birthYear: parseInt(document.getElementById('saju-birthYear').value),
                                birthMonth: parseInt(document.getElementById('saju-birthMonth').value),
                                birthDay: parseInt(document.getElementById('saju-birthDay').value),
                                birthHour: parseInt(document.getElementById('saju-birthHour').value),
                                birthMinute: parseInt(document.getElementById('saju-birthMinute').value),
                                gender: document.getElementById('saju-gender').value,
                                calendarType: document.getElementById('saju-calendarType').value
                            };
                            
                            showStatus('saju-status', '요청 중...', true);
                            
                            const response = await fetch('/api/fortune/saju/calculate', {
                                method: 'POST',
                                headers: { 'Content-Type': 'application/json' },
                                body: JSON.stringify(requestBody)
                            });
                            
                            const result = await response.json();
                            
                            if (response.ok) {
                                showStatus('saju-status', '✅ 성공!', true);
                                showResponse('saju-response', result, true);
                            } else {
                                showStatus('saju-status', '❌ 오류: ' + (result.message || '알 수 없는 오류'), false);
                                showResponse('saju-response', result, false);
                            }
                        } catch (error) {
                            showStatus('saju-status', '❌ 네트워크 오류: ' + error.message, false);
                            showResponse('saju-response', { error: error.message }, false);
                        } finally {
                            button.classList.remove('loading');
                        }
                    }
                    
                    async function testTodayFortune() {
                        const button = event.target;
                        button.classList.add('loading');
                        
                        try {
                            const requestBody = {
                                birthYear: parseInt(document.getElementById('daily-birthYear').value),
                                birthMonth: parseInt(document.getElementById('daily-birthMonth').value),
                                birthDay: parseInt(document.getElementById('daily-birthDay').value),
                                birthHour: parseInt(document.getElementById('daily-birthHour').value),
                                birthMinute: parseInt(document.getElementById('daily-birthMinute').value),
                                gender: document.getElementById('daily-gender').value,
                                calendarType: document.getElementById('daily-calendarType').value
                            };
                            
                            showStatus('daily-status', '요청 중...', true);
                            
                            const response = await fetch('/api/fortune/daily/today', {
                                method: 'POST',
                                headers: { 'Content-Type': 'application/json' },
                                body: JSON.stringify(requestBody)
                            });
                            
                            const result = await response.json();
                            
                            if (response.ok) {
                                showStatus('daily-status', '✅ 성공!', true);
                                showResponse('daily-response', result, true);
                            } else {
                                showStatus('daily-status', '❌ 오류: ' + (result.message || '알 수 없는 오류'), false);
                                showResponse('daily-response', result, false);
                            }
                        } catch (error) {
                            showStatus('daily-status', '❌ 네트워크 오류: ' + error.message, false);
                            showResponse('daily-response', { error: error.message }, false);
                        } finally {
                            button.classList.remove('loading');
                        }
                    }
                    
                    async function testTojeong() {
                        const button = event.target;
                        button.classList.add('loading');
                        
                        try {
                            const requestBody = {
                                birthYear: parseInt(document.getElementById('tojeong-birthYear').value),
                                birthMonth: parseInt(document.getElementById('tojeong-birthMonth').value),
                                birthDay: parseInt(document.getElementById('tojeong-birthDay').value),
                                targetYear: parseInt(document.getElementById('tojeong-targetYear').value)
                            };
                            
                            showStatus('tojeong-status', '요청 중...', true);
                            
                            const response = await fetch('/api/fortune/tojeong', {
                                method: 'POST',
                                headers: { 'Content-Type': 'application/json' },
                                body: JSON.stringify(requestBody)
                            });
                            
                            const result = await response.json();
                            
                            if (response.ok) {
                                showStatus('tojeong-status', '✅ 성공!', true);
                                showResponse('tojeong-response', result, true);
                            } else {
                                showStatus('tojeong-status', '❌ 오류: ' + (result.message || '알 수 없는 오류'), false);
                                showResponse('tojeong-response', result, false);
                            }
                        } catch (error) {
                            showStatus('tojeong-status', '❌ 네트워크 오류: ' + error.message, false);
                            showResponse('tojeong-response', { error: error.message }, false);
                        } finally {
                            button.classList.remove('loading');
                        }
                    }
                    
                    async function testZodiac() {
                        const button = event.target;
                        button.classList.add('loading');
                        
                        try {
                            // 출생 정보를 LocalDate 형식으로 변환
                            const birthYear = parseInt(document.getElementById('zodiac-birthYear').value);
                            const birthMonth = parseInt(document.getElementById('zodiac-birthMonth').value);
                            const birthDay = parseInt(document.getElementById('zodiac-birthDay').value);
                            
                            // YYYY-MM-DD 형식으로 birthDate 생성
                            const birthDate = `${birthYear}-${String(birthMonth).padStart(2, '0')}-${String(birthDay).padStart(2, '0')}`;
                            
                            const requestBody = {
                                birthDate: birthDate,
                                targetDate: document.getElementById('zodiac-targetDate').value
                            };
                            
                            showStatus('zodiac-status', '요청 중...', true);
                            
                            const response = await fetch('/api/fortune/zodiac', {
                                method: 'POST',
                                headers: { 'Content-Type': 'application/json' },
                                body: JSON.stringify(requestBody)
                            });
                            
                            const result = await response.json();
                            
                            if (response.ok) {
                                showStatus('zodiac-status', '✅ 성공!', true);
                                showResponse('zodiac-response', result, true);
                            } else {
                                showStatus('zodiac-status', '❌ 오류: ' + (result.message || '알 수 없는 오류'), false);
                                showResponse('zodiac-response', result, false);
                            }
                        } catch (error) {
                            showStatus('zodiac-status', '❌ 네트워크 오류: ' + error.message, false);
                            showResponse('zodiac-response', { error: error.message }, false);
                        } finally {
                            button.classList.remove('loading');
                        }
                    }
                    
                    async function testGanjiCalendar() {
                        const button = event.target;
                        button.classList.add('loading');
                        
                        try {
                            const year = document.getElementById('calendar-year').value;
                            const month = document.getElementById('calendar-month').value;
                            
                            showStatus('calendar-status', '요청 중...', true);
                            
                            const response = await fetch(`/api/fortune/calendar/ganji/${year}/${month}`);
                            
                            const result = await response.json();
                            
                            if (response.ok) {
                                showStatus('calendar-status', '✅ 성공!', true);
                                showResponse('calendar-response', result, true);
                            } else {
                                showStatus('calendar-status', '❌ 오류: ' + (result.message || '알 수 없는 오류'), false);
                                showResponse('calendar-response', result, false);
                            }
                        } catch (error) {
                            showStatus('calendar-status', '❌ 네트워크 오류: ' + error.message, false);
                            showResponse('calendar-response', { error: error.message }, false);
                        } finally {
                            button.classList.remove('loading');
                        }
                    }
                    
                    async function testSystemStatus() {
                        const button = event.target;
                        button.classList.add('loading');
                        
                        try {
                            showStatus('status-status', '요청 중...', true);
                            
                            const response = await fetch('/api/system/status');
                            const result = await response.json();
                            
                            if (response.ok) {
                                showStatus('status-status', '✅ 성공!', true);
                                showResponse('status-response', result, true);
                            } else {
                                showStatus('status-status', '❌ 오류: ' + (result.message || '알 수 없는 오류'), false);
                                showResponse('status-response', result, false);
                            }
                        } catch (error) {
                            showStatus('status-status', '❌ 네트워크 오류: ' + error.message, false);
                            showResponse('status-response', { error: error.message }, false);
                        } finally {
                            button.classList.remove('loading');
                        }
                    }
                </script>
            </body>
            </html>
            """;
        
        return ResponseEntity.ok()
                .header("Content-Type", "text/html; charset=utf-8")
                .body(html);
    }
} 