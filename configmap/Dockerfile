# 사용할 Java 이미지
FROM openjdk:17

# JAR 파일 경로 (Gradle 빌드 시 생성되는 위치)
ARG JAR_FILE=build/libs/*.jar

# JAR 복사
COPY ${JAR_FILE} app.jar

# 실행 명령어
ENTRYPOINT ["java", "-jar", "/app.jar"]