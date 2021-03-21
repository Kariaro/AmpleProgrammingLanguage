package com.hardcoded.compiler.impl.expression;

import com.hardcoded.compiler.lexer.Token;

/**
 * A greater than expression
 * 
 * @author HardCoded
 * @since 0.2.0
 */
public class GtExpr extends Expr {
	private GtExpr(Token token) {
		super(token);
	}
	
	@Override
	public Type getType() {
		return Type.GT;
	}
		
	public static GtExpr get(Token token) {
		return new GtExpr(token);
	}
}
