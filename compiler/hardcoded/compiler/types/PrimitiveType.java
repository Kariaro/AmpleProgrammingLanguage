package hardcoded.compiler.types;

import hardcoded.compiler.constants.AtomType;

public class PrimitiveType extends Type {
	public PrimitiveType(String name, AtomType type, int size, boolean signed) {
		super(name, type, size, signed);
	}
	
	public PrimitiveType(String name, AtomType type, int size) {
		super(name, type, size);
	}
}
