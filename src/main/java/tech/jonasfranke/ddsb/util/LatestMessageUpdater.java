package tech.jonasfranke.ddsb.util;

import discord4j.common.util.Snowflake;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;

public class LatestMessageUpdater {

    private static final Logger logger = LoggerFactory.getLogger(LatestMessageUpdater.class);
    /// Map contains the channel id as key and the updating thread as value
    private static final HashMap<Snowflake, Thread> threads = new HashMap<>();
    /// Arraylist that holds stopped messages by their id
    private static final ArrayList<Snowflake> stoppedMessages = new ArrayList<>();

    public static boolean addChannel(Snowflake channelId, Thread thread) {
        if (!threads.containsKey(channelId)) {
            threads.put(channelId, thread);
            return true;
        } else {
            logger.error("Channel has already a running thread...");
            return false;
        }
    }

    public static void removeChannel(Snowflake channelId) {
        threads.remove(channelId);
        threads.forEach(
                (c, t) -> {
                    if (t.isInterrupted() || !t.isAlive()) {
                        threads.remove(c);
                        logger.debug("Removed one dead thread from UpdateEmbedThread.threads");
                    }
                });
    }

    public static boolean hasRunningThread(Snowflake channelId) {
        logger.debug("hasRunningThread is: " + threads.containsKey(channelId));
        logger.debug(threads.toString());
        return threads.containsKey(channelId);
    }

    public static void stopMessage(Snowflake messageId) {
        if (messageId != null) {
            try {
                stoppedMessages.add(messageId);
                //threads.get(channelId).interrupt(); Threads should not be interrupted
                logger.info("Stopping message " + messageId.asString());
                removeChannel(messageId);
            } catch (Exception e) {
                logger.error("An error occurred while trying to stop thread with message id: " + messageId.asString());
                logger.error(Arrays.toString(e.getStackTrace()));
            }
        } else
            logger.error("MessageId is null");

    }

    public static boolean isMessageStopped(Snowflake messageId) {
        logger.debug("stoppedMessages: " + stoppedMessages + " requested info for " + messageId.asString());
        return stoppedMessages.contains(messageId);
    }
}
