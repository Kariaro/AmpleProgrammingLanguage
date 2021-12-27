package hardcoded.compiler.types;

import hardcoded.compiler.expression.LowType;
import hardcoded.compiler.statement.Construct;

public class ConstructType extends HighType {
	private final Construct construct;
	public ConstructType(String name, LowType type, Construct construct) {
		super(name, type);
		this.construct = construct;
	}
	
	public boolean hasMember(String name) {
		return construct.hasMember(name);
	}
	
	public Construct getConstruct() {
		return construct;
	}
	
	@Override
	public String toString() {
		return "construct %s".formatted(name());
	}
}
