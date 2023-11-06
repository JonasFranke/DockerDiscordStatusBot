FROM openjdk:21
RUN cp /usr/share/zoneinfo/Europe/Berlin /etc/localtime
RUN mkdir /app
COPY build/libs/*.jar /app/app.jar
WORKDIR /app
CMD ["java", "-jar", "app.jar"]