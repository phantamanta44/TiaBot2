package io.github.phantamanta44.tiabot2.command;

import io.github.phantamanta44.discord4j.util.StringUtils;

public class ArgTokenizer {

	private String orig;
	private int pos;
	
	public ArgTokenizer(String[] args) {
		this(StringUtils.concat(args));
	}
	
	public ArgTokenizer(String str) {
		this.orig = str;
		this.pos = 0;
	}
	
	public ArgTokenizer reset() {
		pos = 0;
		return this;
	}
	
	public boolean hasNext() {
		return pos < orig.length() + 1;
	}
	
	public char nextChar() {
		return orig.charAt(pos++);
	}
	
	public String nextString() {
		if (pos >= orig.length())
			return null;
		int start = pos;
		while (pos < orig.length() && !Character.isSpaceChar(orig.charAt(pos)))
			pos++;
		return orig.substring(start, pos).trim();
	}
	
	public String nextInlineCode() {
		// TODO Implement
		return null;
	}
	
	public String nextBlockCode() {
		// TODO Implement
		return null;
	}
	
	public int nextInt() {
		// TODO Implement
		return -1;
	}
	
	public User nextUserTag() {
		// TODO Implement
		return null;
	}
	
	public Channel nextChannelTag() {
		// TODO Implement
		return null;
	}

	public Role nextRoleTag() {
		// TODO Implement
		return null;
	}
	
}
