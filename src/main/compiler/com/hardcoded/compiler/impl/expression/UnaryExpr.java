package com.hardcoded.compiler.impl.expression;

import com.hardcoded.compiler.lexer.Token;

/**
 * A unary expression
 * 
 * <pre>
 * Valid syntax:
 *   OPERATOR [expr]
 *   [expr] OPERATOR
 * </pre>
 * 
 * @author HardCoded
 * @since 0.2.0
 */
public class UnaryExpr extends Expr {
	private Type type;
	
	private UnaryExpr(Type type, Token token) {
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
		
	public static UnaryExpr get(Type type, Token token) {
		return new UnaryExpr(type, token);
	}
}
