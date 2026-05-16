# Build stage
FROM eclipse-temurin:21-jdk-jammy AS builder
WORKDIR /workspace

COPY gradlew gradle.properties settings.gradle ./
COPY gradle ./gradle
COPY app/build.gradle app/build.gradle
COPY app/src app/src

RUN chmod +x ./gradlew
RUN ./gradlew -p app clean installDist --no-daemon

# Runtime stage
FROM eclipse-temurin:21-jre-jammy
WORKDIR /app

COPY --from=builder /workspace/app/build/install/app /app

# Optional defaults - override at runtime.
ENV GOOGLE_CREDENTIALS_PATH="/app/credentials.json"
ENV COMMAND_PREFIX="!"

ENTRYPOINT ["./bin/app"]
