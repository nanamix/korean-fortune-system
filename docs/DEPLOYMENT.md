# 🚀 배포/운영/테스트 가이드

## 0. 사전 준비

```bash
# 환경 변수 파일 생성 (처음 한 번만)
cp .env.example .env
# .env 파일을 편집하여 실제 값 입력
```

## 1. 빠른 단독 실행 (가장 간단)

로컬에서 즉시 실행 — 외부 DB/Redis 없이 H2 인메모리로 동작합니다.

```bash
# 옵션 A: 로컬 JAR 실행
./gradlew bootJar
java -jar build/libs/korean-fortune-app.jar --spring.profiles.active=dev
# → http://localhost:8080/fortune-app.html

# 옵션 B: Docker 단독 실행
docker compose -f docker/docker-compose.standalone.yml up -d
# → http://localhost:8080/fortune-app.html
```

## 2. 환경별 도커 자동화

### 개발 환경
```bash
./gradlew dockerBuildDev
./gradlew dockerComposeUp   # docker-compose.yaml + docker-compose.dev.yaml
./gradlew dockerComposeDown
```

### 운영 환경
```bash
./gradlew dockerBuildProd
./gradlew dockerComposeUpProd   # docker-compose.yaml + docker-compose.prod.yaml
./gradlew dockerComposeDown
```

## 3. 직접 Docker Compose 명령어

```bash
# 개발 환경 (MySQL + Redis + 앱)
docker compose -f docker/docker-compose.yaml -f docker/docker-compose.dev.yaml up -d

# 운영 환경 (MySQL + Redis + Nginx + 앱 × 2 replica)
docker compose -f docker/docker-compose.yaml -f docker/docker-compose.prod.yaml up -d

# 단독 실행 (H2 인메모리)
docker compose -f docker/docker-compose.standalone.yml up -d

# 전체 중지
docker compose -f docker/docker-compose.yaml down
```

## 4. GitHub Actions CI/CD

`.github/workflows/ci.yml` 파이프라인:
1. **Build & Test** — `main`, `develop` 브랜치 push/PR 시 자동 실행
2. **Docker Build & Push** — `main` 브랜치 push 시 `ghcr.io/nanamix/korean-fortune-system:latest` 에 게시
3. **Notification** — 배포 성공/실패 로그 출력

서버에서 새 이미지를 당겨 재시작:
```bash
docker compose -f docker/docker-compose.yaml -f docker/docker-compose.prod.yaml pull
docker compose -f docker/docker-compose.yaml -f docker/docker-compose.prod.yaml up -d
```

## 5. 환경 변수 및 오버라이드

- `.env.example` → `.env` 복사 후 실제 값 설정
- 각 compose 오버라이드(`prod`, `dev`, `standalone`)에서 환경별 설정 분리
- `OPENAI_API_KEY`, `TELEGRAM_BOT_TOKEN` 등 민감 정보는 절대 Git에 커밋하지 마세요

## 6. 운영 팁

- **로그**: ELK 스택 또는 `/app/logs/` 볼륨 마운트
- **모니터링**: Prometheus (`http://localhost:9090`) + Grafana (`http://localhost:3000`)
- **헬스체크**: `http://localhost:8080/actuator/health`
- **로컬 개발**: DB/Redis만 도커로 띄우고 `./gradlew runDev` 사용
- **GUI**: `/fortune-app.html` — 사주팔자, 오늘운세, 토정비결, 별자리, 간지달력 탭 UI
