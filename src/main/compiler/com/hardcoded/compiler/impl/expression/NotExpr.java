package com.hardcoded.compiler.impl.expression;

import com.hardcoded.compiler.lexer.Token;

/**
 * A not expression
 * 
 * @author HardCoded
 * @since 0.2.0
 */
public class NotExpr extends Expr {
	private NotExpr(Token token) {
		super(token);
	}
	
	@Override
	public Type getType() {
		return Type.NOT;
	}
		
	public static NotExpr get(Token token) {
		return new NotExpr(token);
	}
}
