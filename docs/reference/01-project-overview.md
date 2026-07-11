# 01. 프로젝트 개요

> 한국형 만세력 운세 시스템의 목적, 기능, 기술 스택, 디렉토리 구조를 정리한 진입 문서입니다.
> 더 깊은 내용은 [아키텍처](02-architecture.md) · [계산 방법론](03-saju-calculation-methodology.md) · [데이터 모델](04-data-model.md) · [API 레퍼런스](05-api-reference.md) · [AI와 폴백](06-ai-and-fallback.md) 문서를 참조하세요.

---

## 1.1 목적

전통 사주팔자(만세력)와 토정비결·별자리 운세를 정통 명리 규칙으로 계산하고, 결과를 이메일/텔레그램으로 발송하거나 선택적으로 LLM 해석을 덧붙이는 REST 백엔드 + 단일 페이지 웹앱입니다. `README.md` 기준 서비스명은 "한국형 만세력 운세 시스템"이며 패키지 루트는 `com.fortune` 입니다.

## 1.2 기능 요약

| 기능 | 설명 | 담당 서비스 |
|------|------|-------------|
| 사주팔자 계산 | 4주(연·월·일·시) 간지 + 십신·지장간·12운성·대운 파생 | `GanjiCalculatorService` |
| 오늘의 운세 / 일일 운세 | 사주 기반 일일 점수·카테고리(연애/직업/건강/재물)·조언 | `DailyFortuneService` |
| 토정비결 | 144괘(상8×중6×하3) 기반 연간 운세 | `TojeongBigyeolService` |
| 별자리 운세 | 서양 별자리 일일/월간 운세 | `ZodiacFortuneService` |
| 간지달력 | 월별 간지 달력 + 길일/절기 (JSON API + HTML 뷰) | `GanjiCalendarService` |
| 이메일 / 텔레그램 발송 | 계산 결과 알림 발송 | `EmailService`, `TelegramService` |
| AI 해석 (선택) | LLM 기반 사주/일일/토정/별자리 해석, 실패 시 로컬 폴백 | `AIFortuneService` → `com.fortune.ai` |
| 시스템 모니터링 | Actuator/Prometheus 상태 노출 | `SystemController`, `FortuneHealthIndicator` |

계산 엔진은 검증된 사주 라이브러리 **`lunar-java`(cn.6tail:lunar)**에 위임합니다. 4주·십신·지장간·12운성·대운·절기를 라이브러리가 산출하고, 한국 음력 입력만 `LunarSolarConverter`(Time4J `KoreanCalendar`)로 양력 변환 후 투입합니다(경도보정 -30분). 검증 기준값은 양력 1981-03-20 01:59(남) → 신유/신묘/정유/신축, 대운 역행·대운수 5 입니다. 상세는 [03. 계산 방법론](03-saju-calculation-methodology.md) 참조.

## 1.3 기술 스택

`build.gradle` 과 `README.md`("현재 기준") 기준입니다.

| 분류 | 기술 | 근거 |
|------|------|------|
| Framework | Spring Boot 4.0.6 | `build.gradle:3` |
| Language | Java 21 (Corretto 툴체인) | `build.gradle:11-14` |
| Build | Gradle 9.4.1 Wrapper | `build.gradle:513-516` |
| DB | H2 인메모리(개발/테스트), MySQL·PostgreSQL 드라이버(운영) | `build.gradle:49-51` |
| ORM | Spring Data JPA | `build.gradle:48` |
| 캐시 | Caffeine (per-cache 설정) | `build.gradle:67`, `CacheConfig.java` |
| 보안 | Spring Security + JWT(jjwt 0.11.5) | `build.gradle:43,59-61` |
| 음양력 | Time4J `time4j-base:5.9.4` | `build.gradle:64` |
| 관측성 | Micrometer + OpenTelemetry 브리지, Prometheus 레지스트리 | `build.gradle:110-111` |
| 메일/템플릿 | spring-boot-starter-mail, Thymeleaf | `build.gradle:85,97` |
| AI | Spring AI 미사용 — OpenAI-compatible 포트 + 로컬 fallback | `build.gradle:53`, `com.fortune.ai` |

> 주의: `README.md` 상단 "🛠️ 기술 스택" 표에는 Spring Boot 3.4.5 / Java 17 / Gradle 8.12 로 적힌 낡은 값이 남아 있으나, 같은 문서 하단 "현재 기준" 절과 `build.gradle` 이 실제 값(Boot 4.0.6 / Java 21 / Gradle 9.4.1)입니다.

## 1.4 실행 프로필

`application.yml` 기본은 `dev`(H2, 보안·AI 비활성)입니다. 나머지는 `application-<profile>.yml` 로 분리됩니다.

| 프로필 | 용도 | DB / 특징 |
|--------|------|-----------|
| `dev` | 기본 개발 | H2 인메모리, `ddl-auto=create-drop`, 보안·AI off |
| `ai` | AI 활성화 오버레이 | `app.fortune.ai.enabled=true`, `OPENAI_API_KEY` 필요 (`application-ai.yml`) |
| `mysql` | 로컬 MySQL | MySQL, `ddl-auto=update` (`application-mysql.yml:10,17`) |
| `prod` | 운영 | MySQL, `ddl-auto=validate`, 보안 on, OTLP export on (`application-prod.yml:22,49,76`) |
| `docker` / `perf` / `test` | 컨테이너 / 성능 / 테스트 | 각 `application-*.yml` |

핵심 토글: `app.fortune.ai.enabled`(기본 false), `app.fortune.security.enabled`(기본 false) — `application.yml:47-59`.

## 1.5 디렉토리 구조 (개요)

```
korean-fortune-system/
├── src/main/java/com/fortune/
│   ├── controller/   # REST + HTML 뷰 (Fortune/CalendarView/System/ApiDocumentation)
│   ├── service/      # 계산·발송·달력 등 비즈니스 로직 + 계산 엔진
│   ├── ai/           # Ports&Adapters AI 계층 (Facade/Port/Provider/Fallback)
│   ├── dto/          # 요청·응답 DTO (SajuResult 등)
│   ├── entity/       # JPA 엔티티 (User, SajuData, SecurityAuditLog, TojeongGwaEntity)
│   ├── repository/   # Spring Data JPA 리포지토리
│   ├── config/       # Cache/Security/Web/AI/Async 설정
│   ├── security/     # JWT 필터·유틸
│   ├── enums/        # Wuxing, Zodiac, Gender 등
│   ├── exception/    # GlobalExceptionHandler
│   ├── health/       # FortuneHealthIndicator
│   └── validation/   # BirthDate 검증
├── src/main/resources/
│   ├── static/fortune-app.html   # 단일 페이지 프론트엔드
│   ├── application*.yml           # 프로필별 설정
│   └── data.sql / indexes.sql     # 토정비결 64괘 레거시 시드(런타임 미사용) 등
├── database/         # schema.sql / init.sql / data.sql (MySQL 운영용)
└── docs/             # 문서 (본 reference/ 포함)
```

프론트엔드는 `src/main/resources/static/fortune-app.html` 단일 페이지로, 탭 UI에서 위 API들을 호출합니다.

## 1.6 빠른 시작 / 링크

```bash
./gradlew bootJar
java -jar build/libs/korean-fortune-app.jar --spring.profiles.active=dev
# AI 활성화: OPENAI_API_KEY 설정 후 ./gradlew runWithAI  (profiles=dev,ai)
```

| 항목 | 경로 |
|------|------|
| 웹앱(메인) | `http://localhost:8080/fortune-app.html` |
| API 문서 | `http://localhost:8080/api/docs` |
| API 테스트 페이지 | `http://localhost:8080/api/docs/test` |
| 간지달력 뷰 | `http://localhost:8080/api/calendar/view/current` |
| 헬스체크 | `http://localhost:8080/actuator/health` |
| Prometheus | `http://localhost:8080/actuator/prometheus` |

- 아키텍처 상세 → [02-architecture.md](02-architecture.md)
- 계산 방법론 → [03-saju-calculation-methodology.md](03-saju-calculation-methodology.md)
- 데이터 모델/스키마 → [04-data-model.md](04-data-model.md)
- API 전수 → [05-api-reference.md](05-api-reference.md)
- AI/폴백/프롬프트 보안 → [06-ai-and-fallback.md](06-ai-and-fallback.md)
