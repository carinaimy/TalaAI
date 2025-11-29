# Multi-stage build for User Service
FROM eclipse-temurin:21-jdk-alpine AS builder

WORKDIR /app

# Copy parent pom and common modules first (for better caching)
COPY pom.xml ./
COPY shared/common-core ./shared/common-core

# Copy service files
COPY services/user-service/pom.xml ./services/user-service/
COPY services/user-service/src ./services/user-service/src

# Build the application
RUN apk add --no-cache maven && \
    mvn clean package -DskipTests -f services/user-service/pom.xml

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
