# 11. Docker 배포

> 멀티스테이지 Dockerfile 이미지 빌드와 Docker Compose 스택(standalone / dev / prod) 실행법, 환경변수 주입, 헬스체크를 정리합니다.
> 관련 문서: [08. 설치 가이드](./08-installation-guide.md) · [12. CI/CD 및 운영](./12-cicd-and-operations.md) · [README 인덱스](./README.md)

---

## 11.1 Dockerfile (멀티스테이지)

`Dockerfile` 은 Amazon Corretto 21 기반의 멀티스테이지 빌드입니다.

| 스테이지 | 베이스 | 역할 |
|----------|--------|------|
| `builder` | `amazoncorretto:21` | `./gradlew bootJar --no-daemon -x test` 로 빌드 후 `app.jar` 생성 |
| `runtime` | `amazoncorretto:21` | 비특권 사용자 `fortune`(uid 1001)로 실행, `SPRING_PROFILES_ACTIVE=prod`, 포트 8080 |
| `development` | `runtime` 확장 | 개발 도구 추가, `SPRING_PROFILES_ACTIVE=dev`, DevTools 재시작/LiveReload 활성 |

런타임 특징:

- 타임존 `Asia/Seoul` 고정
- JVM 옵션(`JAVA_OPTS`): ZGC, `UseContainerSupport`, `MaxRAMPercentage=75.0`, OOM 시 힙덤프(`/app/logs/`)
- `dumb-init` 를 PID 1로 사용
- 내장 헬스체크: 30초 간격으로 `curl -f http://localhost:8080/actuator/health`

### 이미지 빌드

```bash
# 운영(runtime) 이미지
./gradlew dockerBuildProd
# 또는 직접
docker build -f Dockerfile -t korean-fortune:latest --target runtime .

# 개발(development) 이미지
./gradlew dockerBuildDev
# 또는 직접
docker build -f Dockerfile -t korean-fortune:dev --target development .
```

> 멀티 아키텍처 빌드: `docker buildx build --platform linux/amd64,linux/arm64 -t korean-fortune:latest .`

---

## 11.2 Compose 파일 구성

Docker Compose 파일은 `docker/` 아래에 있으며, base + override 방식으로 조합합니다.

| 파일 | 역할 |
|------|------|
| `docker/docker-compose.yaml` | base 스택 (app, mysql, redis, nginx, prometheus, grafana, elasticsearch, logstash, kibana, ollama) |
| `docker/docker-compose.dev.yaml` | 개발 오버라이드 (development 타깃, DevTools, phpMyAdmin, redis-commander, 디버그 포트 5005) |
| `docker/docker-compose.prod.yaml` | 운영 오버라이드 (prod 프로필, 레플리카 2, 리소스 제한, G1GC) |
| `docker/docker-compose.standalone.yml` | 단독 실행 (외부 DB 없이 GHCR 이미지 + H2) |
| `docker/docker-compose.test.yaml` | 테스트용 (test 프로필, 포트 18080) |

base 스택 주요 서비스:

| 서비스 | 이미지 | 포트 | 비고 |
|--------|--------|------|------|
| app | 로컬 빌드(runtime) | 8080 | 헬스체크 `actuator/health` |
| mysql | `mysql:8.0.35` | 3306 | DB `korean_fortune`, UTF8MB4, KST |
| redis | `redis:7-alpine` | 6379 | 캐시 |
| nginx | `nginx:alpine` | 80, 443 | 리버스 프록시 |
| prometheus | `prom/prometheus` | 9090 | `/actuator/prometheus` 스크레이프 |
| grafana | `grafana/grafana` | 3000 | 대시보드 |
| elasticsearch/logstash/kibana | 8.11.0 | 9200/–/5601 | 로그 스택(ELK) |
| ollama | `ollama/ollama` | 11434 | `--profile ai` 지정 시에만 시작 |

---

## 11.3 스택 실행법

### standalone (가장 간편, 데모/테스트)

외부 DB/Redis 없이 GHCR의 이미지 + H2 인메모리로 실행합니다.

```bash
docker compose -f docker/docker-compose.standalone.yml up -d
# 접속: http://localhost:8080
```

### 개발 스택 (dev)

base + dev 오버라이드. MySQL/Redis + phpMyAdmin/redis-commander 관리 도구 포함.

```bash
./gradlew dockerComposeUp
# 또는
docker compose -f docker/docker-compose.yaml -f docker/docker-compose.dev.yaml up -d
```

접근: 앱 `:8080`, 디버그 `:5005`, phpMyAdmin `:8081`, redis-commander `:8082`.

### 운영 스택 (prod)

base + prod 오버라이드. 레플리카 2, 리소스 제한(메모리 3G, CPU 2.0), G1GC.

```bash
# 환경변수 준비
cp .env.example .env    # 편집 후

./gradlew dockerComposeUpProd
# 또는
docker compose -f docker/docker-compose.yaml -f docker/docker-compose.prod.yaml up -d
```

### 중지 / 로그

```bash
./gradlew dockerComposeDown        # 중지
./gradlew dockerComposeDownAll     # 중지 + 볼륨 삭제
./gradlew dockerComposeLogs        # 로그 팔로우
./gradlew dockerComposeRestart     # 재시작(prod)
```

---

## 11.4 환경변수 주입

Compose는 `.env` 파일(프로젝트 루트) 또는 셸 환경변수를 읽습니다. 미지정 시 base 파일의 기본값이 적용됩니다.

| 변수 | 적용 서비스 | 기본값 |
|------|-------------|--------|
| `MYSQL_ROOT_PASSWORD` / `MYSQL_PASSWORD` | mysql, app | `root_fortune_2025!` / `fortune_secure_2025!` |
| `SPRING_PROFILES_ACTIVE` | app | base=`docker`, prod override=`prod` |
| `APP_FORTUNE_AI_ENABLED` / `APP_FORTUNE_AI_PROVIDER` / `APP_FORTUNE_AI_MODEL` / `APP_FORTUNE_AI_BASE_URL` / `APP_FORTUNE_AI_TIMEOUT` | app | `false` / `openai` / `gpt-5.4-mini` / `https://api.openai.com/v1` / `30s` |
| `OPENAI_API_KEY` | app | (빈 값) |
| `TELEGRAM_BOT_TOKEN` / `TELEGRAM_CHAT_ID` | app(standalone) | (빈 값) |
| `GRAFANA_PASSWORD` | grafana | `admin123` |
| `OTEL_EXPORTER_OTLP_ENDPOINT` / `OTEL_SAMPLING_PROBABILITY` | app | (미설정) / prod=`0.1` |

`docker` 프로필의 앱은 MySQL에 `jdbc:mysql://mysql:3306/korean_fortune` 로 접속하며, `MYSQL_USER`/`MYSQL_PASSWORD` 를 사용합니다.

---

## 11.5 헬스체크

- **app 컨테이너**: 30초 간격 `curl -f http://localhost:8080/actuator/health`, timeout 10초, retries 3, start-period 60초.
- **mysql**: `mysqladmin ping`
- **redis**: `redis-cli ping`

수동 확인:

```bash
curl -f http://localhost:8080/actuator/health
docker compose -f docker/docker-compose.yaml ps      # 컨테이너 상태/헬스
docker inspect --format '{{.State.Health.Status}}' korean-fortune-app
```

컨테이너가 `unhealthy` 로 남으면 로그를 확인합니다.

```bash
docker compose -f docker/docker-compose.yaml logs --tail=100 app
```
