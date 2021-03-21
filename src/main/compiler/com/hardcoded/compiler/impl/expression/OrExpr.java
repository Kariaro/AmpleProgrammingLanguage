package com.hardcoded.compiler.impl.expression;

import com.hardcoded.compiler.lexer.Token;

/**
 * A or expression
 * 
 * @author HardCoded
 * @since 0.2.0
 */
public class OrExpr extends Expr {
	private OrExpr(Token token) {
		super(token);
	}
	
	@Override
	public Type getType() {
		return Type.OR;
	}
		
	public static OrExpr get(Token token) {
		return new OrExpr(token);
	}
}
