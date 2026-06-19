FROM gradle:8.5-jdk21 AS build
WORKDIR /app

# Копируем сначала файлы конфигурации
COPY gradlew .
COPY gradle gradle
COPY build.gradle .
COPY settings.gradle .

# КОМАНДА-СПАСИТЕЛЬ: Выдаем скрипту gradlew права на выполнение в Linux
RUN chmod +x gradlew

# Копируем исходный код
COPY src src

# Теперь команда отработает без ошибок
RUN ./gradlew bootJar -x test

FROM eclipse-temurin:21-jdk-jammy
WORKDIR /app
COPY --from=build /app/build/libs/*.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]