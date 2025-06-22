# Dockerfile
FROM openjdk:17-jdk-slim

VOLUME /tmp

COPY build/libs/korean-fortune-system-1.0.0.jar /korean-fortune-system.jar
EXPOSE 8080

ENV TZ=Asia/Seoul
RUN ln -snf /usr/share/zoneinfo/$TZ /etc/localtime && echo $TZ > /etc/timezone

ENTRYPOINT ["java", "-Djava.security.egd=file:/dev/./urandom", "-jar", "/korean-fortune-system.jar"]
