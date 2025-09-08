# ---- Build stage ----
FROM eclipse-temurin:17-jdk-jammy AS build

WORKDIR /app

# Copy Maven wrapper and pom.xml first (for caching deps)
COPY pom.xml mvnw ./
COPY .mvn .mvn
RUN ./mvnw dependency:go-offline

# Copy source and build
COPY src src
RUN ./mvnw clean package -DskipTests

# ---- Runtime stage ----
FROM eclipse-temurin:17-jre-jammy
WORKDIR /app

# Copy built jar
COPY --from=build /app/target/*.jar app.jar

# Expose port from application.yml
EXPOSE 9090

ENTRYPOINT ["java", "-jar", "app.jar"]
