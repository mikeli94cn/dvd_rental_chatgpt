FROM maven:3.9-eclipse-temurin-21 AS build
WORKDIR /build
COPY pom.xml .
COPY src ./src
COPY HealthCheck.java ./HealthCheck.java
RUN --mount=type=cache,target=/root/.m2 mvn -B -ntp -DskipTests package && javac HealthCheck.java

FROM eclipse-temurin:21-jre-jammy
WORKDIR /app
RUN groupadd --system rental && useradd --system --gid rental rental
COPY --from=build /build/target/dvd-rental-0.1.0-SNAPSHOT.jar /app/application.jar
COPY --from=build /build/HealthCheck.class /app/HealthCheck.class
USER rental
EXPOSE 8084
ENV JAVA_TOOL_OPTIONS="-XX:MaxRAMPercentage=70 -XX:+ExitOnOutOfMemoryError"
HEALTHCHECK --interval=15s --timeout=5s --start-period=90s --retries=6 CMD ["java", "-cp", "/app", "HealthCheck"]
ENTRYPOINT ["java", "-jar", "/app/application.jar"]
