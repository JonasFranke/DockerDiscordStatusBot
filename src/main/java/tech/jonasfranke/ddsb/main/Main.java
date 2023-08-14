package tech.jonasfranke.ddsb.main;

import discord4j.common.util.Snowflake;
import discord4j.core.DiscordClient;
import discord4j.core.GatewayDiscordClient;
import discord4j.core.event.domain.interaction.ChatInputInteractionEvent;
import discord4j.core.event.domain.message.MessageCreateEvent;
import discord4j.core.object.entity.Message;
import discord4j.core.object.entity.channel.MessageChannel;
import discord4j.core.spec.MessageCreateSpec;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import tech.jonasfranke.ddsb.util.DockerManager;
import tech.jonasfranke.ddsb.util.GlobalCommandRegistrar;
import tech.jonasfranke.ddsb.util.LatestMessageUpdater;
import tech.jonasfranke.ddsb.util.UpdateEmbedThread;

import java.text.ParseException;
import java.util.HashMap;
import java.util.List;

public class Main {

    private static Logger logger = LoggerFactory.getLogger(Main.class);
    private static boolean cancelThreads = false;
    private static boolean noUpdate = false;
    private static DiscordClient client;
    private static GatewayDiscordClient gateway;

    public static void main(String[] args) {
        printStartupMessage();
        final String token = System.getenv("DISCORD_TOKEN");
        logger.debug("Using token: " + token);
        client = DiscordClient.create(token);
        gateway = client.login().block();
        final long applicationId = client.getApplicationId().block();
        /// Map contains the message id as key and the channel id as value
        final HashMap<Snowflake, Snowflake> messageIds = new HashMap<>();

        if (System.getenv("NO_UPDATE") != null) {
            if (System.getenv("NO_UPDATE").equalsIgnoreCase("true") || System.getenv("NO_UPDATE").equalsIgnoreCase("false")) {
                noUpdate = Boolean.parseBoolean(System.getenv("NO_UPDATE"));
                logger.info("NO_UPDATE is set to " + noUpdate);
            } else {
                throw new RuntimeException("Could not parse NO_UPDATE env variable");
            }
        }

        assert gateway != null;
        gateway.on(MessageCreateEvent.class).subscribe(event -> {
            final Message message = event.getMessage();
            switch (message.getContent().toLowerCase()) {
                case "!ping" -> {
                    final MessageChannel channel = message.getChannel().block();
                    assert channel != null;
                    channel.createMessage("Pong!").block();
                }
                case "just a second..." -> {
                    if (message.getAuthor().isPresent() && message.getAuthor().get().isBot()) {
                        final Message embed = event.getMessage().getChannel().block().createMessage(MessageCreateSpec.builder().addEmbed(new DockerManager().createDockerEmbed(message.getGuildId().get(), true, isNoUpdate())).build()).block();
                        if (!cancelThreads && !isNoUpdate()) {
                            assert embed != null;
                            UpdateEmbedThread thread = new UpdateEmbedThread(client, embed.getId(), messageIds, message.getGuildId().get());
                            if (LatestMessageUpdater.hasRunningThread(embed.getChannelId())) {
                                logger.debug("Detected that channel already has a thread running, stopping it now id: " + embed.getChannelId());
                                LatestMessageUpdater.stopMessage(LatestMessageUpdater.getRunningThreadMessageId(embed.getChannelId()));
                            }
                            messageIds.put(embed.getId(), embed.getChannelId());
                            if (LatestMessageUpdater.addChannel(embed.getChannelId(), thread))
                                thread.start();
                            logger.debug("Requested new thread for channel id: " + embed.getChannelId().asString());
                        }
                    }

                }
                default -> {
                    if (!message.getContent().equals(" "))
                        logger.info("Message received: " + message.getContent());
                }
            }
        });

        gateway.on(ChatInputInteractionEvent.class, event -> {
            switch (event.getCommandName()) {
                case "ping" -> {
                    return event.reply("Pong!");
                }
                case "ds" -> {
                    return event.reply("Just a second...");
                }
                case "stopupdate" -> {
                    if (event.getOption("stop").isPresent() && event.getOption("stop").get().getValue().get().asBoolean()) {
                        cancelThreads = true;
                        logger.info("Set cancelThreads to " + true);
                        return event
                                .reply("Stopped all threads")
                                .withEphemeral(true);
                    } else {
                        cancelThreads = false;
                        logger.info("Set cancelThreads to " + false);
                        return event
                                .reply("Allowed updating")
                                .withEphemeral(true);
                    }

                }
                default -> {
                    return null;
                }
            }
        }).subscribe();

        List<String> commands = List.of("stopupdating.json", "ds.json", "ping.json");

        try {
            new GlobalCommandRegistrar(gateway.getRestClient()).registerCommands(commands);
        } catch (Exception e) {
            logger.error("Error trying to register global slash commands", e);
        }

        gateway.onDisconnect().block();
    }

    private static void printStartupMessage() {
        System.out.println("DDSB is now running!");
        System.out.println("_________________________________________________");
        System.out.println();
        System.out.println("  $$$$$$$\\  $$$$$$$\\   $$$$$$\\  $$$$$$$\\  ");
        System.out.println("  $$  __$$\\ $$  __$$\\ $$  __$$\\ $$  __$$\\ ");
        System.out.println("  $$ |  $$ |$$ |  $$ |$$ /  \\__|$$ |  $$ |");
        System.out.println("  $$ |  $$ |$$ |  $$ |\\$$$$$$\\  $$$$$$$\\ |");
        System.out.println("  $$ |  $$ |$$ |  $$ | \\____$$\\ $$  __$$\\ ");
        System.out.println("  $$ |  $$ |$$ |  $$ |$$\\   $$ |$$ |  $$ |");
        System.out.println("  $$$$$$$  |$$$$$$$  |\\$$$$$$  |$$$$$$$  |");
        System.out.println("  \\_______/ \\_______/  \\______/ \\_______/ ");
        System.out.println();
        System.out.println("_________________________________________________");
    }

    public static boolean isCancelThreads() {
        return cancelThreads;
    }

    public static DiscordClient getClient() {
        return client;
    }

    public static boolean isNoUpdate() {
        return noUpdate;
    }

    public static GatewayDiscordClient getGateway() {
        return gateway;
    }
}
