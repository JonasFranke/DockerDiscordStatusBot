package tech.jonasfranke.ddsb.util;

import com.github.dockerjava.api.DockerClient;
import com.github.dockerjava.api.command.InspectContainerResponse;
import com.github.dockerjava.api.model.Container;
import com.github.dockerjava.core.DefaultDockerClientConfig;
import com.github.dockerjava.core.DockerClientBuilder;
import com.github.dockerjava.core.DockerClientConfig;
import discord4j.core.spec.EmbedCreateSpec;
import discord4j.rest.util.Color;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.TimeUnit;

public class DockerManager {

    private final ArrayList<Container> containerNames = new ArrayList<>();

    /// Map contains the container id as key and the container uptime as value
    private final HashMap<String, String> containers = new HashMap<>();
    private final DockerClientConfig standard;
    private final Logger logger = LoggerFactory.getLogger(DockerManager.class);

    public DockerManager() {
        standard = DefaultDockerClientConfig.createDefaultConfigBuilder().build();
    }

    public void handleDocker() throws IOException, InterruptedException, ParseException {
        /*Process p = Runtime.getRuntime().exec("docker ps");
        InputStream is = p.getInputStream();
        BufferedReader br = new BufferedReader(new InputStreamReader(is));
        String line;
        while ((line = br.readLine()) != null) {
            if (!line.toUpperCase().startsWith("CONTAINER")) {
                ArrayList<String> out = new ArrayList<>();
                for (String s : line.split(" ")) {
                    if (!s.isEmpty()) {
                        out.add(s);

                    }
                }
                System.out.println(line);
                containerNames.add(out.get(1));
                containers.put(out.get(1), "Uptime: " + out.get(7) + " " + out.get(8));
            }
        }

        p.waitFor();
        return "Done";*/

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
            SimpleDateFormat isoFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'");

            Date startTimeDate = isoFormat.parse(startTimeString);
            Calendar cal = Calendar.getInstance();
            TimeZone tz = cal.getTimeZone();
            logger.debug("Timezone: " + tz.getDisplayName() + " : " + tz.getRawOffset());
            startTimeDate.setTime(startTimeDate.getTime() + TimeUnit.HOURS.toMillis(tz.getRawOffset()));

            Calendar startTime = Calendar.getInstance();
            startTime.setTime(startTimeDate);

            Calendar now = Calendar.getInstance();

            long uptimeMillis = now.getTimeInMillis() - startTime.getTimeInMillis();
            logger.debug("System time: " + System.currentTimeMillis() + " Start time: " + startTime.getTimeInMillis() + " Now: " + now.getTimeInMillis());

            long uptimeSeconds = uptimeMillis / 1000;
            logger.info(container.getNames()[0] + ": Uptime: " + uptimeSeconds + " Sekunden");
            this.containers.put(container.getId(), String.valueOf(uptimeSeconds));
            logger.info(container.getId());
        }
    }

    public EmbedCreateSpec createDockerEmbed() throws IOException, InterruptedException, ParseException {
        handleDocker();
        EmbedCreateSpec.Builder builder = EmbedCreateSpec.builder()
            .color(Color.BLUE)
            .title("Docker Status")
            .description("This is the status of the docker containers")
            .addField("\u200B", "\u200B", false)
            .addField("\u200B", "\u200B", false);

        for (Container containerName : containerNames) {
            builder
                .addField(containerName.getNames()[0].replaceFirst("/", ""), containers.get(containerName.getId()), false);
        }
        return builder.build();
    }
}
