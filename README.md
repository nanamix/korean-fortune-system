# 🔮 한국형 만세력 운세 시스템

[![Java](https://img.shields.io/badge/Java-17-orange?style=flat-square&logo=openjdk)](https://openjdk.org/)
[![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.3.x-brightgreen?style=flat-square&logo=spring)](https://spring.io/projects/spring-boot)
[![Docker](https://img.shields.io/badge/Docker-Ready-blue?style=flat-square&logo=docker)](https://www.docker.com/)
[![License](https://img.shields.io/badge/License-Apache%202.0-blue?style=flat-square)](LICENSE)
[![Build Status](https://img.shields.io/badge/Build-Passing-brightgreen?style=flat-square)](https://github.com/korean-fortune-system/actions)

> 전통 사주팔자와 토정비결, 서양 별자리 운세를 현대적 기술로 구현한 종합 운세 서비스

## 🌟 주요 기능

### 📊 전통 사주학

- **사주팔자 계산**: 생년월일시 기반 정확한 사주 계산
- **오행 분석**: 목화토금수 오행 상생상극 분석
- **신살 해석**: 길신과 흉신의 영향 분석
- **일일 운세**: 매일의 길흉과 방위, 색깔 제공

### 📜 토정비결 64괘

- **연간 운세**: 전통 토정비결 공식을 통한 괘 산출
- **월별 길흉**: 각 달의 길한 시기와 주의할 시기
- **상세 해석**: 각 괘의 의미와 현대적 적용

### ⭐ 서양 점성술

- **12별자리 운세**: 생년월일 기반 별자리 판별
- **분야별 운세**: 애정, 직업, 건강, 재물 운세
- **궁합 분석**: 별자리 간 상성 분석

### 🤖 AI 기반 해석

- **개인화된 해석**: OpenAI GPT-4를 활용한 맞춤형 운세 해석
- **자연어 질문응답**: 운세 관련 궁금증에 대한 AI 답변
- **현대적 조언**: 전통 지혜와 현대적 관점의 조화

### 📆 간지달력

- **월별 달력**: 간지와 절기가 표시된 전통 달력
- **길일 선택**: 중요한 일정을 위한 길일 추천
- **24절기**: 정확한 절기 정보 제공

## 🏗️ 기술 스택

### Backend

- **Java 17**: 안정적인 Java 버전
- **Spring Boot 3.3.x**: 최신 스프링 부트 프레임워크
- **Spring Security**: OAuth2, JWT 기반 인증/인가
- **Spring Data JPA**: 데이터베이스 연동
- **Spring AI**: OpenAI, Ollama 연동

### Database & Cache

- **MySQL 8.0**: 메인 데이터베이스
- **Redis**: 고성능 캐싱
- **H2**: 테스트용 인메모리 데이터베이스

### AI & ML

- **OpenAI GPT-4**: 자연어 처리 및 해석
- **Ollama**: 로컬 AI 모델 지원
- **Spring AI**: AI 서비스 통합

### DevOps & Monitoring

- **Docker & Docker Compose**: 컨테이너화
- **Prometheus & Grafana**: 모니터링 및 메트릭
- **ELK Stack**: 로그 수집 및 분석
- **Nginx**: 리버스 프록시 및 로드 밸런싱

### Documentation & Testing

- **OpenAPI 3 (Swagger)**: API 문서화
- **JUnit 5**: 단위 테스트
- **Testcontainers**: 통합 테스트
- **Jacoco**: 코드 커버리지

## 🚀 빠른 시작

### 사전 요구사항

- Java 17 이상
- Docker & Docker Compose
- Git

### 1. 저장소 클론

```bash
git clone https://github.com/korean-fortune-system/korean-fortune-system.git
cd korean-fortune-system
```

### 2. 환경 설정

```bash
# 환경변수 파일 생성
cp .env.example .env

# 필요한 API 키 설정 (선택사항)
# OPENAI_API_KEY=your-openai-api-key
# GOOGLE_CLIENT_ID=your-google-client-id
# KAKAO_CLIENT_ID=your-kakao-client-id
```

### 3. Docker Compose로 실행

```bash
# 전체 스택 시작
docker-compose up -d

# 로그 확인
docker-compose logs -f app
```

### 4. 개발 환경 실행

```bash
# 데이터베이스만 시작
docker-compose up -d mysql redis

# 애플리케이션 개발 모드 실행
./gradlew bootRun
```

## 📋 API 엔드포인트

### 🔮 운세 서비스

```http
POST /api/fortune/saju/calculate          # 사주팔자 계산
POST /api/fortune/daily/today             # 오늘의 운세
POST /api/fortune/daily/tomorrow          # 내일의 운세
POST /api/fortune/daily/{date}            # 특정 날짜 운세
POST /api/fortune/tojeong                 # 토정비결 계산
POST /api/fortune/zodiac                  # 별자리 운세
GET  /api/fortune/calendar/ganji/{year}/{month}  # 간지달력
```

### 🤖 AI 서비스

```http
POST /api/fortune/saju/ai-interpretation       # AI 사주 해석
POST /api/fortune/daily/today/ai-enhanced      # AI 강화 일일 운세
POST /api/fortune/tojeong/ai-interpretation    # AI 토정비결 해석
POST /api/fortune/zodiac/ai-analysis           # AI 별자리 분석
POST /api/fortune/ai/question                  # AI 질문 응답
```

### 🏥 시스템

```http
GET  /api/system/status                   # 시스템 상태
GET  /actuator/health                     # 헬스체크
GET  /swagger-ui.html                     # API 문서
```

## 📊 사용 예시

### 사주팔자 계산

```json
POST /api/fortune/saju/calculate
{
  "birthYear": 1981,
  "birthMonth": 3,
  "birthDay": 20,
  "birthHour": 01,
  "birthMinute": 59,
  "gender": "M",
  "calendarType": "SOLAR"
}
```

### 응답 예시

```json
{
  "success": true,
  "message": "성공",
  "data": {
    "yearPillar": "경오",
    "monthPillar": "신사",
    "dayPillar": "갑자",
    "timePillar": "신미",
    "dayMaster": "갑",
    "birthDate": "1990-05-15",
    "calendarType": "SOLAR",
    "gender": "M"
  },
  "timestamp": "2025-06-23T10:30:00"
}
```

## 🧪 테스트

### 단위 테스트 실행

```bash
./gradlew test
```

### 통합 테스트 실행

```bash
./gradlew integrationTest
```

### 성능 테스트 실행

```bash
./gradlew performanceTest
```

### 테스트 커버리지 확인

```bash
./gradlew jacocoTestReport
open build/reports/jacoco/test/html/index.html
```

## 🏗️ 빌드 및 배포

### 로컬 빌드

```bash
# JAR 파일 생성
./gradlew bootJar

# Docker 이미지 빌드
./gradlew dockerBuild

# 전체 빌드 (테스트 + 품질검사 포함)
./gradlew buildProd
```

### 운영 배포

```bash
# 운영 환경으로 배포
docker-compose -f docker-compose.yml -f docker-compose.prod.yml up -d

# 서비스 상태 확인
docker-compose ps
docker-compose logs app
```

## 📈 모니터링

### 대시보드 접속

- **애플리케이션**: <http://localhost:8080>
- **API 문서**: <http://localhost:8080/swagger-ui.html>
- **Grafana**: <http://localhost:3000> (admin/fortune_grafana_2025!)
- **Prometheus**: <http://localhost:9090>
- **Kibana**: <http://localhost:5601>

### 주요 메트릭

- API 응답 시간
- 데이터베이스 커넥션 풀
- 캐시 히트율
- JVM 메모리 사용량
- 사용자 요청 패턴

## 🤝 기여 가이드

### 개발 환경 설정

1. 저장소 포크 및 클론
2. 개발 브랜치 생성: `git checkout -b feature/새로운기능`
3. 개발 환경 실행: `./gradlew devRun`
4. 코드 작성 및 테스트
5. 커밋 및 푸시
6. Pull Request 생성

### 코딩 컨벤션

- **Java**: Google Java Style Guide 준수
- **Git**: Conventional Commits 사용
- **문서**: 한국어 주석 및 문서화
- **테스트**: 최소 80% 코드 커버리지 유지

### 커밋 메시지 형식

```markdown
feat: 새로운 기능 추가
fix: 버그 수정
docs: 문서 수정
style: 코드 포맷팅
refactor: 리팩토링
test: 테스트 추가/수정
chore: 빌드 설정 등
```

## 🐞 문제 해결

### 자주 발생하는 문제

#### 1. 데이터베이스 연결 실패

```bash
# MySQL 컨테이너 상태 확인
docker-compose ps mysql

# 로그 확인
docker-compose logs mysql

# 재시작
docker-compose restart mysql
```

#### 2. Redis 캐시 문제

```bash
# Redis 캐시 초기화
docker-compose exec redis redis-cli FLUSHALL
```

#### 3. AI 서비스 응답 없음

```bash
# OpenAI API 키 확인
echo $OPENAI_API_KEY

# Ollama 서비스 상태 확인
docker-compose logs ollama
```

#### 4. 메모리 부족

```bash
# Docker 메모리 설정 증가
# Docker Desktop > Settings > Resources > Memory 4GB 이상 설정
```

## 📄 라이선스

이 프로젝트는 [Apache License 2.0](LICENSE) 하에 배포됩니다.

## 👥 팀

- **개발팀**: 핵심 시스템 개발
- **AI팀**: 인공지능 기능 구현
- **운세팀**: 전통 운세학 검증
- **DevOps팀**: 인프라 및 배포 관리

## 🔗 관련 링크

- **GitHub**: <https://github.com/korean-fortune-system>
- **API 문서**: <https://api.korean-fortune.com/swagger-ui.html>
- **이슈 트래커**: <https://github.com/korean-fortune-system/issues>
- **위키**: <https://github.com/korean-fortune-system/wiki>

## 📞 지원

### 기술 지원

- **이메일**: <admin@jyha.net>
- **GitHub Issues**: 버그 리포트 및 기능 요청

### 비즈니스 문의

- **이메일**: <admin@jyha.net>

---

<div align="center">

<!-- trunk-ignore(markdownlint/MD036) -->
**🔮 전통의 지혜와 현대 기술의 만남 🔮**

Made with ❤️ by Korean Fortune Team

</div>
