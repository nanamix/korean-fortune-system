# 10. 개발 및 테스트

> 개발 환경 세팅, 코드 구조 진입점, 주요 Gradle 태스크, 테스트 실행/작성 규약, 프로필별 실행을 정리합니다.
> 관련 문서: [08. 설치 가이드](./08-installation-guide.md) · [12. CI/CD 및 운영](./12-cicd-and-operations.md) · [README 인덱스](./README.md)

---

## 10.1 개발 환경 세팅

1. Java 21 설치 및 소스 클론 → [08. 설치 가이드](./08-installation-guide.md) 참고
2. 개발 실행:

```bash
./gradlew runDev          # dev 프로필, AI 비활성화, H2 인메모리 DB
```

3. IDE에서 열 때는 Gradle 프로젝트로 import 하고, 프로젝트 JDK를 21로 설정합니다.
4. 개발 프로필(`dev`)은 Spring DevTools(자동 재시작·LiveReload)와 H2 콘솔(`/h2-console`)이 활성화되어 있습니다.

기술 스택 요약:

| 분류 | 기술 |
|------|------|
| Framework | Spring Boot 4.0.6 |
| Language | Java 21 (Amazon Corretto) |
| Build | Gradle 9.4.1 Wrapper |
| DB | H2(dev/test), MySQL(docker/prod), PostgreSQL 드라이버 포함 |
| ORM | Spring Data JPA |
| Cache | Caffeine |
| 보안 | Spring Security, JWT (jjwt 0.11.5) |
| 음양력 변환 | Time4J (`net.time4j:time4j-base`) |
| 모니터링 | Actuator, Micrometer Prometheus, Micrometer Tracing (OTel 브리지) |
| 테스트 | JUnit 5, Spring Boot Test, Testcontainers(mysql) |

---

## 10.2 코드 구조 진입점

```
src/main/java/com/fortune/
├── KoreanFortuneApplication.java   # @SpringBootApplication 메인 클래스 (진입점)
├── controller/                     # REST API + HTML 뷰 컨트롤러
│   ├── FortuneController.java      # 운세 계산/발송 API (/api/fortune/**)
│   ├── CalendarViewController.java # 간지달력 HTML 뷰 (/api/calendar/view/**)
│   ├── ApiDocumentationController.java # 자체 API 문서 (/api/docs)
│   └── SystemController.java       # 시스템 상태 (/api/system/**)
├── service/                        # 비즈니스 로직 (사주/일일/토정/별자리/간지/음양력/AI/발송)
├── dto/                            # 요청·응답 객체 (SajuRequest, SajuResult 등)
├── entity/                         # JPA 엔티티 (User, SecurityAuditLog 등)
├── repository/                     # 데이터 접근 계층
├── config/                         # SecurityConfig, CacheConfig, AIConfig, AsyncConfig, WebConfig
└── security/                       # JWT 필터·유틸 (JwtTokenUtil, JwtAuthenticationFilter 등)

src/main/resources/
├── application.yml                 # 기본 설정 (기본 프로필 dev)
├── application-{dev,mysql,docker,prod,ai,test,perf}.yml
├── static/                         # fortune-app.html, index.html, manifest.json, sw.js
└── data.sql / indexes.sql          # 초기 데이터·인덱스
```

주요 API 흐름: 클라이언트 → `FortuneController` → 각 `*Service` (예: `GanjiCalculatorService` 로 사주 계산) → `dto` 결과 반환. AI는 `AIFortuneService` 가 Optional 주입되어 비활성 시 null 로 동작합니다.

---

## 10.3 주요 Gradle 태스크

| 태스크 | 그룹 | 설명 |
|--------|------|------|
| `bootRun` | application | 기본 실행 |
| `runDev` | application | `dev` 프로필 + AI 비활성 실행 |
| `runWithoutAI` | application | AI 없이 실행 |
| `runWithAI` | application | `dev,ai` 프로필 실행 (`OPENAI_API_KEY` 필수) |
| `bootJar` | build | 실행 JAR 생성 → `build/libs/korean-fortune-app.jar` |
| `startLocal` | application | 빌드된 JAR을 `dev` 프로필로 직접 실행 |
| `runLocalJar` | application | JAR 실행 안내 정보 출력 |
| `test` | verification | 단위 테스트 (`test` 프로필, AI 강제 비활성, 병렬) |
| `integrationTest` | verification | `@Tag("integration")` 테스트만 실행 (`test` 이후 실행) |
| `ciTest` | ci | `test` + `integrationTest` |
| `ciBuild` | ci | `clean` + `ciTest` + `bootJar` |
| `dockerBuildDev` / `dockerBuildProd` | docker | Docker 이미지 빌드 (development/runtime 타깃) |
| `dockerComposeUp` / `dockerComposeUpProd` | docker | Compose 시작 (개발/운영) |
| `dockerComposeDown` / `dockerComposeDownAll` | docker | Compose 중지 / 볼륨까지 삭제 |
| `deployDev` / `deployProd` / `fullDeploy` | deployment | 빌드 + Compose 배포 |
| `cleanDependencies` | build | Gradle 의존성 캐시 정리 |

전체 태스크 목록은 `./gradlew tasks` 또는 `build.gradle` 하단의 안내 배너를 참고하세요.

---

## 10.4 테스트 실행

### 전체 / 통합

```bash
./gradlew test              # 단위 테스트
./gradlew integrationTest   # @Tag("integration") 통합 테스트
./gradlew ciTest            # 둘 다
```

`test` 태스크 설정(`build.gradle`):

- `spring.profiles.active=test` 로 실행 (H2 인메모리 DB)
- `app.fortune.ai.enabled=false` — AI 강제 비활성
- 병렬 실행: `maxParallelForks = 프로세서 수 / 2`
- JVM 힙: `256m`~`1g`
- 로그 이벤트: passed / skipped / failed

### 단일 테스트 / 특정 메서드 실행

```bash
# 클래스 단위
./gradlew test --tests "com.fortune.service.GanjiCalculatorServiceTest"

# 메서드 단위
./gradlew test --tests "com.fortune.service.GanjiCalculatorServiceTest.특정_메서드명"

# 패턴 매칭
./gradlew test --tests "com.fortune.ai.*"
```

### Testcontainers 관련

`build.gradle` 에 `org.testcontainers:junit-jupiter` 와 `org.testcontainers:mysql` 의존성이 선언되어 있습니다. 이들 컨테이너를 사용하는 테스트를 실행하려면 로컬에 **Docker 데몬이 실행 중**이어야 합니다. Docker 없이 실행 가능한 테스트(H2 기반)만 돌리려면 위 `--tests` 필터로 대상을 좁히세요.

---

## 10.5 테스트 작성 규약

- 프레임워크: JUnit 5(`useJUnitPlatform`), Spring Boot Test, Spring Security Test.
- 통합 테스트는 클래스에 `@Tag("integration")` 을 부여합니다 → `integrationTest` 태스크에서만 실행됩니다. (예: `src/test/java/com/fortune/FortuneIntegrationTest.java`)
- 통합 테스트 예시에는 세부 태그도 사용됩니다: `@Tag("performance")`, `@Tag("security")`, `@Tag("validation")`, `@Tag("concurrency")`, `@Tag("accuracy")`, `@Tag("error-handling")`, `@Tag("scenario")`.
- 웹 계층 테스트는 `MockMvc` + Jackson `ObjectMapper` 로 JSON 요청/응답을 검증합니다.
- 테스트는 `test` 프로필에서 AI가 강제 비활성화되므로, AI 경로는 fallback 동작을 전제로 작성합니다.
- 테스트 소스 위치: `src/test/java/com/fortune/` (하위 `controller/`, `service/`, `ai/`, `config/`).

---

## 10.6 프로필별 실행

| 실행 목적 | 명령 |
|-----------|------|
| 개발 (H2, AI off) | `./gradlew runDev` |
| 로컬 MySQL 연동 | `java -jar build/libs/korean-fortune-app.jar --spring.profiles.active=mysql` |
| AI 활성화 | `OPENAI_API_KEY=sk-... ./gradlew runWithAI` |
| 성능 프로필 | `java -jar ...korean-fortune-app.jar --spring.profiles.active=perf` |
| 운영 시뮬레이션 | `java -jar ...korean-fortune-app.jar --spring.profiles.active=prod` |

프로필별 데이터베이스·보안·AI 설정 차이는 [08. 설치 가이드 §8.4.1](./08-installation-guide.md) 표를 참고하세요.
