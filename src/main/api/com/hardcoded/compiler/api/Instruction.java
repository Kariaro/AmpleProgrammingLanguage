package com.hardcoded.compiler.api;

/**
 * API access
 * 
 * @author HardCoded
 * @since 0.2.0
 */
public interface Instruction {
	enum Type {
		// no operation
		NOP,
		// no operation. Used for debugging
		MARKER,
		
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
		EQ,
		NEQ,
		LT,
		LTE,
		GT,
		GTE,
		
		// MEMORY
		SET,
		
		// ADDRESS
		LABEL,
		BRZ, // branch if zero
		BNZ, // branch if not zero
		BR, // branch
		
		/** Call a routine */
		CALL,
		
		/** Leave a routine */
		RET,
	}
	
	Type getType();
}
