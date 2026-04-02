FROM eclipse-temurin:21-jdk-alpine

WORKDIR /app

COPY .mvn/ .mvn/
COPY mvnw ./
COPY pom.xml ./

RUN chmod +x mvnw && sed -i 's/\r$//' mvnw

COPY src src

RUN ./mvnw clean package -DskipTests && \
    cp target/*.jar app.jar

WORKDIR /app/target

EXPOSE 8080

ENTRYPOINT ["java", "-jar", "app.jar"]
