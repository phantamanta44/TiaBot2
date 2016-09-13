package io.github.phantamanta44.tiabot2.module.core;

import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.NoSuchElementException;
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
import io.github.phantamanta44.tiabot2.command.ArgTokenizer;
import io.github.phantamanta44.tiabot2.command.ArgVerify;
import io.github.phantamanta44.tiabot2.command.CmdPerm;
import io.github.phantamanta44.tiabot2.command.CommandProvider;
import sx.blah.discord.Discord4J;

@CommandProvider(CoreModule.MOD_ID)
public class CoreModule {

    public static final String MOD_ID = "core";
    private static ModuleConfig config;
    @Module(
            id = MOD_ID, name = "Core", author = "Phanta",
            desc = "This module provides the core functionality of TiaBot. It cannot be disabled."
    )
    public static void moduleInit(ModuleConfig cfg) {
        config = cfg;
    }

    @CommandProvider.Command(
            name = "ping", usage = "ping",
            desc = "Checks the bot's response time."
    )
    public static void cmdPing(String[] args, IEventContext ctx) {
        ctx.send("Calculating response time...")
                .done(m -> m.edit("%s: Approximate reponse time: %dms", ctx.user().tag(), m.timestamp() - ctx.message().timestamp()));
    }

    @CommandProvider.Command(
            name = "togglemod", usage = "togglemod <module>",
            desc = "Toggles a module on or off for this server.",
            perms = {CmdPerm.SERVER_OWNER}
    )
    public static void cmdToggleMod(String[] args, IEventContext ctx) {
        if (!ArgVerify.GUILD_ONE.verify(args, ctx))
            return;
        ModuleConfig cfg = TiaBot.bot().moduleMan().configFor(args[0]);
        if (cfg != null && !cfg.info().id().equalsIgnoreCase(MOD_ID)) {
            cfg.setEnabled(ctx.guild(), !config.enabled(ctx.guild()));
            ctx.send("%s: %s the %s module.", ctx.user().tag(), config.enabled(ctx.guild()) ? "Enabled" : "Disabled", cfg.info().name());
        }
        else
            ctx.send("%s: This module cannot be disabled!", ctx.user().tag());

    }

    @CommandProvider.Command(
            name = "setprefix", usage = "setprefix <`prefix`>",
            desc = "Sets the command prefix for this server.",
            perms = {CmdPerm.SERVER_OWNER}
    )
    public static void cmdSetPrefix(String[] args, IEventContext ctx) {
        if (!ArgVerify.GUILD_ONE.verify(args, ctx))
            return;
        try {
        	ArgTokenizer tokens = new ArgTokenizer(args);
        	String newPref = tokens.nextInlineCode();
            TiaBot.guildCfg(ctx.guild()).addProperty("prefix", newPref);
            ctx.send("%s: Command prefix set to `%s`.", ctx.user().tag(), newPref);
        } catch (NoSuchElementException e) {
        	ctx.send("%s: No suitable parameter provided!", ctx.user().tag());
        }
    }

    @CommandProvider.Command(
            name = "halt", usage = "halt [reason]",
            desc = "Halts the bot.",
            perms = {CmdPerm.BOT_OWNER}
    )
    public static void cmdHalt(String[] args, IEventContext ctx) {
        if (args.length < 1)
            ctx.send("Shutting down!").always(m -> Runtime.getRuntime().exit(130));
        switch (args[0].toLowerCase()) {
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

    @CommandProvider.Command(
            name = "globalsave", usage="globalsave",
            desc = "Writes all configuration files to disk.",
            perms = {CmdPerm.BOT_OWNER}
    )
    public static void cmdGlobalSave(String[] args, IEventContext ctx) {
        TiaBot.saveDb();
        TiaBot.bot().moduleMan().saveAll();
        ctx.send("%s: Save complete.", ctx.user().tag());
    }

    @CommandProvider.Command(
            name = "help", usage="help [module]",
            desc = "Lists available modules and commands."
    )
    public static void cmdHelp(String[] args, IEventContext ctx) {
        boolean priv = ctx.channel() instanceof PrivateChannel;
        if (args.length < 1) {
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
            ModuleConfig cfg = TiaBot.bot().moduleMan().configFor(args[0]);
            if (cfg != null) {
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
            else
                ctx.send("%s: Module with ID '%s' not found!", ctx.user().tag(), args[0]);
        }
    }

    @CommandProvider.Command(
            name = "man", usage = "man <command>",
            desc = "Provides more detailed info about a command."
    )
    public static void cmdMan(String[] args, IEventContext ctx) {
        if (!ArgVerify.ONE.verify(args, ctx))
            return;
        CommandProvider.Command cmd = TiaBot.commander().command(args[0]);
        if (cmd != null) {
            String aliases = StringUtils.concat(cmd.aliases(), ", ");
            String perms = Stream.concat(
                    Arrays.stream(cmd.perms()).map(CmdPerm::toString), Arrays.stream(cmd.dcPerms()).map(Permission::toString)
            ).reduce((a, b) -> a.concat(", ").concat(b)).orElse("None");
            ctx.send("__**Command: %s**__\n%s\n\nUsage: `%s`\nAliases: %s\nPermissions: %s",
                    cmd.name(), cmd.desc(), cmd.usage(), aliases.isEmpty() ? "None" : aliases, perms);
        }
        else
            ctx.send("%s: Command '%s' not found!", ctx.user().tag(), args[0].toLowerCase());
    }

    @CommandProvider.Command(
            name = "listmods", usage = "listmods",
            desc = "Lists all modules and their statuses."
    )
    public static void cmdListMods(String[] args, IEventContext ctx) {
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

    @CommandProvider.Command(
            name = "info", usage = "info",
            desc = "Retrieves statistics and other information about the bot."
    )
    public static void cmdInfo(String[] args, IEventContext ctx) {
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

    @CommandProvider.Command(
            name = "uptime", usage = "uptime",
            desc = "Retrieves the bot's uptime."
    )
    public static void cmdUptime(String[] args, IEventContext ctx) {
        ctx.send("%s: Current uptime: %s", ctx.user().tag(), StringUtils.formatTimeElapsed(
                System.currentTimeMillis() - Discord4J.getLaunchTime().atZone(ZoneId.systemDefault()).toInstant().toEpochMilli()
        ));
    }

    @CommandProvider.Command(
            name = "mem", usage = "mem",
            desc = "Retrieves information about the bot's memory usage."
    )
    public static void cmdMem(String[] args, IEventContext ctx) {
        Runtime rt = Runtime.getRuntime();
        ctx.send("Used Memory: %.2f/%.2fMB", (rt.totalMemory() - rt.freeMemory()) / 1000000F, rt.totalMemory() / 1000000F);
    }

    @CommandProvider.Command(
            name = "revoke", aliases = "unsay", usage = "revoke [count]",
            desc = "Revokes a number of messages sent by the bot.",
            dcPerms = {Permission.MANAGE_MSG}
    )
    public static void cmdRevoke(String[] args, IEventContext ctx) {
        if (!ArgVerify.GUILD.verify(args, ctx))
            return;
        try {
            long toDelete = args.length < 1 ? 1 : Integer.parseInt(args[0]);
            if (toDelete < 1)
                throw new NumberFormatException();
            ctx.channel().messages().sequential()
                    .filter(m -> m.author().equals(TiaBot.bot().user()))
                    .sorted((a, b) -> (int)(b.timestamp() - a.timestamp()))
                    .limit(toDelete).destroyAll()
                    .fail(e -> {
                        ctx.send("%s: Encountered `%s` while trying to delete messages!", ctx.user().tag(), e.getClass().getName());
                        e.printStackTrace();
                    });
        } catch (NumberFormatException e) {
            ctx.send("%s: Invalid numeral value `%s`!", ctx.user().tag(), args[0]);
        }
    }

    @CommandProvider.Command(
            name = "addbot", aliases = "invite", usage = "addbot",
            desc = "Provides a link for adding this bot to a server"
    )
    public static void cmdAddBot(String[] args, IEventContext ctx) {
        ctx.send("%s: https://discordapp.com/oauth2/authorize?client_id=%s&scope=bot", ctx.user().tag(), TiaBot.bot().application().clientId());
    }

}
