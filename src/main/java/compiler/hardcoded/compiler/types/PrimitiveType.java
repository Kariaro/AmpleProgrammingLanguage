package hardcoded.compiler.types;

import hardcoded.compiler.constants.Atom;
import hardcoded.compiler.expression.LowType;

public class PrimitiveType extends HighType {
	public PrimitiveType(String name, Atom type, int size) {
		super(name, LowType.create(type), size);
	}
}
