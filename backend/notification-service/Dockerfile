FROM maven:3.9.9-eclipse-temurin-21-alpine AS build
WORKDIR /app
COPY pom.xml /app/pom.xml
COPY auth-service/pom.xml /app/auth-service/pom.xml
COPY account-service/pom.xml /app/account-service/pom.xml
COPY transaction-service/pom.xml /app/transaction-service/pom.xml
COPY notification-service/pom.xml /app/notification-service/pom.xml
COPY api-gateway/pom.xml /app/api-gateway/pom.xml
RUN mvn -pl notification-service -am dependency:resolve -DskipTests -q
COPY . /app
RUN mvn -pl notification-service package -DskipTests -q

FROM eclipse-temurin:21-jre-alpine
WORKDIR /app
COPY --from=build /app/notification-service/target/*.jar app.jar
EXPOSE 8087
ENTRYPOINT ["java", "-jar", "app.jar"]
