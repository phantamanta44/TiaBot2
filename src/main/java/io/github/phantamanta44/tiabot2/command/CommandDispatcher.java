package io.github.phantamanta44.tiabot2.command;

import java.lang.reflect.AnnotatedType;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.stream.Stream;

import io.github.phantamanta44.discord4j.core.Discord;
import io.github.phantamanta44.discord4j.core.event.EventBus;
import io.github.phantamanta44.discord4j.core.event.Events;
import io.github.phantamanta44.discord4j.core.event.context.IEventContext;
import io.github.phantamanta44.discord4j.core.module.ModuleManager;
import io.github.phantamanta44.discord4j.data.wrapper.ChannelUser;
import io.github.phantamanta44.discord4j.data.wrapper.Guild;
import io.github.phantamanta44.discord4j.data.wrapper.PrivateChannel;
import io.github.phantamanta44.discord4j.util.reflection.Reflect;
import io.github.phantamanta44.tiabot2.TiaBot;
import io.github.phantamanta44.tiabot2.command.args.ArgTokenizer;
import io.github.phantamanta44.tiabot2.command.args.Omittable;

public class CommandDispatcher {

    public static String globalPrefix() {
        return TiaBot.config().get("prefix").getAsString();
    }

    private final List<CmdMeta> commands = new CopyOnWriteArrayList<>();
    private final Map<String, CmdMeta> aliasMap = new ConcurrentHashMap<>();

    public CommandDispatcher(EventBus events, ModuleManager mods) {
        Discord.logger().info("Scanning for commands...");
        Reflect.types().tagged(CommandProvider.class).find().forEach(this::registerAll);
        events.on(Events.MSG_GET, this::parse);
    }

    private void registerAll(Class<?> prov) {
        String modId = prov.getAnnotation(CommandProvider.class).value();
        for (Method m : prov.getDeclaredMethods()) {
            Command info = m.getAnnotation(Command.class);
            if (info != null) {
                CmdMeta meta = new CmdMeta(modId, info, m);
                commands.add(meta);
                for (String alias : info.aliases())
                    aliasMap.put(alias.toLowerCase(), meta);
                aliasMap.put(meta.command.name().toLowerCase(), meta);
            }
        }
    }

    private void parse(IEventContext ctx) {
        if (ctx.message() != null && ctx.message().body() != null) {
            boolean priv = ctx.channel() instanceof PrivateChannel;
            String msg = ctx.message().body();
            String prefix = priv ? globalPrefix() : TiaBot.guildCfg(ctx.guild()).get("prefix").getAsString();
            if (msg.toLowerCase().startsWith(prefix.toLowerCase())) {
                String[] parts = msg.substring(prefix.length()).split("\\s+");
                CmdMeta cmd = aliasMap.get(parts[0].toLowerCase());
                if (cmd != null) {
                    if (!priv) {
                        Guild guild = ctx.guild();
                        if (TiaBot.bot().moduleMan().configFor(cmd.modId).enabled(guild)) {
                            ChannelUser user = ctx.user().of(ctx.guild()).of(ctx.channel());
                            if (!user.has(cmd.command.dcPerms()) || !Arrays.stream(cmd.command.perms()).allMatch(p -> p.test.test(user)))
                                ctx.send("%s: You don't have the necessary permissions!", ctx.user().tag());
                            else
                            	tryInvoke(cmd, Arrays.copyOfRange(parts, 1, parts.length), ctx);
                        }
                    }
                    else if (!cmd.command.guildOnly()
                    		&& Arrays.stream(cmd.command.perms()).allMatch(p -> p.privTest.test(ctx.user())))
                    	tryInvoke(cmd, Arrays.copyOfRange(parts, 1, parts.length), ctx);
                }
            }
        }
    }
    
    private static void tryInvoke(CmdMeta cmd, String[] args, IEventContext ctx) {
    	try {
    		Object[] params = new Object[cmd.paramTypes.length];
    		ArgTokenizer tokenizer = new ArgTokenizer(args);
    		for (int i = 0; i < params.length; i++) {
    			if (cmd.paramTypes[i].getType() == IEventContext.class)
    				params[i] = ctx;
    			else if (cmd.paramTypes[i].getType() == String[].class)
    				params[i] = args;
    			else {
	    			try {
	    				params[i] = tokenizer.resolveType(cmd.paramTypes[i].getType());
	    			} catch (NoSuchElementException e) {
	    				if (!cmd.paramTypes[i].isAnnotationPresent(Omittable.class)) {
		    				ctx.send("%s: Invalid argument %d! Expected %s.", i + 1, cmd.paramTypes[i].getType().getTypeName());
		    				return;
	    				}
	    			}
    			}
    		}
    		cmd.executor.invoke(null, params);
    	} catch (InvocationTargetException | IllegalAccessException e) {
    		Discord.logger().warn("Errored while dispatching command \"{}\"!", cmd.command.name());
    		e.printStackTrace();
    	}
    }

    public Stream<Command> commands() {
        return commands.stream().map(c -> c.command);
    }

    public Stream<Command> commands(String modId) {
        return commands.stream().filter(c -> c.modId.equals(modId)).map(c -> c.command);
    }

    public Command command(String alias) {
        CmdMeta cmd = aliasMap.get(alias.toLowerCase());
        return cmd == null ? null : cmd.command;
    }

    private static class CmdMeta {

        final String modId;
        final Command command;
        final Method executor;
        final AnnotatedType[] paramTypes;

		CmdMeta(String modId, Command cmd, Method method) {
            this.modId = modId;
            this.command = cmd;
            this.executor = method;
            this.paramTypes = Arrays.stream(method.getParameters())
            		.map(Parameter::getAnnotatedType)
            		.toArray(AnnotatedType[]::new);
        }

    }

}
