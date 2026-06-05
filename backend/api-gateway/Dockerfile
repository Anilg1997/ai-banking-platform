FROM maven:3.9.9-eclipse-temurin-21-alpine AS build
WORKDIR /app
COPY api-gateway/pom.xml /app/api-gateway/pom.xml
COPY pom.xml /app/pom.xml
RUN --mount=type=cache,target=/root/.m2 mvn -pl api-gateway -am dependency:resolve -DskipTests -q
COPY . /app
RUN --mount=type=cache,target=/root/.m2 mvn -pl api-gateway package -DskipTests -q

FROM eclipse-temurin:21-jre-alpine
WORKDIR /app
COPY --from=build /app/api-gateway/target/*.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]
