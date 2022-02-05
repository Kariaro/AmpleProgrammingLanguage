package me.hardcoded.compiler.parser.type;

public class ValueType {
	private static final String FLAGS = "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789+/";
	
	public static final int UNSIGNED = 1,
							CONST = 2,
							VOLATILE = 4,
							FLOATING = 8;
	
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
	
	private String getShortName() {
		return name + "@" + getFlagsChar(flags) + "%02x".formatted(size) + "p".repeat(depth);
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
