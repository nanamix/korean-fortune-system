# 🏗️ 스프링 부트 만세력 시스템 소스 구조

## 📁 프로젝트 디렉토리 구조

```
korean-fortune-system/
├── src/
│   ├── main/
│   │   ├── java/
│   │   │   └── com/
│   │   │       └── fortune/
│   │   │           ├── KoreanFortuneApplication.java          # 메인 애플리케이션
│   │   │           ├── config/                               # 설정 클래스들
│   │   │           │   ├── CacheConfig.java                  # 캐시 설정
│   │   │           │   ├── SecurityConfig.java               # 보안 설정
│   │   │           │   └── AsyncConfig.java                  # 비동기 처리 설정
│   │   │           ├── controller/                           # REST 컨트롤러
│   │   │           │   ├── FortuneController.java            # 운세 API 컨트롤러
│   │   │           │   └── SystemController.java             # 시스템 상태 컨트롤러
│   │   │           ├── service/                              # 비즈니스 로직
│   │   │           │   ├── GanjiCalculatorService.java       # 간지 계산 핵심 엔진
│   │   │           │   ├── LunarCalendarService.java         # 음력 변환 서비스
│   │   │           │   ├── SinsalService.java                # 길신/흉신 계산
│   │   │           │   ├── DailyFortuneService.java          # 일일 운세 계산
│   │   │           │   ├── TojeongBigyeolService.java        # 토정비결 서비스
│   │   │           │   ├── GanjiCalendarService.java         # 간지달력 서비스
│   │   │           │   └── ZodiacFortuneService.java         # 별자리 운세 서비스
│   │   │           ├── entity/                               # JPA 엔티티
│   │   │           │   ├── User.java                         # 사용자 엔티티
│   │   │           │   └── SajuData.java                     # 사주 데이터 엔티티
│   │   │           ├── dto/                                  # 데이터 전송 객체
│   │   │           │   ├── SajuRequest.java                  # 사주 계산 요청 DTO
│   │   │           │   ├── SajuResult.java                   # 사주 계산 결과 DTO
│   │   │           │   ├── DailyFortuneResult.java           # 일일 운세 결과 DTO
│   │   │           │   ├── FortuneByCategory.java            # 분야별 운세 DTO
│   │   │           │   ├── FortuneCategory.java              # 운세 카테고리 DTO
│   │   │           │   ├── SinsalInfo.java                   # 신살 정보 DTO
│   │   │           │   ├── WuxingAnalysis.java               # 오행 분석 DTO
│   │   │           │   ├── TojeongRequest.java               # 토정비결 요청 DTO
│   │   │           │   ├── TojeongResult.java                # 토정비결 결과 DTO
│   │   │           │   ├── GanjiCalendarDay.java             # 간지달력 일별 DTO
│   │   │           │   ├── ZodiacRequest.java                # 별자리 요청 DTO
│   │   │           │   ├── ZodiacFortuneResult.java          # 별자리 운세 결과 DTO
│   │   │           │   ├── ZodiacFortune.java                # 별자리 운세 DTO
│   │   │           │   ├── LunarDate.java                    # 음력 날짜 DTO
│   │   │           │   └── ApiResponse.java                  # 공통 API 응답 DTO
│   │   │           ├── enums/                                # 열거형 클래스들
│   │   │           │   ├── Zodiac.java                       # 별자리 열거형
│   │   │           │   ├── WuxingElement.java                # 오행 원소 열거형
│   │   │           │   └── WuxingRelation.java               # 오행 관계 열거형
│   │   │           ├── exception/                            # 예외 처리
│   │   │           │   ├── GlobalExceptionHandler.java      # 전역 예외 처리기
│   │   │           │   └── FortuneCalculationException.java  # 커스텀 예외
│   │   │           ├── validation/                           # 입력 검증
│   │   │           │   ├── ValidBirthDate.java               # 생년월일 검증 어노테이션
│   │   │           │   └── BirthDateValidator.java           # 생년월일 검증 로직
│   │   │           └── health/                               # 헬스체크
│   │   │               └── FortuneHealthIndicator.java       # 커스텀 헬스 인디케이터
│   │   └── resources/
│   │       ├── application.yml                               # 메인 설정 파일
│   │       ├── application-dev.yml                           # 개발 환경 설정
│   │       ├── application-prod.yml                          # 운영 환경 설정
│   │       ├── application-test.yml                          # 테스트 환경 설정
│   │       └── data.sql                                      # 초기 데이터 스크립트
│   └── test/
│       └── java/
│           └── com/
│               └── fortune/
│                   ├── service/                              # 서비스 테스트
│                   │   ├── GanjiCalculatorServiceTest.java   # 간지 계산 테스트
│                   │   ├── DailyFortuneServiceTest.java      # 일일 운세 테스트
│                   │   └── LunarCalendarServiceTest.java     # 음력 변환 테스트
│                   └── controller/                           # 컨트롤러 테스트
│                       └── FortuneControllerTest.java        # API 테스트
├── database/
│   ├── schema.sql                                           # 데이터베이스 스키마
│   ├── data.sql                                             # 기본 데이터
│   └── init.sql                                             # 초기화 스크립트
├── docker/
│   ├── Dockerfile                                           # Docker 이미지 빌드
│   └── docker-compose.yml                                   # Docker Compose 설정
├── docs/
│   ├── API.md                                               # API 문서
│   └── DEPLOYMENT.md                                        # 배포 가이드
├── pom.xml                                                  # Maven 의존성 설정
└── README.md                                                # 프로젝트 설명서
```

---

## 📋 주요 클래스별 역할 및 기능

### 🎯 **Core Services (핵심 서비스)**

#### 1. **GanjiCalculatorService.java**
- **역할**: 간지 계산의 핵심 엔진
- **주요 기능**:
    - 년주/월주/일주/시주 계산
    - 태양시 보정 (경도 차이 + 서머타임)
    - 음력/양력 구분 처리
    - 완전한 사주팔자 생성

```java
// 주요 메서드
public SajuResult calculateCompleteSaju(int year, int month, int day, 
                                       int hour, int minute, String gender, String calendarType)
public String calculateYearPillar(int year)
public String calculateDayPillar(LocalDate date)
private LocalDateTime adjustToSolarTime(LocalDate date, int hour, int minute)
```

#### 2. **LunarCalendarService.java**
- **역할**: 음력-양력 변환
- **주요 기능**:
    - 음력을 양력으로 변환
    - 양력을 음력으로 역변환
    - 윤달 처리

```java
// 주요 메서드
public LocalDate convertLunarToSolar(int lunarYear, int lunarMonth, int lunarDay)
public LunarDate convertSolarToLunar(LocalDate solarDate)
```

#### 3. **SinsalService.java**
- **역할**: 길신/흉신 계산 및 해석
- **주요 기능**:
    - 천덕, 월덕, 천희 등 길신 계산
    - 월살, 일살, 흑도 등 흉신 계산
    - 신살별 상세 해석 제공

```java
// 주요 메서드
public List<SinsalInfo> calculateDailySinsals(LocalDate date, SajuResult saju)
private boolean isCheondeok(LocalDate date, String dayStem)
private boolean isWoldeok(LocalDate date, String dayStem)
```

#### 4. **DailyFortuneService.java**
- **역할**: 일일 운세 종합 계산
- **주요 기능**:
    - 오행 상생상극 분석
    - 분야별 운세 (종합/애정/재물/건강/직업)
    - 길방위/길한색깔 계산
    - 종합 점수 및 조언 생성

```java
// 주요 메서드
public DailyFortuneResult calculateDailyFortune(SajuResult saju, LocalDate targetDate)
private WuxingAnalysis analyzeWuxingRelation(SajuResult saju, String dayPillar)
private FortuneByCategory calculateCategoryFortune(SajuResult saju, LocalDate date, int baseScore)
```

### 🎨 **Specialized Services (전문 서비스)**

#### 5. **TojeongBigyeolService.java**
- **역할**: 토정비결 64괘 운세
- **주요 기능**:
    - 64괘 선택 알고리즘
    - 연간 운세 및 월별 세부 분석
    - 괘별 해석 및 조언

#### 6. **GanjiCalendarService.java**
- **역할**: 간지달력 생성
- **주요 기능**:
    - 월별 간지달력 생성
    - 절기 표시
    - 길일/흉일 판단

#### 7. **ZodiacFortuneService.java**
- **역할**: 서양 별자리 운세
- **주요 기능**:
    - 생일로부터 별자리 결정
    - 일일/주간/월간 별자리 운세
    - 별자리 궁합 분석

### 🌐 **API Layer (컨트롤러)**

#### 8. **FortuneController.java**
- **역할**: REST API 엔드포인트 제공
- **주요 엔드포인트**:
    - `POST /api/fortune/saju/calculate` - 사주 계산
    - `POST /api/fortune/daily/today` - 오늘 운세
    - `POST /api/fortune/daily/tomorrow` - 내일 운세
    - `POST /api/fortune/daily/{date}` - 지정일 운세
    - `POST /api/fortune/tojeong` - 토정비결
    - `GET /api/fortune/calendar/ganji/{year}/{month}` - 간지달력
    - `POST /api/fortune/zodiac` - 별자리 운세

### 💾 **Data Layer (엔티티 & DTO)**

#### 9. **Entity Classes**
```java
// User.java - 사용자 정보
- 생년월일시분, 성별, 양력/음력 구분

// SajuData.java - 계산된 사주 데이터
- 년주/월주/일주/시주, 일간, 보정시간
```

#### 10. **DTO Classes**
```java
// SajuRequest.java - 사주 계산 요청
- 완전한 입력 검증 (생년월일시분, 성별, 달력구분)

// SajuResult.java - 사주 계산 결과
- 사주팔자, 태양시 보정, 음력정보 포함

// DailyFortuneResult.java - 일일 운세 결과
- 종합점수, 분야별 운세, 신살정보, 길방위/색깔
```

### 🔧 **Configuration & Utilities**

#### 11. **Config Classes**
```java
// CacheConfig.java - 캐시 설정
// SecurityConfig.java - 보안 설정 (CORS, 인증)
// AsyncConfig.java - 비동기 처리 설정
```

#### 12. **Exception Handling**
```java
// GlobalExceptionHandler.java - 전역 예외 처리
// FortuneCalculationException.java - 커스텀 예외
```

#### 13. **Validation**
```java
// ValidBirthDate.java - 생년월일 검증 어노테이션
// BirthDateValidator.java - 검증 로직 구현
```

---

## 🧪 **테스트 데이터 기준**

**모든 테스트는 다음 데이터를 기준으로 작성됨:**
- **생년월일**: 1981년 3월 20일
- **생시**: 01시 59분 (축시)
- **성별**: 남성 (M)
- **달력**: 양력 (SOLAR)

**계산 결과:**
- **년주**: 신유 (1981년)
- **월주**: 신묘 (3월)
- **일주**: 을미 (3월 20일)
- **시주**: 정축 (01:59 → 태양시 보정 후 01:27 → 축시)
- **일간**: 을목 (부드럽고 유연한 성격)

---

## 🚀 **빌드 및 실행**

```bash
# 프로젝트 빌드
mvn clean package

# 개발 환경 실행
mvn spring-boot:run

# Docker 실행
docker-compose up -d

# 테스트 실행
mvn test
```

---

## 📱 **API 사용 예시**

```bash
# 사주팔자 계산
curl -X POST http://localhost:8080/api/fortune/saju/calculate \
  -H "Content-Type: application/json" \
  -d '{
    "birthYear": 1981, "birthMonth": 3, "birthDay": 20,
    "birthHour": 1, "birthMinute": 59,
    "gender": "M", "calendarType": "SOLAR"
  }'

# 오늘의 운세
curl -X POST http://localhost:8080/api/fortune/daily/today \
  -H "Content-Type: application/json" \
  -d '{ 동일한 생일 정보 }'
```
