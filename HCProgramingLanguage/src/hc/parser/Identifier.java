package hc.parser;

import hc.token.Symbol;

public class Identifier {
	private Identifier() {
		
	}
	
	public static boolean isValidIdentifier(Symbol symbol) {
		if(symbol == null) return false;
		
		String value = symbol.toString();
		if(value == null || value.isEmpty()) return false;
		char c = value.charAt(0);
		
		if(!Character.isAlphabetic(c)) return false; // A valid identifier needs to start with a letter
		return value.matches("[a-zA-Z0-9_]+");
	}
}
