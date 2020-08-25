package hardcoded.compiler.constants;

/**
 * This is the full instruction set for the ir part of the compiler.
 * 
 * @author HardCoded
 */
public enum Insts {
	mov,		// mov		[R0], [R1]				Set R0 to R1
	
	write,		// write	[R0], [R1]				Write the value of R1 to the pointer R0
	read,		// read		[R0], [R1]				Set R0 to value of pointer R1
	
	// Math
	add,		// add		[R0], [R1], [R2]		Set R0 to (R1 + R2)
	sub,		// sub		[R0], [R1], [R2]		Set R0 to (R1 - R2)
	mul,		// mul		[R0], [R1], [R2]		Set R0 to (R1 * R2)
	div,		// div		[R0], [R1], [R2]		Set R0 to (R1 / R2)
	
	xor,		// xor		[R0], [R1], [R2]		Set R0 to (R1 ^ R2)
	and,		// and		[R0], [R1], [R2]		Set R0 to (R1 & R2)
	or,			// or		[R0], [R1], [R2]		Set R0 to (R1 | R2)
	shr,		// shr		[R0], [R1], [R2]		Set R0 to (R1>> R2)
	shl,		// shl		[R0], [R1], [R2]		Set R0 to (R1<< R2)
	
	neg,		// neg		[R0], [R1]				Set R0 to (-R1)
	nor,		// nor		[R0], [R1]				Set R0 to (~R1)
	not,		// not		[R0], [R1]				Set R0 to (!R1)
	
	// Function
	call,		// call		[R0], [LABEL]			Call label and set R0 to result
	ret,		// ret		[R0]					Return R0
	br,			// br		[LABEL]					Jump to [LABEL]
	brz,		// brz		[LABEL], [R0]			Branch to label if R0 is zero
	
	label,		// label	?
}
