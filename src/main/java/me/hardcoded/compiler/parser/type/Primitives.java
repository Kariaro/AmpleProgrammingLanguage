package me.hardcoded.compiler.parser.type;

import java.util.Map;

public class Primitives {
	// Integer
	public static final ValueType
		CHAR = new ValueType("char", 8, 0, ValueType.UNSIGNED),
		BOOL = new ValueType("bool", 8, 0, 0),
		SHORT = new ValueType("short", 16, 0, 0),
		INT = new ValueType("int", 32, 0, 0),
		LONG = new ValueType("long", 64, 0, 0),
		VOID = new ValueType("void", 64, 0, ValueType.UNSIGNED);
	
	// Floating
	public static final ValueType
		FLOAT = new ValueType("float", 32, 0, ValueType.FLOATING),
		DOUBLE = new ValueType("double", 64, 0, ValueType.FLOATING);
	
	private static final Map<String, ValueType> PRIMITIVES = Map.of(
		"char", CHAR,
		"bool", BOOL,
		"short", SHORT,
		"int", INT,
		"long", LONG,
		"void", VOID,
		"float", FLOAT,
		"double", DOUBLE
	);
	
	public static boolean hasPrimitive(String name) {
		return PRIMITIVES.containsKey(name);
	}
	
	public static ValueType getPrimitive(String name) {
		return PRIMITIVES.get(name);
	}
}
