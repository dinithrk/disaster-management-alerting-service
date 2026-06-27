# # Use Java runtime
# FROM eclipse-temurin:21-jdk-jammy

# # Set working directory inside container
# WORKDIR /app

# # Copy jar file into container
# COPY target/*.jar app.jar

# # Run the application
# ENTRYPOINT ["java", "-jar", "app.jar"]




# syntax=docker/dockerfile:1.7

# Build Stage

FROM maven:3.9.11-eclipse-temurin-21 AS builder

WORKDIR /app

# Copy pom first for dependency caching
COPY pom.xml .

RUN --mount=type=cache,target=/root/.m2 \
    mvn dependency:go-offline

# Copy source
COPY src ./src

# Build
RUN --mount=type=cache,target=/root/.m2 \
    mvn package -DskipTests


# Runtime Stage

FROM eclipse-temurin:21-jre-jammy

WORKDIR /app

COPY --from=builder /app/target/*.jar app.jar

ENTRYPOINT ["java","-jar","app.jar"]