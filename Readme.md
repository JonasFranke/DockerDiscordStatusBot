# DockerDiscordStatusBot
A simple Discord bot that allows you to see the status of any docker container.

---
### How to use
- Clone the repository
- Build a jar using ```gradle shadowJar```
- Build a docker image using ```docker build -t <image name> .```
- Run the docker image using ```docker run -v //var/run/docker.sock:/var/run/docker.sock --network=host --name ddsb <image name>```
