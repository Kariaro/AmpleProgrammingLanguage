package hardcoded.lexer;

import java.util.ArrayList;
import java.util.List;

// TODO: Rename the file 'TokenizerOld.java'
public class TokenizerOld {
	private static final Tokenizer LEXER;
	
	static {
		Tokenizer lexer = new Tokenizer();
		LEXER = lexer.getImmutableTokenizer();
		lexer.add("COMMENT", true).addRegexes("\\/\\*.*?\\*\\/", "//[^\r\n]*");
		lexer.add("SPACE", true).addRegex("[ \t\r\n]");
		lexer.add("WORD").addRegex("[a-zA-Z0-9_]+");
		lexer.add("LITERAL").addRegexes(
			"\'[^\'\\\\]*(?:\\\\.[^\'\\\\]*)*\'",
			"\"[^\"\\\\]*(?:\\\\.[^\"\\\\]*)*\""
		);
		lexer.add("TOKEN").addRegex(".");
		lexer.setDefaultGroup("TOKEN");
	}
	
	private TokenizerOld() {
		
	}
	
	public static Token generateTokenChain(byte[] bytes) {
		return generateTokenChain(LEXER, bytes);
	}
	
	public static Token generateTokenChain(Tokenizer lexer, byte[] bytes) {
		List<TokenizerSymbol> symbols = lexer.parse(bytes);
		if(symbols.isEmpty()) return new Token(null, null);
		
		List<Token> list = new ArrayList<>();
		list.add(new Token(null, null));
		for(TokenizerSymbol symbol : symbols) {
			Token token = new Token(symbol.value(), symbol.group());
			token.fileOffset = symbol.fileOffset();
			token.column = symbol.column();
			token.line = symbol.line();
			list.add(token);
			
		}
		
		Token entry = list.get(0);
		Token token = entry;
		for(Token t : list) {
			token.next = t;
			t.prev = token;
			token = t;
		}
		
		return entry.next;
	}
}
