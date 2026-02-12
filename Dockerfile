FROM maven:3.9-eclipse-temurin-25 AS build

WORKDIR /app

COPY pom.xml pom.xml
RUN mvn dependency:go-offline -B

COPY src ./src
RUN mvn clean package -DskipTests

FROM eclipse-temurin:25-jre-alpine

WORKDIR /app

# might need to update this later when we have our explicit class names
COPY --from=build /app/target/*.jar app.jar
ENTRYPOINT ["java", "-jar", "app.jar"]
