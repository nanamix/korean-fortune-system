# 07. 보안 및 관측성

> JWT 인증, 보안 활성화 스위치, 보안 감사 로그(SecurityAuditLog), Actuator 엔드포인트, Prometheus 메트릭, OpenTelemetry 추적(traceId/spanId), 로그 패턴을 정리합니다.
> 관련 문서: [12. CI/CD 및 운영](./12-cicd-and-operations.md) · [11. Docker 배포](./11-deployment-docker.md) · [README 인덱스](./README.md)

---

## 7.1 보안 활성화 스위치

보안은 `app.fortune.security.enabled` 프로퍼티로 제어됩니다. `SecurityConfig`(`config/SecurityConfig.java`)는 이 값에 따라 두 개의 `SecurityFilterChain` 중 하나를 활성화합니다.

| 값 | 적용 체인 | 동작 |
|----|-----------|------|
| `true` | `securedFilterChain` | 운세 API·헬스체크·H2 콘솔 등은 permitAll, `/actuator/**` 는 `ROLE_ADMIN`, 그 외 인증 필요 |
| `false` (기본, `matchIfMissing=true`) | `openFilterChain` | 모든 요청 permitAll (개발 편의) |

프로필별 기본값:

| 프로필 | `app.fortune.security.enabled` |
|--------|-------------------------------|
| 기본(`application.yml`) / `dev` / `test` | `false` |
| `prod` | `true` |

공통 설정(두 체인 모두):

- CSRF 비활성화 (`csrf.disable`)
- CORS 활성화 — `localhost`/`127.0.0.1` 임의 포트(http/https) origin 패턴 허용, 메서드 GET/POST/PUT/DELETE/PATCH/OPTIONS, 자격증명 허용, max-age 3600초
- 비밀번호 인코더: `BCryptPasswordEncoder`

보안 활성 체인(`securedFilterChain`)은 추가로 보안 헤더를 설정합니다: frame options `sameOrigin`, content-type options, HSTS. 비활성 체인은 H2 콘솔을 위해 frame options를 disable 합니다.

---

## 7.2 JWT 인증

JWT 관련 구성요소는 `security/` 패키지에 있습니다.

| 클래스 | 역할 |
|--------|------|
| `JwtTokenUtil` | 토큰 생성·검증·클레임 추출 |
| `JwtAuthenticationFilter` | 요청별 토큰 검증 필터 |
| `JwtAuthenticationEntryPoint` | 인증 실패(401) 처리 |
| `JwtAccessDeniedHandler` | 인가 실패(403) 처리 |
| `UserPrincipal` | 인증 주체(`UserDetails` 구현) |

라이브러리: `io.jsonwebtoken:jjwt` 0.11.5 (api/impl/jackson).

### 토큰 파라미터 (`JwtTokenUtil`)

| 프로퍼티 | 기본값 | 의미 |
|----------|--------|------|
| `jwt.secret` | `mySecretKey123456789012345678901234567890` | HMAC 서명 키 (운영에서는 반드시 환경변수로 교체) |
| `jwt.expiration` | `86400` (24시간, 초) | 액세스 토큰 만료 |
| `jwt.refresh-expiration` | `604800` (7일, 초) | 리프레시 토큰 만료 |

- 서명 알고리즘: `HS512`
- 액세스 토큰에는 `authorities`(권한 목록) 클레임 포함, 리프레시 토큰에는 `type=refresh` 클레임 포함.
- `isTokenNearExpiry` 로 만료 30분 이내 여부를 판단해 갱신 유도.

> 보안 주의: 기본 `jwt.secret` 은 개발용 하드코딩 값입니다. 운영에서는 `JWT_SECRET` 등 환경변수 또는 시크릿 저장소로 반드시 재정의하세요.

---

## 7.3 보안 감사 로그 (SecurityAuditLog)

보안 이벤트는 `SecurityAuditService`(`service/SecurityAuditService.java`)가 `SecurityAuditLog` 엔티티로 기록합니다.

### 엔티티 (`entity/SecurityAuditLog.java`, 테이블 `security_audit_log`)

| 필드 | 컬럼 | 설명 |
|------|------|------|
| `id` | `id` | PK (IDENTITY) |
| `user` | `user_id` | 사용자(FK, LAZY) |
| `action` | `action` | 이벤트 유형 (예: `LOGIN_SUCCESS`) |
| `resource` | `resource` | 접근 리소스/URI |
| `ipAddress` | `ip_address` | 클라이언트 IP (X-Forwarded-For / X-Real-IP 우선) |
| `userAgent` | `user_agent` | User-Agent |
| `success` | `success` | 성공 여부 |
| `timestamp` | `timestamp` | 발생 시각 (미지정 시 `now()`) |
| `details` | `details` | JSON 상세 |

### 기록 이벤트 및 탐지 로직

- 로그인 성공/실패/로그아웃: `recordLoginSuccess` / `recordLoginFailure` / `recordLogout` (모두 `@Async`)
- 접근 시도 기록: `recordAccessAttempt`
- 보안 위반 기록: `recordSecurityViolation` — `BRUTE_FORCE_ATTACK`, `SUSPICIOUS_ACTIVITY` 는 즉시 알림(`sendSecurityAlert`, 현재 로그 출력)
- 이상 탐지 임계치:
  - 동일 사용자 1시간 내 로그인 실패 **5회 이상** → `BRUTE_FORCE_ATTACK`
  - 동일 IP 1시간 내 로그인 실패 **10회 이상** → `BRUTE_FORCE_ATTACK`
  - 동일 사용자 1시간 내 **서로 다른 IP 5개 이상** → `SUSPICIOUS_ACTIVITY`
- 통계·정리: `getSecurityStatistics(days)`(총 이벤트·로그인 성공/실패·성공률·상위 실패 IP), `cleanupOldAuditLogs(daysToKeep)`(오래된 로그 삭제)

---

## 7.4 Actuator 엔드포인트

노출 엔드포인트(`management.endpoints.web.exposure.include`):

| 프로필 | 노출 엔드포인트 |
|--------|-----------------|
| 기본 / `prod` | `health, info, metrics, prometheus, caches` |
| `dev` | 위 + `trace` |
| Docker dev 오버라이드 | `*` (전체) |

기타 설정:

- health 상세: 기본/`dev` = `always`, `prod` = `when-authorized`
- `info.env.enabled=true` — `info` 에 환경 정보 노출
- 보안 활성 시 `/actuator/**` 는 `ROLE_ADMIN` 권한 필요 (`/actuator/health` 는 예외적으로 permitAll)

주요 URL:

```
GET /actuator/health       # 헬스
GET /actuator/info         # 빌드/환경 정보
GET /actuator/metrics      # 메트릭 인덱스
GET /actuator/prometheus   # Prometheus 포맷 메트릭
GET /actuator/caches       # Caffeine 캐시 목록
```

---

## 7.5 Prometheus 메트릭

- 의존성: `io.micrometer:micrometer-registry-prometheus` (client 버전은 `build.gradle` 에서 `1.3.6` 으로 고정)
- 노출 경로: `/actuator/prometheus`
- `prod` 프로필: `management.metrics.export.prometheus.enabled=true`
- Compose Prometheus(`docker/prometheus/prometheus.yml`)가 job `korean-fortune-app` 으로 `app:8080/actuator/prometheus` 를 5초 간격 스크레이프 (전역 15초)
- 캐시: Caffeine 기반 캐시 정의(`config/CacheConfig.java`) — `users`, `daily-fortune`, `year-pillar`, `day-pillar`, `blacklist`, `fortune-data`, `zodiac-fortune`, `ai-*` 등. `caches` 엔드포인트로 조회.

---

## 7.6 OpenTelemetry 추적 (traceId / spanId)

- 의존성: `io.micrometer:micrometer-tracing-bridge-otel` (OTLP exporter는 `OTEL_EXPORTER_OTLP_ENDPOINT` 설정 시에만 활성)
- 샘플링(`management.tracing.sampling.probability`):

| 프로필 | 샘플링 확률 |
|--------|-------------|
| 기본 / `dev` | `1.0` (전체 추적) |
| `prod` | `${OTEL_SAMPLING_PROBABILITY:0.1}` (10%) |

- OTLP 트레이스 엔드포인트(`management.otlp.tracing.endpoint`):
  - 기본/`dev`: `${OTEL_EXPORTER_OTLP_ENDPOINT:}` — 미설정 시 콘솔 로그로 대체
  - `prod`: `${OTEL_EXPORTER_OTLP_ENDPOINT:http://otel-collector:4318/v1/traces}` (Jaeger / Grafana Tempo 등)
- `.env.example` 에 `OTEL_EXPORTER_OTLP_ENDPOINT`, `OTEL_SAMPLING_PROBABILITY` 항목 제공(주석 상태).

---

## 7.7 로그 패턴

로그 라인에는 MDC 의 `traceId` / `spanId` 가 포함되어 추적과 연계됩니다(`application.yml`).

```
%d{yyyy-MM-dd HH:mm:ss} [%thread] %-5level %logger{36} [trace=%X{traceId:-} span=%X{spanId:-}] - %msg%n
```

로그 파일 경로 및 레벨:

| 프로필 | 파일 경로 | 주요 레벨 |
|--------|-----------|-----------|
| 기본 | `logs/korean-fortune.log` | `com.fortune=INFO`, `org.hibernate.SQL=DEBUG` |
| `dev` | `logs/korean-fortune.log` | `com.fortune=DEBUG`, `io.micrometer.tracing=DEBUG`, `io.opentelemetry=DEBUG` |
| `prod` | `/app/logs/korean-fortune.log` (max 100MB, 30일) | `com.fortune=INFO`, `root=WARN` |
| `test` | (콘솔) | `com.fortune=WARN`, `com.fortune.ai=ERROR` |

로그의 `trace=` 값으로 분산 추적 시스템(Tempo/Jaeger)의 트레이스와 상호 참조할 수 있습니다.
