package io.github.phantamanta44.tiabot2.module.moderate;

import java.util.function.Predicate;

import io.github.phantamanta44.discord4j.core.event.context.IEventContext;
import io.github.phantamanta44.discord4j.core.module.Module;
import io.github.phantamanta44.discord4j.core.module.ModuleConfig;
import io.github.phantamanta44.discord4j.data.Permission;
import io.github.phantamanta44.discord4j.data.wrapper.Message;
import io.github.phantamanta44.discord4j.data.wrapper.User;
import io.github.phantamanta44.discord4j.util.function.Lambdas;
import io.github.phantamanta44.tiabot2.command.Command;
import io.github.phantamanta44.tiabot2.command.CommandProvider;
import io.github.phantamanta44.tiabot2.command.args.InlineCodeBlock;
import io.github.phantamanta44.tiabot2.command.args.Omittable;

@CommandProvider(ModerateModule.MOD_ID)
public class ModerateModule {

    public static final String MOD_ID = "moderate";
    @Module(
            id = MOD_ID, name = "Moderation", author = "Phanta",
            desc = "This module provides a set of tools to make server moderation easier."
    )
    public static void moduleInit(ModuleConfig cfg) {
        // NO-OP
    }

    @Command(
            name = "rm", usage = "rm [count#]", dcPerms = {Permission.MANAGE_MSG},
            desc = "Removes a number of messages from the current channel.",
            guildOnly = true
    )
    public static void cmdRm(@Omittable Integer toDelete, IEventContext ctx) {
        delete(toDelete == null ? 1 : toDelete, Lambdas.acceptAll(), ctx);
    }

    @Command(
            name = "rmregex", usage = "rmregex <'regexp'> [count#]", dcPerms = {Permission.MANAGE_MSG},
            desc = "Removes a number of messages matching a regex filter from the current channel.",
            guildOnly = true
    )
    public static void cmdRmRegex(InlineCodeBlock regex, @Omittable Integer toDelete, IEventContext ctx) {
        delete(toDelete == null ? 1 : toDelete, m -> m.body().matches(regex.getCode()), ctx);
    }

    @Command(
            name = "rmuser", usage = "rmuser <@user> [count#]", dcPerms = {Permission.MANAGE_MSG},
            desc = "Removes a number of messages send by a user from the current channel.",
            guildOnly = true
    )
    public static void cmdRmUser(User user, @Omittable Integer toDelete, IEventContext ctx) {
        delete(toDelete == null ? 1 : toDelete, m -> m.author().equals(user), ctx);
    }

    private static void delete(long toDelete, Predicate<Message> filter, IEventContext ctx) {
        if (toDelete < 1)
            throw new NumberFormatException();
        ctx.message().destroy().fail(e -> {
            ctx.send("%s: Encountered `%s` while trying to delete messages!", ctx.user().tag(), e.getClass().getName());
            e.printStackTrace();
        }).done(() ->
            ctx.channel().messages()
                .sequential()
                .filter(filter)
                .sorted((a, b) -> (int) (b.timestamp() - a.timestamp()))
                .limit(toDelete)
                .destroyAll().fail(e -> {
                    ctx.send("%s: Encountered `%s` while trying to delete messages!", ctx.user().tag(), e.getClass().getName());
                    e.printStackTrace();
                })
        );
    }

}
