package io.github.phantamanta44.tiabot2;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import io.github.phantamanta44.discord4j.core.Discord;
import io.github.phantamanta44.discord4j.data.wrapper.Bot;
import io.github.phantamanta44.discord4j.data.wrapper.Guild;
import io.github.phantamanta44.tiabot2.command.CommandDispatcher;
import sx.blah.discord.Discord4J;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;

public class TiaBot {

    private static final File CFG_FILE = new File("tiabot_cfg.json");

    private static JsonObject config;
    private static Bot bot;
    private static CommandDispatcher commander;

    public static void main(String[] args) {
        try (BufferedReader in = new BufferedReader(new FileReader(CFG_FILE))) {
            config = new JsonParser().parse(in).getAsJsonObject();
        } catch (IOException e) {
            Discord.logger().error("Failed to read config!");
            fail(e, -1);
        }
        ((Logger)Discord4J.LOGGER).setLevel(Level.INFO);
        Discord.authenticate(config.get("token").getAsString(), config.get("ownerId").getAsString())
                .fail(e -> {
                    Discord.logger().error("Authentication failed!");
                    fail(e, 34);
                })
                .done(b -> {
                    bot = b;
                    commander = new CommandDispatcher(b.eventBus(), b.moduleMan());
                    Discord.logger().info("Initialization complete.");
                });
    }

    public static JsonObject config() {
        return config;
    }

    public static Bot bot() {
        return bot;
    }

    private static void fail(Throwable e, int code) {
        e.printStackTrace();
        Runtime.getRuntime().exit(code);
    }

    public static JsonObject guildCfg(Guild guild) {
        JsonObject guilds = guildSection();
        if (!guilds.has(guild.id())) {
            JsonObject gElem = new JsonObject();
            gElem.addProperty("prefix", CommandDispatcher.globalPrefix());
            guilds.add(guild.id(), gElem);
            return gElem;
        }
        return guilds.get(guild.id()).getAsJsonObject();
    }

    private static JsonObject guildSection() {
        if (!config.has("guilds"))
            config.add("guilds", new JsonObject());
        return config.get("guilds").getAsJsonObject();
    }

}
