# ==============================================================================
# Stage 1: Build Application with Maven & Temurin JDK 17
# ==============================================================================
FROM maven:3.9-eclipse-temurin-17-alpine AS builder

WORKDIR /build

# 1. Cache Maven dependencies separately from application source code
COPY pom.xml .
RUN mvn dependency:go-offline -B || true

# 2. Copy source code and package executable Spring Boot JAR
COPY src ./src
RUN mvn clean package -DskipTests -B

# ==============================================================================
# Stage 2: Production Runtime with Minimal Temurin JRE 17 (Alpine)
# ==============================================================================
FROM eclipse-temurin:17-jre-alpine AS runner

LABEL maintainer="Financial System Team" \
      description="Production image for Financial System Backend (Spring Boot 4 / Java 17)"

# Install essential packages:
# - tzdata: ensures timezone consistency (default: Asia/Ho_Chi_Minh)
# - curl: used for container health check probes
RUN apk add --no-cache tzdata curl

# Create dedicated non-root user and group for security hardening
RUN addgroup -g 10001 -S appgroup && \
    adduser -u 10001 -S appuser -G appgroup

WORKDIR /app

# Create necessary directories for local file uploads and logs with proper permissions
RUN mkdir -p /app/uploads/statements /app/logs && \
    chown -R appuser:appgroup /app

# Copy the repackaged executable JAR from the builder stage
COPY --from=builder --chown=appuser:appgroup /build/target/*.jar /app/app.jar

# Switch to non-root user
USER appuser:appgroup

# Configure default container environment variables
ENV SERVER_PORT=8080 \
    SPRING_PROFILES_ACTIVE=prod \
    TZ=Asia/Ho_Chi_Minh \
    JAVA_OPTS="-XX:+UseContainerSupport -XX:MaxRAMPercentage=75.0 -XX:+ExitOnOutOfMemoryError -Djava.security.egd=file:/dev/./urandom"

# Expose Spring Boot application port
EXPOSE 8080

# Declare volume for uploaded financial statement files to ensure data persistence
VOLUME ["/app/uploads"]

# Container health check using Spring Boot Actuator
HEALTHCHECK --interval=30s --timeout=5s --start-period=60s --retries=3 \
  CMD curl -f http://localhost:8080/actuator/health || exit 1

# Launch Spring Boot with exec so PID 1 properly receives and handles SIGTERM
ENTRYPOINT ["sh", "-c", "exec java $JAVA_OPTS -jar /app/app.jar"]

