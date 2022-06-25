package me.hardcoded.compiler.parser.type;

public class ValueType {
	private static final String FLAGS = "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789+/";
	
	public static final int SIGNED = 0,
							UNSIGNED = 1,
							FLOATING = 2,
							GENERIC = 3;
	
	public static final int STORAGE_TYPE = 7,
							CONST = 8,
							VOLATILE = 16;
	
	// The name of the type
	private String name;
	private int flags;
	private int depth;
	private int size;
	
	public ValueType(String name, int size, int depth, int flags) {
		this.name = name;
		this.size = size;
		this.depth = depth;
		this.flags = flags;
	}
	
	public ValueType createChild(int size, int depth, int flags) {
		return new ValueType(name, size, depth, flags);
	}
	
	public ValueType parallel(int size) {
		return new ValueType(name, size, depth, flags);
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
	
	private String getShortName() {
		return name + "@" + getFlagsChar(flags) + "%02x".formatted(size) + "p".repeat(depth);
	}
	
	@Override
	public int hashCode() {
		int result = name != null ? name.hashCode() : 0;
		result = 31 * result + flags;
		result = 31 * result + depth;
		result = 31 * result + size;
		return result;
	}
	
	public String toShortName() {
		StringBuilder sb = new StringBuilder();
		
		if ((flags & CONST) != 0) {
			sb.append("const ");
		}
		
		if ((flags & VOLATILE) != 0) {
			sb.append("volatile ");
		}
		
		if ((flags & UNSIGNED) != 0) {
			sb.append("u");
		}
		
		if ((flags & FLOATING) != 0) {
			sb.append("float");
		} else {
			sb.append("int");
		}
		
		return sb.append("_").append(size).append("*".repeat(depth)).toString();
	}
	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		
		if ((flags & CONST) != 0) {
			sb.append("const ");
		}
		
		if ((flags & VOLATILE) != 0) {
			sb.append("volatile ");
		}
		
		if ((flags & UNSIGNED) != 0) {
			sb.append("unsigned ");
		}
		
		return toShortName();
		// return sb.append(name).append("*".repeat(depth)).toString();
	}
	
	private static char getFlagsChar(int flags) {
		return (char)('a' + (flags & 0b1111));
	}
	
	private static int getFlagsFromChar(char c) {
		return (c - 'a') & 0b1111;
	}
	
	private static char getSizeChar(int size) {
		return switch (size) {
			default -> 'V';
			case 8 -> 'B';
			case 16 -> 'W';
			case 32 -> 'D';
			case 64 -> 'P';
		};
	}
}
