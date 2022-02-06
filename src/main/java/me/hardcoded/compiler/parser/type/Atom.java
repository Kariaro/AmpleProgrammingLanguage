package me.hardcoded.compiler.parser.type;

public enum Atom {
	int_64(false, 64),
	int_32(false, 32),
	int_16(false, 16),
	int_8(false, 8),
	
	uint_64(true, 64),
	uint_32(true, 32),
	uint_16(true, 16),
	uint_8(true, 8),
	
	float_64(true, false, 64),
	float_32(true, false, 32),
	;
	
	public static final Atom[] VALUES = values();
	
	private final boolean unsigned;
	private final boolean floating;
	private final int size;
	
	private Atom(boolean floating, boolean unsigned, int size) {
		this.floating = floating;
		this.unsigned = unsigned;
		this.size = size;
	}
	
	private Atom(boolean unsigned, int size) {
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
		String name = floating ? "float" : "int";
		String pref = unsigned ? "u" : "";
		return Atom.valueOf(pref + name + "_" + size);
	}
}
