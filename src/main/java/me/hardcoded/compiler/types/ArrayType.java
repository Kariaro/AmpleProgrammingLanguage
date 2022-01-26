package me.hardcoded.compiler.types;

import me.hardcoded.compiler.expression.LowType;

public class ArrayType extends HighType {
	public ArrayType(String name, LowType type) {
		super(name, type);
	}
	
	@Override
	public String toString() {
		return name() + "[]".repeat(type().depth());
	}
}
