package com.hardcoded.compiler.impl.expression;

import com.hardcoded.compiler.lexer.Token;

/**
 * A conditional or expression
 * 
 * @author HardCoded
 * @since 0.2.0
 */
public class CorExpr extends Expr {
	private CorExpr(Token token) {
		super(token);
	}
	
	@Override
	public Type getType() {
		return Type.COR;
	}
		
	public static CorExpr get(Token token) {
		return new CorExpr(token);
	}
}
