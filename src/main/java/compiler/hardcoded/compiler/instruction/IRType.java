package hardcoded.compiler.instruction;

import hardcoded.compiler.constants.ExprType;

/**
 * This is the full instruction set for the ir part of the compiler.
 * 
 * @author HardCoded
 */
public enum IRType {
	// Memory instructions
	mov(2),		// mov			[R0], [R1]						R0 = R1
	write(2),	// write		[R0], [R1]						mem[R0] = R1
	read(2),	// read			[R0], [R1]						R0 = mem[R1]
	
	
	/* Logical instructions are instructions that does
	 * arithmetics operations on two registers.
	 * 
	 * The size changes the value return by the output.
	 * 
	 * There is differences between unsigned values and non
	 * unsigned values. These will be importaint to save
	 * for the last stage.
	 */
	add(3),		// add			[R0], [R1], [R2]				Set R0 to (R1 + R2)
	sub(3),		// sub			[R0], [R1], [R2]				Set R0 to (R1 - R2)
	mul(3),		// mul			[R0], [R1], [R2]				Set R0 to (R1 * R2)
	mod(3),		// mod			[R0], [R1], [R2]				Set R0 to (R1 % R2)
	div(3),		// div			[R0], [R1], [R2]				Set R0 to (R1 / R2)
	xor(3),		// xor			[R0], [R1], [R2]				Set R0 to (R1 ^ R2)
	and(3),		// and			[R0], [R1], [R2]				Set R0 to (R1 & R2)
	or(3),		// or			[R0], [R1], [R2]				Set R0 to (R1 | R2)
	shr(3),		// shr			[R0], [R1], [R2]				Set R0 to (R1>> R2)
	shl(3),		// shl			[R0], [R1], [R2]				Set R0 to (R1<< R2)
	neg(2),		// neg			[R0], [R1]						Set R0 to (-R1)
	nor(2),		// nor			[R0], [R1]						Set R0 to (~R1)
	not(2),		// not			[R0], [R1]						Set R0 to (!R1)
	
	
	/* Boolean instructions are instructions that returns either true
	 * or false.
	 * 
	 * The return value of these instructions will always be be integer.
	 */
	eq(3),		// eq			[R0], [R1], [R2]				Set R0 to (R1== R2)
	neq(3),		// neq			[R0], [R1], [R2]				Set R0 to (R1!= R2)
	lt(3),		// lt			[R0], [R1], [R2]				Set R0 to (R1 < R2)
	lte(3),		// lte			[R0], [R1], [R2]				Set R0 to (R1<= R2)
	gt(3),		// gt			[R0], [R1], [R2]				Set R0 to (R1 > R2)
	gte(3),		// gte			[R0], [R1], [R2]				Set R0 to (R1>= R2)
	
	
	// Branching instructions
	call(-1),	// call			[R0], [LABEL]					Call label and set R0 to result
	ret(1),		// ret			[R0]							Return R0
	br(1),		// br			[LABEL]							Jump to [LABEL]
	brz(2),		// brz			[R0], [LABEL]					Jump to [LABEL] if R0 is zero
	bnz(2),		// bnz			[R0], [LABEL]					Jump to [LABEL] if R0 is not zero
	
	
	// Misc
	nop(-1),	// nop											No operation
	label(1),	// label		[LABEL]							Define a label
	data(2),	// data			[LABEL], [VALUE]				Define data of type [TYPE]
	;
	
	public final int args;
	private IRType(int args) {
		this.args = args;
	}
	
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
			case mod: return mod;
			
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
