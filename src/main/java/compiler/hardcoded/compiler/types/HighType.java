package hardcoded.compiler.types;

import hardcoded.compiler.expression.LowType;

/**
 * This is a the high representation of a type inside the programming language.<br>
 * The coresponding low level representation is a {@linkplain hardcoded.compiler.expression.LowType}
 * 
 * @author HardCoded
 */
public class HighType {
	public static final HighType INVALID = new HighType(":INVALID", LowType.INVALID);
	
	private final String name;
	private final int size;
	private final LowType type;
	
	@Deprecated
	public HighType(String name, LowType type, int size) {
		this.name = name;
		this.size = size;
		this.type = type;
	}
	
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
	
	public String toString() {
		if(type.isPointer()) {
			return name + "*".repeat(type.depth());
		}
		return name;
	}
}
