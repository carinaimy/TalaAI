# syntax=docker/dockerfile:1.4
# Multi-stage Dockerfile for Event Service
# Optimized for Docker layer caching with BuildKit cache mounts
# Build from root: cd ../.. && DOCKER_BUILDKIT=1 docker build -f services/event-service/Dockerfile -t tala/event-service .

# Stage 1: Build
FROM eclipse-temurin:21-jdk-alpine AS build
WORKDIR /app

# Install Maven
RUN apk add --no-cache maven

# LAYER 1: Copy only POM files for dependency resolution
COPY ../../pom.xml ./pom.xml
COPY ../../shared/common-core/pom.xml ./shared/common-core/
COPY pom.xml ./services/event-service/pom.xml

# Download dependencies (cached unless pom.xml changes)
# Use BuildKit cache mount for Maven repository
RUN --mount=type=cache,target=/root/.m2 \
    mvn dependency:go-offline -pl services/event-service -am || true

# LAYER 2: Copy shared module source
COPY ../../shared ./shared

# LAYER 3: Copy event service source
COPY src ./services/event-service/src

# Build (only rebuilds if source changed)
# Use BuildKit cache mount for Maven repository
RUN --mount=type=cache,target=/root/.m2 \
    mvn clean package -pl services/event-service -am -DskipTests

# Stage 2: Runtime
FROM eclipse-temurin:21-jre-alpine
WORKDIR /app

# Add non-root user
RUN addgroup -S spring && adduser -S spring -G spring
USER spring:spring

# Copy JAR
COPY --from=build /app/services/event-service/target/*.jar app.jar

# Health check
HEALTHCHECK --interval=30s --timeout=3s --start-period=40s --retries=3 \
  CMD wget --no-verbose --tries=1 --spider http://localhost:8081/actuator/health || exit 1

# JVM options for Apple Silicon
ENV JAVA_OPTS="-XX:+UseG1GC -XX:MaxRAMPercentage=75.0 -XX:InitialRAMPercentage=50.0 -XX:+UseStringDeduplication"

# Expose port
EXPOSE 8081

# Run
ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar app.jar"]
