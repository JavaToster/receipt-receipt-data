# Этап 1: Сборка приложения
FROM maven:3.9.5-eclipse-temurin-21 AS builder
WORKDIR /app

# Копируем файлы проекта и собираем JAR
COPY pom.xml .

RUN mvn dependency:go-offline -B

COPY src ./src
RUN mvn clean package -DskipTests

# Этап 2: Финальный образ
FROM eclipse-temurin:21-jdk-alpine
WORKDIR /app

# Копируем собранный JAR из предыдущего этапа
COPY --from=builder /app/target/*.jar app.jar

# Указываем порт, на котором работает Spring Boot
EXPOSE 8081

# Команда запуска
ENTRYPOINT ["java", "-jar", "app.jar"]
