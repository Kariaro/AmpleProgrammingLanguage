package com.hardcoded.compiler.api;

import java.util.List;

/**
 * API access
 * 
 * @author HardCoded
 * @since 0.2.0
 */
public interface Expression {
	public enum Type {
		ATOM,
		
		// UNARY
		NOT,
		NOR,
		NEG,
		
		// BINARY
		ADD,
		SUB,
		MUL,
		DIV,

		SHR,
		SHL,

		XOR,
		AND,
		OR,
		
		MOD,
		
		CAND,
		COR,
		
		COMMA,
		
		// equality operation
		EQ,
		NEQ,
		LT,
		LTE,
		GT,
		GTE,
		
		// class operation
		MEMBER,
		
		// pointer operation
		ARRAY, // []
		
		
		// changing operation
		CALL,
		SET,
		
		// no operation
		NOP,
	}
	
	Type getType();
	List<Expression> getExpressions();
	boolean isPure();
	
	/**
	 * Returns the start offset of this expression.
	 * @return the start offset of this expression
	 */
	int getStartOffset();
	
	/**
	 * Returns the end offset of this expression.
	 * @return the end offset of this expression
	 */
	int getEndOffset();
}
