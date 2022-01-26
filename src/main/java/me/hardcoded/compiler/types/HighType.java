package me.hardcoded.compiler.types;

import me.hardcoded.compiler.expression.LowType;

/**
 * This is a the high representation of a type inside the programming language.<br>
 * The coresponding low level representation is a {@linkplain me.hardcoded.compiler.expression.LowType}
 * 
 * @author HardCoded
 */
public class HighType {
	// TODO: This class should maybe extend LowType.
	
	public static final HighType INVALID = new HighType(":INVALID", LowType.INVALID);
	
	private final String name;
	private final int size;
	private final LowType type;
	
	public HighType(String name, LowType type) {
		this.name = name;
		this.type = type;
		this.size = type.size();
	}
	
	public LowType type() {
		return type;
	}
	
	public String name() {
		return name;
	}
	
	public int depth() {
		return type.depth();
	}
	
	public int size() {
		return size;
	}
	
	public boolean isSigned() {
		return type.isSigned();
	}
	
	public boolean isPointer() {
		return type.isPointer();
	}
	
	@Override
	public String toString() {
		if(type.isPointer()) {
			return name + "*".repeat(type.depth());
		}
		return name;
	}
}
