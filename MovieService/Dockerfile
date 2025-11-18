# Multi-stage build for MovieService using Rocky Linux
FROM rockylinux:9 AS builder

# Install Maven and Java 11
RUN dnf update -y && \
    dnf install -y maven java-11-openjdk java-11-openjdk-devel && \
    dnf clean all

# Set working directory
WORKDIR /app

# Copy pom.xml first for better layer caching
COPY pom.xml .
COPY MovieService/pom.xml MovieService/

# Download dependencies (this layer will be cached if pom.xml doesn't change)
RUN mvn dependency:go-offline -f MovieService/pom.xml -B

# Copy source code
COPY MovieService/src MovieService/src
COPY MovieService/mvnw MovieService/
COPY MovieService/.mvn MovieService/.mvn

# Build the application
RUN mvn clean package -f MovieService/pom.xml -DskipTests -B

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
COPY --from=builder /app/MovieService/target/*.jar app.jar

# Change ownership
RUN chown appuser:appuser app.jar

# Switch to non-root user
USER appuser

# Expose port
EXPOSE 8081

# Health check (using wget as fallback if curl not available)
HEALTHCHECK --interval=30s --timeout=3s --start-period=40s --retries=3 \
    CMD wget --no-verbose --tries=1 --spider http://localhost:8081/actuator/health || exit 1

# Run the application
ENTRYPOINT ["java", "-jar", "app.jar"]

