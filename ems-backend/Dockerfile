FROM maven:3.9.9-amazoncorretto-21-debian AS build
WORKDIR /app
COPY pom.xml .
COPY src ./src
RUN mvn clean package -DskipTests

FROM amazoncorretto:21
WORKDIR /app

COPY --from=build /app/target/*.jar app.jar

RUN mkdir -p /app/logs

ENTRYPOINT ["java", "-jar", "app.jar"]