package tech.jonasfranke.ddsb.util;

import discord4j.common.util.Snowflake;
import discord4j.core.DiscordClient;
import discord4j.discordjson.json.MessageEditRequest;
import discord4j.rest.entity.RestChannel;
import discord4j.rest.entity.RestMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import tech.jonasfranke.ddsb.main.Main;

import java.util.HashMap;

public class UpdateEmbedThread extends Thread {

    private final Logger logger = LoggerFactory.getLogger(UpdateEmbedThread.class);

    private final DiscordClient client;
    private final HashMap<Snowflake, Snowflake> messageIds;
    private final Snowflake id;

    public UpdateEmbedThread(DiscordClient client, Snowflake id, HashMap<Snowflake, Snowflake> messageIds) {
        this.messageIds = messageIds;
        this.client = client;
        this.id = id;
    }

    @Override
    public void run() {
        logger.info("Starting thread for message " + id.asString());
        while (!Main.isCancelThreads()) {
            try {
                Thread.sleep(20*1000);
            } catch (InterruptedException e) {
                logger.error("Thread interrupted", e);
            }
            RestChannel channel = client.getChannelById(messageIds.get(id));

            RestMessage message = channel.getRestMessage(id);
            message.edit(MessageEditRequest.builder().addEmbed(new DockerManager().createDockerEmbed().asRequest()).build()).block();
            logger.info("Updated message " + id.asString());
        }
        logger.info("Stopping thread for message " + id.asString());
        this.interrupt();
    }
}
