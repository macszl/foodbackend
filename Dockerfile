FROM maven:3.9.6-amazoncorretto-17 as build
COPY . .
RUN mvn clean package -DskipTests -X

FROM openjdk:17-jdk-slim
COPY --from=build /target/foodbackend-0.0.1-SNAPSHOT.jar foodbackend.jar
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "foodbackend.jar"]
