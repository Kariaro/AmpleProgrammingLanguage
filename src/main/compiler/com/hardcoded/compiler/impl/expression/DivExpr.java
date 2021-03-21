package com.hardcoded.compiler.impl.expression;

import com.hardcoded.compiler.lexer.Token;

/**
 * A divide expression
 * 
 * @author HardCoded
 * @since 0.2.0
 */
public class DivExpr extends Expr {
	private DivExpr(Token token) {
		super(token);
	}
	
	@Override
	public Type getType() {
		return Type.DIV;
	}
	
	public static DivExpr get(Token token) {
		return new DivExpr(token);
	}
}
