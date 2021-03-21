package com.hardcoded.compiler.api;

import java.util.List;

public interface Expression {
	public enum Type {
		ATOM,
		
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
		
		NOT,
		NOR,
		NEG,
		
		COMMA,
		
		// equality operation
		EQ,
		NEQ,
		LT,
		LTE,
		GT,
		GTE,
		
		// pointer operation
		ARRAY, // []
		
		// changing operation
		CALL,
		SET,
		
		// no operation
		NOP,
	}
	
	// Get the type of this expression
	Type getType();
	
	List<Expression> getExpressions();
	
	default boolean isPure() {
		switch(getType()) {
			case SET:
			case CALL:
				return false;
			
			default:
				return true;
		}
	}
}
