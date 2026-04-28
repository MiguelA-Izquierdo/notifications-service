FROM maven:3.9-eclipse-temurin-17-alpine AS builder
WORKDIR /app

# Step 1: resolve dependencies — this layer is cached as long as pom.xml doesn't change
COPY pom.xml .
RUN mvn dependency:go-offline -q

# Step 2: compile — only re-runs when source code changes
COPY src ./src
RUN mvn package -DskipTests -q

FROM eclipse-temurin:17-jre-alpine
WORKDIR /app
COPY --from=builder /app/target/*.jar app.jar
ENTRYPOINT ["java", "-jar", "app.jar"]