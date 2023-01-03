package tech.jonasfranke.ddsb.util;

import discord4j.common.util.Snowflake;
import discord4j.core.DiscordClient;
import discord4j.core.GatewayDiscordClient;
import discord4j.core.object.entity.GuildEmoji;

import java.util.List;
import java.util.Objects;

public enum CustomEmote {
    GreenUpArrow("GreenUpArrow", "1058388762121490542", ":arrow_up:");// Adding a \ in front of the emote such as \:Dog_Spin: will display the name along with the ID such as  <a:Dog_Spin:801604978040897568>

    private final String id;
    private final String name;
    private String alias;
    CustomEmote(String name, String id) {
        this.id = id;
        this.name = name;
    }

    CustomEmote(String name, String id, String alias) {
        this.id = id;
        this.name = name;
        this.alias = alias;
    }

    public String getId() {
        return id;
    }

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

    public String getFullStringOrAlias(Snowflake guildID, GatewayDiscordClient gateway) {
        if (alias == null) {
            return "<:" + name + ":" + id + ">";
        } else {
            List<GuildEmoji> emoteList = gateway.getGuildEmojis(guildID).collectList().block();
            for (GuildEmoji emote : Objects.requireNonNull(emoteList)) {
                if (emote.getId().asString().equalsIgnoreCase(getId())) {
                    return "<:" + emote.getName() + ":" + getId() + ">";
                } else {
                    return ":" + getAlias() + ":";
                }
            }
        }
        return getId();
    }
}
