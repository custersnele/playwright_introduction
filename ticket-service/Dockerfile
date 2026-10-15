FROM maven:3.9-eclipse-temurin-21 AS build
WORKDIR /build
COPY pom.xml .
RUN mvn -q -B dependency:go-offline
COPY src ./src
RUN mvn -q -B package -DskipTests

FROM eclipse-temurin:21-jre-alpine
WORKDIR /app
COPY --from=build /build/target/ticket-service.jar app.jar
EXPOSE 8080
ENV JAVA_TOOL_OPTIONS="-Xmx128m -Djava.awt.headless=true"
CMD ["java", "-jar", "app.jar"]
