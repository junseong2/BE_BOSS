# 1. Java 17 기반 이미지 사용
FROM openjdk:17-jdk-slim

# 2. 작업 디렉토리 설정
WORKDIR /app

# 3. 빌드된 .jar 파일을 컨테이너로 복사
COPY target/shop-0.0.1-SNAPSHOT.jar /shop.jar

# 4. Spring Boot 애플리케이션 실행
ENTRYPOINT ["java", "-jar", "/shop.jar", "--server.port=5000", "--server.address=0.0.0.0"]

# 5. 외부에 노출할 포트 설정 (Spring Boot 포트 5000)
EXPOSE 5000
