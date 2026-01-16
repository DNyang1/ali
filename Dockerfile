FROM openjdk:21-jdk-slim

# 작업 폴더 생성
WORKDIR /app

# JAR 복사
COPY build/libs/*.jar app.jar

# 실행 명령어
ENTRYPOINT ["java", "-Xmx512m", "-jar", "app.jar"]