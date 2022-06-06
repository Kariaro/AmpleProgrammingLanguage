package me.hardcoded.compiler.parser.type;

public class Primitives {
	// Integer
	public static final ValueType
		I32 = new ValueType("i32", 32, 0, ValueType.SIGNED);
	
	// Floating
	public static final ValueType
		F32 = new ValueType("f32", 32, 0, ValueType.FLOATING);
}
