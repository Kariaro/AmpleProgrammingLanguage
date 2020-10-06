package hardcoded.compiler.types;

import hardcoded.compiler.expression.LowType;

public class PointerType extends HighType {
	private HighType type;
	private int depth;
	
	public PointerType(HighType type, int depth) {
		super(type.name(), type.type(), LowType.getPointerSize());
		this.depth = depth;
		this.type = type;
	}
	
	public String toString() {
		StringBuilder sb = new StringBuilder();
		for(int i = 0; i < depth; i++) sb.append("*");
		return type + sb.toString();
	}
}
