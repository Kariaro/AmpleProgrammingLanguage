package me.hardcoded.compiler.parser.expr;

import me.hardcoded.compiler.impl.ISyntaxPosition;
import me.hardcoded.compiler.parser.serial.TreeType;
import me.hardcoded.compiler.parser.type.Primitives;
import me.hardcoded.compiler.parser.type.ValueType;

public class StrExpr extends Expr {
	private String value;
	
	public StrExpr(ISyntaxPosition syntaxPosition, String value) {
		super(syntaxPosition);
		this.value = value;
	}
	
	public String getValue() {
		return value;
	}
	
	@Override
	public boolean isEmpty() {
		return true;
	}
	
	@Override
	public boolean isPure() {
		return true;
	}
	
	@Override
	public ValueType getType() {
		// TODO: Get the primitive type from the context type class
		return Primitives.I8.createArray(1);
	}
	
	@Override
	public TreeType getTreeType() {
		return TreeType.STRING;
	}
	
	@Override
	public String toString() {
		return value;
	}
}