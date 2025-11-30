# syntax=docker/dockerfile:1.4
# Multi-stage build for User Service
# Optimized for Docker layer caching with BuildKit cache mounts
# Build from root: cd ../.. && DOCKER_BUILDKIT=1 docker build -f services/user-service/Dockerfile -t tala/user-service .
FROM eclipse-temurin:21-jdk-alpine AS builder

WORKDIR /app

# Install Maven
RUN apk add --no-cache maven

# LAYER 1: Copy only POM files for dependency resolution
COPY pom.xml ./
COPY shared/common-core/pom.xml ./shared/common-core/
COPY services/user-service/pom.xml ./services/user-service/

# Download dependencies (cached unless pom.xml changes)
# Use BuildKit cache mount for Maven repository
RUN --mount=type=cache,target=/root/.m2 \
    mvn dependency:go-offline -pl services/user-service -am || true

# LAYER 2: Copy shared module source
COPY shared/common-core ./shared/common-core

# LAYER 3: Copy service source code
COPY services/user-service/src ./services/user-service/src

# Build the application (only rebuilds if source changed)
# Use BuildKit cache mount for Maven repository
RUN --mount=type=cache,target=/root/.m2 \
    mvn clean package -DskipTests -pl services/user-service -am

# Runtime stage
FROM eclipse-temurin:21-jre-alpine

WORKDIR /app

# Create non-root user
RUN addgroup -g 1001 tala && \
    adduser -D -u 1001 -G tala tala

# Copy jar from builder
COPY --from=builder /app/services/user-service/target/*.jar app.jar

# Change ownership
RUN chown -R tala:tala /app

USER tala

# Health check
HEALTHCHECK --interval=30s --timeout=3s --start-period=40s --retries=3 \
    CMD wget --no-verbose --tries=1 --spider http://localhost:8084/actuator/health || exit 1

EXPOSE 8084

ENTRYPOINT ["java", "-XX:+UseContainerSupport", "-XX:MaxRAMPercentage=75.0", "-jar", "app.jar"]
