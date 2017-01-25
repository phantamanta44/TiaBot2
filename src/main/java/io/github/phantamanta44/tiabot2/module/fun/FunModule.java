package io.github.phantamanta44.tiabot2.module.fun;

import io.github.phantamanta44.discord4j.core.event.context.IEventContext;
import io.github.phantamanta44.discord4j.core.module.Module;
import io.github.phantamanta44.discord4j.core.module.ModuleConfig;
import io.github.phantamanta44.discord4j.data.wrapper.User;
import io.github.phantamanta44.discord4j.util.StringUtils;
import io.github.phantamanta44.tiabot2.command.Command;
import io.github.phantamanta44.tiabot2.command.CommandProvider;
import io.github.phantamanta44.tiabot2.command.args.Omittable;
import org.apache.commons.lang3.StringEscapeUtils;
import org.apache.commons.lang3.mutable.MutableInt;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import sx.blah.discord.api.internal.json.objects.EmbedObject;
import sx.blah.discord.util.EmbedBuilder;

import java.util.*;
import java.util.stream.Collectors;

@CommandProvider(FunModule.MOD_ID)
public class FunModule {

    public static final String MOD_ID = "fun";
    @Module(
            id = MOD_ID, name = "Fun", author = "Phanta",
            desc = "This module provides a lot of random stuff for fun."
    )
    public static void moduleInit(ModuleConfig cfg) {
        // NO-OP
    }

    @Command(
            name = "bash", usage = "bash [quote#]",
            desc = "Retrieves an IRC quote from the Bash database."
    )
    public static void cmdBash(@Omittable Integer quoteId, IEventContext ctx) {
        try {
            Document doc;
            if (quoteId != null) {
                if (quoteId < 0) {
                    ctx.send("Invalid quote number \"%s\"!", quoteId);
                    return;
                }
                doc = Jsoup.connect(String.format("http://bash.org/?quote=%d", quoteId)).get();
            }
            else
                doc = Jsoup.connect("http://bash.org/?random").get();
            Element qList = doc.getElementsByAttributeValue("valign", "top").get(0);
            String id = qList.child(0).child(0).child(0).text(), quote = qList.child(1).html().replaceAll("<br>", "\n");
            EmbedObject embed = new EmbedBuilder()
                    .withTitle(String.format("Quote %s", id))
                    .withDescription(StringEscapeUtils.unescapeHtml4(quote))
                    .withColor(0xEF5350)
                    .build();
            ctx.channel().send(embed);
        } catch (IndexOutOfBoundsException ex) {
            ctx.send("%s: Quote does not exist!", ctx.user().tag());
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    @Command(
            name = "flipacoin", usage = "flipacoin [count#]", aliases = "flip",
            desc = "Flips a coin a certain number of times."
    )
    public static void cmdFlipACoin(@Omittable Integer count, IEventContext ctx) {
        int num = count != null ? count : 1;
        if (num < 1)
            ctx.send("%s: Flip count too low!", ctx.user().tag());
        else if (num == 1)
            ctx.send("%s: Got %s!", ctx.user().tag(), Math.random() >= 0.5F ? "heads" : "tails");
        else {
            int heads = 0, tails = 0;
            StringBuilder sb = new StringBuilder();
            Random random = new Random();
            for (int i = 0; i < num; i++) {
                if (random.nextBoolean()) {
                    heads++;
                    sb.append("H");
                } else {
                    tails++;
                    sb.append("T");
                }
            }
            String coinStr = sb.toString();
            if (coinStr.length() > 40)
                coinStr = coinStr.substring(0, 40) + "...";
            EmbedObject embed = new EmbedBuilder()
                    .withTitle(String.format("Flipped %d Coins!", num))
                    .withDescription(coinStr)
                    .appendField("Heads", String.format("%d (%.1f%%)", heads, 100 * heads / (float)num), true)
                    .appendField("Tails", String.format("%d (%.1f%%)", tails, 100 * tails / (float)num), true)
                    .withColor(0x2196F3)
                    .build();
            ctx.channel().send(embed);
        }
    }

    @Command(
            name = "rolladice", usage = "rolladice [faces#] [count#]", aliases = "roll",
            desc = "Rolls a number of fair dice."
    )
    public static void cmdRollADice(@Omittable Integer faces, @Omittable Integer count, IEventContext ctx) {
        int numFaces = faces != null ? faces : 6;
        int numDice = count != null ? count : 1;
        if (numFaces < 2)
            ctx.send("%s: Face count too low!", ctx.user().tag());
        else if (numDice < 1)
            ctx.send("%s: Dice count too low!", ctx.user().tag());
        else if (numDice > 100)
            ctx.send("%s: Dice count too high!", ctx.user().tag());
        else if (numDice == 1)
            ctx.send("%s: Rolled a %d!", ctx.user().tag(), 1 + (int)Math.floor(Math.random() * numFaces));
        else {
            int[] rolls = new int[numDice];
            Random random = new Random();
            for (int i = 0; i < numDice; i++)
                rolls[i] = random.nextInt(numFaces) + 1;
            String diceStr = Arrays.stream(rolls)
                    .limit(10)
                    .mapToObj(Integer::toString)
                    .collect(Collectors.joining(", "));
            if (rolls.length > 10)
                diceStr += "...";
            int sum = Arrays.stream(rolls).sum();
            Map<Integer, MutableInt> freq = new HashMap<>();
            for (int roll : rolls)
                freq.computeIfAbsent(roll, key -> new MutableInt(0)).increment();
            List<Map.Entry<Integer, MutableInt>> sorted = freq.entrySet().stream()
                    .sequential()
                    .sorted(Comparator.comparingInt(a -> a.getValue().intValue()))
                    .collect(Collectors.toList());
            Map.Entry<Integer, MutableInt> least = sorted.get(0), most = sorted.get(freq.size() - 1);
            EmbedObject embed = new EmbedBuilder()
                    .withTitle(String.format("Rolled %d D%d!", numDice, numFaces))
                    .withDescription(diceStr)
                    .appendField("Sum", Integer.toString(sum), true)
                    .appendField("Average", String.format("%.2f", sum / (float)numDice), true)
                    .appendField("Most Rolled", String.format("%d, rolled %d time(s)", most.getKey(), most.getValue().intValue()), true)
                    .appendField("Least Rolled", String.format("%d, rolled %d time(s)", least.getKey(), least.getValue().intValue()), true)
                    .withColor(0x2196F3)
                    .build();
            ctx.channel().send(embed);
        }
    }

    @Command(
            name = "levelcheck", usage = "levelcheck <@user> <property...>", aliases = "checklevel",
            desc = "Checks a property of a user."
    )
    public static void cmdLevelCheck(User user, String[] args, IEventContext ctx) {
        if (args.length <= 1) {
            ctx.send("%s: No property specified!", ctx.user().tag());
            return;
        }
        String prop = Arrays.stream(args).skip(1L).collect(Collectors.joining(" "));
        Random rand = new Random(prop.hashCode() ^ user.id().hashCode());
        ctx.send("%s: Detected %s of %.1f%% in %s", ctx.user().tag(), prop, rand.nextDouble() * 100, user.tag());
    }

}
