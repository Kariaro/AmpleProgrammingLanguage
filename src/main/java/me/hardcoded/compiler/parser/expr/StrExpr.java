package me.hardcoded.compiler.parser.expr;

import me.hardcoded.compiler.impl.ISyntaxPosition;

public class StrExpr extends Expr {
	private String value;
	
	public StrExpr(String value, ISyntaxPosition syntaxPosition) {
		super(syntaxPosition);
		this.value = value;
	}
	
	public String getValue() {
		return value;
	}
	
	@Override
	public boolean isEmpty() {
		return false;
	}
	
	@Override
	public boolean isPure() {
		return true;
	}
}
