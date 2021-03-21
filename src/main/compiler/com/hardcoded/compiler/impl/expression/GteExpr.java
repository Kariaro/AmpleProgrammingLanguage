package com.hardcoded.compiler.impl.expression;

import com.hardcoded.compiler.lexer.Token;

/**
 * A greater than equals expression
 * 
 * @author HardCoded
 * @since 0.2.0
 */
public class GteExpr extends Expr {
	private GteExpr(Token token) {
		super(token);
	}
	
	@Override
	public Type getType() {
		return Type.GTE;
	}
		
	public static GteExpr get(Token token) {
		return new GteExpr(token);
	}
}
