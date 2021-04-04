package com.hardcoded.compiler.impl.expression;

import com.hardcoded.compiler.lexer.Token;

/**
 * A binary expression
 * 
 * <pre>
 * Valid syntax:
 *   [expr] OPERATOR [expr]
 * </pre>
 * 
 * @author HardCoded
 * @since 0.2.0
 */
public class BinaryExpr extends Expr {
	private Type type;
	
	private BinaryExpr(Type type, Token token) {
		super(token);
		this.type = type;
	}
	
	@Override
	public Type getType() {
		return type;
	}
	
	@Override
	public String toString() {
		String str = list.toString();
		str = str.substring(1, str.length() - 1);
		return String.format("%s(%s)", type.name().toLowerCase(), str);
	}
		
	public static BinaryExpr get(Type type, Token token) {
		return new BinaryExpr(type, token);
	}
}
