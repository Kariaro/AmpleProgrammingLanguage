package com.hardcoded.compiler.impl.expression;

import com.hardcoded.compiler.lexer.Token;

/**
 * A call expression
 * 
 * @author HardCoded
 * @since 0.2.0
 */
public class CallExpr extends Expr {
	private CallExpr(Token token) {
		super(token);
	}
	
	@Override
	public Type getType() {
		return Type.CALL;
	}
		
	public static CallExpr get(Token token) {
		return new CallExpr(token);
	}
}
