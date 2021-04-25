package com.hardcoded.compiler.impl.expression;

import com.hardcoded.compiler.api.Expression;
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
	private final Type type;
	
	private BinaryExpr(Type type, Token token) {
		super(token);
		this.type = check(type);
	}
	
	@Override
	public Type getType() {
		return type;
	}
	
	@Override
	public Expr add(Expression expr) {
		assert list.size() < 2 : "A binary expression can only contain two elements";
		super.add(expr);
		return this;
	}
		
	public static BinaryExpr get(Type type, Token token) {
		return new BinaryExpr(type, token);
	}
	
	/**
	 * Throws an exception if the type is not valid for this type
	 * @param type the type to check
	 */
	private static Type check(Type type) {
		switch(type) {
			case SET:
			case ARRAY:
			case COR:
			case CAND:
			case EQ:
			case NEQ:
			case DIV:
			case MUL:
			case MOD:
			case MEMBER:
			case GT:
			case GTE:
			case LT:
			case LTE:
			case SHL:
			case SHR: return type;
			
			default: throw new ExpressionException("The type %s is not a valid for a binary expression", type);
		}
	}
}
