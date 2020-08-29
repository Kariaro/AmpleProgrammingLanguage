package hardcoded.compiler;

import hardcoded.compiler.Expression.AtomType;

public class PrimitiveType extends Type {
	private AtomType type;
	
	public PrimitiveType(String name, AtomType type, int size, boolean signed) {
		super(name, size, signed);
		this.type = type;
	}
	
	public PrimitiveType(String name, AtomType type, int size) {
		super(name, size);
		this.type = type;
	}
	
	public AtomType getType() {
		return type;
	}
}
