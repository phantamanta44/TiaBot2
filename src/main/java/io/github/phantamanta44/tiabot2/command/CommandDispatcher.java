package io.github.phantamanta44.tiabot2.command;

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
import io.github.phantamanta44.discord4j.data.wrapper.PrivateChannel;
import io.github.phantamanta44.discord4j.util.reflection.Reflect;
import io.github.phantamanta44.tiabot2.TiaBot;
import io.github.phantamanta44.tiabot2.command.args.ArgTokenizer;
import io.github.phantamanta44.tiabot2.command.args.Omittable;
import io.github.phantamanta44.tiabot2.command.args.Passed;

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
        	String prefix = ctx.channel() instanceof PrivateChannel ? globalPrefix() : TiaBot.guildCfg(ctx.guild()).get("prefix").getAsString();
        	for (String expr : ctx.message().body().split(";")) {
                expr = expr.trim();
                Object passed = null;
        		for (String seg : expr.split("\\|")) {
                    seg = seg.trim();
        			if (seg.toLowerCase().startsWith(prefix.toLowerCase())) {
                        String[] parts = seg.substring(prefix.length()).split("\\s+");
                        CmdMeta cmd = aliasMap.get(parts[0].toLowerCase());
                        if (cmd != null) {
                            try {
                                passed = tryParseCommand(cmd, ctx, Arrays.copyOfRange(parts, 1, parts.length), passed);
                            } catch (ExecutionFailedException e) {
                                ctx.send(e.getMessage());
                            }
                        } else {
                            ctx.send("%s: No such command `%s`!", ctx.user().tag(), parts[0]);
                        }
                    }
        		}
        	}
        }
    }
    
    private Object tryParseCommand(CmdMeta cmd, IEventContext ctx, String[] args, Object passed) throws ExecutionFailedException {
        if (!(ctx.channel() instanceof PrivateChannel)) {
            if (TiaBot.bot().moduleMan().configFor(cmd.modId).enabled(ctx.guild())) {
                ChannelUser cu = ctx.user().of(ctx.guild()).of(ctx.channel());
                if (!cu.has(cmd.command.dcPerms()) || !Arrays.stream(cmd.command.perms()).allMatch(p -> p.test.test(cu)))
                    throw new ExecutionFailedException("%s: You don't have the necessary permissions!", ctx.user().tag());
                else
                	return tryInvoke(cmd, args, ctx, passed);
            }
            else
            	throw new ExecutionFailedException("%s: This command isn't available on this server!", ctx.user().name());
        }
        else if (!cmd.command.guildOnly()
        		&& Arrays.stream(cmd.command.perms()).allMatch(p -> p.privTest.test(ctx.user())))
        	return tryInvoke(cmd, args, ctx, passed);
    	throw new ExecutionFailedException("%s: This command can only be used in a server!", ctx.user().tag());
    }
    
    private static Object tryInvoke(CmdMeta cmd, String[] args, IEventContext ctx, Object passed) throws ExecutionFailedException {
    	try {
    		Object[] params = new Object[cmd.paramTypes.length];
    		ArgTokenizer tokenizer = new ArgTokenizer(args);
    		for (int i = 0; i < params.length; i++) {
    		    if (cmd.paramTypes[i].getType() == IEventContext.class)
    				params[i] = ctx;
    			else if (cmd.paramTypes[i].getType() == String[].class)
    				params[i] = args;
    			else if (cmd.paramTypes[i].isAnnotationPresent(Passed.class)) {
    			    if (passed == null) {
    			        if (!cmd.paramTypes[i].isAnnotationPresent(Omittable.class)) {
                            String argType = cmd.paramTypes[i].getType().getTypeName();
                            throw new ExecutionFailedException("%s: No passed value! Expected %s.", ctx.user().tag(), argType.substring(argType.lastIndexOf('.') + 1));
                        }
                    } else if (!cmd.paramTypes[i].getType().isAssignableFrom(passed.getClass())) {
                        String argType = cmd.paramTypes[i].getType().getTypeName();
                        throw new ExecutionFailedException("%s: Invalid passed value! Expected %s.", ctx.user().tag(), argType.substring(argType.lastIndexOf('.') + 1));
                    } else {
    			        params[i] = passed;
                    }
                } else {
	    			try {
	    				params[i] = tokenizer.resolveType(cmd.paramTypes[i].getType());
	    			} catch (NoSuchElementException e) {
	    				if (!cmd.paramTypes[i].isAnnotationPresent(Omittable.class)) {
						    String argType = cmd.paramTypes[i].getType().getTypeName();
						    throw new ExecutionFailedException("%s: Invalid argument %d! Expected %s.", ctx.user().tag(), i + 1, argType.substring(argType.lastIndexOf('.') + 1));
	    				}
	    			}
    			}
    		}
    		return cmd.executor.invoke(null, params);
    	} catch (InvocationTargetException | IllegalAccessException e) {
    		Discord.logger().warn("Errored while dispatching command \"{}\"!", cmd.command.name());
    		e.printStackTrace();
    		throw new ExecutionFailedException("%s: Encountered `%s` while executing command `%s`!", ctx.user().tag(), e.getClass().getName(), cmd.command.name());
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
        final Parameter[] paramTypes;

		CmdMeta(String modId, Command cmd, Method method) {
            this.modId = modId;
            this.command = cmd;
            this.executor = method;
            this.paramTypes = method.getParameters();
        }

    }
    
    @SuppressWarnings("serial")
	private static class ExecutionFailedException extends Exception {
    	
    	ExecutionFailedException(String format, Object... args) {
    		super(String.format(format, args));
    	}
    	
    }

}
