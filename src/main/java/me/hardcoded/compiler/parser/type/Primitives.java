package me.hardcoded.compiler.parser.type;

public class Primitives {
	public static final ValueType NONE = new ValueType("none", 0, 0, 0);
	
	public static final ValueType
		I8  = new ValueType("i8",   8, 0, 0),
		I16 = new ValueType("i16", 16, 0, 0),
		I32 = new ValueType("i32", 32, 0, 0),
		I64 = new ValueType("i64", 64, 0, 0);
	
	public static final ValueType[] VALUES = { NONE, I8, I16, I32, I64 };
	/*
	
	// Integer
	public static final ValueType
		I32 = new ValueType("i32", 32, 0, ValueType.SIGNED);
	
	// Floating
	public static final ValueType
		F32 = new ValueType("f32", 32, 0, ValueType.FLOATING);
	 */
}
