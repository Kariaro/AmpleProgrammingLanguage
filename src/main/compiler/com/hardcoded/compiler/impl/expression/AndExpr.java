package com.hardcoded.compiler.impl.expression;

import com.hardcoded.compiler.lexer.Token;

/**
 * A and expression
 * 
 * @author HardCoded
 * @since 0.2.0
 */
public class AndExpr extends Expr {
	private AndExpr(Token token) {
		super(token);
	}
	
	@Override
	public Type getType() {
		return Type.AND;
	}
		
	public static AndExpr get(Token token) {
		return new AndExpr(token);
	}
}
