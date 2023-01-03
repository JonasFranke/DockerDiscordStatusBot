[![wakatime](https://wakatime.com/badge/user/49ee5b93-5588-4f44-a2a6-bceec1836f4a/project/11927978-fe31-424f-8dc8-e6278c354e31.svg)](https://wakatime.com/badge/user/49ee5b93-5588-4f44-a2a6-bceec1836f4a/project/11927978-fe31-424f-8dc8-e6278c354e31) [![Docker Image CI](https://github.com/JonasFranke/DockerDiscordStatusBot/actions/workflows/docker-build.yml/badge.svg)](https://github.com/JonasFranke/DockerDiscordStatusBot/actions/workflows/docker-build.yml)
# ⚠️ DISCLAIMER: Mounting the docker socket into the container is a security risk. If you don't know what you are doing, don't do it. ⚠️
# DockerDiscordStatusBot
A simple Discord bot that allows you to see the status of any docker container.
View the DockerHub page [here](https://hub.docker.com/r/jnsfrnk/ddsb).

---
### How to use
- Clone the repository
- Build a jar using ```gradle shadowJar```
- Build a docker image using ```docker build -t <image name> .```
- Run the docker image using ```docker run -v //var/run/docker.sock:/var/run/docker.sock --network=host --name ddsb -e DISCORD_TOKEN=<YOUR-DISCORD-BOT-TOKEN> <image name>```

- Add the bot to your server
- Use the ```/ds``` command to see all running containers
- Use the ```/stopupdating <TRUE/FALSE>``` command to stop the bot from updating the status

