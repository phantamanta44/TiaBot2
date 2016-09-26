package io.github.phantamanta44.tiabot2.command.args;

public class CodeBlock extends InlineCodeBlock {

	final String lang;
	
	public CodeBlock(String code, String lang) {
		super(code);
		this.lang = lang;
	}
	
	public String getLang() {
		return lang;
	}

}
