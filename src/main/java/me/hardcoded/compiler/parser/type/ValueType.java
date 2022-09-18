package me.hardcoded.compiler.parser.type;

public class ValueType {
	public static final int SIGNED = 0,
		UNSIGNED = 1,
		FLOATING = 2,
		GENERIC = 3;
	
	public static final int STORAGE_TYPE = 7,
		CONST = 8;
	
	// The name of the type
	private final String name;
	private final int flags;
	private final int depth;
	private final int size;
	
	public ValueType(String name, int size, int depth, int flags) {
		this.name = name;
		this.size = size;
		this.depth = depth;
		this.flags = flags;
	}
	
	/**
	 * Create an array of this type
	 */
	public ValueType createArray(int depth) {
		// TODO: Make sure this is not already an array
		return new ValueType(name, size, depth, flags);
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
		StringBuilder sb = new StringBuilder();
		
		if ((flags & CONST) != 0) {
			sb.append("const ");
		}
		
		switch (flags & STORAGE_TYPE) {
			case SIGNED -> sb.append("int");
			case UNSIGNED -> sb.append("uint");
			case FLOATING -> sb.append("float");
			default -> sb.append("unk");
		}
		
		return sb.append("_").append(size).append("[]".repeat(depth)).toString();
	}
	
	@Override
	public String toString() {
		return toShortName();
	}
}
