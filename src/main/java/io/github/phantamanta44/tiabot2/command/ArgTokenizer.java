package io.github.phantamanta44.tiabot2.command;

import java.util.NoSuchElementException;

import io.github.phantamanta44.discord4j.data.wrapper.Channel;
import io.github.phantamanta44.discord4j.data.wrapper.Guild;
import io.github.phantamanta44.discord4j.data.wrapper.Role;
import io.github.phantamanta44.discord4j.data.wrapper.User;
import io.github.phantamanta44.discord4j.util.StringUtils;
import io.github.phantamanta44.tiabot2.TiaBot;

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
			throw new NoSuchElementException("No such token available!");
		int start = pos;
		while (pos < orig.length() && !Character.isSpaceChar(orig.charAt(pos)))
			pos++;
		return orig.substring(start, pos).trim();
	}
	
	public String nextString(int length) {
		if (pos + length >= orig.length())
			throw new NoSuchElementException("No such token available!");
		pos += length;
		return orig.substring(pos - length, pos);
	}
	
	public String nextUntil(char to) {
		int at = orig.indexOf(to, pos);
		if (at == -1)
			throw new NoSuchElementException("No such token available!");
		String found = orig.substring(pos, at);
		pos = at + 1;
		return found;
	}
	
	public String nextUntil(String to) {
		int at = orig.indexOf(to, pos);
		if (at == -1)
			throw new NoSuchElementException("No such token available!");
		String found = orig.substring(pos, at);
		pos = at + to.length();
		return found;
	}
	
	public String nextInlineCode() {
		int opening = -1, ind = pos;
		while (ind < orig.length()) {
			if (orig.charAt(ind) == '`') {
				if (opening == -1)
					opening = ind + 1;
				else {
					pos = ind + 1;
					return orig.substring(opening, ind - 1);
				}
			} else if (orig.charAt(ind) == '\\') {
				ind += 2;
				continue;
			}
			ind++;
		}
		throw new NoSuchElementException("No such token available!");
	}
	
	public String nextBlockCode() {
		int opening = -1, ind = pos;
		while (ind < orig.length() - 2) {
			if (orig.charAt(ind) == '`'
					&& orig.charAt(ind + 1) == '`'
					&& orig.charAt(ind + 2) == '`') {
				if (opening == -1)
					opening = ind + 3;
				else {
					pos = ind + 3;
					return orig.substring(opening, ind - 1);
				}
			}
		}
		throw new NoSuchElementException("No such token available!");
	}
	
	public int nextInt() {
		final int current = pos;
		try {
			return Integer.parseInt(nextString());
		} catch (NumberFormatException e) {
			pos = current;
			throw new NoSuchElementException("No such token available!");
		}
	}
	
	public User nextUserTag() {
		final int current = pos;
		String tag = nextString();
		if (!tag.startsWith("<@") || !tag.startsWith(">") || tag.charAt(2) == '&') {
			pos = current;
			throw new NoSuchElementException("No such token available!");
		}
		if (tag.charAt(2) == '!')
			tag = tag.substring(3, tag.length() - 1);
		else
			tag = tag.substring(2, tag.length() - 1);
		User user = TiaBot.bot().user(tag);
		if (user == null) {
			pos = current;
			throw new NoSuchElementException("No such token available!");
		}
		return user;
	}
	
	public Channel nextChannelTag() {
		final int current = pos;
		String tag = nextString();
		if (!tag.startsWith("<#") || !tag.startsWith(">")) {
			pos = current;
			throw new NoSuchElementException("No such token available!");
		}
		Channel chan = TiaBot.bot().channel(tag.substring(2, tag.length() - 1));
		if (chan == null) {
			pos = current;
			throw new NoSuchElementException("No such token available!");
		}
		return chan;
	}

	public Role nextRoleTag(Guild guild) {
		final int current = pos;
		String tag = nextString();
		if (!tag.startsWith("<@&") || !tag.startsWith(">")) {
			pos = current;
			throw new NoSuchElementException("No such token available!");
		}
		Role role = guild.role(tag.substring(3, tag.length() - 1));
		if (role == null) {
			pos = current;
			throw new NoSuchElementException("No such token available!");
		}
		return role;
	}
	
}
