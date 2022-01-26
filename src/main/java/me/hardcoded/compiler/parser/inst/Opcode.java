package me.hardcoded.compiler.parser.inst;

public enum Opcode {
	// Arithmetic instructions
	MOV,				// r0 = r1
	ADD,				// r0 = r1 + r2
	SUB,				// r0 = r1 - r2
	MUL,				// r0 = r1 * r2
	DIV,				// r0 = r1 / r2
	MOD,				// r0 = r1 % r2
	AND,				// r0 = r1 & r2
	XOR,				// r0 = r1 ^ r2
	OR,					// r0 = r1 | r2
	SHR,				// r0 = r1 >> r2
	SHL,				// r0 = r1 << r2
	INC,				// r0 = r1 + 1
	DEC,				// r0 = r1 - 1
	
	// Unary instructions
	NOT,				// r0 = !(r1)
	NEG,				// r0 = -(r1)
	POS,				// r0 = +(r1)
	NOR,				// r0 = ~(r1)
	
	// Memory instructions
	STR,				// (*r0) = r1
	LDL,				// r0 = (*r1)
	
	// Branching instructions
	LABLE,				// A label
	JMP,				// Jump to a label
	JE,					// Jump if equal
	JN,					// Jump if not equal
	RET,				// Return from a function
	
	// Call instructions
	CALL,				// Variable instruction
	
}
