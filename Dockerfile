FROM eclipse-temurin:19-alpine

COPY build/libs/*.jar /app.jar
WORKDIR /
CMD ["java", "-jar", "app.jar"]