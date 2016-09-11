package io.github.phantamanta44.tiabot2.command;

import io.github.phantamanta44.discord4j.core.Discord;
import io.github.phantamanta44.discord4j.data.wrapper.ChannelUser;

import java.util.function.Predicate;

public enum CmdPerm {

    BOT_OWNER(u -> u.id().equalsIgnoreCase(Discord.ownerId())),
    SERVER_OWNER(u -> u.guild().owner().equals(u));

    public final Predicate<ChannelUser> test;

    CmdPerm(Predicate<ChannelUser> test) {
        this.test = test;
    }

}
