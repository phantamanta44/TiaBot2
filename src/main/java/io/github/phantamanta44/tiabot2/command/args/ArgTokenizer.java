package io.github.phantamanta44.tiabot2.command.args;

import io.github.phantamanta44.discord4j.core.module.ModuleConfig;
import io.github.phantamanta44.discord4j.data.wrapper.Channel;
import io.github.phantamanta44.discord4j.data.wrapper.User;
import io.github.phantamanta44.tiabot2.TiaBot;
import io.github.phantamanta44.tiabot2.command.Command;

import java.lang.reflect.Type;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.function.Function;

public class ArgTokenizer {
    
    private static final Map<Type, Function<ArgTokenizer, Object>> typeMap = new HashMap<>();
    
    static {
        typeMap.put(String.class, ArgTokenizer::nextString);
        typeMap.put(InlineCodeBlock.class, ArgTokenizer::nextInlineCode);
        typeMap.put(CodeBlock.class, ArgTokenizer::nextBlockCode);
        typeMap.put(Integer.class, ArgTokenizer::nextInt);
        typeMap.put(User.class, ArgTokenizer::nextUserTag);
        typeMap.put(Channel.class, ArgTokenizer::nextChannelTag);
        typeMap.put(Command.class, ArgTokenizer::nextCommand);
        typeMap.put(ModuleConfig.class, ArgTokenizer::nextModule);
    }

    private String[] parts;
    private int pos;
    
    public ArgTokenizer(String[] args) {
        this.parts = args;
        this.pos = 0;
    }
    
    public ArgTokenizer(String str) {
        this(str.split("\\s"));
    }
    
    public ArgTokenizer reset() {
        pos = 0;
        return this;
    }
    
    public boolean hasNext() {
        return pos < parts.length;
    }
    
    public String nextString() {
        if (!hasNext())
            throw new NoSuchElementException("No such token available!");
        return parts[pos++];
    }
    
    public InlineCodeBlock nextInlineCode() {
        final int start = pos - 1;
        try {
            StringBuilder sb = new StringBuilder(nextString());
            if (sb.charAt(0) != '`') {
                pos--;
                throw new NoSuchElementException("No such token available!");
            }
            while (!(sb.charAt(sb.length() - 1) == '`' && sb.charAt(sb.length() - 2) != '\\')) {
                if (hasNext())
                    sb.append(' ').append(nextString());
                else {
                    pos = start;
                    throw new NoSuchElementException("No such token available!");
                }
            }
            return new InlineCodeBlock(sb.substring(1, sb.length() - 1));
        } catch (IndexOutOfBoundsException e) {
            pos = start;
            throw new NoSuchElementException("No such token available!");
        }
    }
    
    public CodeBlock nextBlockCode() {
        final int start = pos;
        try {
            StringBuilder sb = new StringBuilder(nextString());
            if (!sb.substring(0, 3).equals("```")) {
                pos--;
                throw new NoSuchElementException("No such token available!");
            }
            final int str0Len = sb.length();
            while (sb.length() <= str0Len || !sb.substring(sb.length() - 3, sb.length()).equals("```")) {
                if (hasNext())
                    sb.append(' ').append(nextString());
                else {
                    pos = start;
                    throw new NoSuchElementException("No such token available!");
                }
            }
            return new CodeBlock(sb.substring(str0Len, sb.length() - 3).trim(), sb.substring(3, str0Len));
        } catch (IndexOutOfBoundsException e) {
            pos = start;
            throw new NoSuchElementException("No such token available!");
        }
    }
    
    public Integer nextInt() {
        try {
            String str = nextString();
            return Integer.parseInt(str);
        } catch (NumberFormatException e) {
            pos--;
            throw new NoSuchElementException("No such token available!");
        }
    }
    
    public User nextUserTag() {
        String tag = nextString();
        if (!tag.startsWith("<@") || !tag.endsWith(">") || tag.charAt(2) == '&') {
            pos--;
            throw new NoSuchElementException("No such token available!");
        }
        if (tag.charAt(2) == '!')
            tag = tag.substring(3, tag.length() - 1);
        else
            tag = tag.substring(2, tag.length() - 1);
        User user = TiaBot.bot().user(tag);
        if (user == null) {
            pos--;
            throw new NoSuchElementException("Unknown user!");
        }
        return user;
    }
    
    public Channel nextChannelTag() {
        String tag = nextString();
        if (!tag.startsWith("<#") || !tag.endsWith(">")) {
            pos--;
            throw new NoSuchElementException("No such token available!");
        }
        Channel chan = TiaBot.bot().channel(tag.substring(2, tag.length() - 1));
        if (chan == null) {
            pos--;
            throw new NoSuchElementException("Unknown channel!");
        }
        return chan;
    }
    
    public Command nextCommand() {
        String cmdName = nextString();
        Command cmd = TiaBot.commander().command(cmdName);
        if (cmd == null) {
            pos--;
            throw new NoSuchElementException("Unknown command!");
        }
        return cmd;
    }
    
    public ModuleConfig nextModule() {
        String modId = nextString();
        ModuleConfig cfg = TiaBot.bot().moduleMan().configFor(modId);
        if (cfg == null) {
            pos--;
            throw new NoSuchElementException("Unknown command!");
        }
        return cfg;
    }
    
    @SuppressWarnings("unchecked")
    public <T> T resolveType(Type type) {
        Function<ArgTokenizer, Object> mapper = typeMap.get(type);
        return mapper == null ? null : (T)mapper.apply(this);
    }
    
}
