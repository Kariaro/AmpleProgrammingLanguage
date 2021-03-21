package com.hardcoded.compiler.impl.expression;

import com.hardcoded.compiler.lexer.Token;

/**
 * A add expression
 * 
 * @author HardCoded
 * @since 0.2.0
 */
public class AddExpr extends Expr {
	private AddExpr(Token token) {
		super(token);
	}
	
	@Override
	public Type getType() {
		return Type.ADD;
	}
		
	public static AddExpr get(Token token) {
		return new AddExpr(token);
	}
}
