package io.github.phantamanta44.tiabot2.command.args;

import java.io.UnsupportedEncodingException;
import java.nio.charset.Charset;
import java.util.Locale;

class DelegateString implements CharSequence {

	private final String backing;
	
	public DelegateString(String backing) {
		this.backing = backing;
	}

	public int length() {
		return backing.length();
	}

	public boolean isEmpty() {
		return backing.isEmpty();
	}

	public char charAt(int index) {
		return backing.charAt(index);
	}

	public int codePointAt(int index) {
		return backing.codePointAt(index);
	}

	public int codePointBefore(int index) {
		return backing.codePointBefore(index);
	}

	public int codePointCount(int beginIndex, int endIndex) {
		return backing.codePointCount(beginIndex, endIndex);
	}

	public int offsetByCodePoints(int index, int codePointOffset) {
		return backing.offsetByCodePoints(index, codePointOffset);
	}

	public void getChars(int srcBegin, int srcEnd, char[] dst, int dstBegin) {
		backing.getChars(srcBegin, srcEnd, dst, dstBegin);
	}

	@Deprecated
	public void getBytes(int srcBegin, int srcEnd, byte[] dst, int dstBegin) {
		backing.getBytes(srcBegin, srcEnd, dst, dstBegin);
	}

	public byte[] getBytes(String charsetName) throws UnsupportedEncodingException {
		return backing.getBytes(charsetName);
	}

	public byte[] getBytes(Charset charset) {
		return backing.getBytes(charset);
	}

	public byte[] getBytes() {
		return backing.getBytes();
	}

	public boolean equals(Object anObject) {
		return backing.equals(anObject);
	}

	public boolean contentEquals(StringBuffer sb) {
		return backing.contentEquals(sb);
	}

	public boolean contentEquals(CharSequence cs) {
		return backing.contentEquals(cs);
	}

	public boolean equalsIgnoreCase(String anotherString) {
		return backing.equalsIgnoreCase(anotherString);
	}

	public int compareTo(String anotherString) {
		return backing.compareTo(anotherString);
	}

	public int compareToIgnoreCase(String str) {
		return backing.compareToIgnoreCase(str);
	}

	public boolean regionMatches(int toffset, String other, int ooffset, int len) {
		return backing.regionMatches(toffset, other, ooffset, len);
	}

	public boolean regionMatches(boolean ignoreCase, int toffset, String other, int ooffset, int len) {
		return backing.regionMatches(ignoreCase, toffset, other, ooffset, len);
	}

	public boolean startsWith(String prefix, int toffset) {
		return backing.startsWith(prefix, toffset);
	}

	public boolean startsWith(String prefix) {
		return backing.startsWith(prefix);
	}

	public boolean endsWith(String suffix) {
		return backing.endsWith(suffix);
	}

	public int hashCode() {
		return backing.hashCode();
	}

	public int indexOf(int ch) {
		return backing.indexOf(ch);
	}

	public int indexOf(int ch, int fromIndex) {
		return backing.indexOf(ch, fromIndex);
	}

	public int lastIndexOf(int ch) {
		return backing.lastIndexOf(ch);
	}

	public int lastIndexOf(int ch, int fromIndex) {
		return backing.lastIndexOf(ch, fromIndex);
	}

	public int indexOf(String str) {
		return backing.indexOf(str);
	}

	public int indexOf(String str, int fromIndex) {
		return backing.indexOf(str, fromIndex);
	}

	public int lastIndexOf(String str) {
		return backing.lastIndexOf(str);
	}

	public int lastIndexOf(String str, int fromIndex) {
		return backing.lastIndexOf(str, fromIndex);
	}

	public String substring(int beginIndex) {
		return backing.substring(beginIndex);
	}

	public String substring(int beginIndex, int endIndex) {
		return backing.substring(beginIndex, endIndex);
	}

	public CharSequence subSequence(int beginIndex, int endIndex) {
		return backing.subSequence(beginIndex, endIndex);
	}

	public String concat(String str) {
		return backing.concat(str);
	}

	public String replace(char oldChar, char newChar) {
		return backing.replace(oldChar, newChar);
	}

	public boolean matches(String regex) {
		return backing.matches(regex);
	}

	public boolean contains(CharSequence s) {
		return backing.contains(s);
	}

	public String replaceFirst(String regex, String replacement) {
		return backing.replaceFirst(regex, replacement);
	}

	public String replaceAll(String regex, String replacement) {
		return backing.replaceAll(regex, replacement);
	}

	public String replace(CharSequence target, CharSequence replacement) {
		return backing.replace(target, replacement);
	}

	public String[] split(String regex, int limit) {
		return backing.split(regex, limit);
	}

	public String[] split(String regex) {
		return backing.split(regex);
	}

	public String toLowerCase(Locale locale) {
		return backing.toLowerCase(locale);
	}

	public String toLowerCase() {
		return backing.toLowerCase();
	}

	public String toUpperCase(Locale locale) {
		return backing.toUpperCase(locale);
	}

	public String toUpperCase() {
		return backing.toUpperCase();
	}

	public String trim() {
		return backing.trim();
	}

	public String toString() {
		return backing.toString();
	}

	public char[] toCharArray() {
		return backing.toCharArray();
	}

	public String intern() {
		return backing.intern();
	}
	
}
