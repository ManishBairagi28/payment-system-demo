# Multi-stage build for Payment Batch Job
FROM amazoncorretto:21-alpine3.20-jdk AS builder

# Install Maven
RUN apk add --no-cache maven

# Set working directory
WORKDIR /app

# Copy pom.xml and download dependencies
COPY pom.xml .
RUN mvn dependency:go-offline -B

# Copy source code and build
COPY src ./src
RUN mvn clean package -DskipTests

# Runtime stage
FROM eclipse-temurin:21-jre-alpine

# Create logs directory
RUN mkdir -p /logs

# Set working directory
WORKDIR /app

# Copy JAR from builder stage
COPY --from=builder /app/target/payment-batch-job.jar app.jar

# Run the batch job
ENTRYPOINT ["java", "-jar", "app.jar"]
