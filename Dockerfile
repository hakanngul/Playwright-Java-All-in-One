# Multi-stage build for Playwright test framework
FROM maven:3.9.6-eclipse-temurin-21 AS builder

# Set working directory
WORKDIR /app

# Copy pom.xml and download dependencies
COPY pom.xml .
RUN mvn dependency:go-offline -B

# Copy source code and build
COPY src ./src
RUN mvn clean package -DskipTests

# Runtime stage
FROM mcr.microsoft.com/playwright/java:v1.54.0-jammy

# Install Java 21
RUN apt-get update && \
    apt-get install -y openjdk-21-jdk && \
    apt-get clean && \
    rm -rf /var/lib/apt/lists/*

# Set Java environment
ENV JAVA_HOME=/usr/lib/jvm/java-21-openjdk-amd64
ENV PATH=$JAVA_HOME/bin:$PATH

# Create app user
RUN groupadd -r appuser && useradd -r -g appuser appuser

# Set working directory
WORKDIR /app

# Copy built application
COPY --from=builder /app/target/core-playwright-*.jar app.jar
COPY --from=builder /app/target/lib ./lib
COPY src/test/resources ./resources

# Create directories for test outputs
RUN mkdir -p screenshots videos logs traces downloads && \
    chown -R appuser:appuser /app

# Switch to non-root user
USER appuser

# Install Playwright browsers
RUN npx playwright install --with-deps

# Set environment variables
ENV BROWSER_HEADLESS=true
ENV PARALLEL_EXECUTION=true
ENV THREAD_COUNT=2
ENV SCREENSHOT_ON_FAILURE=true
ENV VIDEO_RECORDING=false
ENV ENVIRONMENT=test

# Health check
HEALTHCHECK --interval=30s --timeout=10s --start-period=5s --retries=3 \
    CMD java -version || exit 1

# Default command
CMD ["java", "-jar", "app.jar"]

# Labels
LABEL maintainer="Playwright Framework Team"
LABEL version="1.0.0"
LABEL description="Playwright Test Automation Framework"
