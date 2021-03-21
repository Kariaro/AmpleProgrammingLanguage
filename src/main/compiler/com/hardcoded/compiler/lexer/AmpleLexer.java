package com.hardcoded.compiler.lexer;

/**
 * This is the lexer class that defines the operators and tokens
 * of the programming language.
 * 
 * @author HardCoded
 * @since 0.2.0
 */
public class AmpleLexer {
	private static final LexerTokenizer LEXER;
	
	static {
		LexerTokenizer lexer = new LexerTokenizer();
		lexer.addGroup("TOKEN").addRegex(".");
		lexer.addGroup("WHITESPACE", true).addRegexes("[ \t\r\n]");
		lexer.addGroup("COMMENT", true)
			.addDelimiter("/*", "", "*/")
			.addRegex("//[^\r\n]*")
		;
		lexer.addGroup("IDENTIFIER").addRegex("[a-zA-Z_][a-zA-Z0-9_]*");
		lexer.addGroup("DELIMITER").addStrings(
			"+=", "-=", "*=", "/=", "%=", "^=", ">>=", "<<=",
			">>", "<<", "++", "--", "&=", "|=",
			"||", "&&", "==", ">=", "<=", "!="
		);
		lexer.addGroup("STRING").addDelimiter("\"", "\\", "\"");
		lexer.addGroup("CHAR").addDelimiter("\'", "\\", "\'");
		lexer.addGroup("NUMBER").addRegexes(
			"0x[0-9a-fA-F]+",
			"0b[0-1]+",
			"[0-9]+[.][0-9]",
			"[0-9]+"
		);
		lexer.addGroup("BOOL").addStrings("true", "false");
		
		LEXER = lexer.getImmutableTokenizer();
	}
	
	public static LexerTokenizer getLexer() {
		return LEXER;
	}
}
