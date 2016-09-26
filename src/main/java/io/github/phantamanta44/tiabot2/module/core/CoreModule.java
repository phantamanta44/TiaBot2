package io.github.phantamanta44.tiabot2.module.core;

import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Stream;

import org.apache.commons.lang3.tuple.Pair;

import io.github.phantamanta44.discord4j.core.event.context.IEventContext;
import io.github.phantamanta44.discord4j.core.module.Module;
import io.github.phantamanta44.discord4j.core.module.ModuleConfig;
import io.github.phantamanta44.discord4j.core.module.ModuleManager;
import io.github.phantamanta44.discord4j.data.Permission;
import io.github.phantamanta44.discord4j.data.wrapper.Guild;
import io.github.phantamanta44.discord4j.data.wrapper.PrivateChannel;
import io.github.phantamanta44.discord4j.util.StringUtils;
import io.github.phantamanta44.tiabot2.TiaBot;
import io.github.phantamanta44.tiabot2.command.CmdPerm;
import io.github.phantamanta44.tiabot2.command.Command;
import io.github.phantamanta44.tiabot2.command.CommandProvider;
import io.github.phantamanta44.tiabot2.command.args.InlineCodeBlock;
import io.github.phantamanta44.tiabot2.command.args.Omittable;
import sx.blah.discord.Discord4J;

@CommandProvider(CoreModule.MOD_ID)
public class CoreModule {

    public static final String MOD_ID = "core";
    @Module(
            id = MOD_ID, name = "Core", author = "Phanta",
            desc = "This module provides the core functionality of TiaBot. It cannot be disabled."
    )
    public static void moduleInit(ModuleConfig cfg) {
        // NO-OP
    }

    @Command(
            name = "ping", usage = "ping",
            desc = "Checks the bot's response time."
    )
    public static void cmdPing(IEventContext ctx) {
        ctx.send("Calculating response time...")
                .done(m -> m.edit("%s: Approximate reponse time: %dms", ctx.user().tag(), m.timestamp() - ctx.message().timestamp()));
    }

    @Command(
            name = "togglemod", usage = "togglemod <module>",
            desc = "Toggles a module on or off for this server.",
            dcPerms = Permission.MANAGE_SERV, guildOnly = true
    )
    public static void cmdToggleMod(ModuleConfig cfg, IEventContext ctx) {
        if (!cfg.info().id().equalsIgnoreCase(MOD_ID)) {
            cfg.setEnabled(ctx.guild(), !cfg.enabled(ctx.guild()));
            ctx.send("%s: %s the %s module.", ctx.user().tag(), cfg.enabled(ctx.guild()) ? "Enabled" : "Disabled", cfg.info().name());
        }
        else
            ctx.send("%s: This module cannot be disabled!", ctx.user().tag());
    }

    @Command(
            name = "setprefix", usage = "setprefix <'prefix'>",
            desc = "Sets the command prefix for this server.",
            dcPerms = Permission.MANAGE_SERV, guildOnly = true
    )
    public static void cmdSetPrefix(InlineCodeBlock pref, IEventContext ctx) {
        TiaBot.guildCfg(ctx.guild()).addProperty("prefix", pref.getCode());
        ctx.send("%s: Command prefix set to `%s`.", ctx.user().tag(), pref.getCode());
    }

    @Command(
            name = "halt", usage = "halt [reason]",
            desc = "Halts the bot.",
            perms = CmdPerm.BOT_OWNER
    )
    public static void cmdHalt(@Omittable String status, IEventContext ctx) {
        if (status == null) {
            ctx.send("Shutting down!").always(m -> Runtime.getRuntime().exit(130));
            return;
        }
        switch (status.toLowerCase()) {
            case "reboot":
                ctx.send("Rebooting!").always(m -> Runtime.getRuntime().exit(32));
                break;
            case "update":
                ctx.send("Rebooting for update!").always(m -> Runtime.getRuntime().exit(33));
                break;
            default:
                ctx.send("%s: Unknown exit status!", ctx.user().tag());
                break;
        }
    }

    @Command(
            name = "globalsave", usage="globalsave",
            desc = "Writes all configuration files to disk.",
            perms = CmdPerm.BOT_OWNER
    )
    public static void cmdGlobalSave(IEventContext ctx) {
        TiaBot.saveDb();
        TiaBot.bot().moduleMan().saveAll();
        ctx.send("%s: Save complete.", ctx.user().tag());
    }

    @Command(
            name = "help", usage="help [module]",
            desc = "Lists available modules and commands."
    )
    public static void cmdHelp(@Omittable ModuleConfig cfg, IEventContext ctx) {
        boolean priv = ctx.channel() instanceof PrivateChannel;
        if (cfg == null) {
            Stream<ModuleManager.ModMeta> modules;
            if (priv)
                modules = TiaBot.bot().moduleMan().modules().filter(m -> m.config.enabled(ctx.guild()));
            else
                modules = TiaBot.bot().moduleMan().modules();
            ctx.send("__**Available Modules**__\n%s",
                modules.map(m -> String.format("- %s (`%s`): %s", m.info.name(), m.info.id(), m.info.desc()))
                        .reduce((a, b) -> a.concat("\n").concat(b)).orElse("Nothing to see here.")
            );
        } else {
            if (priv || cfg.enabled(ctx.guild())) {
                ctx.send(String.format("__**Available Commands: %s**__\n%s", cfg.info().name(),
                    TiaBot.commander().commands(cfg.info().id())
                            .map(c -> String.format("- `%s`: %s", c.usage(), c.desc()))
                            .reduce((a, b) -> a.concat("\n").concat(b)).orElse("Nothing to see here.")
                ));
            }
            else
                ctx.send("%s: Module '%s' is not enabled!", ctx.user().tag(), cfg.info().name());
        }
    }

    @Command(
            name = "man", usage = "man <command>",
            desc = "Provides more detailed info about a command."
    )
    public static void cmdMan(Command cmd, IEventContext ctx) {
        String aliases = StringUtils.concat(cmd.aliases(), ", ");
        String perms = Stream.concat(
                Arrays.stream(cmd.perms()).map(CmdPerm::toString), Arrays.stream(cmd.dcPerms()).map(Permission::toString)
        ).reduce((a, b) -> a.concat(", ").concat(b)).orElse("None");
        ctx.send("__**Command: %s**__\n%s\n\nUsage: `%s`\nAliases: %s\nPermissions: %s",
                cmd.name(), cmd.docs().isEmpty() ? cmd.desc() : cmd.docs(), cmd.usage(), aliases.isEmpty() ? "None" : aliases, perms);
    }

    @Command(
            name = "listmods", usage = "listmods",
            desc = "Lists all modules and their statuses."
    )
    public static void cmdListMods(IEventContext ctx) {
        Stream<ModuleManager.ModMeta> modules = TiaBot.bot().moduleMan().modules();
        if (ctx.channel() instanceof PrivateChannel) {
            ctx.send("__**All Modules**__\n%s",
                    modules.map(m -> String.format("- `%s`: %s", m.info.id(), m.info.desc()))
                            .reduce((a, b) -> a.concat("\n").concat(b)).orElse("Nothing to see here.")
            );
        } else {
            ctx.send("__**All Modules**__\n%s",
                    modules.map(m -> String.format("%s `%s`: %s", m.config.enabled(ctx.guild()) ? "+" : "-", m.info.id(), m.info.desc()))
                            .reduce((a, b) -> a.concat("\n").concat(b)).orElse("Nothing to see here.")
            );
        }
    }

    @Command(
            name = "info", usage = "info",
            desc = "Retrieves statistics and other information about the bot."
    )
    public static void cmdInfo(IEventContext ctx) {
        List<Pair<String, Object>> info = new ArrayList<>();
        info.add(Pair.of("User ID", TiaBot.bot().user().id()));
        info.add(Pair.of("Servers", TiaBot.bot().guilds().count()));
        info.add(Pair.of("Channels", TiaBot.bot().channels().count()));
        info.add(Pair.of("Users", TiaBot.bot().guilds().flatMap(Guild::users).count()));
        String infoStr = info.stream()
                .map(e -> e.getKey().concat(": ").concat(String.valueOf(e.getValue())))
                .reduce((a, b) -> a.concat("\n").concat(b)).get();
        ctx.send("__**Bot Information**__\n```%s```\nSource code available at https://github.com/phantamanta44/TiaBot2", infoStr);
    }

    @Command(
            name = "uptime", usage = "uptime",
            desc = "Retrieves the bot's uptime."
    )
    public static void cmdUptime(IEventContext ctx) {
        ctx.send("%s: Current uptime: %s", ctx.user().tag(), StringUtils.formatTimeElapsed(
                System.currentTimeMillis() - Discord4J.getLaunchTime().atZone(ZoneId.systemDefault()).toInstant().toEpochMilli()
        ));
    }

    @Command(
            name = "mem", usage = "mem",
            desc = "Retrieves information about the bot's memory usage."
    )
    public static void cmdMem(IEventContext ctx) {
        Runtime rt = Runtime.getRuntime();
        ctx.send("Used Memory: %.2f/%.2fMB", (rt.totalMemory() - rt.freeMemory()) / 1000000F, rt.totalMemory() / 1000000F);
    }

    @Command(
            name = "revoke", aliases = "unsay", usage = "revoke [count]",
            desc = "Revokes a number of messages sent by the bot.",
            dcPerms = Permission.MANAGE_MSG, guildOnly = true
    )
    public static void cmdRevoke(@Omittable Integer toDelete, IEventContext ctx) {
        toDelete = toDelete == null ? 1 : toDelete;
        ctx.channel().messages().sequential()
                .filter(m -> m.author().equals(TiaBot.bot().user()))
                .sorted((a, b) -> (int)(b.timestamp() - a.timestamp()))
                .limit(toDelete).destroyAll()
                .fail(e -> {
                    ctx.send("%s: Encountered `%s` while trying to delete messages!", ctx.user().tag(), e.getClass().getName());
                    e.printStackTrace();
                });
    }

    @Command(
            name = "addbot", aliases = "invite", usage = "addbot",
            desc = "Provides a link for adding this bot to a server"
    )
    public static void cmdAddBot(String[] args, IEventContext ctx) {
        ctx.send("%s: https://discordapp.com/oauth2/authorize?client_id=%s&scope=bot", ctx.user().tag(), TiaBot.bot().application().clientId());
    }

}
