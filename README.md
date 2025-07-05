# 🔮 한국형 만세력 운세 시스템 v2.5

[![Java](https://img.shields.io/badge/Java-21-orange.svg)](https://openjdk.java.net/projects/jdk/17/)
[![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.5.3-brightgreen.svg)](https://spring.io/projects/spring-boot)
[![License](https://img.shields.io/badge/License-Apache%202.0-blue.svg)](https://opensource.org/licenses/Apache-2.0)
[![Build Status](https://img.shields.io/badge/Build-Passing-success.svg)](https://github.com/korean-fortune-system)

전통 사주팔자와 토정비결, 별자리 운세를 제공하는 종합 운세 서비스입니다.

## ✨ 주요 기능

### 🎯 핵심 운세 서비스
- **📊 사주팔자 계산**: 전통 사주학 기반 정확한 사주팔자 계산
- **📅 일일 운세**: 오늘/내일/특정 날짜의 상세 운세 분석
- **📜 토정비결**: 64괘 기반 연간 운세 해석
- **⭐ 별자리 운세**: 서양 점성술 기반 12성좌 운세
- **📆 간지달력**: 월별 간지달력과 길일/흉일 안내

### 🤖 AI 기능 (선택적)
- **🎯 AI 사주 해석**: OpenAI를 활용한 개인화된 사주 분석
- **💬 자연어 질문 답변**: "올해 연애운은 어떤가요?" 같은 자연어 질문 지원
- **📈 상세 운세 조언**: AI가 생성하는 맞춤형 조언

### 🔧 기술적 특징
- **☁️ 클라우드 네이티브**: Docker 및 Kubernetes 지원
- **📚 캐시 시스템**: Redis/Caffeine 기반 고성능 캐싱
- **📖 API 문서화**: Swagger/OpenAPI 3.0 완전 지원
- **🔒 보안**: JWT 인증 및 OAuth2 연동
- **📊 모니터링**: Actuator + Prometheus 메트릭

## 🚀 빠른 시작

### 📋 사전 요구사항
- Java 21 이상
- Docker & Docker Compose (선택적)
- MySQL 8.0 (운영환경, 개발시 H2 자동 사용)
- OPEN-AI 및 Ollama (Optional)

### 🏃‍♂️ 로컬 실행

```bash
# 1. 저장소 클론
git clone https://github.com/your-org/korean-fortune-system.git
cd korean-fortune-system

# 2. 개발 환경으로 실행
./gradlew runDev

# 3. AI 기능과 함께 실행 (OpenAI API 키 필요)
export OPENAI_API_KEY=your-api-key
./gradlew runWithAI
```

애플리케이션이 실행되면:
- 🌐 **API 서버**: http://localhost:8080
- 📖 **API 문서**: http://localhost:8080/swagger-ui.html
- 🔍 **헬스체크**: http://localhost:8080/actuator/health

### 🐳 Docker로 실행

```bash
# 개발 환경
docker-compose -f docker-compose.yaml -f docker-compose.dev.yaml up -d

# 운영 환경
docker-compose -f docker-compose.yaml -f docker-compose.prod.yaml up -d
```

## 📚 API 사용법

### 🔮 사주팔자 계산

```bash
curl -X POST http://localhost:8080/api/fortune/saju/calculate \
  -H "Content-Type: application/json" \
  -d '{
    "birthYear": 1990,
    "birthMonth": 5,
    "birthDay": 15,
    "birthHour": 14,
    "birthMinute": 30,
    "gender": "M",
    "calendarType": "SOLAR"
  }'
```

### 📅 오늘의 운세

```bash
curl -X POST http://localhost:8080/api/fortune/daily/today \
  -H "Content-Type: application/json" \
  -d '{
    "birthYear": 1990,
    "birthMonth": 5,
    "birthDay": 15,
    "birthHour": 14,
    "birthMinute": 30,
    "gender": "M",
    "calendarType": "SOLAR"
  }'
```

### 📜 토정비결

```bash
curl -X POST http://localhost:8080/api/fortune/tojeong \
  -H "Content-Type: application/json" \
  -d '{
    "birthYear": 1990,
    "birthMonth": 5,
    "birthDay": 15,
    "targetYear": 2025
  }'
```

### ⭐ 별자리 운세

```bash
curl -X POST http://localhost:8080/api/fortune/zodiac \
  -H "Content-Type: application/json" \
  -d '{
    "birthDate": "1990-05-15",
    "targetDate": "2025-06-24"
  }'
```

### 📆 간지달력 조회

```bash
curl http://localhost:8080/api/fortune/calendar/ganji/2025/6
```

## 🏗️ 프로젝트 구조

```
src/
├── main/
│   ├── java/com/fortune/
│   │   ├── controller/         # REST API 컨트롤러
│   │   ├── service/           # 비즈니스 로직 서비스
│   │   ├── dto/               # 데이터 전송 객체
│   │   ├── config/            # 설정 클래스
│   │   ├── enums/             # 열거형 정의
│   │   └── security/          # 보안 관련 클래스
│   └── resources/
│       ├── application.yml    # 기본 설정
│       ├── application-dev.yml # 개발 환경 설정
│       ├── application-prod.yml # 운영 환경 설정
│       └── application-ai.yml  # AI 기능 설정
├── test/                      # 테스트 코드
├── docker/                    # Docker 설정 파일
└── docs/                      # 문서
```

## 🔧 설정 및 환경변수

### 🔑 필수 환경변수

```bash
# 데이터베이스 (운영환경)
DB_USERNAME=fortune_user
DB_PASSWORD=fortune_secure_2025!

# AI 기능 (선택적)
OPENAI_API_KEY=your-openai-api-key
AI_ENABLED=true

# Redis (운영환경)
REDIS_HOST=localhost
REDIS_PORT=6379
REDIS_PASSWORD=your-redis-password
```

### 📝 프로파일별 설정

- **`dev`**: 개발 환경 (H2 DB, 상세 로깅)
- **`test`**: 테스트 환경 (인메모리 DB, AI 비활성화)
- **`prod`**: 운영 환경 (MySQL, Redis, 성능 최적화)
- **`ai`**: AI 기능 활성화
- **`docker`**: Docker 환경용 설정

## 🧪 테스트

```bash
# 단위 테스트 실행
./gradlew test

# 통합 테스트 실행
./gradlew integrationTest

# 모든 테스트 실행
./gradlew ciTest

# 테스트 커버리지 확인
./gradlew jacocoTestReport
```

### 📊 테스트 구조
- **단위 테스트**: 각 서비스와 컴포넌트의 독립적 테스트
- **통합 테스트**: 전체 애플리케이션 컨텍스트 테스트
- **성능 테스트**: API 응답 시간 및 동시성 테스트
- **보안 테스트**: 인증/인가 및 보안 헤더 테스트

## 🚀 배포

### 🐳 Docker 배포

```bash
# 1. 이미지 빌드
./gradlew dockerBuildProd

# 2. 운영 환경 배포
docker-compose -f docker-compose.yaml -f docker-compose.prod.yaml up -d

# 3. 상태 확인
docker-compose ps
curl http://localhost:8080/actuator/health
```

### ☁️ 클라우드 배포

#### Kubernetes
```bash
# 네임스페이스 생성
kubectl create namespace korean-fortune

# 시크릿 생성 (환경변수)
kubectl create secret generic fortune-secrets \
  --from-literal=db-password=your-password \
  --from-literal=openai-api-key=your-key \
  -n korean-fortune

# 애플리케이션 배포
kubectl apply -f k8s/ -n korean-fortune
```

#### AWS ECS/Fargate
```bash
# ECR에 이미지 푸시
aws ecr get-login-password --region ap-northeast-2 | docker login --username AWS --password-stdin your-account.dkr.ecr.ap-northeast-2.amazonaws.com
docker tag korean-fortune:latest your-account.dkr.ecr.ap-northeast-2.amazonaws.com/korean-fortune:latest
docker push your-account.dkr.ecr.ap-northeast-2.amazonaws.com/korean-fortune:latest

# ECS 서비스 업데이트
aws ecs update-service --cluster korean-fortune --service fortune-service --force-new-deployment
```

## 📊 모니터링

### 🔍 헬스체크 엔드포인트
- **기본 상태**: `/actuator/health`
- **상세 상태**: `/actuator/health/detailed`
- **애플리케이션 정보**: `/actuator/info`
- **메트릭**: `/actuator/metrics`
- **Prometheus**: `/actuator/prometheus`

### 📈 주요 메트릭
- **응답 시간**: API 호출 시간 분포
- **처리량**: 초당 요청 수 (RPS)
- **에러율**: HTTP 4xx/5xx 응답 비율
- **JVM 메트릭**: 메모리, GC, 스레드 상태
- **비즈니스 메트릭**: 사주 계산 횟수, 인기 기능 등

## 🤝 기여하기

### 📝 개발 워크플로우

1. **이슈 생성**: 버그 리포트나 기능 요청
2. **브랜치 생성**: `feature/새기능` 또는 `bugfix/버그수정`
3. **개발 및 테스트**: 코드 작성 및 테스트 추가
4. **코드 리뷰**: Pull Request 생성
5. **병합**: 리뷰 완료 후 main 브랜치 병합

### 🎨 코딩 스타일
- **Java**: Google Java Style Guide 준수
- **커밋 메시지**: Conventional Commits 규칙 사용
- **브랜치 명명**: `feature/기능명`, `bugfix/버그명` 형식
- **테스트**: 새로운 기능에는 반드시 테스트 추가

### 🔧 로컬 개발 설정

```bash
# 개발 환경 설정
cp .env.example .env
# .env 파일을 편집하여 필요한 환경변수 설정

# 개발 서버 실행
./gradlew runDev

# 테스트 실행
./gradlew test

# 코드 스타일 검사
./gradlew checkFormat
```

## 📞 지원 및 문의

### 🆘 문제 해결
- **문서**: [Wiki](https://github.com/korean-fortune-system/nananmix/wiki) 참조

### 📧 연락처
- **메인테이너**: 하진영 (admin@jyha.net)

## 📄 라이선스

이 프로젝트는 [Apache License 2.0](LICENSE) 하에 배포됩니다.

```
Copyright 2025 Korean Fortune System Team

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

    http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
```

## 🙏 감사의 말

이 프로젝트는 다음 기술들을 기반으로 만들어졌습니다:

- **Spring Framework**: 강력한 Java 애플리케이션 프레임워크
- **OpenAI**: AI 기반 운세 해석 서비스
- **Docker**: 컨테이너화 및 배포 지원
- **Swagger**: API 문서화 도구
- **한국 전통 사주학**: 수천년 전통의 지혜

---

<div align="center">

**🔮 ✨ 모든 분들의 행운을 빕니다! ✨ 🔮**

[⭐ Star](https://github.com/korean-fortune-system) |
[🐛 Report Bug](https://github.com/korean-fortune-system/issues) |
[💡 Request Feature](https://github.com/korean-fortune-system/issues)

</div>
