package hardcoded.compiler.types;

import hardcoded.compiler.expression.LowType;

/**
 * This is a the high representation of a type inside the programming language.<br>
 * The coresponding low level representation is a {@linkplain hardcoded.compiler.constants.LowType}
 * 
 * @author HardCoded
 */
public class HighType {
	private final String name;
	private final int size;
	private final LowType type;
	
	public HighType(String name, LowType type, int size) {
		this.name = name;
		this.size = size;
		this.type = type;
	}
	
	public LowType type() {
		return type;
	}
	
	public String name() {
		return name;
	}
	
	public int size() {
		return size;
	}
	
	public boolean isSigned() {
		return type.isSigned();
	}
	
	public String toString() {
		return name;
	}
}
