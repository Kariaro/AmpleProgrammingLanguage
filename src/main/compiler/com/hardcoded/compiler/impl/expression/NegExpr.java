package com.hardcoded.compiler.impl.expression;

import com.hardcoded.compiler.lexer.Token;

/**
 * A neg expression
 * 
 * @author HardCoded
 * @since 0.2.0
 */
public class NegExpr extends Expr {
	private NegExpr(Token token) {
		super(token);
	}
	
	@Override
	public Type getType() {
		return Type.NOR;
	}
		
	public static NegExpr get(Token token) {
		return new NegExpr(token);
	}
}
