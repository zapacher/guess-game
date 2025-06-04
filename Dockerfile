FROM gradle:8.4-jdk17 AS builder

WORKDIR /app

COPY . .

RUN ./gradlew bootJar

FROM openjdk:17-jdk-slim

WORKDIR /app

COPY --from=builder /app/build/libs/app.jar /app/app.jar

CMD ["java", "-jar", "/app/app.jar"]