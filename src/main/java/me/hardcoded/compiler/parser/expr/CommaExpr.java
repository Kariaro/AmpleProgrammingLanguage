package me.hardcoded.compiler.parser.expr;

import me.hardcoded.compiler.impl.ISyntaxPosition;

import java.util.List;

public class CommaExpr extends Expr {
	private List<Expr> values;
	
	public CommaExpr(List<Expr> values, ISyntaxPosition syntaxPosition) {
		super(syntaxPosition);
		this.values = values;
	}
	
	public List<Expr> getValues() {
		return values;
	}
	
	@Override
	public boolean isEmpty() {
		return values.isEmpty();
	}
	
	@Override
	public boolean isPure() {
		for (Expr expr : values) {
			if (!expr.isPure()) {
				return false;
			}
		}
		
		return true;
	}
}
