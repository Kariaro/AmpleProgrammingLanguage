package me.hardcoded.compiler.parser.expr;

import me.hardcoded.compiler.impl.ISyntaxPosition;

public class NullExpr extends Expr {
	public NullExpr(ISyntaxPosition syntaxPosition) {
		super(syntaxPosition);
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
