package hardcoded.compiler.types;

import hardcoded.compiler.expression.LowType;

/**
 * This is a the high representation of a type inside the programming language.<br>
 * The coresponding low level representation is a {@linkplain hardcoded.compiler.expression.LowType}
 * 
 * @author HardCoded
 */
public class HighType {
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
	
	public int size() {
		return size;
	}
	
	public boolean isSigned() {
		return type.isSigned();
	}
	
	public String toString() {
		if(type.isPointer()) {
			StringBuilder sb = new StringBuilder().append(name);
			for(int i = 0; i < type.depth(); i++) sb.append('*');
			return sb.toString();
		}
		return name;
	}
}
