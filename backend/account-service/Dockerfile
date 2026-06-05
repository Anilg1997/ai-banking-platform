FROM maven:3.9.9-eclipse-temurin-21-alpine AS build
WORKDIR /app
COPY account-service/pom.xml /app/account-service/pom.xml
COPY pom.xml /app/pom.xml
RUN --mount=type=cache,target=/root/.m2 mvn -pl account-service -am dependency:resolve -DskipTests -q
COPY . /app
RUN --mount=type=cache,target=/root/.m2 mvn -pl account-service package -DskipTests -q

FROM eclipse-temurin:21-jre-alpine
WORKDIR /app
COPY --from=build /app/account-service/target/*.jar app.jar
EXPOSE 8083
ENTRYPOINT ["java", "-jar", "app.jar"]
