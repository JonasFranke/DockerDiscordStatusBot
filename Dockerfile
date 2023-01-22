FROM arm32v7/openjdk:11-ea-16-jdk-slim

RUN mkdir -p /app
COPY build/libs/*.jar /app/app.jar
WORKDIR /app
CMD ["java", "-jar", "app.jar"]