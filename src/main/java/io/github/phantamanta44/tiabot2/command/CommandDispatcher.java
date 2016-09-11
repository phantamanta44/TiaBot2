package io.github.phantamanta44.tiabot2.command;

import com.github.fge.lambdas.Throwing;
import io.github.lukehutch.fastclasspathscanner.FastClasspathScanner;
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

import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.function.BiConsumer;
import java.util.stream.Stream;

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
            CommandProvider.Command info = m.getAnnotation(CommandProvider.Command.class);
            if (info != null) {
                CmdMeta meta = new CmdMeta(modId, info, m);
                commands.add(meta);
                for (String alias : info.aliases())
                    aliasMap.put(alias, meta);
                aliasMap.put(meta.command.name(), meta);
            }
        }
    }

    private void parse(IEventContext ctx) {
        System.out.println(String.format("%s: %s", ctx.message().author().name(), ctx.message().body()));
        if (ctx.message() != null && ctx.message().body() != null) {
            boolean priv = ctx.channel() instanceof PrivateChannel;
            String[] parts = ctx.message().body().split("\\s+");
            String prefix = priv ? globalPrefix() : TiaBot.guildCfg(ctx.guild()).get("prefix").getAsString();
            if (parts[0].startsWith(prefix)) {
                CmdMeta cmd = aliasMap.get(parts[0].substring(prefix.length()));
                if (cmd != null) {
                    if (!priv) {
                        Guild guild = ctx.guild();
                        if (TiaBot.bot().moduleMan().configFor(cmd.modId).enabled(guild)) {
                            ChannelUser user = ctx.user().of(ctx.guild()).of(ctx.channel());
                            if (!user.has(cmd.command.dcPerms()) || !Arrays.stream(cmd.command.perms()).allMatch(p -> p.test.test(user)))
                                ctx.send("%s: You don't have the necessary permissions!", ctx.user().tag());
                            else
                                cmd.executor.accept(Arrays.copyOfRange(parts, 1, parts.length), ctx);
                        }
                    }
                    else
                        cmd.executor.accept(Arrays.copyOfRange(parts, 1, parts.length), ctx);
                }
            }
        }
    }

    public Stream<CommandProvider.Command> commands() {
        return commands.stream().map(c -> c.command);
    }

    private static class CmdMeta {

        final String modId;
        final CommandProvider.Command command;
        final BiConsumer<String[], IEventContext> executor;

        CmdMeta(String modId, CommandProvider.Command cmd, Method method) {
            this.modId = modId;
            this.command = cmd;
            this.executor = Throwing.biConsumer((args, ctx) -> method.invoke(null, args, ctx));
        }

    }

}
