package hardcoded.exporter.chockintosh;

enum OpCode {
	// Standard Instructions
	NOP      (0b0000_0000, false), // No operation
	GOTO     (0b0000_0001, false), // Go to address
	OUTCLK   (0b0000_0010, false), // Clocks the output clock line
	RETURN   (0b0000_0011, false), // Return from subroutine
	WAIT     (0b0000_0100, false), // Waits for input
	HALT     (0b0000_0101, false), // Halts the clock
	CALL     (0b0000_0110, false), // Call a subroutine
	CALLP    (0b0000_0111, false), // Call a subroutine at pointer
	
	// Branching Instructions
	SIF      (0b0000_1000, false), // Skip two bytes if FLAGMASK & FLAG != 0
	SIF_CL   (0b0000_1001, false), // Skip two bytes if L carry
	SIF_CM   (0b0000_1010, false), // Skip two bytes if M carry
	SIF_0L   (0b0000_1011, false), // Skip two bytes if W == 0
	SIF_0M   (0b0000_1100, false), // Skip two bytes if W2 == 0
	SIF_EQ   (0b0000_1101, false), // Skip two bytes if WW == XX
	SIF_GT   (0b0000_1110, false), // Skip two bytes if WW > XX
	SIF_LT   (0b0000_1111, false), // Skip two bytes if WW < XX
	
	// Register Instructions
	CLR      (0b0001_0000, true),  // Clear register
	INC      (0b0010_0000, true),  // Increment reg value
	INVERT   (0b0011_0000, true),  // Inverts the register
	DEC      (0b0100_0000, true),  // Decrement reg value
	SETRW    (0b0101_0000, true),
	SETWR    (0b0110_0000, true),
	SETRW2   (0b0111_0000, true),
	SETX     (0b1000_0000, true),
	SETRX    (0b1001_0000, true),
	SETRX2   (0b1010_0000, true),
	SETRAMAW (0b1011_0000, true),
	
	
	MOVERW   (0b0101_0000, true),  // Moves R to W
	MOVEWR   (0b0110_0000, true),  // Moves W to R
	MOVE16   (0b0111_0000, true),  // Moves Y1 to Z1 and Y2 to Z2
	WRITETO  (0b1000_0000, true),  // Open a register
	READFROM (0b1001_0000, true),  // Read from a register, (executed in the same cycle as WRITETO)
	MOVECR   (0b1010_0000, true),  // Set reg to constant
	GRABP    (0b1011_0000, true),  // Take from rom at pointer
	PUSH     (0b1100_0000, true),  // ???
	POP      (0b1101_0000, true),  // ???
	
	// ALU Instructions
	OR       (0b1111_0000, false), // W = W or X
	AND      (0b1111_0001, false), // W = W and X
	XOR      (0b1111_0010, false), // W = W xor X
	ADD      (0b1111_0011, false), // W = W + X
	SUB      (0b1111_0100, false), // W = W - X
	MUL      (0b1111_0101, false), // W = W * X
	BSX      (0b1111_0110, false), // W = W >> X
	
	// Serial Instructions
	CALLSERIAL (0b1111_1000, false), // Enter a serial memory bank
	RETSERIAL  (0b1111_1001, false), // Return from a serial memory bank
	RETSERW0   (0b1111_1010, false), // Return from a serial memory bank only if W is zero
	
	;
	
	final int code;
	final boolean hasRegister;
	private OpCode(int code, boolean hasRegister) {
		this.code = code;
		this.hasRegister = hasRegister;
	}
	
	public static OpCode get(int index) {
		for(OpCode op : values()) {
			if(op.hasRegister ? (op.code & 0xf0) == (index & 0xf0):(op.code == index)) {
				return op;
			}
		}
		
		return null;
	}
}