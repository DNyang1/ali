# 1. Java 21 실행 환경 (Eclipse Temurin 사용)
FROM eclipse-temurin:21-jdk-jammy

# 2. 컨테이너 내 작업 폴더 생성
WORKDIR /app

# 3. 빌드된 JAR 파일을 컨테이너로 복사
COPY build/libs/*.jar app.jar

# 4. 실행 명령어 (메모리 제한 옵션 포함)
ENTRYPOINT ["java", "-Xmx512m", "-jar", "app.jar"]