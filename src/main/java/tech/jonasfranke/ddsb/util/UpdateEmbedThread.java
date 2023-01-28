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
    private final ArrayList<Snowflake> runningChannels = new ArrayList<>();
    private final ArrayList<Snowflake> stoppedMessages = new ArrayList<>();

    public UpdateEmbedThread(DiscordClient client, Snowflake id, HashMap<Snowflake, Snowflake> messageIds, Snowflake guildId) {
        this.messageIds = messageIds;
        this.client = client;
        this.id = id;
        this.guildId = guildId;
    }

    @Override
    public void run() {
        logger.info("Starting thread for message " + id.asString());
        if (runningChannels.contains(messageIds.get(id))) {
            logger.info("Stopping thread for message " + id.asString() + " because channel is already running");
            this.interrupt();
        }
        while (!Main.isCancelThreads() && !stoppedMessages.contains(id)) {
            try {
                Thread.sleep(20*1000);
            } catch (InterruptedException e) {
                logger.error("Thread interrupted", e);
            }
            RestChannel channel = client.getChannelById(messageIds.get(id));

            RestMessage message = channel.getRestMessage(id);
            message.edit(MessageEditRequest.builder().addEmbed(new DockerManager().createDockerEmbed(guildId).asRequest()).build()).block();
            logger.info("Updated message " + id.asString());
        }
        logger.info("Stopping thread for message " + id.asString());
        removeChannel(messageIds.get(id));
        this.interrupt();
    }

    public void addChannel(Snowflake channelId) {
        runningChannels.add(channelId);
    }

    public void removeChannel(Snowflake channelId) {
        runningChannels.remove(channelId);
    }

    public void stopMessage(Snowflake messageId) {
        if (messageId != null)
            stoppedMessages.add(messageId);
    }
}
