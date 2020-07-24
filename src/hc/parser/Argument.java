package hc.parser;

import hc.token.Symbol;

public class Argument {
	private final Primitive type;
	private final boolean pointer;
	private final Symbol name;
	
	private Argument(Primitive type, boolean pointer, Symbol name) {
		this.type = type;
		this.pointer = pointer;
		this.name = name;;
	}
	
	public static final Argument create(Primitive type, boolean pointer, Symbol name) {
		return new Argument(type, pointer, name);
	}
	
	public Primitive getType() {
		return type;
	}
	
	public boolean isPointer() {
		return pointer;
	}
	
	public Symbol getName() {
		return name;
	}
	
	@Override
	public String toString() {
		return type + (pointer ? "* ":" ") + name;
	}
}
