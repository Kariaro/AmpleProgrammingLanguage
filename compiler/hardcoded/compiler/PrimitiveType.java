package hardcoded.compiler;

import hardcoded.compiler.Expression.ExprType;

public class PrimitiveType extends Type {
	private ExprType type;
	public PrimitiveType(String name, ExprType type, int size, boolean floating, boolean signed) {
		super(name, size, floating, signed);
		this.type = type;
	}
	
	public PrimitiveType(String name, ExprType type, int size) {
		super(name, size);
		this.type = type;
	}
	
	public ExprType getType() {
		return type;
	}
}
