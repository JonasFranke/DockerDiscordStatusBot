FROM gradle:jdk17-jammy AS build
WORKDIR /app
COPY . .
RUN gradle clean shadowJar

FROM eclipse-temurin:17-jre-jammy

WORKDIR /app
RUN ls
COPY --from=build /app/build/libs/ddsb-*.jar bot.jar

ENTRYPOINT ["java", "-jar", "bot.jar"]
