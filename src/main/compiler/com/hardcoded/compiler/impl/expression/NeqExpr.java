package com.hardcoded.compiler.impl.expression;

import com.hardcoded.compiler.lexer.Token;

/**
 * A not equals expression
 * 
 * @author HardCoded
 * @since 0.2.0
 */
public class NeqExpr extends Expr {
	private NeqExpr(Token token) {
		super(token);
	}
	
	@Override
	public Type getType() {
		return Type.NEQ;
	}
		
	public static NeqExpr get(Token token) {
		return new NeqExpr(token);
	}
}
