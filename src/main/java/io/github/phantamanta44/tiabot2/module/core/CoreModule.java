package io.github.phantamanta44.tiabot2.module.core;

import io.github.phantamanta44.discord4j.core.event.context.IEventContext;
import io.github.phantamanta44.discord4j.core.module.Module;
import io.github.phantamanta44.discord4j.core.module.ModuleConfig;
import io.github.phantamanta44.discord4j.util.StringUtils;
import io.github.phantamanta44.tiabot2.TiaBot;
import io.github.phantamanta44.tiabot2.command.ArgVerify;
import io.github.phantamanta44.tiabot2.command.CmdPerm;
import io.github.phantamanta44.tiabot2.command.CommandProvider;

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
        if (!cfg.info().id().equalsIgnoreCase(MOD_ID)) {
            cfg.setEnabled(ctx.guild(), !config.enabled(ctx.guild()));
            ctx.send("%s the %s module.", config.enabled(ctx.guild()) ? "Enabled" : "Disabled", cfg.info().name());
        }
        else
            ctx.send("This module cannot be disabled!");

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
        TiaBot.guildCfg(ctx.guild()).addProperty("prefix", newPref);
        ctx.send("Prefix set to `%s`.", newPref);
    }

    @CommandProvider.Command(
            name = "halt", usage = "halt [reason]",
            desc = "Halts the bot.",
            perms = {CmdPerm.BOT_OWNER}
    )
    public static void cmdHalt(String[] args, IEventContext ctx) {
        if (args.length < 1) {
            ctx.send("Shutting down!");
            Runtime.getRuntime().exit(130);
        }
        switch (args[0].toLowerCase()) {
            case "reboot":
                Runtime.getRuntime().exit(32);
            case "update":
                Runtime.getRuntime().exit(33);
            default:
                ctx.send("Unknown exit status!");
        }
    }

}
