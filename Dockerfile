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

EXPOSE 8081
ENTRYPOINT ["java", "-jar", "app.jar"]
