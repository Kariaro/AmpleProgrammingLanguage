package com.hardcoded.compiler.impl.expression;

import com.hardcoded.compiler.lexer.Token;

/**
 * A equals expression
 * 
 * @author HardCoded
 * @since 0.2.0
 */
public class EqExpr extends Expr {
	private EqExpr(Token token) {
		super(token);
	}
	
	@Override
	public Type getType() {
		return Type.EQ;
	}
		
	public static EqExpr get(Token token) {
		return new EqExpr(token);
	}
}
