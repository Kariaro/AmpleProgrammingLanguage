package me.hardcoded.compiler.parser.expr;

import me.hardcoded.compiler.impl.ISyntaxPosition;
import me.hardcoded.compiler.parser.type.ValueType;
import me.hardcoded.lexer.Token;

public class CastExpr extends Expr {
	private ValueType cast;
	private Expr expr;
	
	public CastExpr(Expr expr, ValueType cast, ISyntaxPosition syntaxPosition) {
		super(syntaxPosition);
		this.expr = expr;
		this.cast = cast;
	}
	
	public ValueType getCastType() {
		return cast;
	}
	
	public Expr getValue() {
		return expr;
	}
	
	@Override
	public boolean isEmpty() {
		return false;
	}
	
	@Override
	public boolean isPure() {
		return expr.isPure();
	}
}
