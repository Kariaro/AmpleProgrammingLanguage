package hc.parser;

import hc.token.Symbol;

public enum Keyword {
	WHILE,
	FOR,
	IF,
	DO,
	ELSE,
	SWITCH,
	CASE,
	GOTO,
	;
	
	public static boolean isKeyword(Symbol symbol) { return getKeyword(symbol) != null; }
	public static Keyword getKeyword(Symbol symbol) {
		if(symbol == null) return null;
		
		String value = symbol.toString();
		if(value == null) return null;
		
		for(Keyword key : values())
			if(value.equalsIgnoreCase(key.toString())) return key;
		
		return null;
	}
}
