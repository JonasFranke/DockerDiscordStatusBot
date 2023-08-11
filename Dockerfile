FROM gradle:jdk17-jammy
WORKDIR /app
COPY build.gradle settings.gradle ./
COPY src ./src
RUN gradle shadowJar
ENTRYPOINT ["java", "-jar", "build/libs/ddsb-1.0-SNAPSHOT-all.jar"]
