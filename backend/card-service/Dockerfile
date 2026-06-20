FROM maven:3.9.9-eclipse-temurin-21-alpine AS build
WORKDIR /app
ARG SERVICE=card-service
COPY pom.xml /app/pom.xml
COPY auth-service/pom.xml /app/auth-service/pom.xml
COPY account-service/pom.xml /app/account-service/pom.xml
COPY transaction-service/pom.xml /app/transaction-service/pom.xml
COPY notification-service/pom.xml /app/notification-service/pom.xml
COPY api-gateway/pom.xml /app/api-gateway/pom.xml
COPY card-service/pom.xml /app/card-service/pom.xml
RUN mvn -pl ${SERVICE} -am dependency:resolve -DskipTests -q
COPY . /app
RUN mvn -pl ${SERVICE} package -DskipTests -q

FROM eclipse-temurin:21-jre-alpine
WORKDIR /app
ARG SERVICE=card-service
COPY --from=build /app/${SERVICE}/target/*.jar app.jar
EXPOSE 8089
ENTRYPOINT ["java", "-jar", "app.jar"]
