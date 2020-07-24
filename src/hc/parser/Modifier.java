package hc.parser;

import hc.token.Symbol;

public enum Modifier {
	EXPORT,
	CONST,
	;
	
	public static boolean isModifier(Symbol symbol) { return getModifier(symbol) != null; }
	public static Modifier getModifier(Symbol symbol) {
		if(symbol == null) return null;
		
		String value = symbol.toString();
		if(value == null) return null;
		
		for(Modifier key : values())
			if(value.equalsIgnoreCase(key.toString())) return key;
		
		return null;
	}
}
