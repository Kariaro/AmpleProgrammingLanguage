package com.hardcoded.compiler.impl.expression;

import com.hardcoded.compiler.lexer.Token;

/**
 * A sub expression
 * 
 * @author HardCoded
 * @since 0.2.0
 */
public class SubExpr extends Expr {
	private SubExpr(Token token) {
		super(token);
	}
	
	@Override
	public Type getType() {
		return Type.SUB;
	}
	
	public static SubExpr get(Token token) {
		return new SubExpr(token);
	}
}
