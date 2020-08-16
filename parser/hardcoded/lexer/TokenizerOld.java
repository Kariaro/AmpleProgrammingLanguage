package hardcoded.lexer;

import java.util.ArrayList;
import java.util.List;

public class TokenizerOld {
	private TokenizerOld() {}
	
	public static Token generateTokenChain(Tokenizer lexer, byte[] bytes) {
		List<Symbol> symbols = lexer.parse(bytes);
		if(symbols.isEmpty()) return new Token(null, null);
		
		List<Token> list = new ArrayList<>();
		list.add(new Token(null, null));
		for(Symbol symbol : symbols) {
			Token token = new Token(symbol.value(), symbol.group());
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
	
	public static Token generateTokenChain(byte[] bytes) {
		return generateTokenChain(TokenizerFactory.getDefaultLexer(), bytes);
	}
}
