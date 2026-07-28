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
| POST | `/saju/calculate-and-send` | `SajuRequest` (+`notification`) | `SajuResult` | 계산 후 발송하고 화면용 결과 반환 (`:403`) |
| POST | `/daily/today-and-send` | `SajuRequest` (+`notification`) | `DailyFortuneResult` | 계산 후 발송하고 화면용 결과 반환 (`:435`) |
| POST | `/tojeong/calculate-and-send` | `TojeongRequest` (+`notification`) | `TojeongResult` | 계산 후 발송하고 화면용 결과 반환 (`:469`) |
| POST | `/zodiac/calculate-and-send` | `ZodiacRequest` (+`notification`) | `ZodiacFortuneResult` | 계산 후 발송하고 화면용 결과 반환 (`:500`) |
| POST | `/telegram/test` | `TelegramTestRequest` | `String` | 텔레그램 발송 테스트 (`:508`) |
| POST | `/ai/interpret-saju` | `SajuRequest` | `String` | AI 사주 해석 (`:271`) |
| POST | `/ai/daily-advice` | `SajuRequest` + `targetDate` (query) | `String` | AI 일일 조언 (`:304`) |
| POST | `/ai/ask` | `SajuRequest` + `question` (query) | `String` | AI 질문 답변 (`:340`) |

AI 3종 엔드포인트는 `AIFortuneService` 가 미주입(비활성)이면 `errorCode="AI_SERVICE_DISABLED"` HTTP 400 을 반환합니다 (`FortuneController.java:278-281`). AI 세부는 [06 문서](06-ai-and-fallback.md).

### 요청 DTO 요약

| DTO | 필수 필드 (검증) | 파일 |
|-----|------------------|------|
| `SajuRequest` | `birthYear`(1900–2100), `birthMonth`(1–12), `birthDay`(1–31), `birthHour`(0–23), `birthMinute`(0–59), `gender`(`M`/`F`), `calendarType`(`SOLAR`/`LUNAR`), 선택 `birthSecond`·`leapMonth`·`birthLongitude`(124–132)·`applyEquationOfTime`·`applyHistoricalDst`·`notification` | `SajuRequest.java` |
| `TojeongRequest` | `birthYear`(1900–2030), `birthMonth`, `birthDay`, `targetYear`(2020–2040) + 선택 `notification` | `TojeongRequest.java:21-43` |
| `ZodiacRequest` | `birthDate`, `targetDate` + 선택 `birthTime`, `birthLatitude`(-90~90), `birthLongitude`(-180~180), `timeZone`(IANA), `calendarType`(`SOLAR`/`LUNAR`), `leapMonth`, `notification` | `ZodiacRequest.java` |
| `NotificationRequest` | `recipientName`(필수), `email`(형식), `telegramChatId`(숫자), 선택 `discordWebhookUrl`, `notificationType`(`email`/`telegram`/`discord`/`both`/`all`) | `NotificationRequest.java` |
| `TelegramTestRequest` | `chatId`(long, nullable), `message` | `dto/TelegramTestRequest.java` |

### 발송(`*-and-send`) 동작

`notification` 이 있을 때만 발송하며, `notificationType` 에 따라 이메일·텔레그램·Discord 또는 조합으로 분기합니다 ([02 §2.3](02-architecture.md)). 성공 시 `data` 는 일반 계산 API와 같은 운세 결과 객체이므로, 클라이언트는 발송을 사용해도 화면 결과를 동일하게 렌더링할 수 있습니다.

### 별자리 개인화 계약

- `birthTime`, `birthLatitude`, `birthLongitude`가 모두 있으면 `astrologyProfile.precision=BIRTH_TIME_LOCATION`이며 상승궁까지 계산합니다.
- 셋 중 하나라도 없으면 `precision=DATE_ONLY`이며 태양궁·달궁만 계산합니다. 시간대가 없으면 `Asia/Seoul`을 사용합니다.
- `calendarType=LUNAR`이면 `leapMonth`를 포함해 먼저 양력으로 변환합니다.
- 결과의 `astrologyProfile`에는 Sun·Moon·Rising의 별자리와 별자리 내 각도, 원소·양상·주인 행성·데칸·출생 달 위상이 포함됩니다.
- `majorTransits`에는 대상일 이동 태양·달과 출생 태양·달·상승궁 사이의 주요 각, orb, 점수 가감값, 해석이 포함됩니다.
- `todayFortune.scoreBasis`와 `monthlyFortune.scoreBasis`는 중립 기준점과 출생 차트·transit·날짜 리듬의 실제 가감값을 제공합니다.

계산 모델과 한계는 [14. 서양 점성술 계산](14-western-astrology.md)을 참고하세요.

### 출생 위치 검색 — `/api/location`

| 메서드 | 경로 | 요청 | 응답 data | 비고 |
|--------|------|------|-----------|------|
| GET | `/search` | `q`(2~80자 도시·지역·우편번호) | `LocationSearchResult[]` | `name`, `countryCode`, WGS84 `latitude`·`longitude`, IANA `timeZone` |

서버가 고정된 Open-Meteo Geocoding API를 호출하므로 사용자 입력으로 외부 호스트를
바꿀 수 없습니다. 검색어 원문은 애플리케이션 로그와 DB에 저장하지 않습니다.
외부 제공자 오류는 HTTP 502와 `LOCATION_SEARCH_UNAVAILABLE`을 반환하며, UI는
기존 좌표·시간대 직접 입력을 계속 제공해야 합니다.

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
