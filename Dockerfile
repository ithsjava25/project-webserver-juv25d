FROM maven:3.9-eclipse-temurin-25 AS build

WORKDIR /app

COPY pom.xml pom.xml
RUN mvn dependency:go-offline -B

COPY src ./src
RUN mvn clean package -DskipTests -Dmaven.shade.skip=true
RUN mvn dependency:copy-dependencies -DoutputDirectory=target/deps -DincludeScope=runtime

FROM eclipse-temurin:25-jre-alpine

WORKDIR /app

COPY --from=build /app/target/deps/     libs/
COPY --from=build /app/target/classes/  classes/

ENTRYPOINT ["java", "-cp", "classes:libs/*","org.juv25d.App"]
