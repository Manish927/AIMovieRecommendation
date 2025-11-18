# Multi-stage build for RecommendationService using Rocky Linux
FROM rockylinux:9 AS builder

# Install Maven and Java 11
RUN dnf update -y && \
    dnf install -y maven java-11-openjdk java-11-openjdk-devel && \
    dnf clean all

# Set working directory
WORKDIR /app

# Copy pom.xml first for better layer caching
COPY pom.xml .
COPY recommendation/pom.xml recommendation/

# Download dependencies
RUN mvn dependency:go-offline -f recommendation/pom.xml -B

# Copy source code
COPY recommendation/src recommendation/src

# Build the application
RUN mvn clean package -f recommendation/pom.xml -DskipTests -B

# Runtime stage
FROM rockylinux:9

# Install Java 11 runtime and wget for health checks
RUN dnf update -y && \
    dnf install -y java-11-openjdk-headless wget && \
    dnf clean all && \
    rm -rf /var/cache/dnf

# Create app user for security
RUN useradd -m -u 1000 appuser && \
    mkdir -p /app && \
    chown -R appuser:appuser /app

# Set working directory
WORKDIR /app

# Copy JAR from builder stage
COPY --from=builder /app/recommendation/target/*.jar app.jar

# Change ownership
RUN chown appuser:appuser app.jar

# Switch to non-root user
USER appuser

# Expose port
EXPOSE 8083

# Health check
HEALTHCHECK --interval=30s --timeout=3s --start-period=40s --retries=3 \
    CMD wget --no-verbose --tries=1 --spider http://localhost:8083/actuator/health || exit 1

# Run the application
ENTRYPOINT ["java", "-jar", "app.jar"]

