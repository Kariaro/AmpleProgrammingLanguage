package com.hardcoded.compiler.impl.expression;

import com.hardcoded.compiler.lexer.Token;

/**
 * A conditional and expression
 * 
 * @author HardCoded
 * @since 0.2.0
 */
public class CandExpr extends Expr {
	private CandExpr(Token token) {
		super(token);
	}
	
	@Override
	public Type getType() {
		return Type.CAND;
	}
		
	public static CandExpr get(Token token) {
		return new CandExpr(token);
	}
}
