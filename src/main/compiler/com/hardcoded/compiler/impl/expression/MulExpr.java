package com.hardcoded.compiler.impl.expression;

import com.hardcoded.compiler.lexer.Token;

/**
 * A multiply expression
 * 
 * @author HardCoded
 * @since 0.2.0
 */
public class MulExpr extends Expr {
	private MulExpr(Token token) {
		super(token);
	}
	
	@Override
	public Type getType() {
		return Type.MUL;
	}
	
	public static MulExpr get(Token token) {
		return new MulExpr(token);
	}
}
