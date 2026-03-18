FROM eclipse-temurin:21-jdk-alpine

WORKDIR /app

COPY .mvn/ .mvn
COPY mvnw mvnw.cmd
COPY pom.xml .

RUN apk add --no-cache maven

RUN ./mvnw dependency:go-offline -B

COPY src src

RUN ./mvnw clean package -DskipTests

WORKDIR /app/target

EXPOSE 8080

ENTRYPOINT ["java", "-jar", "api-0.0.1-SNAPSHOT.jar"]
