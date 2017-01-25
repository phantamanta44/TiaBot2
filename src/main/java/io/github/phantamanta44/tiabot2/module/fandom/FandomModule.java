package io.github.phantamanta44.tiabot2.module.fandom;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import io.github.phantamanta44.discord4j.core.event.context.IEventContext;
import io.github.phantamanta44.discord4j.core.module.Module;
import io.github.phantamanta44.discord4j.core.module.ModuleConfig;
import io.github.phantamanta44.discord4j.util.CollUtils;
import io.github.phantamanta44.discord4j.util.io.IOUtils;
import io.github.phantamanta44.tiabot2.command.Command;
import io.github.phantamanta44.tiabot2.command.CommandProvider;
import sx.blah.discord.api.internal.json.objects.EmbedObject;
import sx.blah.discord.util.EmbedBuilder;

import java.util.Map;

@CommandProvider(FandomModule.MOD_ID)
public class FandomModule {

    public static final String MOD_ID = "fandom";
    @Module(
            id = "fandom", name = "Fandom", author = "Phanta",
            desc = "This module provides fandom-centric content."
    )
    public static void initModule(ModuleConfig cfg) {
        // NO-OP
    }

    private static final String PONY_URL = "https://cdn.rawgit.com/phantamanta44/08ed9291d3bfdfe0ff8e2a83f427e010/raw/ponies.json";

    @Command(
            name = "bestpony", usage = "bestpony", aliases = "bestpone",
            desc = "Finds out who best pony is."
    )
    public static void cmdBestPony(IEventContext ctx) {
        IOUtils.requestXml(PONY_URL).map(xml -> new JsonParser().parse(xml).getAsJsonObject()).done(ponies -> {
            JsonObject bestPony = CollUtils.random(ponies.entrySet()).getValue().getAsJsonObject();
            EmbedObject embed = new EmbedBuilder()
                    .withTitle(bestPony.get("name").getAsString())
                    .withDescription("is best pony!")
                    .withImage(bestPony.get("image").getAsString())
                    .withColor(Integer.parseInt(bestPony.get("coat").getAsString().substring(1), 16))
                    .build();
            ctx.channel().send(embed);
        });
    }

}
