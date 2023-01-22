FROM arm32v7/openjdk:11-ea-16-jdk-slim

COPY build/libs/*.jar /app.jar
WORKDIR /
CMD ["java", "-jar", "app.jar"]