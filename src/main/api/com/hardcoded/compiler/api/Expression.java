package com.hardcoded.compiler.api;

import java.util.List;

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
}
