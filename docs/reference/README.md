# 📚 레퍼런스 문서 인덱스

한국형 만세력 운세 시스템의 설치·개발·운영·계산 방법론을 다루는 레퍼런스 문서 모음입니다. 각 문서는 실제 소스(`build.gradle`, `Dockerfile`, `docker/*.yml`, `.env.example`, `.github/workflows/*`, `application*.yml`)를 근거로 작성되었습니다.

> 처음이라면 **[08. 설치 가이드](./08-installation-guide.md)** 부터 시작하세요.

---

## 문서 목록

| # | 문서 | 설명 |
|---|------|------|
| 01 | [프로젝트 개요](./01-project-overview.md) | 시스템 소개, 주요 기능, 기술 스택 개요 |
| 02 | [아키텍처](./02-architecture.md) | 계층 구조, 컴포넌트 구성, 데이터 흐름 |
| 03 | [사주 계산 방법론](./03-saju-calculation-methodology.md) | 사주팔자·간지·오행·십신·대운 계산 알고리즘 |
| 04 | [데이터 모델](./04-data-model.md) | JPA 엔티티·DTO·DB 스키마 |
| 05 | [API 레퍼런스](./05-api-reference.md) | REST API 엔드포인트·요청/응답 스펙 |
| 06 | [AI 및 Fallback](./06-ai-and-fallback.md) | OpenAI 호환 AI 포트·결정적 fallback 구조 |
| 07 | [보안 및 관측성](./07-security-and-observability.md) | JWT 인증, 보안 감사 로그, Actuator·Prometheus·OpenTelemetry 추적, 로그 패턴 |
| 08 | [설치 가이드](./08-installation-guide.md) | 사전 요구(JDK 21), 클론, 로컬 빌드·실행, 프로필·`.env`, 접속 URL, 트러블슈팅 |
| 09 | [사용자 가이드](./09-user-guide.md) | 웹 UI(`fortune-app.html`) 사용법, 입력값 의미, 결과 해석, 이메일/텔레그램 발송 |
| 10 | [개발 및 테스트](./10-development-and-testing.md) | 개발 세팅, 코드 진입점, Gradle 태스크, 테스트 실행·규약, 프로필별 실행 |
| 11 | [Docker 배포](./11-deployment-docker.md) | 멀티스테이지 이미지 빌드, Compose 스택(standalone/dev/prod), 환경변수, 헬스체크 |
| 12 | [CI/CD 및 운영](./12-cicd-and-operations.md) | GitHub Actions CI/CD 파이프라인, 배포 흐름, 운영 점검(health·로그) |
| 13 | [알림 연동 가이드](./13-notifications-guide.md) | 이메일(Gmail 앱 비밀번호·AWS SES SMTP·기타 SMTP), 텔레그램, Discord 설정 |

> 01·02·03·04·05·06 문서는 다른 작업자가 작성합니다. 07~12는 본 세트에서 제공됩니다.

---

## 빠른 참조

| 하고 싶은 것 | 문서 |
|--------------|------|
| 처음 설치하고 실행하기 | [08. 설치 가이드](./08-installation-guide.md) |
| 웹 UI로 운세 보기 | [09. 사용자 가이드](./09-user-guide.md) |
| 코드 수정·테스트 | [10. 개발 및 테스트](./10-development-and-testing.md) |
| Docker로 배포 | [11. Docker 배포](./11-deployment-docker.md) |
| CI/CD·운영 점검 | [12. CI/CD 및 운영](./12-cicd-and-operations.md) |
| 이메일(Gmail/SES)·텔레그램·Discord 발송 | [13. 알림 연동 가이드](./13-notifications-guide.md) |
| 인증·모니터링·추적 | [07. 보안 및 관측성](./07-security-and-observability.md) |
