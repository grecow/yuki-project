FROM openjdk:19-jdk-slim
COPY . .
RUN ./gradlew clean build
RUN groupadd spring && adduser spring --ingroup spring
USER spring:spring
ENTRYPOINT ["java","-jar","/build/libs/realtimeapi-0.0.1.war"]
