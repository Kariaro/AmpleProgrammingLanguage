package hc.parser;

import hc.token.Symbol;

public enum Primitive {
	UINT_8("uint_8"),
	UINT_16("uint_16"),
	UINT_32("uint_32"),
	UINT_64("uint_64"),
	
	OBJECT("object"), // Anything
	VOID("void"),
	INT("int"),
	CHAR("char"),
	BOOL("bool"),
	// STRING("string"),
	
	BYTE("BYTE"),
	WORD("WORD"),
	DWORD("DWORD"),
	;
	
	private final String name;
	private Primitive(String name) {
		this.name = name;
	}
	
	public String toString() {
		return name;
	}
	
	public static boolean isPrimitive(Symbol symbol) { return getPrimitive(symbol) != null; }
	public static Primitive getPrimitive(Symbol symbol) {
		if(symbol == null) return null;
		
		String value = symbol.toString();
		if(value == null) return null;
		
		for(Primitive key : values())
			if(key.name.equals(value)) return key;
		
		return null;
	}
}
