# Multi-stage Dockerfile for Event Service
# Optimized for Apple Silicon (ARM64)

# Stage 1: Build
FROM maven:3.9-eclipse-temurin-21-alpine AS build
WORKDIR /app

# Copy parent POM
COPY ../../pom.xml ./pom.xml

# Copy shared modules
COPY ../../shared ./shared

# Copy event service
COPY . ./services/event-service

# Build
RUN mvn clean package -pl services/event-service -am -DskipTests

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
