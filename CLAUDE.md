# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Overview

한국형 만세력 운세 시스템 — Spring Boot 4.0.x REST API 서비스로 사주팔자·오늘의 운세·토정비결(144괘)·별자리 운세·간지달력을 계산하고, 결과를 이메일/텔레그램으로 발송하며 선택적으로 AI 해석을 붙인다. 프론트엔드는 `src/main/resources/static/fortune-app.html` 단일 페이지.

## Build & Run

Gradle wrapper 사용. 툴체인은 **Java 21** (`build.gradle`의 `JavaLanguageVersion.of(21)`, Dockerfile `amazoncorretto:21`, CI `java-version: '21'` 모두 일치). `List.getFirst` 등 Java 21+ API는 커밋 `11f3e70`에서 제거된 상태이므로 재도입 시 21 기준 확인.

```bash
./gradlew bootJar        # build/libs/korean-fortune-app.jar 생성
./gradlew runDev         # 개발 실행 (dev 프로필, AI 비활성)
./gradlew runWithAI      # AI 활성 (dev,ai 프로필 — OPENAI_API_KEY 필요)
java -jar build/libs/korean-fortune-app.jar --spring.profiles.active=dev
```

접속: `http://localhost:18080/fortune-app.html` (메인 GUI), `/api/docs` (자체 API 문서), `/actuator/health`.

## Test

```bash
./gradlew test                          # 단위 테스트 (test 프로필, AI 강제 비활성)
./gradlew integrationTest               # @Tag("integration") 만
./gradlew ciTest                        # test + integrationTest
./gradlew test --tests '*DailyFortuneServiceTest'   # 단일 테스트 클래스
```

`test` 태스크는 `spring.profiles.active=test`, `app.fortune.ai.enabled=false`를 system property로 강제 주입하고 병렬 실행한다. Testcontainers(mysql)가 통합 테스트에 사용되므로 Docker 데몬 필요.

## Architecture

패키지 루트 `com.fortune` (`src/main/java/com/fortune/`):

- **`controller/`** — `FortuneController`가 대부분의 운세 엔드포인트를 담당하는 파사드. `*/calculate-and-send` 계열은 계산 서비스 호출 후 `EmailService`/`TelegramService`로 발송까지 한 번에 처리한다. 계산-only와 계산+발송 엔드포인트가 쌍으로 존재. `CalendarViewController`(HTML 뷰), `ApiDocumentationController`(자체 `/api/docs`), `SystemController`.
- **`service/`** — 도메인 계산 로직. `GanjiCalculatorService`(사주 4주 + 십신·지장간·12운성·대운), `LunarSolarConverter`(Time4J `KoreanCalendar` 음양력, 정적 유틸), `LunarCalendarService`, `DailyFortuneService`, `TojeongBigyeolService`, `ZodiacFortuneService`, `SinsalService`, `GanjiCalendarService`. 발송은 `EmailService`(Thymeleaf 템플릿), `TelegramService`.
  - **사주 계산 엔진**: 4주·십신·지장간·12운성·대운·절기는 검증된 라이브러리 **`lunar-java`(cn.6tail:lunar)**에 위임하고, 결과를 한글 간지로 매핑한다(손으로 짠 천문/만세력 산술 없음). 한국 음력 입력만 `LunarSolarConverter`(Time4J)로 양력 변환 후 투입. 검증 기준은 **1981-03-20 → 신유/신묘/정유/신축**(`GanjiCalculatorServiceTest`). 알고리즘·매핑은 `docs/reference/03-saju-calculation-methodology.md` 참조.
- **`ai/`** — **Ports & Adapters로 격리된 AI 계층**. Spring AI 의존성을 쓰지 않는다. `AiFortuneFacade`가 진입점이며 `AiProviderPort`(`complete()`) 구현체를 `Optional`로 주입받는다. provider가 없거나 `app.fortune.ai.enabled=false`이면 항상 `FallbackFortuneInterpreter`(규칙 기반)로 폴백하고, provider 호출이 예외/빈 응답이어도 폴백. 실제 구현체는 `OpenAiCompatibleFortuneProvider`. 프롬프트는 `AiPromptFactory`가 생성. **AI를 건드릴 때는 이 파사드/폴백 계약을 깨지 말 것** — AI 없이도 전 기능이 동작해야 한다.
- **`entity/` + `repository/`** — JPA. `User`, `SajuData`, `SecurityAuditLog`, `TojeongGwaEntity`.
- **`security/` + `config/SecurityConfig`** — JWT 인증(jjwt). `app.fortune.security.enabled` 플래그로 개발 시 비활성 가능(기본 dev에서 false).
- **`config/`** — `CacheConfig`(Caffeine, per-cache 설정 — Redis 미사용), `AsyncConfig`, `AIConfig`, `WebConfig`.
- **`dto/`, `enums/`**(`Wuxing`/오행, `Zodiac`, `Gender`), **`exception/`**(`GlobalExceptionHandler`), **`validation/`**(`@ValidBirthDate`).

관측성: Micrometer + OpenTelemetry 브리지. 로그 패턴에 `traceId`/`spanId` MDC 포함. Prometheus는 `/actuator/prometheus`, OTLP export는 `OTEL_EXPORTER_OTLP_ENDPOINT` 설정 시에만 활성.

## Profiles & Config

`application.yml`이 베이스(기본 `dev`, H2 인메모리). 프로필별 override: `application-{dev,ai,mysql,docker,prod,perf,test}.yml`. 주요 토글은 `app.fortune.*` 트리 아래 — `ai.enabled`, `ai.provider`, `security.enabled`, `email.enabled`, `telegram.*`, `cache.*`.

시크릿은 `.env`, shell export, application 설정 파일에 저장하지 않는다. Compose는 `docker/docker-compose.openbao.override.yml`, 운영 절차는 `ops/openbao/README.md`를 따른다.

## Docker

Compose 파일은 `docker/` 아래에 있고 base + override 조합으로 실행:

```bash
docker compose -f docker/docker-compose.yaml -f docker/docker-compose.prod.yaml up -d   # MySQL+Redis+Nginx+모니터링
docker compose -f docker/docker-compose.standalone.yml up -d                              # 단독 실행
```

멀티스테이지 `Dockerfile` (`development` / `runtime` 타깃). `./gradlew dockerComposeUp*` 래퍼 태스크도 있음.

## Notes

- `build.gradle`에는 과거 마이그레이션 흔적인 no-op 태스크가 다수 있다(`removeSpringDoc`, `fixDuplicateBeans` 등 — println만 함). 실제 빌드 로직 아님, 무시할 것.
- SpringDoc/Swagger는 비활성(`springdoc.*.enabled: false`) — API 문서는 자체 `/api/docs` 엔드포인트(`ApiDocumentationController`) 사용.
- DB 스키마/시드는 `database/{schema,data,init}.sql`, `src/main/resources/{data,indexes}.sql`.
