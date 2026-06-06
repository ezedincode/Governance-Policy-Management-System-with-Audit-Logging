FROM maven:3.9.6-eclipse-temurin-21 AS build
WORKDIR /app
COPY . .
RUN mvn clean package -DskipTests -pl Discovery_Service -am
FROM eclipse-temurin:21-jre-jammy
WORKDIR /app
COPY --from=build /app/Discovery_Service/target/Discovery_Service-0.0.1-SNAPSHOT.jar app.jar
EXPOSE 8761
ENTRYPOINT ["java", "-jar", "app.jar"]
