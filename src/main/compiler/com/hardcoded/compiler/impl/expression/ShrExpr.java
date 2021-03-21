package com.hardcoded.compiler.impl.expression;

import com.hardcoded.compiler.lexer.Token;

/**
 * A shift right expression
 * 
 * @author HardCoded
 * @since 0.2.0
 */
public class ShrExpr extends Expr {
	private ShrExpr(Token token) {
		super(token);
	}
	
	@Override
	public Type getType() {
		return Type.SHR;
	}
		
	public static ShrExpr get(Token token) {
		return new ShrExpr(token);
	}
}
