package com.hardcoded.compiler.impl.expression;

import com.hardcoded.compiler.lexer.Token;

/**
 * A xor expression
 * 
 * @author HardCoded
 * @since 0.2.0
 */
public class XorExpr extends Expr {
	private XorExpr(Token token) {
		super(token);
	}
	
	@Override
	public Type getType() {
		return Type.XOR;
	}
		
	public static XorExpr get(Token token) {
		return new XorExpr(token);
	}
}
