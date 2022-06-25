package me.hardcoded.compiler.parser.expr;

import me.hardcoded.compiler.impl.ISyntaxPosition;
import me.hardcoded.compiler.parser.serial.TreeType;
import me.hardcoded.compiler.parser.type.ValueType;

public class CastExpr extends Expr {
	private ValueType type;
	private Expr value;
	
	public CastExpr(ISyntaxPosition syntaxPosition, ValueType type, Expr value) {
		super(syntaxPosition);
		this.type = type;
		this.value = value;
	}
	
	public Expr getValue() {
		return value;
	}
	
	@Override
	public boolean isEmpty() {
		return false;
	}
	
	@Override
	public boolean isPure() {
		return false;
	}
	
	@Override
	public ValueType getType() {
		return type;
	}
	
	@Override
	public TreeType getTreeType() {
		return TreeType.CAST;
	}
	
	@Override
	public String toString() {
		return "cast<" + type + ">( " + value + " )";
	}
}
