# 🚀 배포/운영/테스트 가이드

## 1. 환경별 도커 자동화

### 개발 환경
```bash
./gradlew buildDockerDev
./gradlew upDockerDev
./gradlew downDockerDev
```

### 운영 환경
```bash
./gradlew buildDockerProd
./gradlew upDockerProd
./gradlew downDockerProd
```

### 테스트 환경
```bash
./gradlew buildDockerTest
./gradlew upDockerTest
./gradlew downDockerTest
```

## 2. 직접 도커 명령어
```bash
docker-compose -f docker/docker-compose.yaml -f docker/docker-compose.dev.yaml up -d
docker-compose -f docker/docker-compose.yaml -f docker/docker-compose.prod.yaml up -d
docker-compose -f docker/docker-compose.yaml -f docker/docker-compose.test.yaml up -d
```

## 3. 환경 변수 및 오버라이드
- .env 파일 또는 각 compose 오버라이드에서 관리
- 운영/테스트/개발 환경별로 포트, DB, 캐시, AI, API KEY 등 분리 가능

## 4. 운영 팁
- 로그: ELK, Grafana, Prometheus 활용
- DB/캐시/AI 등 외부 서비스 교체: compose 오버라이드로 손쉽게 가능
- 로컬 개발: DB/redis만 도커로 띄우고, `./gradlew bootRun`으로 빠른 개발 가능
