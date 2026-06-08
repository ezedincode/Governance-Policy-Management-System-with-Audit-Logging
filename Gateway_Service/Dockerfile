FROM maven:3.9.6-eclipse-temurin-21 AS build
WORKDIR /app
COPY . .
RUN mvn clean package -DskipTests -pl Gateway_Service -am
FROM eclipse-temurin:21-jre-jammy
WORKDIR /app
COPY --from=build /app/Gateway_Service/target/Gateway_Service-0.0.1-SNAPSHOT.jar app.jar
EXPOSE 8088
ENTRYPOINT ["java", "-jar", "app.jar"]
