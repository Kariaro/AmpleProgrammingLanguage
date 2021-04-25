package com.hardcoded.compiler.impl.expression;

import com.hardcoded.compiler.api.Expression;
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
	private final Type type;
	
	private UnaryExpr(Type type, Token token) {
		super(token);
		this.type = check(type);
	}
	
	@Override
	public Type getType() {
		return type;
	}
	
	@Override
	public Expr add(Expression expr) {
		assert list.size() < 1 : "A unary expression can only contain one element";
		super.add(expr);
		return this;
	}
		
	public static UnaryExpr get(Type type, Token token) {
		return new UnaryExpr(type, token);
	}
	
	/**
	 * Throws an exception if the specified type is not valid for this expression
	 * @param type the type to check
	 */
	private static Type check(Type type) {
		switch(type) {
			case NOR:
			case NOT:
			case NEG: return type;
			
			default: throw new ExpressionException("The type %s is not a valid for a unary expression", type);
		}
	}
}
