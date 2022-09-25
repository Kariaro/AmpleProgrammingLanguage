package me.hardcoded.compiler.parser.type;

public class Primitives {
	public static final ValueType NONE = new ValueType("none", 0, 0, 0);
	public static final ValueType LINKED = new ValueType("?", 0, 0, ValueType.LINKED);
	public static final ValueType VARARGS = new ValueType("...", 0, 1, ValueType.VARARGS);
	
	public static final ValueType
		I8 = new ValueType("i8", 8, 0, 0),
		I16 = new ValueType("i16", 16, 0, 0),
		I32 = new ValueType("i32", 32, 0, 0),
		I64 = new ValueType("i64", 64, 0, 0);
	
	public static final ValueType
		U8 = new ValueType("u8", 8, 0, ValueType.UNSIGNED),
		U16 = new ValueType("u16", 16, 0, ValueType.UNSIGNED),
		U32 = new ValueType("u32", 32, 0, ValueType.UNSIGNED),
		U64 = new ValueType("u64", 64, 0, ValueType.UNSIGNED);
	
	public static final ValueType
		F32 = new ValueType("f32", 32, 0, ValueType.FLOATING),
		F64 = new ValueType("f64", 64, 0, ValueType.FLOATING);
	
	public static final ValueType[] VALUES = {
		NONE, LINKED, VARARGS,
		I8, I16, I32, I64,
		U8, U16, U32, U64,
		//		F32, F64
	};
}
