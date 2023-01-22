FROM openjdk:17
RUN apt-get update && apt-get install -y box86
RUN cp /usr/share/zoneinfo/Europe/Berlin /etc/localtime
RUN mkdir /app
COPY build/libs/*.jar /app/app.jar
WORKDIR /app
CMD ["box86", "java", "-jar", "app.jar"]