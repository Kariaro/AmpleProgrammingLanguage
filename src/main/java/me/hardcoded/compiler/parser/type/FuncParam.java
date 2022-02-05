package me.hardcoded.compiler.parser.type;

public class FuncParam {
	private ValueType type;
	private Reference reference;
	
	public FuncParam(ValueType type, Reference reference) {
		this.type = type;
		this.reference = reference;
	}
	
	public ValueType getType() {
		return type;
	}
	
	public Reference getReference() {
		return reference;
	}
	
	@Override
	public String toString() {
		return type + " " + reference;
	}
}
