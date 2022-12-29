FROM openjdk:17
RUN mkdir /app
COPY build/libs/*.jar /app/app.jar
WORKDIR /app
CMD ["java", "-jar", "app.jar"]