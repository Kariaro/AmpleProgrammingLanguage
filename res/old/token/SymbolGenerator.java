package hc.token;

import java.util.ArrayList;
import java.util.List;

public class SymbolGenerator {
	public SymbolGenerator() {
		
	}
	
	// private static final String whitespace = " \t\r\n";
	// private static final String groupStart = "\"'({[";
	// private static final String logical = "+-*/&|^%<>~";
	// private static final String delims = " \t\r\n;{}[]()";
	// private static final String[] group = { "/*", "\"", "'" };
	
	public Symbol generate(Token token) {
		List<Symbol> list = new ArrayList<>();
		
		while(token != null) {
			list.add(new Symbol(token.value));
			token = token.next();
		}
		
		Symbol entry = new Symbol(null);
		Symbol symbol = entry;
		for(Symbol s : list) {
			symbol.next = s;
			s.prev = symbol;
			symbol = s;
		}
		
		return entry ;
	}
}
