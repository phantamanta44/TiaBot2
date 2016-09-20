package io.github.phantamanta44.tiabot2.module.script;

import java.util.*;
import java.util.stream.Stream;

import io.github.phantamanta44.discord4j.core.StaticInit;
import io.github.phantamanta44.discord4j.core.event.Events;
import io.github.phantamanta44.discord4j.core.event.Handler;
import io.github.phantamanta44.discord4j.core.event.context.IEventContext;
import io.github.phantamanta44.discord4j.data.wrapper.Bot;
import io.github.phantamanta44.discord4j.data.wrapper.ChannelUser;
import io.github.phantamanta44.discord4j.data.wrapper.Guild;
import io.github.phantamanta44.discord4j.data.wrapper.PrivateChannel;
import io.github.phantamanta44.discord4j.util.io.IOUtils;
import io.github.phantamanta44.tiabot2.TiaBot;
import io.github.phantamanta44.tiabot2.command.CommandDispatcher;
import io.github.phantamanta44.tiabot2.jsapi.ScriptExecutor;
import io.github.phantamanta44.tiabot2.module.script.host.*;
import org.mozilla.javascript.*;

@Handler(ScriptingModule.MOD_ID)
public class CustomCommandExecutor {

    private static final Map<String, Collection<String>> loaded = new HashMap<>();

	public static void register(String guild, String name) {
		Collection<String> forGuild = loaded.get(guild);
		if (forGuild == null) {
			forGuild = new HashSet<>();
			loaded.put(guild, forGuild);
		}
		forGuild.add(name);
	}
	
	public static void unregister(String guild, String name) {
		Collection<String> forGuild = loaded.get(guild);
		if (forGuild != null) {
			forGuild.remove(name);
			if (forGuild.isEmpty())
				loaded.remove(guild);
		}
	}

    public static void clearRegistry() {
        loaded.values().forEach(Collection::clear);
        loaded.clear();
    }
	
	public static Stream<String> commands(String guild) {
		return loaded.containsKey(guild) ? loaded.get(guild).stream() : Stream.of();
	}
	
	@Handler.On(Events.MSG_GET)
	public static void onMessage(IEventContext ctx) {
		if (ctx.message() != null && ctx.message().body() != null) {
			String msg = ctx.message().body();
			String prefix = TiaBot.guildCfg(ctx.guild()).get("prefix").getAsString();
			if (msg.toLowerCase().startsWith(prefix.toLowerCase())) {
                String[] parts = msg.substring(prefix.length()).split("\\s+");
				if (loaded.containsKey(ctx.guild().id()) && loaded.get(ctx.guild().id()).contains(parts[0].toLowerCase())) {
                    IOUtils.readFile(ScriptingModule.forCommand(ctx.guild(), parts[0]))
                        .done(s -> {
                            try {
                                Scriptable scope = ScriptExecutor.start(true);
                                ScriptableObject.defineClass(scope, HostObjectDiscordAPI.class);
                                ScriptableObject.defineClass(scope, HostObjectGuild.class);
                                ScriptableObject.defineClass(scope, HostObjectChannel.class);
                                ScriptableObject.defineClass(scope, HostObjectUser.class);
                                ScriptableObject.defineClass(scope, HostObjectMessage.class);
                                ScriptableObject.defineClass(scope, HostObjectRole.class);
                                ScriptableObject.putConstProperty(scope, "api", Context.getCurrentContext().newObject(scope, "DiscordAPI"));
                                ScriptableObject.putConstProperty(scope, "guild", HostObjectGuild.impl(ctx.guild(), scope));
                                ScriptableObject.putConstProperty(scope, "channel", HostObjectChannel.impl(ctx.channel(), scope));
                                ScriptableObject.putConstProperty(scope, "sender", HostObjectUser.impl(ctx.user().of(ctx.guild()), scope));
                                ScriptableObject.putProperty(scope, "args", Arrays.copyOfRange(parts, 1, parts.length));
                                ScriptExecutor.execute(parts[0].toLowerCase(), s.stream().reduce((a, b) -> a.concat("\n").concat(b)).orElse(""));
                                ((HostObjectDiscordAPI) scope.get("api", scope)).flushBufferSafe(ctx);
                            } catch (RhinoException e) {
                                ctx.send(e.getMessage());
                            } catch (Exception e) {
                                ctx.send("%s: Encountered `%s` while executing command!", ctx.user().tag(), e.getClass().getName());
                            }
                        }).fail(e -> {
                            ctx.send("%s: Encountered `%s` while executing command!", ctx.user().tag(), e.getClass().getName());
                        });
                }
			}
		}
	}

}
