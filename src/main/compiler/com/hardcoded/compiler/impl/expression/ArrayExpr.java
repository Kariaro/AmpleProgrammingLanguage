package com.hardcoded.compiler.impl.expression;

import com.hardcoded.compiler.lexer.Token;

/**
 * A array expression
 * 
 * @author HardCoded
 * @since 0.2.0
 */
public class ArrayExpr extends Expr {
	private ArrayExpr(Token token) {
		super(token);
	}
	
	@Override
	public Type getType() {
		return Type.ARRAY;
	}
		
	public static ArrayExpr get(Token token) {
		return new ArrayExpr(token);
	}
}
