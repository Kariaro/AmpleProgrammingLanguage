package com.hardcoded.compiler.impl.expression;

import com.hardcoded.compiler.lexer.Token;

/**
 * A comma expression
 * 
 * @author HardCoded
 * @since 0.2.0
 */
public class CommaExpr extends Expr {
	private CommaExpr(Token token) {
		super(token);
	}
	
	@Override
	public Type getType() {
		return Type.COMMA;
	}
	
	public static CommaExpr get(Token token) {
		return new CommaExpr(token);
	}
}
