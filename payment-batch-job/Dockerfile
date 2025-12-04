# Multi-stage build for Payment Batch Job
FROM maven:3.9-eclipse-temurin-21 AS builder

# Set working directory
WORKDIR /app

# Copy pom.xml and download dependencies
COPY pom.xml .
RUN mvn dependency:go-offline -B

# Copy source code and build
COPY src ./src
RUN mvn clean package -DskipTests

# Runtime stage
FROM eclipse-temurin:21-jre

# Create logs directory
RUN mkdir -p /logs

# Set working directory
WORKDIR /app

# Copy JAR from builder stage
COPY --from=builder /app/target/payment-batch-job.jar app.jar

# Run the batch job
ENTRYPOINT ["java", "-jar", "app.jar"]
