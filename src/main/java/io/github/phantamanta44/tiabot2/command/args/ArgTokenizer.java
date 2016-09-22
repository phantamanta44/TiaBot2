package io.github.phantamanta44.tiabot2.command.args;

import java.util.HashMap;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.function.Function;

import io.github.phantamanta44.discord4j.data.wrapper.Channel;
import io.github.phantamanta44.discord4j.data.wrapper.User;
import io.github.phantamanta44.tiabot2.TiaBot;

public class ArgTokenizer {
	
	private static final Map<Class<?>, Function<ArgTokenizer, Object>> typeMap = new HashMap<>();
	
	static {
		typeMap.put(String.class, ArgTokenizer::nextString);
		typeMap.put(InlineCodeBlock.class, ArgTokenizer::nextInlineCode);
		typeMap.put(CodeBlock.class, ArgTokenizer::nextBlockCode);
		typeMap.put(Integer.TYPE, ArgTokenizer::nextInt);
		typeMap.put(User.class, ArgTokenizer::nextUserTag);
		typeMap.put(Channel.class, ArgTokenizer::nextChannelTag);
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
		StringBuilder sb = new StringBuilder(nextString());
		if (sb.charAt(0) != '`') {
			pos--;
			throw new NoSuchElementException("No such token available!");
		}
		final int start = pos - 1;
		while (!(
				sb.charAt(sb.length() - 1) == '`'
				&& (sb.charAt(sb.length() - 3) == '\\' || sb.charAt(sb.length() - 2) != '\\')
		)) {
			if (hasNext())
				sb.append(' ').append(nextString());
			else {
				pos = start;
				throw new NoSuchElementException("No such token available!");
			}
		}
		return new InlineCodeBlock(sb.substring(1, sb.length() - 1));
	}
	
	public CodeBlock nextBlockCode() {
		StringBuilder sb = new StringBuilder(nextString());
		if (!sb.substring(0, 3).equals("```")) {
			pos--;
			throw new NoSuchElementException("No such token available!");
		}
		final int start = pos - 1, str0Len = sb.length();
		while (!sb.substring(sb.length() - 3, sb.length()).equals("```")) {
			if (hasNext())
				sb.append(' ').append(nextString());
			else {
				pos = start;
				throw new NoSuchElementException("No such token available!");
			}
		}
		return new CodeBlock(sb.substring(str0Len, sb.length() - 3).trim());
	}
	
	public int nextInt() {
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
	
	@SuppressWarnings("unchecked")
	public <T> T resolveType(Class<T> type) {
		Function<ArgTokenizer, Object> mapper = typeMap.get(type);
		return mapper == null ? null : (T)mapper.apply(this);
	}
	
}
