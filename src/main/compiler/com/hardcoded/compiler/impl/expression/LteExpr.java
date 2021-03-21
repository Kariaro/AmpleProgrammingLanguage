package com.hardcoded.compiler.impl.expression;

import com.hardcoded.compiler.lexer.Token;

/**
 * A less than equals expression
 * 
 * @author HardCoded
 * @since 0.2.0
 */
public class LteExpr extends Expr {
	private LteExpr(Token token) {
		super(token);
	}
	
	@Override
	public Type getType() {
		return Type.LTE;
	}
		
	public static LteExpr get(Token token) {
		return new LteExpr(token);
	}
}
