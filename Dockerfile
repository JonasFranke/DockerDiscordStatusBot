FROM openjdk:17

RUN apt install git build-essential cmake && git clone --branch "v0.2.6" https://github.com/ptitSeb/box86 && cd box86 && mkdir build && cd build && cmake .. -DRPI3=1 -DCMAKE_BUILD_TYPE=RelWithDebInfo && make -j1 && make install && systemctl restart systemd-binfmt


RUN cp /usr/share/zoneinfo/Europe/Berlin /etc/localtime
RUN mkdir /app
COPY build/libs/*.jar /app/app.jar
WORKDIR /app
CMD ["box86", "java", "-jar", "app.jar"]