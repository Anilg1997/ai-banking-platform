FROM maven:3.9.9-eclipse-temurin-21-alpine AS build
WORKDIR /app
COPY notification-service/pom.xml /app/notification-service/pom.xml
COPY pom.xml /app/pom.xml
RUN --mount=type=cache,target=/root/.m2 mvn -pl notification-service -am dependency:resolve -DskipTests -q
COPY . /app
RUN --mount=type=cache,target=/root/.m2 mvn -pl notification-service package -DskipTests -q

FROM eclipse-temurin:21-jre-alpine
WORKDIR /app
COPY --from=build /app/notification-service/target/*.jar app.jar
EXPOSE 8087
ENTRYPOINT ["java", "-jar", "app.jar"]
