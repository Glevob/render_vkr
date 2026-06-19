# 1. Используем официальный образ Gradle для сборки проекта
FROM gradle:8.5-jdk17 AS build
WORKDIR /app

# Копируем файлы конфигурации Gradle для предварительного скачивания зависимостей (кэширование)
COPY gradlew .
COPY gradle gradle
COPY build.gradle .
COPY settings.gradle .

# Копируем исходный код приложения
COPY src src

# Собираем проект, пропуская тесты. В Gradle собранный файл падает в папку build/libs/
RUN ./gradlew bootJar -x test

# 2. Минимальный образ для запуска готового приложения
FROM eclipse-temurin:17-jdk-jammy
WORKDIR /app

# Копируем jar-файл (в Gradle он обычно называется *-SNAPSHOT.jar или просто по имени проекта)
COPY --from=build /app/build/libs/*.jar app.jar

EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]