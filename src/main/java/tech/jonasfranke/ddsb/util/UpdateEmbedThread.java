package tech.jonasfranke.ddsb.util;

import discord4j.common.util.Snowflake;
import discord4j.core.DiscordClient;
import discord4j.discordjson.json.MessageEditRequest;
import discord4j.rest.entity.RestChannel;
import discord4j.rest.entity.RestMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import tech.jonasfranke.ddsb.main.Main;

import java.util.ArrayList;
import java.util.HashMap;

public class UpdateEmbedThread extends Thread {

    private final Logger logger = LoggerFactory.getLogger(UpdateEmbedThread.class);

    private final DiscordClient client;
    private final HashMap<Snowflake, Snowflake> messageIds;
    private final Snowflake id;
    private final Snowflake guildId;

    public UpdateEmbedThread(DiscordClient client, Snowflake id, HashMap<Snowflake, Snowflake> messageIds, Snowflake guildId) {
        this.messageIds = messageIds;
        this.client = client;
        this.id = id;
        this.guildId = guildId;
    }

    @Override
    public void run() {
        Snowflake mId = id;
        logger.info("Starting thread for message " + mId.asString());
        this.setName("MessageUpdatingThread for " + mId.asString());
        RestChannel channel = client.getChannelById(messageIds.get(mId));
        RestMessage message = channel.getRestMessage(mId);
        boolean messageRunning = true;
        while (true) {
            logger.debug("Message with Id " + mId.asString() + " is running isCancelThreads " + Main.isCancelThreads() + " isMessageStopped " + LatestMessageUpdater.isMessageStopped(mId) + " messageRunning " + messageRunning); //TODO: remove this after tests
            if (LatestMessageUpdater.isMessageStopped(mId)) {
                logger.info("Stopping thread for message " + mId.asString() + " because channel is already running");
                message.edit(MessageEditRequest.builder().addEmbed(new DockerManager().createDockerEmbed(guildId, false).asRequest()).build()).block();
                messageRunning = false;
                break;
            }
            if (!messageRunning) {
                message.edit(MessageEditRequest.builder().addEmbed(new DockerManager().createDockerEmbed(guildId, false).asRequest()).build()).block();
                break;
            }
            if (Main.isCancelThreads()) {
                logger.info("Stopping thread for message " + mId.asString() + " because cancelThreads is " + Main.isCancelThreads());
                message.edit(MessageEditRequest.builder().addEmbed(new DockerManager().createDockerEmbed(guildId, false).asRequest()).build()).block();
                break;
            }
            try {
                Thread.sleep(20*1000);
            } catch (InterruptedException e) {
                logger.error("Thread interrupted", e);
            }

            message.edit(MessageEditRequest.builder().addEmbed(new DockerManager().createDockerEmbed(guildId, messageRunning).asRequest()).build()).block();
            logger.info("Updated message " + mId.asString());

        }
        logger.info("Stopped thread for message " + mId.asString());
        LatestMessageUpdater.removeChannel(channel.getId());
        this.interrupt();
    }
}
