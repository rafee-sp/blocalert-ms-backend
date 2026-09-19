FROM maven:3.9-eclipse-temurin-25 AS build
WORKDIR /build

COPY pom.xml ./pom.xml

RUN mvn -N install -DskipTests

COPY shared-events ./shared-events
RUN cd shared-events && mvn clean install -DskipTests

COPY notification-service ./notification-service
RUN cd notification-service && mvn clean package -DskipTests

FROM eclipse-temurin:25-jre-alpine
WORKDIR /app
ADD https://github.com/open-telemetry/opentelemetry-java-instrumentation/releases/download/v2.31.1/opentelemetry-javaagent.jar /otel/opentelemetry-javaagent.jar
COPY --from=build /build/notification-service/target/*.jar app.jar
EXPOSE 8085
ENTRYPOINT ["java", "-javaagent:/otel/opentelemetry-javaagent.jar", "-jar", "app.jar"]