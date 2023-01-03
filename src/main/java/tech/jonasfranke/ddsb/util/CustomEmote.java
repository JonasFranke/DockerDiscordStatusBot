package tech.jonasfranke.ddsb.util;

import discord4j.common.util.Snowflake;
import discord4j.core.GatewayDiscordClient;
import discord4j.core.object.entity.GuildEmoji;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.logging.Logger;

public enum CustomEmote {
    GreenUpArrow("GreenUpArrow", ":arrow_up:");// Adding a \ in front of the emote such as \:Dog_Spin: will display the name along with the ID such as  <a:Dog_Spin:801604978040897568>   "1058388762121490542"

    private final String name;
    private String alias;
    private Snowflake id;
    private Snowflake guildID;
    private GatewayDiscordClient gateway;
    private boolean isInitialized = false;
    private final Logger LOGGER = Logger.getLogger(CustomEmote.class.getName());
    CustomEmote(String name) {
        this.name = name;
    }

    CustomEmote(String name, String alias) {
        this.name = name;
        this.alias = alias;
    }

    public CustomEmote init(Snowflake guildID, GatewayDiscordClient gateway) {
        this.gateway = gateway;
        this.guildID = guildID;
        isInitialized = true;
        id = getIDofEmote();
        return this;
    }

    public Snowflake getId() {
        return id;
    }

    /// Deprecated, use getFullStringOrAlias() instead
    @Deprecated
    public String getFullString(String name, String id) {
        return "<:" + name + ":" + id + ">";//1058388762121490542
    }

    public String getName() {
        return name;
    }

    public String getAlias() {
        return alias;
    }

    private Snowflake getIDofEmote() {
        if (!isInitialized) {
            LOGGER.warning("CustomEmote " + name + " is not initialized!");
            throw new IllegalStateException("CustomEmote not initialized!");
        }
        List<GuildEmoji> emoteList = gateway.getGuildEmojis(guildID).collectList().block();
        for (GuildEmoji emote : Objects.requireNonNull(emoteList)) {
            if (emote.getName().equalsIgnoreCase(name)) {
                LOGGER.info("Found emote " + name + " with ID " + emote.getId().asString());
                return emote.getId();
            }
        }
        return null;
    }

    public String getFullStringOrAlias() {
        if (!isInitialized) {
            LOGGER.warning("CustomEmote " + name + " is not initialized!");
            throw new IllegalStateException("CustomEmote not initialized!");
        }
        if (alias == null) {
            return "<:" + name + ":" + id + ">";
        } else {
            List<GuildEmoji> emoteList = new ArrayList<>();
            gateway.getGuildEmojis(guildID).subscribe(emoteList::add);
            if (emoteList.isEmpty()) {
                LOGGER.warning("Could not find emotes. Emote list is empty or null!");
                return getAlias();
            }
            for (GuildEmoji emote : Objects.requireNonNull(emoteList)) {
                if (emote.getName().equalsIgnoreCase(name)) {
                    LOGGER.info("Found emote " + name + " with ID " + emote.getId().asString());
                    return "<:" + name + ":" + emote.getId().asString() + ">";
                }
            }
            LOGGER.info("Emote " + name + " not found, using alias " + alias);
            return null;
        }
    }
}
