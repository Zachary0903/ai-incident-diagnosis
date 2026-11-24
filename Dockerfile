# syntax=docker/dockerfile:1
# Shared image for all services. Build one with:  --build-arg MODULE=order-service
ARG MODULE

FROM maven:3.9.9-eclipse-temurin-17 AS build
ARG MODULE
WORKDIR /src
COPY . .
RUN --mount=type=cache,target=/root/.m2 \
    mvn -q -B -pl ${MODULE} -am package -DskipTests

FROM eclipse-temurin:17-jre
ARG MODULE
WORKDIR /app
ADD https://github.com/open-telemetry/opentelemetry-java-instrumentation/releases/download/v2.10.0/opentelemetry-javaagent.jar /otel/opentelemetry-javaagent.jar
COPY --from=build /src/${MODULE}/target/${MODULE}.jar /app/app.jar
ENTRYPOINT ["java", "-javaagent:/otel/opentelemetry-javaagent.jar", "-jar", "/app/app.jar"]
