package me.hardcoded.compiler.parser.type;

public class ValueType {
	public static final int SIGNED = 0,
		UNSIGNED = 1,
		FLOATING = 2,
		GENERIC = 3,
		LINKED = 4,
		VARARGS = 5,
		STRUCT = 6;
	
	public static final int STORAGE_TYPE = 15,
		CONST = 16;
	
	// The name of the type
	private final String name;
	private final int flags;
	private final int depth;
	private final int size;
	private final StructData structData;
	
	public ValueType(String name, int size, int depth, int flags) {
		this(name, size, depth, flags, null);
	}
	
	public ValueType(String name, int size, int depth, int flags, StructData data) {
		this.name = name;
		this.size = size;
		this.depth = depth;
		this.flags = flags;
		this.structData = data;
	}
	
	/**
	 * Create an array of this type
	 */
	public ValueType createArray(int depth) {
		return new ValueType(name, size, depth, flags, structData);
	}
	
	public String getName() {
		return name;
	}
	
	public int getSize() {
		return size;
	}
	
	public int getFlags() {
		return flags;
	}
	
	public int getDepth() {
		return depth;
	}
	
	public boolean isSigned() {
		return (flags & STORAGE_TYPE) == SIGNED;
	}
	
	public boolean isFloating() {
		return (flags & STORAGE_TYPE) == FLOATING;
	}
	
	public boolean isUnsigned() {
		return (flags & STORAGE_TYPE) == UNSIGNED;
	}
	
	public boolean isLinked() {
		return (flags & STORAGE_TYPE) == LINKED;
	}
	
	public boolean isVarargs() {
		return (flags & STORAGE_TYPE) == VARARGS;
	}
	
	public boolean isStruct() {
		return (flags & STORAGE_TYPE) == STRUCT;
	}
	
	public int calculateBytes() {
		return (getDepth() > 0) ? getPointerSize() : (getSize() >> 3);
	}
	
	public static int getPointerSize() {
		return 8;
	}
	
	public StructData getStructData() {
		return structData;
	}
	
	@Override
	public int hashCode() {
		int result = name != null ? name.hashCode() : 0;
		result = 31 * result + flags;
		result = 31 * result + depth;
		result = 31 * result + size;
		return result;
	}
	
	@Override
	public boolean equals(Object obj) {
		if (!(obj instanceof ValueType that))
			return false;
		return this.getDepth() == that.getDepth()
			&& this.getFlags() == that.getFlags()
			&& this.getSize() == that.getSize();
	}
	
	public String toShortName() {
		if (isLinked()) {
			return "?";
		}
		
		if (isVarargs()) {
			return "..." + "[]".repeat(depth);
		}
		
		StringBuilder sb = new StringBuilder();
		
		if ((flags & CONST) != 0) {
			sb.append("const ");
		}
		
		// int size = calculateBytes();
		switch (flags & STORAGE_TYPE) {
			case SIGNED -> sb.append("i");
			case UNSIGNED -> sb.append("u");
			case FLOATING -> sb.append("f");
			case STRUCT -> sb.append("struct<").append(name).append(">");
			case LINKED -> sb.append("?");
			case VARARGS -> sb.append(".");
			default -> sb.append("unk");
		}
		
		return sb.append(size).append("[]".repeat(depth)).toString();
	}
	
	@Override
	public String toString() {
		return toShortName();
	}
}
