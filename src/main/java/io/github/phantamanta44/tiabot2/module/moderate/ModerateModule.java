package io.github.phantamanta44.tiabot2.module.moderate;

import io.github.phantamanta44.discord4j.core.event.context.IEventContext;
import io.github.phantamanta44.discord4j.core.module.Module;
import io.github.phantamanta44.discord4j.core.module.ModuleConfig;
import io.github.phantamanta44.discord4j.data.Permission;
import io.github.phantamanta44.discord4j.data.wrapper.Channel;
import io.github.phantamanta44.discord4j.data.wrapper.Message;
import io.github.phantamanta44.discord4j.util.function.Lambdas;
import io.github.phantamanta44.tiabot2.TiaBot;
import io.github.phantamanta44.tiabot2.command.ArgVerify;
import io.github.phantamanta44.tiabot2.command.CommandProvider;

import java.util.function.Predicate;

@CommandProvider(ModerateModule.MOD_ID)
public class ModerateModule {

    public static final String MOD_ID = "moderate";
    private static ModuleConfig config;
    @Module(
            id = MOD_ID, name = "Moderation", author = "Phanta",
            desc = "This module provides a set of tools to make server moderation easier."
    )
    public static void moduleInit(ModuleConfig cfg) {
        config = cfg;
    }

    @CommandProvider.Command(
            name = "rm", usage = "rm [count]", dcPerms = {Permission.MANAGE_MSG},
            desc = "Removes a number of messages from the current channel."
    )
    public static void cmdRm(String[] args, IEventContext ctx) {
        if (!ArgVerify.GUILD.verify(args, ctx))
            return;
        try {
            delete(ctx.channel(), args.length < 1 ? 1 : Integer.parseInt(args[0]), Lambdas.acceptAll(), ctx);
        } catch (NumberFormatException e) {
            ctx.send("%s: Invalid numeral value `%s`!", ctx.user().tag(), args[0]);
        }
    }
/* TODO Finish
    @CommandProvider.Command(
            name = "rmregex", usage = "rm <`regexp`> [count]", dcPerms = {Permission.MANAGE_MSG},
            desc = "Removes a number of messages matching a regex filter from the current channel."
    )
    public static void cmdRmRegex(String[] args, IEventContext ctx) {
        if (!ArgVerify.GUILD_ONE.verify(args, ctx))
            return;
        try {
            delete(ctx.channel(), args.length < 2 ? 1 : Integer.parseInt(args[1]), Lambdas.acceptAll(), ctx);
        } catch (NumberFormatException e) {
            ctx.send("%s: Invalid numeral value `%s`!", ctx.user().tag(), args[1]);
        }
    }
*/
    private static void delete(Channel chan, long toDelete, Predicate<Message> filter, IEventContext ctx) {
        if (toDelete < 1)
            throw new NumberFormatException();
        ctx.channel().messages().sequential()
                .sorted((a, b) -> (int)(b.timestamp() - a.timestamp()))
                .limit(toDelete).destroyAll()
                .fail(e -> {
                    ctx.send("%s: Encountered `%s` while trying to delete messages!", ctx.user().tag(), e.getClass().getName());
                    e.printStackTrace();
                });
    }

}
