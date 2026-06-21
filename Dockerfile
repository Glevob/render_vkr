FROM gradle:8.5-jdk21 AS build
WORKDIR /app

# Копирование файлов конфигурации
COPY gradlew .
COPY gradle gradle
COPY build.gradle .
COPY settings.gradle .

# Выдача скрипту gradlew права на выполнение в Linux
RUN chmod +x gradlew

COPY src src

RUN ./gradlew bootJar -x test

FROM eclipse-temurin:21-jdk-jammy
WORKDIR /app
COPY --from=build /app/build/libs/*.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]