package io.github.phantamanta44.tiabot2.module.script;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintStream;

import io.github.phantamanta44.discord4j.core.event.context.IEventContext;
import io.github.phantamanta44.discord4j.core.module.Module;
import io.github.phantamanta44.discord4j.core.module.ModuleConfig;
import io.github.phantamanta44.discord4j.util.http.HttpUtils;
import io.github.phantamanta44.tiabot2.command.ArgVerify;
import io.github.phantamanta44.tiabot2.command.CmdPerm;
import io.github.phantamanta44.tiabot2.command.CommandProvider;

@CommandProvider(ScriptingModule.MOD_ID)
public class ScriptingModule {

    private static ModuleConfig config;

    public static final String MOD_ID = "script";
    @Module(
            id = MOD_ID, name = "Scripting", author = "Phanta",
            desc = "This module provides a way to write custom commands in JS."
    )
    public static void moduleInit(ModuleConfig cfg) {
        config = cfg;
    }
    
    @CommandProvider.Command(
    		name = "createcc", usage = "createcc <command> <srcUrl>",
    		aliases = "mkcc", perms = CmdPerm.SERVER_OWNER,
    		desc = "Creates a custom command from the linked script.",
    		docs = "Creates a custom command from the linked script. Documentation on the scripting API is available at https://example.com."
	) // TODO Deploy some kind of API docs for this thing
    public static void cmdCreateCC(String[] args, IEventContext ctx) {
    	if (!ArgVerify.GUILD_TWO.verify(args, ctx))
    		return;
    	args[0] = args[0].toLowerCase();
    	if (!args[0].matches("\\w+")) {
    		ctx.send("%s: Command names must be alphanumeric!", ctx.user().tag());
    		return;
    	}
    	File cmdFile = new File(config.dataDirFor(ctx.guild()), args[0] + ".js");
    	if (cmdFile.exists()) {
    		ctx.send("%s: A command already exists by this name!", ctx.user().tag());
    		return;
    	}
    	try {
    		HttpUtils.requestXml(args[1]).done(s -> {
    			try (PrintStream out = new PrintStream(new FileOutputStream(cmdFile))) {
    				out.println(s);
    				CustomCommandExecutor.register(ctx.guild().id(), args[0]);
    				ctx.send("%s: Registered custom command `%s`!", ctx.user().tag(), args[0]);
    			} catch (IOException e) {
    				ctx.send("%s: Encountered a `%s` while reading the script!", ctx.user().tag());
    			}
    		});
    	} catch (IllegalArgumentException e) {
    		ctx.send("%s: Not a valid URL!", ctx.user().tag());
    	}
    }
    
    @CommandProvider.Command(
    		name = "destroycc", usage = "destroycc <command>",
    		aliases = "rmcc", perms = CmdPerm.SERVER_OWNER,
    		desc = "Deletes a previously created custom command."
	)
    public static void cmdDestroyCC(String[] args, IEventContext ctx) {
    	if (!ArgVerify.GUILD_ONE.verify(args, ctx))
    		return;
    	args[0] = args[0].toLowerCase();
    	File cmdFile = new File(config.dataDirFor(ctx.guild()), args[0] + ".js");
    	if (cmdFile.delete()) {
    		CustomCommandExecutor.unregister(ctx.guild().id(), args[0]);
    		ctx.send("%s: Successfully deleted command!", ctx.user().tag());
    	}
    	else
    		ctx.send("%s: No such command!", ctx.user().tag());
    }
    
    @CommandProvider.Command(
    		name = "listccs", usage = "listccs", aliases = "lscc",
    		desc = "Lists available custom commands on this server."
	)
    public static void cmdListCCs(String[] args, IEventContext ctx) {
    	if (!ArgVerify.GUILD.verify(args, ctx))
    		return;
    	ctx.send(String.format("__**Custom Commands: %s**__\n%s", ctx.guild().name(),
            CustomCommandExecutor.commands(ctx.guild().id())
                    .map(c -> String.format("- `%s`", c))
                    .reduce((a, b) -> a.concat("\n").concat(b)).orElse("Nothing to see here.")
        ));
    }

}
