package tech.jonasfranke.ddsb.main;

import discord4j.core.DiscordClient;
import discord4j.core.GatewayDiscordClient;
import discord4j.core.event.domain.interaction.ApplicationCommandInteractionEvent;
import discord4j.core.event.domain.message.MessageCreateEvent;
import discord4j.core.object.entity.Message;
import discord4j.core.object.entity.channel.MessageChannel;
import discord4j.core.spec.MessageCreateSpec;
import discord4j.discordjson.json.ApplicationCommandRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import tech.jonasfranke.ddsb.util.DockerManager;

import java.io.IOException;
import java.text.ParseException;

public class Main {

    static Logger logger = LoggerFactory.getLogger(Main.class);
    public static void main(String[] args) {
        printStartupMessage();
        final String token = "MTA1NzY2OTA4MjExMzExNDIyMw.GxQmTY.g1AvsWIO2_XQV2xhzhOKHrErNHceg3VkZw68SY";
        final DiscordClient client = DiscordClient.create(token);
        final GatewayDiscordClient gateway = client.login().block();
        final long applicationId = client.getApplicationId().block();

        assert gateway != null;
        gateway.on(MessageCreateEvent.class).subscribe(event -> {
            final Message message = event.getMessage();
            switch (message.getContent().toLowerCase()) {
                case "!ping" -> {
                    final MessageChannel channel = message.getChannel().block();
                    assert channel != null;
                    channel.createMessage("Pong!").block();
                    break;
                }
                case "just a second..." -> {
                    try {
                        event.getMessage().getChannel().block().createMessage(MessageCreateSpec.builder().addEmbed(new DockerManager().createDockerEmbed()).build()).block();
                    } catch (IOException | InterruptedException | ParseException e) {
                        throw new RuntimeException(e);
                    }
                }
                default -> {
                    if (!message.getContent().equals(" "))
                        logger.info("Message received: " + message.getContent());
                }
            }
        });

        gateway.on(ApplicationCommandInteractionEvent.class, event -> {
            switch (event.getCommandName()) {
                case "ping" -> {
                    return event.reply("Pong!");
                }
                case "ds" -> {
                    return event.reply("Just a second...");
                }
                default -> {
                    return null;
                }
            }
        }).subscribe();

        ApplicationCommandRequest pingPongCommand = ApplicationCommandRequest.builder()
                .name("ping")
                .description("Replies with Pong!")
                .build();
        ApplicationCommandRequest dockerStatusCommand = ApplicationCommandRequest.builder()
                .name("ds")
                .description("Runs docker ps")
                .build();

        client.getApplicationService().createGlobalApplicationCommand(applicationId, pingPongCommand).subscribe();
        client.getApplicationService().createGlobalApplicationCommand(applicationId, dockerStatusCommand).subscribe();

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
}
