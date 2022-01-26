package me.hardcoded.compiler.parser.type;

public class FuncParam {
	private ValueType type;
	private Reference reference;
	
	public FuncParam(ValueType type, Reference reference) {
		this.type = type;
		this.reference = reference;
	}
	
	@Override
	public String toString() {
		return type + " " + reference;
	}
}
