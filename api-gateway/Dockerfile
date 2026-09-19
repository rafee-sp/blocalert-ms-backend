FROM maven:3.9-eclipse-temurin-25 AS build
WORKDIR /build

COPY pom.xml ./pom.xml
RUN mvn -N install -DskipTests

COPY api-gateway ./api-gateway
RUN cd api-gateway && mvn clean package -DskipTests

FROM eclipse-temurin:25-jre-alpine
RUN apk add --no-cache wget
WORKDIR /app
ADD https://github.com/open-telemetry/opentelemetry-java-instrumentation/releases/download/v2.31.1/opentelemetry-javaagent.jar /otel/opentelemetry-javaagent.jar
COPY --from=build /build/api-gateway/target/*.jar app.jar
EXPOSE 8081
ENTRYPOINT ["java", "-javaagent:/otel/opentelemetry-javaagent.jar", "-jar", "app.jar"]