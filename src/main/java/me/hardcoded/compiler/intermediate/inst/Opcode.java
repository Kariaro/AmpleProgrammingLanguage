package me.hardcoded.compiler.intermediate.inst;

public enum Opcode {
	// Arithmetic instructions
	MOV,				// r0 = r1
	ADD,				// r0 = r1 + r2
	SUB,				// r0 = r1 - r2
	AND,				// r0 = r1 & r2
	XOR,				// r0 = r1 ^ r2
	OR,					// r0 = r1 | r2
	EQ,					// r0 = r1 == r2
	NEQ,				// r0 = r1 != r2
	// Not unsigned safe
	MOD,				// r0 = r1 % r2
	MUL,				// r0 = r1 * r2
	DIV,				// r0 = r1 / r2
	SHR,				// r0 = r1 >> r2
	SHL,				// r0 = r1 << r2
	GTE,				// r0 = r1 >= r2
	GT,					// r0 = r1 > r2
	LTE,				// r0 = r1 <= r2
	LT,					// r0 = r1 < r2
	
	// Unary instructions
	NOT,				// r0 = !(r1)
	NEG,				// r0 = -(r1)
	NOR,				// r0 = ~(r1)
	
	// Type instructions
	CAST,				// r0 = ( SIZE )(r1)
	
	// Special instructions
	STACK_ALLOC,        // allocate memory on the stack
	INLINE_ASM,         // asm('type', 'command', references...)
	
	// Memory instructions
	STORE,				// (*r0) = r1
	LOAD,				// r0 = (*r1)
	
	// Branching instructions
	LABEL,				// A label
	JMP,				// Jump to a label
	JNZ,				// Jump if not zero
	JZ,					// Jump if zeros
	RET,				// Return from a function
	
	// Call instructions
	CALL,				// Variable instruction
	
}
