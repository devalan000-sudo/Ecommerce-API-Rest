FROM eclipse-temurin:21-jdk-alpine

WORKDIR /app

COPY .mvn/ .mvn
COPY pom.xml ./

RUN chmod +x mvnw && sed -i 's/\r$//' mvnw

COPY src src

RUN ./mvnw clean package -DskipTests

WORKDIR /app/target

EXPOSE 8080

ENTRYPOINT ["java", "-jar", "api-0.0.1-SNAPSHOT.jar"]
