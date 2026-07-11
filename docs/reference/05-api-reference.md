# 05. API 레퍼런스

> `FortuneController`, `CalendarViewController`, `SystemController`, `ApiDocumentationController` 의 REST 엔드포인트 전수입니다.
> 관련: [아키텍처](02-architecture.md) · [데이터 모델](04-data-model.md) · [AI와 폴백](06-ai-and-fallback.md)

---

## 5.1 공통 규약

- 모든 JSON 응답은 `ApiResponse<T>` 래퍼: `{ success, data, message, errorCode, timestamp }` (`ApiResponse.java:22-71`).
- 요청 바디는 `@Valid` 로 검증되며, 실패 시 `GlobalExceptionHandler` 가 `errorCode="입력 검증 오류"` 로 응답 (`GlobalExceptionHandler.java:36-46`).
- 각 컨트롤러 메서드는 자체 try/catch 로 도메인 에러 코드를 반환하므로, 실패는 HTTP 400 + `errorCode` 조합입니다.
- 기본 프로필(dev)은 보안 off — 인증 없이 호출 가능. 운영은 `app.fortune.security.enabled=true` ([02 §2.6](02-architecture.md)).

## 5.2 FortuneController — `/api/fortune`

파일: `controller/FortuneController.java`.

| 메서드 | 경로 | 요청 | 응답 data | 비고 |
|--------|------|------|-----------|------|
| POST | `/saju/calculate` | `SajuRequest` (body) | `SajuResult` | 사주팔자 계산 (`:71`) |
| POST | `/daily` | `SajuRequest` (body) + `targetDate` (query, ISO date) | `DailyFortuneResult` | 특정일 운세 (`:103`) |
| POST | `/daily/today` | `SajuRequest` | `DailyFortuneResult` | 오늘 운세 (`:139`) |
| POST | `/tojeong` | `TojeongRequest` | `TojeongResult` | 토정비결 (`:173`) |
| POST | `/zodiac` | `ZodiacRequest` | `ZodiacFortuneResult` | 별자리 운세 (`:206`) |
| GET | `/calendar/ganji/{year}/{month}` | path: year(1900–2100), month(1–12) | `GanjiCalendarResponse` | 범위 밖이면 `INVALID_YEAR`/`INVALID_MONTH` (`:231-245`) |
| GET | `/health` | — | `String` | 운세 시스템 상태 문구 (`:371`) |
| POST | `/saju/calculate-and-send` | `SajuRequest` (+`notification`) | `String` | 계산 후 발송 (`:383`) |
| POST | `/daily/today-and-send` | `SajuRequest` (+`notification`) | `String` | 계산 후 발송 (`:415`) |
| POST | `/tojeong/calculate-and-send` | `TojeongRequest` (+`notification`) | `String` | 계산 후 발송 (`:449`) |
| POST | `/zodiac/calculate-and-send` | `ZodiacRequest` (+`notification`) | `String` | 계산 후 발송 (`:480`) |
| POST | `/telegram/test` | `TelegramTestRequest` | `String` | 텔레그램 발송 테스트 (`:508`) |
| POST | `/ai/interpret-saju` | `SajuRequest` | `String` | AI 사주 해석 (`:271`) |
| POST | `/ai/daily-advice` | `SajuRequest` + `targetDate` (query) | `String` | AI 일일 조언 (`:304`) |
| POST | `/ai/ask` | `SajuRequest` + `question` (query) | `String` | AI 질문 답변 (`:340`) |

AI 3종 엔드포인트는 `AIFortuneService` 가 미주입(비활성)이면 `errorCode="AI_SERVICE_DISABLED"` HTTP 400 을 반환합니다 (`FortuneController.java:278-281`). AI 세부는 [06 문서](06-ai-and-fallback.md).

### 요청 DTO 요약

| DTO | 필수 필드 (검증) | 파일 |
|-----|------------------|------|
| `SajuRequest` | `birthYear`(1900–2100), `birthMonth`(1–12), `birthDay`(1–31), `birthHour`(0–23), `birthMinute`(0–59), `gender`(`M`/`F`), `calendarType`(`SOLAR`/`LUNAR`) + 선택 `notification` | `SajuRequest.java:23-58` |
| `TojeongRequest` | `birthYear`(1900–2030), `birthMonth`, `birthDay`, `targetYear`(2020–2040) + 선택 `notification` | `TojeongRequest.java:21-43` |
| `ZodiacRequest` | `birthDate`(LocalDate), `targetDate`(LocalDate) + 선택 `notification` | `ZodiacRequest.java:22-30` |
| `NotificationRequest` | `recipientName`(필수), `email`(형식), `telegramChatId`(숫자), `notificationType`(`email`/`telegram`/`both`) | `NotificationRequest.java:24-35` |
| `TelegramTestRequest` | `chatId`(long, nullable), `message` | `dto/TelegramTestRequest.java` |

### 발송(`*-and-send`) 동작

`notification` 이 있을 때만 발송하며, `notificationType` 에 따라 이메일/텔레그램/둘 다로 분기합니다 ([02 §2.3](02-architecture.md)). 성공 시 `data` 는 "…성공적으로 발송되었습니다" 문자열입니다.

### 요청/응답 예시 — `POST /api/fortune/saju/calculate`

```json
// 요청
{
  "birthYear": 1981, "birthMonth": 3, "birthDay": 20,
  "birthHour": 1, "birthMinute": 59,
  "gender": "M", "calendarType": "SOLAR"
}
```

```json
// 응답 (요약)
{
  "success": true,
  "data": {
    "yearPillar": "신유", "monthPillar": "신묘",
    "dayPillar": "정유", "timePillar": "신축",
    "dayMaster": "정",
    "wuxingAnalysis": { "woodCount": 1, "fireCount": 1, "...": "..." },
    "daeunForward": false, "daeunNumber": 5,
    "dayDetail": { "stem": "정", "branch": "유", "twelveStage": "...", "...": "..." }
  },
  "message": "성공",
  "timestamp": "2026-..."
}
```

`data` 구조 상세는 [04 §4.3.1 SajuResult](04-data-model.md).

## 5.3 CalendarViewController — `/api/calendar`

파일: `controller/CalendarViewController.java`. JSON이 아닌 **HTML 페이지**를 반환합니다(`Content-Type: text/html`).

| 메서드 | 경로 | 요청 | 응답 | 비고 |
|--------|------|------|------|------|
| GET | `/view/{year}/{month}` | path: year, month | HTML | 간지달력 렌더링 (`:37`) |
| GET | `/view/current` | — | HTML | 현재 월로 위임 (`:64-68`) |

내부적으로 `GanjiCalendarService.generateMonthlyCalendar(year, month)` 로 데이터를 만든 뒤 서버에서 HTML 문자열을 조립합니다. 오류 시 에러 HTML 반환 (`CalendarViewController.java:44-56`).

## 5.4 SystemController — `/api/system`

파일: `controller/SystemController.java`.

| 메서드 | 경로 | 요청 | 응답 data | 비고 |
|--------|------|------|-----------|------|
| GET | `/status` | — | `SystemStatus` | 시스템명·버전·상태·uptime·기능 맵 (`:26-38`) |

`SystemStatus` 는 `systemName`, `version`, `status`("RUNNING"), `currentTime`, `uptime`(JVM 기동시간 계산), `features`(사주팔자계산/일일운세/토정비결/… → Boolean) 를 담습니다 (`SystemController.java:29-73`).

## 5.5 ApiDocumentationController — `/api/docs`

파일: `controller/ApiDocumentationController.java`. SpringDoc/Swagger 대신 자체 문서/테스트 페이지를 제공합니다.

| 메서드 | 경로 | 요청 | 응답 | 비고 |
|--------|------|------|------|------|
| GET | `/api/docs` | — | `Map` (JSON) | 엔드포인트/에러코드/응답형식 목록 (`:32`) |
| GET | `/api/docs/{category}/{endpoint}` | path | `Map` (JSON) | 상세 스텁 (전체는 `/api/docs` 참조) (`:211`) |
| GET | `/api/docs/test` | — | HTML | 인터랙티브 API 테스트 페이지 (`:232`) |

`/api/docs` 응답에는 각 API의 method/url/requestBody 예시와 에러 코드 사전(`SAJU_CALC_ERROR`, `AI_SERVICE_DISABLED` 등)이 포함됩니다 (`ApiDocumentationController.java:189-199`).

## 5.6 에러 코드 요약

컨트롤러가 반환하는 도메인 에러 코드(HTTP 400):

| 코드 | 발생 지점 |
|------|-----------|
| `SAJU_CALC_ERROR` / `SAJU_SEND_ERROR` | 사주 계산/발송 실패 |
| `DAILY_FORTUNE_ERROR` / `DAILY_SEND_ERROR` | 일일 운세 |
| `TOJEONG_CALC_ERROR` / `TOJEONG_SEND_ERROR` | 토정비결 |
| `ZODIAC_FORTUNE_ERROR` / `ZODIAC_SEND_ERROR` | 별자리 |
| `GANJI_CALENDAR_ERROR` / `INVALID_YEAR` / `INVALID_MONTH` | 간지달력 |
| `AI_SERVICE_DISABLED` / `AI_INTERPRETATION_ERROR` / `AI_ADVICE_ERROR` / `AI_QUESTION_ERROR` | AI |
| `TELEGRAM_TEST_ERROR` | 텔레그램 테스트 |

`GlobalExceptionHandler` 가 잡는 프레임워크 레벨 코드: `입력 검증 오류`, `INVALID_REQUEST_BODY`, `METHOD_NOT_ALLOWED`, `리소스 없음`, `잘못된 인수`, `시스템 오류` (`GlobalExceptionHandler.java:26-99`).
