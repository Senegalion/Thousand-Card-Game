# syntax=docker/dockerfile:1
# Backend image (Spring Boot 4.1.x). Build context: repository root.
# Stages: deps -> dev (compose.override.yaml) -> builder -> runner.
# The production stages (builder/runner) are finalized in the Build phase (ADR-008).

FROM eclipse-temurin:25-jdk AS deps
WORKDIR /workspace/backend
RUN apt-get update \
    && apt-get install -y --no-install-recommends curl \
    && rm -rf /var/lib/apt/lists/*
COPY backend/mvnw backend/pom.xml ./
COPY backend/.mvn ./.mvn
COPY backend/config ./config
RUN --mount=type=cache,target=/root/.m2 \
    ./mvnw -B -q dependency:go-offline

FROM deps AS dev
COPY backend/ ./
EXPOSE 8080
CMD ["./mvnw", "-B", "spring-boot:run"]

FROM deps AS builder
COPY backend/src ./src
RUN --mount=type=cache,target=/root/.m2 \
    ./mvnw -B -q package -DskipTests \
    && cp target/*.jar /workspace/app.jar

FROM eclipse-temurin:25-jre AS runner
WORKDIR /app
RUN groupadd --system app && useradd --system --gid app --no-create-home app
COPY --from=builder --chown=app:app /workspace/app.jar ./app.jar
USER app
EXPOSE 8080
ENTRYPOINT ["java", "-XX:MaxRAMPercentage=60", "-jar", "/app/app.jar"]
