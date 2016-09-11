package io.github.phantamanta44.tiabot2.module.core;

import io.github.phantamanta44.discord4j.core.event.context.IEventContext;
import io.github.phantamanta44.discord4j.core.module.Module;
import io.github.phantamanta44.discord4j.core.module.ModuleConfig;
import io.github.phantamanta44.discord4j.core.module.ModuleManager;
import io.github.phantamanta44.discord4j.data.Permission;
import io.github.phantamanta44.discord4j.data.wrapper.PrivateChannel;
import io.github.phantamanta44.discord4j.util.StringUtils;
import io.github.phantamanta44.tiabot2.TiaBot;
import io.github.phantamanta44.tiabot2.command.ArgVerify;
import io.github.phantamanta44.tiabot2.command.CmdPerm;
import io.github.phantamanta44.tiabot2.command.CommandDispatcher;
import io.github.phantamanta44.tiabot2.command.CommandProvider;

import java.util.Arrays;
import java.util.stream.Stream;

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

    private static final ArgVerify VERIFY_TOGGLEMOD = new ArgVerify().server().count(1);
    @CommandProvider.Command(
            name = "togglemod", usage = "togglemod <module>",
            desc = "Toggles a module on or off for this server.",
            perms = {CmdPerm.SERVER_OWNER}
    )
    public static void cmdToggleMod(String[] args, IEventContext ctx) {
        if (!VERIFY_TOGGLEMOD.verify(args, ctx))
            return;
        ModuleConfig cfg = TiaBot.bot().moduleMan().configFor(args[0]);
        if (cfg != null && !cfg.info().id().equalsIgnoreCase(MOD_ID)) {
            cfg.setEnabled(ctx.guild(), !config.enabled(ctx.guild()));
            ctx.send("%s: %s the %s module.", ctx.user().tag(), config.enabled(ctx.guild()) ? "Enabled" : "Disabled", cfg.info().name());
        }
        else
            ctx.send("%s: This module cannot be disabled!", ctx.user().tag());

    }

    private static final ArgVerify VERIFY_SETPREFIX = new ArgVerify().server().count(1);
    @CommandProvider.Command(
            name = "setprefix", usage = "setprefix <prefix>",
            desc = "Sets the command prefix for this server.",
            perms = {CmdPerm.SERVER_OWNER}
    )
    public static void cmdSetPrefix(String[] args, IEventContext ctx) {
        if (!VERIFY_SETPREFIX.verify(args, ctx))
            return;
        String newPref = StringUtils.concat(args);
        if (newPref.startsWith("`") && newPref.endsWith("`"))
            newPref = newPref.substring(1, newPref.length() - 1);
        TiaBot.guildCfg(ctx.guild()).addProperty("prefix", newPref);
        ctx.send("%s: Command prefix set to `%s`.", ctx.user().tag(), newPref);
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

    private static final ArgVerify VERIFY_MAN = new ArgVerify().count(1);
    @CommandProvider.Command(
            name = "man", usage = "man [command]",
            desc = "Provides more detailed info about a command."
    )
    public static void cmdMan(String[] args, IEventContext ctx) {
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

}
