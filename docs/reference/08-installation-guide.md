# 08. 설치 가이드

> 한국형 만세력 운세 시스템을 로컬(macOS/Linux)에 설치하고 실행하는 단계별 안내입니다.
> 관련 문서: [10. 개발 및 테스트](./10-development-and-testing.md) · [11. Docker 배포](./11-deployment-docker.md) · [README 인덱스](./README.md)

---

## 8.1 사전 요구사항

| 항목 | 버전 / 요구 | 필수 여부 | 비고 |
|------|-------------|-----------|------|
| JDK | Java 21 (Amazon Corretto 권장) | 필수 | `build.gradle`의 toolchain이 Java 21로 고정 |
| Gradle | 프로젝트 내장 Wrapper 9.4.1 | 자동 | 별도 설치 불필요, `./gradlew` 사용 |
| Git | 최신 | 필수 | 소스 클론 |
| Docker / Docker Compose | 최신 | 선택 | 컨테이너 실행·통합 테스트 시에만 |

빌드 산출물은 `build/libs/korean-fortune-app.jar` 이며, 기본 접속 주소는 `http://localhost:8080/fortune-app.html` 입니다.

### JDK 21 설치

Java 21이 이미 설치되어 있다면 버전만 확인하고 넘어가면 됩니다.

```bash
java -version   # "21" 로 시작하면 정상
```

#### 방법 A — asdf (권장)

```bash
asdf plugin add java
asdf install java corretto-21.0.5.11.1   # 사용 가능한 최신 corretto-21.x 로 대체
asdf local java corretto-21.0.5.11.1     # 프로젝트 디렉토리에서 실행 → .tool-versions 생성
java -version
```

> 참고: 저장소에는 `.tool-versions` 파일이 포함되어 있지 않으므로, asdf 사용 시 위 `asdf local` 로 직접 생성합니다.

#### 방법 B — SDKMAN!

```bash
curl -s "https://get.sdkman.io" | bash
source "$HOME/.sdkman/bin/sdkman-init.sh"
sdk install java 21.0.5-amzn
sdk use java 21.0.5-amzn
```

#### 방법 C — Homebrew (macOS)

```bash
brew install --cask corretto@21
# 또는 OpenJDK
brew install openjdk@21
```

설치 후 `JAVA_HOME` 이 Java 21을 가리키는지 확인하세요.

```bash
echo $JAVA_HOME
```

---

## 8.2 소스 클론

```bash
git clone https://github.com/nanamix/korean-fortune-system.git
cd korean-fortune-system
```

Gradle Wrapper 실행 권한이 없다면 부여합니다.

```bash
chmod +x gradlew
```

---

## 8.3 로컬 빌드 및 실행

### 8.3.1 JAR 빌드 후 실행 (가장 간단)

```bash
# 실행 가능한 JAR 생성 (→ build/libs/korean-fortune-app.jar)
./gradlew bootJar

# dev 프로필(H2 인메모리 DB)로 실행
java -jar build/libs/korean-fortune-app.jar --spring.profiles.active=dev
```

### 8.3.2 Gradle 태스크로 직접 실행

```bash
# 개발 환경 실행 (AI 비활성화)
./gradlew runDev

# AI 기능 활성화 실행 (OPENAI_API_KEY 환경변수 필수)
./gradlew runWithAI

# 빌드된 JAR 자동 실행 (dev 프로필)
./gradlew startLocal
```

- `runDev` / `runWithoutAI` : `--spring.profiles.active=dev --app.fortune.ai.enabled=false` 로 실행합니다.
- `runWithAI` : `OPENAI_API_KEY` 가 없으면 빌드가 즉시 실패하며, `dev,ai` 프로필로 실행합니다.

실행 후 브라우저에서 `http://localhost:8080/fortune-app.html` 로 접속합니다.

---

## 8.4 프로필 및 환경변수 설정

### 8.4.1 Spring 프로필

| 프로필 | 데이터베이스 | 보안 | AI | 용도 |
|--------|--------------|------|----|------|
| `dev` (기본) | H2 인메모리 (`jdbc:h2:mem:devdb`) | 비활성 | 비활성 | 로컬 개발, 이메일/텔레그램 발송 활성 |
| `mysql` | 로컬 MySQL (`localhost:3306`) | - | - | 로컬 MySQL 연동 개발 |
| `postgres` | PostgreSQL (`localhost:5432`, `POSTGRES_URL`로 override) | - | - | PostgreSQL 연동 개발/운영 |
| `docker` | MySQL 컨테이너 (`mysql:3306`) | - | 환경변수 제어 | Docker Compose 실행 |
| `prod` | MySQL (`localhost:3306`) | 활성 | 환경변수 제어 | 운영 |
| `ai` | (병행 프로필) | - | 활성 | `dev,ai` 처럼 AI 강제 활성화 |
| `perf` | (HikariCP 확장) | - | - | 성능 테스트 |
| `test` | H2 인메모리 (`jdbc:h2:mem:testdb`) | 비활성 | 강제 비활성 | 자동화 테스트 |

프로필 지정은 `--spring.profiles.active=<프로필>` 인자 또는 `SPRING_PROFILES_ACTIVE` 환경변수를 사용합니다. 복수 지정은 쉼표로 결합합니다(예: `dev,ai`).

### 8.4.2 `.env` 파일 (Docker Compose용)

Docker Compose 실행 시 환경변수는 `.env` 파일로 주입합니다. 템플릿을 복사해 값을 채웁니다.

```bash
cp .env.example .env
```

`.env.example` 의 주요 항목:

| 변수 | 기본값 | 설명 |
|------|--------|------|
| `MYSQL_ROOT_PASSWORD` | `root_fortune_2025!` | MySQL root 비밀번호 |
| `MYSQL_PASSWORD` | `fortune_secure_2025!` | 애플리케이션 DB 사용자 비밀번호 |
| `REDIS_PASSWORD` | (빈 값) | Redis 비밀번호 |
| `SPRING_PROFILES_ACTIVE` | `docker` | 활성 프로필 |
| `AI_ENABLED` | `false` | AI 기능 스위치 |
| `OPENAI_API_KEY` | (빈 값) | OpenAI 호환 API 키 |
| `POSTGRES_URL` / `POSTGRES_USER` / `POSTGRES_PASSWORD` | `localhost:5432` 기본 | PostgreSQL 프로필(`postgres`) 연결 |
| `TELEGRAM_BOT_TOKEN` / `TELEGRAM_CHAT_ID` | (빈 값) | 텔레그램 발송 |
| `DISCORD_WEBHOOK_URL` | (빈 값) | Discord 발송 webhook |
| `MAIL_HOST` / `MAIL_PORT` / `MAIL_USERNAME` / `MAIL_PASSWORD` / `MAIL_FROM` | Gmail SMTP 기본 | 이메일 발송 ([13. 알림 가이드](13-notifications-guide.md) 참조) |
| `GRAFANA_PASSWORD` | `admin123` | 모니터링 Grafana 관리자 비밀번호 |
| `OTEL_EXPORTER_OTLP_ENDPOINT` | (주석) | OTLP 트레이스 수집기 |

### 8.4.3 로컬 개발에서 이메일/텔레그램 발송 설정

`dev` 프로필은 이메일·텔레그램 발송이 활성화되어 있습니다. 두 가지 방법 중 하나로 자격증명을 주입합니다.

방법 1 — 환경변수:

```bash
export EMAIL_USERNAME=your-email@gmail.com   # Gmail 앱 비밀번호 사용
export EMAIL_PASSWORD=your-app-password
export TELEGRAM_BOT_TOKEN=123456:AA...
export TELEGRAM_CHAT_ID=123456789
```

방법 2 — 시크릿 파일(권장): `src/main/resources/application-dev-secrets.yml` 파일을 생성합니다. 이 파일은 `.gitignore` 에 포함되어 커밋되지 않습니다. 템플릿은 `application-dev-secrets.yml.template` / `application-dev-secrets.yml.example` 을 참고하세요. `dev` 프로필은 `optional:classpath:application-dev-secrets.yml` 을 자동으로 import 합니다.

### 8.4.4 AI 기능 설정

AI는 기본 비활성화 상태이며, `ai` 프로필 + `OPENAI_API_KEY` 가 함께 있을 때만 외부 모델을 호출합니다. 그 외에는 결정적(deterministic) fallback 으로 응답합니다.

```bash
export OPENAI_API_KEY=sk-...
./gradlew runWithAI
```

OpenAI 호환 서버·모델은 다음 변수로 변경합니다.

```bash
export APP_FORTUNE_AI_PROVIDER=openai
export APP_FORTUNE_AI_MODEL=gpt-5.4-mini
export APP_FORTUNE_AI_BASE_URL=https://api.openai.com/v1
export APP_FORTUNE_AI_TIMEOUT=30s
```

---

## 8.5 접속 URL

애플리케이션 실행 후 다음 URL을 사용할 수 있습니다.

| 서비스 | URL | 설명 |
|--------|-----|------|
| 운세 앱 (메인 GUI) | `http://localhost:8080/fortune-app.html` | 사주팔자·운세·토정비결·별자리·간지달력 탭 UI |
| 홈페이지 | `http://localhost:8080` | 랜딩 페이지 |
| API 문서 | `http://localhost:8080/api/docs` | 자체 REST API 문서 (SpringDoc 아님) |
| API 테스트 | `http://localhost:8080/api/docs/test` | 인터랙티브 테스트 페이지 |
| 간지달력 (이번 달) | `http://localhost:8080/api/calendar/view/current` | 간지달력 HTML 뷰 |
| 헬스체크 | `http://localhost:8080/actuator/health` | 시스템 상태 |
| Actuator | `http://localhost:8080/actuator` | 관리 엔드포인트 |
| H2 콘솔 (dev) | `http://localhost:8080/h2-console` | 인메모리 DB 콘솔 |

---

## 8.6 트러블슈팅

### 포트 8080 충돌

`Web server failed to start. Port 8080 was already in use.` 오류 시:

```bash
# 8080 점유 프로세스 확인 (macOS/Linux)
lsof -i :8080

# 다른 포트로 실행
java -jar build/libs/korean-fortune-app.jar --spring.profiles.active=dev --server.port=8090
```

### JDK 버전 불일치

`Unsupported class file major version` 또는 toolchain 오류 시, 셸이 Java 21을 사용하는지 확인합니다.

```bash
java -version
echo $JAVA_HOME
./gradlew --version   # JVM 항목이 21인지 확인
```

Java 21이 아니면 8.1의 asdf/SDKMAN/Homebrew 절차로 전환하세요.

### Time4J 음력 데이터 / 의존성 다운로드 실패

음력→양력 변환은 `net.time4j:time4j-base` 를 사용합니다. 최초 빌드 시 Maven Central 에서 의존성을 내려받으므로 네트워크가 필요합니다. 다운로드가 끊기거나 캐시가 손상되면 다음으로 복구합니다.

```bash
./gradlew --refresh-dependencies clean bootJar
# 캐시가 심하게 손상된 경우
./gradlew cleanDependencies
```

### Gradle Wrapper 실행 오류

```bash
chmod +x gradlew
./gradlew clean bootJar
```

### Docker 빌드 시 `./gradlew: not found`

Wrapper 실행 권한을 부여하거나(위), Docker 없이 로컬 빌드로 대체합니다.

```bash
./gradlew bootJar
java -jar build/libs/korean-fortune-app.jar --spring.profiles.active=dev
```
