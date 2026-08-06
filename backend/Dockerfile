# Build Stage
FROM eclipse-temurin:17-jdk AS builder

WORKDIR /app

# Gradle 설정 파일 먼저 복사
COPY build.gradle settings.gradle gradlew ./
COPY gradle ./gradle

# Gradle Wrapper 실행 권한 부여
RUN chmod +x ./gradlew

# 소스 코드 복사
COPY src ./src

# Spring Boot 실행 JAR 생성
RUN ./gradlew clean bootJar --no-daemon


# Run Stage
FROM eclipse-temurin:17-jre

WORKDIR /app

# 빌드된 JAR 파일 복사
COPY --from=builder /app/build/libs/*.jar app.jar

EXPOSE 8090

ENTRYPOINT ["java", "-jar", "app.jar"]