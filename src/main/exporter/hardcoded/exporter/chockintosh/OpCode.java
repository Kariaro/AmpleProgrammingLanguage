package hardcoded.exporter.chockintosh;

enum OpCode {
	// NOP  (0x00),
	LDR  (0x01),
	SDR  (0x02),
	LDL  (0x03),
	SIF  (0x04),
	GOTO (0x05),
	INCW (0x06),
	HALT (0x07),
	NOT  (0x08),
	BSP  (0x0E),
	BSM  (0x09),
	OR   (0x0D),
	AND  (0x0A),
	XOR  (0x0B),
	ADD  (0x0C),
	SUB  (0x0F),
	;
	
	final int code;
	private OpCode(int code) {
		this.code = code;
	}
	
	public static OpCode get(int index) {
		for(OpCode op : values()) {
			if(op.code == index) {
				return op;
			}
		}
		
		return null;
	}
}