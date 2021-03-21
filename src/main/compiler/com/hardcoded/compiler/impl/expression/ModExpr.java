package com.hardcoded.compiler.impl.expression;

import com.hardcoded.compiler.lexer.Token;

/**
 * A modulo expression
 * 
 * @author HardCoded
 * @since 0.2.0
 */
public class ModExpr extends Expr {
	private ModExpr(Token token) {
		super(token);
	}
	
	@Override
	public Type getType() {
		return Type.MOD;
	}
		
	public static ModExpr get(Token token) {
		return new ModExpr(token);
	}
}
