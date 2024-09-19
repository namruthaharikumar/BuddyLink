# Stage 1: Build the application
LABEL author="Namrutha"
FROM maven:3.8.4-openjdk-17 AS build
WORKDIR /app
COPY pom.xml .
COPY src ./src
RUN mvn clean package -DskipTests

# Stage 2: Run the application
FROM openjdk:17-jdk-slim
WORKDIR /app
COPY --from=build /app/target/beafriend-1.0-SNAPSHOT.jar /app/beafriend-1.0-SNAPSHOT.jar
EXPOSE 8080
ENTRYPOINT ["java", "-Xmx128m", "-jar", "/app/beafriend-1.0-SNAPSHOT.jar"]