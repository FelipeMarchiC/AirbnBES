# Etapa de build com Java 21
FROM maven:3.9.6-eclipse-temurin-21 AS build
WORKDIR /app
COPY . .
RUN mvn -pl web -am clean package -DskipTests

# Etapa de execução com Java 21
FROM eclipse-temurin:21-jdk
WORKDIR /app
COPY --from=build /app/web/target/*.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]
