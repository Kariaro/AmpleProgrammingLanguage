package me.hardcoded.compiler.parser.type;

public enum Atom {
	i64(false, 64),
	i32(false, 32),
	i16(false, 16),
	i8(false, 8),
	
	u64(true, 64),
	u32(true, 32),
	u16(true, 16),
	u8(true, 8),
	
	f64(true, false, 64),
	f32(true, false, 32),
	;
	
	private final boolean unsigned;
	private final boolean floating;
	private final int size;
	
	Atom(boolean floating, boolean unsigned, int size) {
		this.floating = floating;
		this.unsigned = unsigned;
		this.size = size;
	}
	
	Atom(boolean unsigned, int size) {
		this(false, unsigned, size);
	}
	
	public boolean isUnsigned() {
		return unsigned;
	}
	
	public boolean isFloating() {
		return floating;
	}
	
	public int getSize() {
		return size;
	}
	
	public static Atom create(boolean floating, boolean unsigned, int size) {
		// TODO: Create a lookup table
		String name = floating ? "f" : "i";
		String pref = unsigned ? "u" : "";
		return Atom.valueOf(pref + name + "_" + size);
	}
}
