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
		SUB,			// sub(a, b)    ->    add(a, not(b))
		MUL,
		DIV,
		SHR,
		SHL,
		XOR,
		AND,
		OR,
		MOD,
		EQ,
		NEQ,
		LT,				// lt (a, b)    ->    gte(b, a)
		LTE,            // lte(a, b)    ->    gt (b, a)
		GT,
		GTE,
		
		// MEMORY
		SET,
		
		// ADDRESS
		LABEL,
		BRZ, // branch if zero
		BNZ, // branch if not zero
		BR, // branch
		
		// COMPILER
		INLINE_ARRAY,
		
		/** Call a routine */
		CALL,
		
		/** Leave a routine */
		RET,
		
		;
		
		public boolean isJump() {
			return this == BR
				|| this == BRZ
				|| this == BNZ;
		}
		
		public boolean isConditionalJump() {
			return this == BRZ
				|| this == BNZ;
		}
		
		/**
		 * Returns {@code true} if the instruction modifies data elsewhere
		 * @return {@code true} if the instruction modifies data elsewhere
		 */
		public boolean isVolatile() {
			// WRITE
			return this == CALL;
		}
	}
	
	Type getType();
}
