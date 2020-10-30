package hardcoded.compiler.types;

import hardcoded.compiler.expression.LowType;

public class ArrayType extends HighType {
	public ArrayType(String name, LowType type) {
		super(name, type);
	}
	
	public String toString() {
		return name() + "[]".repeat(type().depth());
	}
}
