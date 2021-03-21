package com.hardcoded.compiler.impl.expression;

import com.hardcoded.compiler.lexer.Token;

/**
 * A shift left expression
 * 
 * @author HardCoded
 * @since 0.2.0
 */
public class ShlExpr extends Expr {
	private ShlExpr(Token token) {
		super(token);
	}
	
	@Override
	public Type getType() {
		return Type.SHL;
	}
		
	public static ShlExpr get(Token token) {
		return new ShlExpr(token);
	}
}
