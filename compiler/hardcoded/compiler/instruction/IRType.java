package hardcoded.compiler.instruction;

import hardcoded.compiler.constants.ExprType;

/**
 * This is the full instruction set for the ir part of the compiler.
 * 
 * @author HardCoded
 */
public enum IRType {
	// Memory instructions
	mov,		// mov			[R0], [R1]						R0 = R1
	write,		// write		[R0], [R1]						mem[R0] = R1
	read,		// read			[R0], [R1]						R0 = mem[R1]
	
	
	/* Logical instructions are instructions that does
	 * arithmetics operations on two registers.
	 * 
	 * The size changes the value return by the output.
	 * 
	 * There is differences between unsigned values and non
	 * unsigned values. These will be importaint to save
	 * for the last stage.
	 */
	add,		// add			[R0], [R1], [R2]				Set R0 to (R1 + R2)
	sub,		// sub			[R0], [R1], [R2]				Set R0 to (R1 - R2)
	mul,		// mul			[R0], [R1], [R2]				Set R0 to (R1 * R2)
	div,		// div			[R0], [R1], [R2]				Set R0 to (R1 / R2)
	xor,		// xor			[R0], [R1], [R2]				Set R0 to (R1 ^ R2)
	and,		// and			[R0], [R1], [R2]				Set R0 to (R1 & R2)
	or,			// or			[R0], [R1], [R2]				Set R0 to (R1 | R2)
	shr,		// shr			[R0], [R1], [R2]				Set R0 to (R1>> R2)
	shl,		// shl			[R0], [R1], [R2]				Set R0 to (R1<< R2)
	neg,		// neg			[R0], [R1]						Set R0 to (-R1)
	nor,		// nor			[R0], [R1]						Set R0 to (~R1)
	not,		// not			[R0], [R1]						Set R0 to (!R1)
	
	
	/* Boolean instructions are instructions that returns either true
	 * or false.
	 * 
	 * The return value of these instructions will always be be integer.
	 */
	eq,			// eq			[R0], [R1], [R2]				Set R0 to (R1== R2)
	neq,		// neq			[R0], [R1], [R2]				Set R0 to (R1!= R2)
	lt,			// lt			[R0], [R1], [R2]				Set R0 to (R1 < R2)
	lte,		// lte			[R0], [R1], [R2]				Set R0 to (R1<= R2)
	gt,			// gt			[R0], [R1], [R2]				Set R0 to (R1 > R2)
	gte,		// gte			[R0], [R1], [R2]				Set R0 to (R1>= R2)
	
	
	// Branching instructions
	call,		// call			[R0], [LABEL]					Call label and set R0 to result
	ret,		// ret			[R0]							Return R0
	br,			// br			[LABEL]							Jump to [LABEL]
	brz,		// brz			[R0], [LABEL]					Jump to [LABEL] if R0 is zero
	bnz,		// bnz			[R0], [LABEL]					Jump to [LABEL] if R0 is not zero
	
	
	// Misc
	nop,		// nop											No operation
	label,		// label										Define a label
	data,		// data			[LABEL], [VALUE]				Define data of type [TYPE]
	;
	
	
	public static final IRType convert(ExprType type) {
		switch(type) {
			case nop: return nop;
			
			case neg: return neg;
			case not: return not;
			case nor: return nor;
			case ret: return ret;
			
			case eq: return eq;
			case neq: return neq;
			case lt: return lt;
			case lte: return lte;
			case gt: return gt;
			case gte: return gte;

			case add: return add;
			case sub: return sub;
			case mul: return mul;
			case div: return div;
			
			case xor: return xor;
			case and: return and;
			case shl: return shl;
			case shr: return shr;
			case or: return or;
			
			default: {
				// Invalid
				return null;
			}
		}
	}
}
