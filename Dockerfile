FROM eclipse-temurin:latest

COPY build/libs/*.jar /app.jar
WORKDIR /
CMD ["java", "-jar", "app.jar"]