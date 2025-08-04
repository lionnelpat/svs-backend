FROM eclipse-temurin:17-jdk AS builder
WORKDIR /app
COPY mvnw pom.xml ./
COPY .mvn .mvn
RUN ./mvnw dependency:go-offline

COPY . .
RUN ./mvnw clean package -DskipTests

# ---- Runtime image ----
FROM eclipse-temurin:17-jdk-jammy
WORKDIR /app
COPY --from=builder /app/target/*.jar app.jar

ARG SPRING_PROFILE=prod
ENV SPRING_PROFILES_ACTIVE=$SPRING_PROFILE
ENV CORS_ALLOWED_ORIGINS=https://svs-frontend.model-technologie.com,http://localhost:4200
ENV SERVER_ADDRESS=0.0.0.0

EXPOSE 8081
ENTRYPOINT ["java", "-jar", "app.jar"]
