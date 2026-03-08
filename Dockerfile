# 🔮 한국형 만세력 운세 시스템 - Dockerfile
# 멀티 스테이지 빌드를 사용하여 최적화된 프로덕션 이미지를 생성합니다.
# Amazon Corretto 17 (Java LTS, Amazon Linux 2023 기반) 사용

# ==================== 빌드 스테이지 ====================
FROM amazoncorretto:17 AS builder

# 메타데이터
LABEL maintainer="Korean Fortune Team <admin@jyha.net>"
LABEL version="2.6.0"
LABEL description="Korean Traditional Fortune Telling System"

# 작업 디렉토리 설정
WORKDIR /build

# 시스템 의존성 설치 (Amazon Linux 2023)
RUN dnf install -y curl git && \
    dnf clean all

# Gradle Wrapper 파일들 먼저 복사
COPY gradlew ./
COPY gradle gradle
COPY gradle.properties ./
COPY build.gradle ./
COPY settings.gradle ./

# Gradle wrapper 실행 권한 부여 및 테스트
RUN chmod +x gradlew && \
    ./gradlew --version

# 전체 프로젝트 복사
COPY . .

# 애플리케이션 빌드
RUN ./gradlew bootJar --no-daemon -x test

# JAR 파일 이름 확인 및 표준화
RUN ls -la build/libs/ && \
    mv build/libs/*.jar build/libs/app.jar

# ==================== 런타임 스테이지 ====================
FROM amazoncorretto:17 AS runtime

# 메타데이터
LABEL maintainer="Korean Fortune Team <admin@jyha.net>"
LABEL version="2.6.0"
LABEL description="Korean Traditional Fortune Telling System - Runtime"

# 보안을 위한 비특권 사용자 생성 (Amazon Linux 2023)
RUN groupadd -g 1001 fortune && \
    useradd -u 1001 -g fortune -m fortune

# 필수 런타임 도구 설치 (Amazon Linux 2023)
RUN dnf install -y curl tzdata dumb-init && \
    dnf clean all

# 타임존 설정
ENV TZ=Asia/Seoul
RUN ln -snf /usr/share/zoneinfo/$TZ /etc/localtime && echo $TZ > /etc/timezone

# 작업 디렉토리 생성 및 권한 설정
WORKDIR /app
RUN chown -R fortune:fortune /app

# 빌드된 JAR 파일 복사
COPY --from=builder --chown=fortune:fortune /build/build/libs/app.jar /app/app.jar

# 설정 디렉토리 생성
RUN mkdir -p /app/config /app/logs && \
    chown -R fortune:fortune /app/config /app/logs

# JVM 최적화 설정 (Amazon Corretto JDK 17 최적화)
ENV JAVA_OPTS="-XX:+UseZGC \
               -XX:+UseContainerSupport \
               -XX:MaxRAMPercentage=75.0 \
               -XX:+HeapDumpOnOutOfMemoryError \
               -XX:HeapDumpPath=/app/logs/ \
               -XX:+ExitOnOutOfMemoryError \
               -Djava.security.egd=file:/dev/./urandom \
               -Dspring.backgroundpreinitializer.ignore=true"

# 애플리케이션 설정
ENV SPRING_PROFILES_ACTIVE=prod
ENV SERVER_PORT=8080

# 포트 노출
EXPOSE 8080

# 헬스체크 설정
HEALTHCHECK --interval=30s --timeout=3s --start-period=60s --retries=3 \
    CMD curl -f http://localhost:8080/actuator/health || exit 1

# 사용자 전환
USER fortune

# 실행 명령 (dumb-init를 사용하여 PID 1 문제 해결)
ENTRYPOINT ["dumb-init", "--"]
CMD ["sh", "-c", "java $JAVA_OPTS -jar /app/app.jar"]

# ==================== 개발 스테이지 (선택적) ====================
FROM runtime AS development

# 개발 도구 추가 설치 (Amazon Linux 2023)
USER root
RUN dnf install -y vim-enhanced htop procps-ng && \
    dnf clean all

# 개발용 환경 변수
ENV SPRING_PROFILES_ACTIVE=dev
ENV SPRING_DEVTOOLS_RESTART_ENABLED=true
ENV SPRING_DEVTOOLS_LIVERELOAD_ENABLED=true

# 소스 코드 볼륨 마운트를 위한 디렉토리
RUN mkdir -p /app/src && chown -R fortune:fortune /app/src

USER fortune

# ==================== 멀티 아키텍처 지원 ====================
# docker buildx build --platform linux/amd64,linux/arm64 -t korean-fortune:latest .

ARG TARGETPLATFORM
ARG BUILDPLATFORM
RUN echo "Building on $BUILDPLATFORM for $TARGETPLATFORM"

# ==================== 빌드 인수 ====================
ARG BUILD_DATE
ARG VCS_REF
ARG VERSION=2.6.0

# 메타데이터 라벨
LABEL org.opencontainers.image.created=$BUILD_DATE \
      org.opencontainers.image.url="https://github.com/nanamix/korean-fortune-system" \
      org.opencontainers.image.source="https://github.com/nanamix/korean-fortune-system" \
      org.opencontainers.image.version=$VERSION \
      org.opencontainers.image.revision=$VCS_REF \
      org.opencontainers.image.vendor="Korean Fortune Team" \
      org.opencontainers.image.title="Korean Fortune System" \
      org.opencontainers.image.description="전통 사주팔자와 토정비결을 제공하는 한국형 운세 시스템" \
      org.opencontainers.image.licenses="Apache-2.0"
