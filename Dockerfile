# ================================
# Stage 1: Build Stage
# ================================
FROM maven:3.9-eclipse-temurin-23 AS builder

WORKDIR /build

# Copy pom.xml first for better dependency caching
COPY pom.xml .

# Download dependencies (this layer will be cached if pom.xml doesn't change)
RUN mvn dependency:go-offline -B

# Copy source code
COPY src ./src

# Build the application (skip tests for faster builds)
RUN mvn clean package -DskipTests -B

# ================================
# Stage 2: Runtime Stage
# ================================
FROM eclipse-temurin:23-jre-alpine

WORKDIR /app

# Create non-root user and group for security
RUN addgroup -S spring && adduser -S spring -G spring

# Create directories for data and logs
RUN mkdir -p /app/data /app/logs && \
    chown -R spring:spring /app

# Copy the built JAR from build stage
COPY --from=builder /build/target/*.jar app.jar

# Change ownership to spring user
RUN chown spring:spring app.jar

# Switch to non-root user
USER spring:spring

# Expose the application port
EXPOSE 8080

# Set optimal JVM flags for containers
ENV JAVA_OPTS="-XX:+UseContainerSupport \
    -XX:MaxRAMPercentage=75.0 \
    -XX:InitialRAMPercentage=50.0 \
    -XX:+ExitOnOutOfMemoryError \
    -XX:+UseG1GC \
    -XX:+UseStringDeduplication \
    -Djava.security.egd=file:/dev/./urandom"

# Health check using actuator endpoint
HEALTHCHECK --interval=30s --timeout=5s --start-period=60s --retries=3 \
  CMD wget --no-verbose --tries=1 --spider http://localhost:8080/actuator/health || exit 1

# Run the application
ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar app.jar"]
