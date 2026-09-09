# ==========================================
# Stage 1: Build & Package the Application
# ==========================================
FROM maven:3.9-eclipse-temurin-21-alpine AS builder

WORKDIR /app

# 1. Copy POM file to leverage Docker layer caching
COPY pom.xml .

# 2. Download project dependencies offline
RUN mvn dependency:go-offline -B

# 3. Copy source code and build executable JAR
COPY src ./src
RUN mvn clean package -DskipTests

# ==========================================
# Stage 2: Minimal Lightweight Runtime
# ==========================================
FROM eclipse-temurin:21-jre-alpine

WORKDIR /app

# Create and switch to a non-root user for security
RUN addgroup -S spring && adduser -S spring -G spring
USER spring:spring

# Copy only the built JAR from the builder stage
COPY --from=builder /app/target/*.jar app.jar

EXPOSE 8080

# Run with container-aware JVM memory flags
ENTRYPOINT ["java", "-XX:+UseG1GC", "-XX:MaxRAMPercentage=75.0", "-jar", "app.jar"]