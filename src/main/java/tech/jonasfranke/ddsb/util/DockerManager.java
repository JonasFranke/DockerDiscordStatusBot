package tech.jonasfranke.ddsb.util;

import com.github.dockerjava.api.DockerClient;
import com.github.dockerjava.api.command.InspectContainerResponse;
import com.github.dockerjava.api.model.Container;
import com.github.dockerjava.core.DefaultDockerClientConfig;
import com.github.dockerjava.core.DockerClientBuilder;
import com.github.dockerjava.core.DockerClientConfig;
import discord4j.common.util.Snowflake;
import discord4j.core.spec.EmbedCreateSpec;
import discord4j.rest.util.Color;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import tech.jonasfranke.ddsb.main.Main;

import java.text.SimpleDateFormat;
import java.time.OffsetDateTime;
import java.util.*;

public class DockerManager {

    private final ArrayList<Container> containerNames = new ArrayList<>();

    /// Map contains the container id as key and the container uptime as value
    private final HashMap<String, String> containerUptime = new HashMap<>();
    /// Map contains the container id as key and the container start time as value
    private final HashMap<String, Long> containerUptimeRelative = new HashMap<>();
    private final Logger logger = LoggerFactory.getLogger(DockerManager.class);

    public void handleDocker() {
        DockerClientConfig config = DefaultDockerClientConfig.createDefaultConfigBuilder()
                .withDockerHost("unix:///var/run/docker.sock")
                .withDockerTlsVerify(false)
                .build();

        DockerClient dockerClient = null;
        try {
            dockerClient = DockerClientBuilder.getInstance(config).build();
            logger.info("Docker client created");
        } catch (NullPointerException e) {
            logger.error("Couldn't find docker socket. Please make sure docker is running and mounted correctly.");
            System.exit(2);
        }

        assert dockerClient != null;
        List<Container> containers = dockerClient.listContainersCmd().exec();
        containerNames.addAll(containers);
        for (Container container : containers) {
            InspectContainerResponse containerInfo = dockerClient.inspectContainerCmd(container.getId()).exec();

            String startTimeString = containerInfo.getState().getStartedAt();
            //2022-12-30T18:56:20.820972774Z
            assert startTimeString != null;
            long startTime = OffsetDateTime.parse(startTimeString).toInstant().toEpochMilli();

            Calendar now = Calendar.getInstance();

            long uptimeMillis = now.getTimeInMillis() - startTime;
            logger.debug("Start time: " + startTime + " Now: " + now.getTimeInMillis());

            long uptimeSeconds = uptimeMillis / 1000;
            logger.info(container.getNames()[0] + ": Uptime: " + uptimeSeconds + " Sekunden");
            this.containerUptime.put(container.getId(), String.valueOf(uptimeSeconds));
            this.containerUptimeRelative.put(container.getId(), startTime / 1000);// Discord mag keine millisekunden
            logger.info(container.getId());
        }
    }

    public EmbedCreateSpec createDockerEmbed(Snowflake guildID, boolean running) {
        handleDocker();
        EmbedCreateSpec.Builder builder = EmbedCreateSpec.builder()
            .color(Color.BLUE)
            .title("Docker Status")
            .description("This is the status of the docker containers")
            .addField("\u200B", "\u200B", false)
            .addField("\u200B", "\u200B", false);

        for (Container containerName : containerNames) {
            builder
                .addField(CustomEmote.GreenUpArrow.init(guildID, Main.getGateway()).getFullStringOrAlias() + " " + containerName.getNames()[0].replaceFirst("/", ""), /*containerUptime.get(containerName.getId()) +*/ "<t:" + containerUptimeRelative.get(containerName.getId()) + ":R>", false);
        }

        Calendar now = Calendar.getInstance();
        SimpleDateFormat formatter = new SimpleDateFormat("dd.MM.yyyy HH:mm:ss");
        if (running) {
            builder.footer("Last updated: " + formatter.format(now.getTime()), null);
        } else
            builder.footer("Last updated: " + formatter.format(now.getTime()) + " (Stopped thread for updating this message)", null);


        return builder.build();
    }
    public EmbedCreateSpec createDockerEmbed(Snowflake guildID, boolean running, boolean isNoUpdate) {
        handleDocker();
        EmbedCreateSpec.Builder builder = EmbedCreateSpec.builder()
            .color(Color.BLUE)
            .title("Docker Status")
            .description("This is the status of the docker containers")
            .addField("\u200B", "\u200B", false)
            .addField("\u200B", "\u200B", false);

        for (Container containerName : containerNames) {
            builder
                .addField(CustomEmote.GreenUpArrow.init(guildID, Main.getGateway()).getFullStringOrAlias() + " " + containerName.getNames()[0].replaceFirst("/", ""), /*containerUptime.get(containerName.getId()) +*/ "<t:" + containerUptimeRelative.get(containerName.getId()) + ":R>", false);
        }

        Calendar now = Calendar.getInstance();
        SimpleDateFormat formatter = new SimpleDateFormat("dd.MM.yyyy HH:mm:ss");
        if (running) {
            if (!isNoUpdate) {
                builder.footer("Last updated: " + formatter.format(now.getTime()), null);
            } else {
                builder.footer("Last updated: " + formatter.format(now.getTime()) + " ❌ Update", null);
            }

        } else
            builder.footer("Last updated: " + formatter.format(now.getTime()) + " (Stopped thread for updating this message)", null);


        return builder.build();
    }
}
