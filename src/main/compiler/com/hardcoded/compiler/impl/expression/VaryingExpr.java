package com.hardcoded.compiler.impl.expression;

import com.hardcoded.compiler.lexer.Token;

/**
 * A varying expression
 * 
 * @author HardCoded
 * @since 0.2.0
 */
public class VaryingExpr extends Expr {
	private final Type type;
	
	private VaryingExpr(Type type, Token token) {
		super(token);
		this.type = check(type);
	}
	
	@Override
	public Type getType() {
		return type;
	}
	
	public int size() {
		return list.size();
	}
		
	public static VaryingExpr get(Type type, Token token) {
		return new VaryingExpr(type, token);
	}
	
	/**
	 * Throws an exception if the specified type is not valid for this expression
	 * @param type the type to check
	 */
	private static Type check(Type type) {
		switch(type) {
			case CALL:
			case COMMA:
			case ADD:
			case SUB:
			case OR:
			case XOR: return type;
			
			default: throw new ExpressionException("The type %s is not a valid for a binary expression", type);
		}
	}
}
