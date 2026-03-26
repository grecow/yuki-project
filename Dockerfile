FROM openjdk:19-jdk-slim

WORKDIR /app
COPY . .
RUN chmod +x gradlew && ./gradlew clean build -x test

RUN groupadd -r spring && useradd -r -g spring spring
USER spring:spring

EXPOSE 8080
HEALTHCHECK --interval=30s --timeout=5s CMD curl -f http://localhost:8080/actuator/health || exit 1

ENTRYPOINT ["java", "-jar", "/app/build/libs/realtimeapi-0.0.1.war"]
