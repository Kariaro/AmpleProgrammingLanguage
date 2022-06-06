package me.hardcoded.compiler.parser.expr;

import me.hardcoded.compiler.impl.ISyntaxPosition;
import me.hardcoded.compiler.parser.type.TreeType;
import me.hardcoded.compiler.parser.type.ValueType;

public class NumExpr extends Expr {
	private int value;
	
	public NumExpr(ISyntaxPosition syntaxPosition, int value) {
		super(syntaxPosition);
		this.value = value;
	}
	
	public int getValue() {
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
		throw new UnsupportedOperationException();
	}
	
	@Override
	public TreeType getTreeType() {
		return TreeType.NUM;
	}
	
	@Override
	public String toString() {
		return "(" + value + ")";
	}
}
