FROM maven:3.9-eclipse-temurin-25 AS build

WORKDIR /app

COPY pom.xml pom.xml
RUN mvn dependency:go-offline -B

COPY src ./src
RUN mvn clean package -DskipTests

RUN mvn dependency:copy-dependencies -DoutputDirectory=target/deps -DincludeScope=runtime

FROM eclipse-temurin:25-jre-alpine

WORKDIR /app

COPY --from=build /app/target/deps/                         libs/

COPY --from=build /app/target/classes/org/juv25d/*.class    classes/org/juv25d/

COPY --from=build /app/target/classes/org/juv25d/util/      classes/org/juv25d/util/
COPY --from=build /app/target/classes/org/juv25d/logging/   classes/org/juv25d/logging/

COPY --from=build /app/target/classes/org/juv25d/http/      classes/org/juv25d/http/
COPY --from=build /app/target/classes/org/juv25d/plugin/    classes/org/juv25d/plugin/
COPY --from=build /app/target/classes/org/juv25d/router/    classes/org/juv25d/router/

COPY --from=build /app/target/classes/org/juv25d/handler/   classes/org/juv25d/handler/
COPY --from=build /app/target/classes/org/juv25d/filter/    classes/org/juv25d/filter/

COPY --from=build /app/src/main/resources/                  resources/

ENTRYPOINT ["java", "-cp", "classes:libs/*:resources","org.juv25d.App"]
