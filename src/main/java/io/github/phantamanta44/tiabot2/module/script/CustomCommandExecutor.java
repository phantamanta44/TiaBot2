package io.github.phantamanta44.tiabot2.module.script;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.stream.Stream;

import io.github.phantamanta44.discord4j.core.event.Events;
import io.github.phantamanta44.discord4j.core.event.Handler;
import io.github.phantamanta44.discord4j.core.event.context.IEventContext;

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
	
	public static Stream<String> commands(String guild) {
		return loaded.containsKey(guild) ? loaded.get(guild).stream() : Stream.of();
	}
	
	@Handler.On(Events.MSG_GET)
	public static void onMessage(IEventContext ctx) {
		
	}
	
}
