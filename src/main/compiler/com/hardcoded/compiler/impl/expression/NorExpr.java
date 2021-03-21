package com.hardcoded.compiler.impl.expression;

import com.hardcoded.compiler.lexer.Token;

/**
 * A nor expression
 * 
 * @author HardCoded
 * @since 0.2.0
 */
public class NorExpr extends Expr {
	private NorExpr(Token token) {
		super(token);
	}
	
	@Override
	public Type getType() {
		return Type.NOR;
	}
		
	public static NorExpr get(Token token) {
		return new NorExpr(token);
	}
}
