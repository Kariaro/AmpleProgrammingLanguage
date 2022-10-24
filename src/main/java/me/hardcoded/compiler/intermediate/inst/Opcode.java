package me.hardcoded.compiler.intermediate.inst;

public enum Opcode {
	// Arithmetic instructions
	MOV,                // r0 = r1           (r = r/imm)
	ADD,                // r0 = r0 +  r1     (r = r +  r)
	SUB,                // r0 = r0 -  r1     (r = r -  r)
	AND,                // r0 = r0 &  r1     (r = r &  r)
	XOR,                // r0 = r0 ^  r1     (r = r ^  r)
	OR,                 // r0 = r0 |  r1     (r = r |  r)
	SHR,                // r0 = r0 >> r1     (r = r >> r)
	SHL,                // r0 = r0 << r1     (r = r << r)
	EQ,                 // r0 = r0 == r1     (r = r == r)
	NEQ,                // r0 = r0 != r1     (r = r != r)
	
	MUL,                // r0 = r0 *  r1     (r = r *  r)
	DIV,                // r0 = r0 /  r1     (r = r /  r)
	MOD,                // r0 = r0 %  r1     (r = r %  r)
	GTE,                // r0 = r0 >= r1     (r = r >= r)
	GT,                 // r0 = r0 >  r1     (r = r >  r)
	
	IMUL,               // r0 = r0 *  r1     (r = r *  r)
	IDIV,               // r0 = r0 /  r1     (r = r /  r)
	IMOD,               // r0 = r0 %  r1     (r = r %  r)
	IGTE,               // r0 = r0 >= r1     (r = r >= r)
	IGT,                // r0 = r0 >  r1     (r = r >  r)
	
	// TODO: Redundant instructions
	LTE,                // r0 = r0 <= r1     (r = r <= r) (GT r2, r1)
	LT,                 // r0 = r0 <  r1     (r = r <  r) (GTE r2, r1)
	ILTE,               // r0 = r0 <= r1     (r = r <= r) (IGT r2, r1)
	ILT,                // r0 = r0 <  r1     (r = r <  r) (IGTE r2, r1)
	
	// Floating point operations
	FADD,
	FSUB,
	FEQ,
	FNEQ,
	FMOD,
	FMUL,
	FDIV,
	FGTE,
	FGT,
	FLTE,
	FLT,
	
	// Unary instructions
	NOT,                // r0 = !(r1)
	NEG,                // r0 = -(r1) // TODO: Change to SUB
	NOR,                // r0 = ~(r1)
	
	// Size instructions
	TRUNC,              // r0, SIZE -> truncate
	SEXT,               // r0, SIZE -> sign extend
	ZEXT,               // r0, SIZE -> zero extend
	
	// Special instructions
	STACK_ALLOC,        // allocate memory on the stack
	INLINE_ASM,         // asm('type', 'command', references...)
	
	// Memory instructions
	STORE,              // (r0 [r1]) = r2
	LOAD,               // r0 = (r1 [r2])
	
	// Branching instructions
	LABEL,              // A label
	JMP,                // goto l0
	JNZ,                // if (r0 != 0) goto l1
	JZ,                 // if (r0 == 0) goto l1
	RET,                // Return from a function
	
	// Call instructions
	CALL,               // Variable instruction
}
