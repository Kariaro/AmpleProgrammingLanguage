package hardcoded.compiler.types;

import hardcoded.compiler.expression.LowType;

public class ArrayType extends HighType {
	public ArrayType(String name, LowType type) {
		super(name, type, type.size());
	}
	
	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append(name());
		for(int i = 0; i < type().depth(); i++) sb.append("[]");
		return sb.toString();
	}
}
