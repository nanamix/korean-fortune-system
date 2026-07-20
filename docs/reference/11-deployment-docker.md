# 11. Docker 배포

> 멀티스테이지 Dockerfile 이미지 빌드와 Docker Compose 스택(standalone / dev / prod) 실행법, 환경변수 주입, 헬스체크를 정리합니다.
> 관련 문서: [08. 설치 가이드](./08-installation-guide.md) · [12. CI/CD 및 운영](./12-cicd-and-operations.md) · [README 인덱스](./README.md)

---

## 11.1 Dockerfile (멀티스테이지)

`Dockerfile` 은 Amazon Corretto 21 기반의 멀티스테이지 빌드입니다.

| 스테이지 | 베이스 | 역할 |
|----------|--------|------|
| `builder` | `amazoncorretto:21` | `./gradlew bootJar --no-daemon -x test` 로 빌드 후 `app.jar` 생성 |
| `runtime` | `amazoncorretto:21` | 비특권 사용자 `fortune`(uid 1001)로 실행, `SPRING_PROFILES_ACTIVE=prod`, 포트 18080 |
| `development` | `runtime` 확장 | 개발 도구 추가, `SPRING_PROFILES_ACTIVE=dev`, DevTools 재시작/LiveReload 활성 |

런타임 특징:

- 타임존 `Asia/Seoul` 고정
- JVM 옵션(`JAVA_OPTS`): ZGC, `UseContainerSupport`, `MaxRAMPercentage=75.0`, OOM 시 힙덤프(`/app/logs/`)
- `dumb-init` 를 PID 1로 사용
- 내장 헬스체크: 30초 간격으로 `curl -f http://localhost:18080/actuator/health`

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
| `docker/docker-compose.openbao.override.yml` | OpenBao 인증, tmpfs secret rendering, file 기반 주입 |
| `docker/docker-compose.standalone.yml` | 단독 실행 (외부 DB 없이 GHCR 이미지 + H2) |
| `docker/docker-compose.test.yaml` | 테스트용 (test 프로필, 포트 18080) |

base 스택 주요 서비스:

| 서비스 | 이미지 | 포트 | 비고 |
|--------|--------|------|------|
| app | 로컬 빌드(runtime) | 18080 | 헬스체크 `actuator/health` |
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
# 접속: http://localhost:18080
```

### 개발 스택 (dev)

base + dev 오버라이드. MySQL/Redis + phpMyAdmin/redis-commander 관리 도구 포함.

```bash
./gradlew dockerComposeUp
# 또는
docker compose -f docker/docker-compose.yaml -f docker/docker-compose.dev.yaml up -d
```

접근: 앱 `:18080`, 디버그 `:5005`, phpMyAdmin `:8081`, redis-commander `:8082`.

### 운영 스택 (prod)

OpenBao bootstrap과 KV 값을 준비한 뒤 base + prod + OpenBao 오버라이드를 함께 실행합니다.

```bash
docker compose \
  -f docker/docker-compose.yaml \
  -f docker/docker-compose.prod.yaml \
  -f docker/docker-compose.openbao.override.yml \
  up -d
```

### 중지 / 로그

```bash
./gradlew dockerComposeDown        # 중지
./gradlew dockerComposeDownAll     # 중지 + 볼륨 삭제
./gradlew dockerComposeLogs        # 로그 팔로우
./gradlew dockerComposeRestart     # 재시작(prod)
```

---

## 11.4 OpenBao secret 주입

자격증명은 `.env`나 Compose environment 값으로 보관하지 않습니다. OpenBao renderer가 KV 문자열을 공유 `tmpfs`에 파일로 만들고 MySQL의 `MYSQL_*_FILE`, Grafana의 `GF_SECURITY_ADMIN_PASSWORD__FILE`, Spring Boot의 `configtree:`가 읽습니다.

비밀이 아닌 runtime selector만 일반 환경변수로 유지합니다. 필수 key와 bootstrap 절차는 `ops/openbao/README.md`를 참고합니다.

---

## 11.5 헬스체크

- **app 컨테이너**: 30초 간격 `curl -f http://localhost:18080/actuator/health`, timeout 10초, retries 3, start-period 60초.
- **mysql**: `mysqladmin ping`
- **redis**: `redis-cli ping`

수동 확인:

```bash
curl -f http://localhost:18080/actuator/health
docker compose -f docker/docker-compose.yaml ps      # 컨테이너 상태/헬스
docker inspect --format '{{.State.Health.Status}}' korean-fortune-app
```

컨테이너가 `unhealthy` 로 남으면 로그를 확인합니다.

```bash
docker compose -f docker/docker-compose.yaml logs --tail=100 app
```
