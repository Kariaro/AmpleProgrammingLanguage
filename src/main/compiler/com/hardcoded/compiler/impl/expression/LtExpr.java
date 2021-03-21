package com.hardcoded.compiler.impl.expression;

import com.hardcoded.compiler.lexer.Token;

/**
 * A less than expression
 * 
 * @author HardCoded
 * @since 0.2.0
 */
public class LtExpr extends Expr {
	private LtExpr(Token token) {
		super(token);
	}
	
	@Override
	public Type getType() {
		return Type.LT;
	}
		
	public static LtExpr get(Token token) {
		return new LtExpr(token);
	}
}
