# 🔮 한국형 만세력 운세 시스템

전통 사주팔자와 토정비결을 제공하는 한국형 운세 시스템입니다.

## ✨ 주요 기능

- **📊 사주팔자 계산**: 전통 사주팔자 계산 및 분석
- **📅 일일/월별 운세**: 개인별 맞춤 운세 제공
- **📜 토정비결**: 64괘 기반 토정비결 운세
- **⭐ 별자리 운세**: 서양 별자리 운세
- **📆 간지달력**: 간지 달력 및 길일 조회 (실제 달력 형태 뷰잉)
- **📧 이메일 발송**: 운세 결과를 이메일로 발송
- **📱 텔레그램 발송**: 운세 결과를 텔레그램 봇으로 발송
- **🤖 AI 운세**: OpenAI 기반 AI 운세 해석
- **🔍 시스템 모니터링**: Spring Boot Actuator 기반 모니터링

## 🚀 빠른 시작

### 1. 로컬 실행 (Docker 없이)

```bash
# JAR 파일 빌드
./gradlew bootJar

# 애플리케이션 실행
java -jar build/libs/korean-fortune-app.jar --spring.profiles.active=dev
```

### 2. Gradle로 직접 실행

```bash
# 개발 환경으로 실행 (AI 비활성화)
./gradlew runDev

# AI 기능 활성화하고 실행 (OPENAI_API_KEY 필요)
./gradlew runWithAI

# JAR 파일 직접 실행
./gradlew startLocal
```

### 3. Docker 실행

```bash
# 개발용 Docker 이미지 빌드
./gradlew dockerBuildDev

# 운영용 Docker 이미지 빌드
./gradlew dockerBuildProd

# Docker Compose로 전체 서비스 시작 (개발 환경)
./gradlew dockerComposeUp

# Docker Compose로 전체 서비스 시작 (운영 환경)
./gradlew dockerComposeUpProd
```

## 🌐 접속 정보

애플리케이션 실행 후 다음 URL로 접속할 수 있습니다:

- **🏠 메인 홈페이지**: http://localhost:8080
- **📚 API 문서**: http://localhost:8080/api/docs
- **🧪 API 테스트**: http://localhost:8080/api/docs/test
- **📅 간지달력 보기**: http://localhost:8080/api/calendar/view/current
- **📊 Actuator**: http://localhost:8080/actuator
- **🔍 헬스체크**: http://localhost:8080/actuator/health

## 📚 API 문서화

SpringDoc/Swagger 대신 자체 API 문서화 시스템을 사용합니다:

### API 문서 확인
```bash
# JSON 형태의 API 문서
curl http://localhost:8080/api/docs

# 브라우저에서 확인
open http://localhost:8080/api/docs
```

### API 테스트
```bash
# 인터랙티브 테스트 페이지
open http://localhost:8080/api/docs/test
```

### 주요 API 엔드포인트

| 기능 | 메서드 | URL | 설명 |
|------|--------|-----|------|
| 사주팔자 계산 | POST | `/api/fortune/saju/calculate` | 전통 사주팔자 계산 |
| 사주팔자 발송 | POST | `/api/fortune/saju/calculate-and-send` | 사주팔자 계산 후 이메일/텔레그램 발송 |
| 오늘의 운세 | POST | `/api/fortune/daily/today` | 오늘의 운세 |
| 오늘의 운세 발송 | POST | `/api/fortune/daily/today-and-send` | 오늘의 운세 계산 후 이메일/텔레그램 발송 |
| 토정비결 | POST | `/api/fortune/tojeong` | 토정비결 64괘 운세 |
| 토정비결 발송 | POST | `/api/fortune/tojeong/calculate-and-send` | 토정비결 계산 후 이메일/텔레그램 발송 |
| 별자리 운세 | POST | `/api/fortune/zodiac` | 서양 별자리 운세 |
| 별자리 운세 발송 | POST | `/api/fortune/zodiac/calculate-and-send` | 별자리 운세 계산 후 이메일/텔레그램 발송 |
| 간지달력 API | GET | `/api/fortune/calendar/ganji/{year}/{month}` | 간지 달력 JSON 조회 |
| 간지달력 뷰 | GET | `/api/calendar/view/{year}/{month}` | 간지달력 HTML 뷰 |
| AI 사주 해석 | POST | `/api/fortune/ai/interpret-saju` | AI 기반 사주 해석 |
| 시스템 상태 | GET | `/api/system/status` | 시스템 상태 확인 |

## 🔑 환경변수 설정

### 개발 환경에서 이메일/텔레그램 발송 설정

개발 환경에서 이메일과 텔레그램 발송을 사용하려면 다음 환경변수를 설정하세요:

```bash
# 이메일 발송 설정
export MAIL_USERNAME=your-email@gmail.com
export MAIL_PASSWORD=your-app-password

# 텔레그램 발송 설정
export TELEGRAM_BOT_TOKEN=your-telegram-bot-token
export TELEGRAM_CHAT_ID=your-telegram-chat-id
```

### AI 기능 설정
AI 기능을 사용하려면 OpenAI API 키를 설정하세요:

```bash
export OPENAI_API_KEY=your-api-key
```

### 이메일 발송 설정
이메일 발송 기능을 사용하려면 SMTP 설정을 추가하세요:

```yaml
# application-prod.yml 또는 환경변수
spring:
  mail:
    host: smtp.gmail.com
    port: 587
    username: your-email@gmail.com
    password: your-app-password
    properties:
      mail:
        smtp:
          auth: true
          starttls:
            enable: true

app:
  fortune:
    email:
      enabled: true
```

### 텔레그램 발송 설정
텔레그램 발송 기능을 사용하려면 봇 토큰을 설정하세요:

```yaml
# application-prod.yml 또는 환경변수
app:
  fortune:
    telegram:
      enabled: true
      bot-token: "your-telegram-bot-token"
      api-url: "https://api.telegram.org/bot"
```

## 📋 주요 Gradle 태스크

```bash
# 애플리케이션 실행
./gradlew bootRun              # 기본 실행 (AI 비활성화)
./gradlew runDev               # 개발 환경으로 실행 (AI 비활성화)
./gradlew runWithAI            # AI 기능 활성화하고 실행 (OPENAI_API_KEY 필요)
./gradlew runWithoutAI         # AI 없이 실행
./gradlew startLocal           # JAR 파일 직접 실행

# 빌드
./gradlew bootJar              # 실행 가능한 JAR 생성
./gradlew runLocalJar          # JAR 파일 실행 정보 출력

# 테스트
./gradlew test                 # 단위 테스트 실행
./gradlew integrationTest      # 통합 테스트 실행

# Docker
./gradlew dockerBuildDev       # 개발용 Docker 이미지 빌드
./gradlew dockerBuildProd      # 운영용 Docker 이미지 빌드
./gradlew dockerComposeUp      # Docker Compose 시작 (개발 환경)
./gradlew dockerComposeUpProd  # Docker Compose 시작 (운영 환경)
./gradlew dockerComposeDown    # Docker Compose 중지
./gradlew dockerComposeRestart # Docker Compose 재시작

# 배포
./gradlew deployDev            # 개발 환경 배포 (빌드 + Docker Compose)
./gradlew deployProd           # 운영 환경 배포 (빌드 + Docker Compose)
./gradlew fullDeploy           # 전체 배포 (빌드 + Docker 이미지 + Docker Compose)
./gradlew ciBuild              # CI 전체 빌드

# 문제 해결
./gradlew verifySpringDocRemoval    # SpringDoc 완전 제거 확인
./gradlew setupApiDocumentation     # API 문서화 대안 설정
./gradlew cleanDependencies         # Gradle 의존성 캐시 정리
```

## 🔧 문제 해결

### Docker 빌드 문제

만약 Docker 빌드에서 `./gradlew: not found` 오류가 발생한다면:

1. **Gradle Wrapper 실행 권한 확인**:
   ```bash
   chmod +x gradlew
   ```

2. **로컬 빌드 사용**:
   ```bash
   ./gradlew bootJar
   java -jar build/libs/korean-fortune-app.jar --spring.profiles.active=dev
   ```

3. **Docker 설치 확인**:
   ```bash
   docker --version
   ```

### Gradle 태스크 오류

만약 Gradle 태스크에서 오류가 발생한다면:

1. **Gradle 캐시 정리**:
   ```bash
   ./gradlew clean
   ```

2. **의존성 새로고침**:
   ```bash
   ./gradlew --refresh-dependencies
   ```

3. **Gradle Wrapper 업데이트**:
   ```bash
   ./gradlew wrapper --gradle-version 8.4
   ```

### SpringDoc 관련 문제

SpringDoc/Swagger 호환성 문제로 인해 자체 API 문서화 시스템을 사용합니다:

1. **API 문서 확인**:
   ```bash
   curl http://localhost:8080/api/docs
   ```

2. **API 테스트**:
   ```bash
   open http://localhost:8080/api/docs/test
   ```

## 📁 프로젝트 구조

```
korean-fortune-system/
├── src/                    # 소스 코드
│   ├── main/java/         # Java 소스
│   │   ├── com/fortune/
│   │   │   ├── controller/    # REST API 컨트롤러
│   │   │   ├── service/       # 비즈니스 로직
│   │   │   ├── dto/           # 데이터 전송 객체
│   │   │   ├── entity/        # JPA 엔티티
│   │   │   ├── repository/    # 데이터 접근 계층
│   │   │   ├── config/        # 설정 클래스
│   │   │   └── security/      # 보안 설정
│   │   └── resources/     # 설정 파일 및 정적 리소스
│   └── test/java/         # 테스트 코드
├── docker/                # Docker 관련 파일
├── database/              # 데이터베이스 스크립트
├── docs/                  # 문서
├── build.gradle           # Gradle 빌드 스크립트
├── Dockerfile             # Docker 이미지 빌드 파일
└── README.md              # 프로젝트 문서
```

## 🛠️ 기술 스택

### Backend
- **Framework**: Spring Boot 3.5.3
- **Language**: Java 21
- **Build Tool**: Gradle 8.4
- **Security**: Spring Security, JWT
- **Database**: H2 (개발), PostgreSQL (운영)
- **ORM**: Spring Data JPA
- **Cache**: Caffeine
- **AI**: Spring AI (OpenAI 연동)

### Documentation & Testing
- **API Documentation**: Spring REST Docs, 자체 API 문서화
- **Testing**: JUnit 5, Spring Boot Test, TestContainers
- **Monitoring**: Spring Boot Actuator

### DevOps
- **Container**: Docker, Docker Compose
- **CI/CD**: Gradle 태스크 기반 자동화

## 🔄 최근 변경사항

### v2.6.0 (2025-01-05)
- ✅ 개발 환경에서 이메일 발송 기능 활성화
- ✅ 개발 환경에서 텔레그램 발송 기능 활성화
- ✅ 환경변수 기반 설정으로 보안 강화
- ✅ 이메일 SMTP 설정 최적화 (타임아웃 추가)
- ✅ 텔레그램 봇 토큰 환경변수 지원
- ✅ README.md 환경변수 설정 가이드 추가

### v2.5.0 (2025-07-05)
- ✅ SpringDoc/Swagger 제거 및 자체 API 문서화 시스템 구현
- ✅ Spring Boot 3.5.3 호환성 문제 해결
- ✅ API 테스트 페이지 추가
- ✅ 홈페이지 개선 및 API 문서 링크 추가
- ✅ Gradle 태스크 자동화 개선
- ✅ Docker Compose 자동화 태스크 추가

### v2.4.0 (2025-06-24)
- ✅ AI 기반 운세 해석 기능 추가
- ✅ 토정비결 64괘 운세 구현
- ✅ 별자리 운세 기능 추가
- ✅ 간지달력 조회 기능 구현

## 📄 라이선스

Apache License 2.0

## 🤝 기여하기

1. Fork the Project
2. Create your Feature Branch (`git checkout -b feature/AmazingFeature`)
3. Commit your Changes (`git commit -m 'Add some AmazingFeature'`)
4. Push to the Branch (`git push origin feature/AmazingFeature`)
5. Open a Pull Request

## 📞 문의

- **이메일**: admin@jyha.net
- **GitHub**: https://github.com/nanamix/korean-fortune-system

---

**🔮 한국형 만세력 운세 시스템** - 전통과 현대가 만나는 운세 서비스
