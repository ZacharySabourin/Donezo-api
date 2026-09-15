# ==========================================
# Build Stage
# ==========================================
FROM maven:3.9-eclipse-temurin-17-alpine AS build

WORKDIR /app

# Copy Maven wrapper and dependency definitions first to leverage layer caching
COPY pom.xml .

# Download dependencies (cached unless pom.xml changes)
RUN mvn dependency:go-offline -B

# Copy source code and build the executable JAR
COPY src ./src
RUN mvn package -DskipTests -B

# ==========================================
# Runtime Stage
# ==========================================
FROM eclipse-temurin:17-jre-alpine

WORKDIR /app

# Create a non-root user for security best practices
RUN addgroup -S appgroup && adduser -S appuser -G appgroup

# Copy the built JAR file from the build stage
COPY --from=build /app/target/donezo-api-*.jar app.jar

# Set ownership to the non-root user
RUN chown -R appuser:appgroup /app

USER appuser

# Expose default HTTP port
EXPOSE 10100

# Configure JVM options for container environments
ENTRYPOINT ["java", "-XX:+UseContainerSupport", "-XX:MaxRAMPercentage=75.0", "-jar", "app.jar"]