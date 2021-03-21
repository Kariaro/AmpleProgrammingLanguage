package com.hardcoded.compiler.impl.expression;

import com.hardcoded.compiler.lexer.Token;

/**
 * A set expression
 * 
 * @author HardCoded
 * @since 0.2.0
 */
public class SetExpr extends Expr {
	private SetExpr(Token token) {
		super(token);
	}
	
	@Override
	public Type getType() {
		return Type.SET;
	}
		
	public static SetExpr get(Token token) {
		return new SetExpr(token);
	}
}
