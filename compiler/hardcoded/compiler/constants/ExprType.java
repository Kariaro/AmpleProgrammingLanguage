package hardcoded.compiler.constants;

public enum ExprType {
	// Memory
	set,	// x = y
	
	// Math
	add,	// x + y
	sub,	// x - y
	div,	// x / y
	mul,	// x * y
	mod,	// x % y
	xor,	// x ^ y
	and,	// x & y
	or,		// x | y
	shl,	// x << y
	shr,	// x >> y
	
	// Unary
	not,	// !x
	nor,	// ~x
	neg,	// -x
	
	// Compares				[Only returns zero or one]
	eq,		// x == y
	neq,	// x != y
	gt,		// x >  y
	gte,	// x >= y
	lt,		// x <  y
	lte,	// x <= y
	cor,	// x || y
	cand,	// x && y
	
	// Pointer
	addptr, // &x
	decptr, // *x
	
	// Function
	call,	// Call
	ret,	// Return
	nop,	// No operation
	
	leave,	// Break
	loop,	// Continue
	
	atom,	// Atom
	cast,	// Cast
	comma,
	invalid, // Invalid expression type
}
