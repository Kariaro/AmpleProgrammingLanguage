package hardcoded.exporter.x86;

import hardcoded.utils.IntBuffer;

class Opcode {
	private int rexval = 0;
	private int regval = 0; // Opcode bits
	private boolean addrex; // Rex prefix [ 0100 WRBX ]
	private boolean adsize; // Address size override: 16/64 bit
	private boolean opsize; // Operand size override: 16 bit
	private boolean regopr; // Operand is written in the opcode.
	
	private int[] opcode;
	private final IntBuffer postfix;
	
	public Opcode() {
		opcode = new int[0];
		postfix = new IntBuffer(15);
	}
	
	public Opcode setRex(boolean enable) {
		addrex = true;
		return this;
	}
	
	/** Use a 64 bit operand size. */
	public Opcode setRexW() { return setRex(rexval | 8); }
	/** 1 bit extension to ModRm.reg */
	public Opcode setRexR() { return setRex(rexval | 4); }
	/** 1 bit extension to Sib.index */
	public Opcode setRexX() { return setRex(rexval | 2); }
	/** 1 bit extension to ModRm.rm */
	public Opcode setRexB() { return setRex(rexval | 1); }
	

	/** Use a 64 bit operand size. */
	public Opcode unsetRexW() { return setRex(rexval & 7); }
	/** 1 bit extension to ModRm.reg */
	public Opcode unsetRexR() { return setRex(rexval & 11); }
	/** 1 bit extension to Sib.index */
	public Opcode unsetRexX() { return setRex(rexval & 13); }
	/** 1 bit extension to ModRm.rm */
	public Opcode unsetRexB() { return setRex(rexval & 14); }
	
	public Opcode setRex(int mask) {
		rexval = mask & 0xf;
		return this;
	}
	
	public Opcode setAddressSize(boolean enable) {
		 adsize = enable;
		 return this;
	}
	
	public Opcode setOperandSize(boolean enable) {
		 opsize = enable;
		 return this;
	}
	
	public boolean hasOperandSize() { return opsize; }
	public boolean hasAddressSize() { return adsize; }
	public boolean hasRexW() { return (rexval & 8) != 0; }
	
	public Opcode setOpcodeRegister(int value) {
		regval = value & 7;
		regopr = true;
		return this;
	}
	
	public Opcode setOpcode(int[] array) {
		opcode = array.clone();
		return this;
	}
	
	public IntBuffer getPostfix() {
		return postfix;
	}
	
	public int[] build() {
		if(opcode.length == 0) return null;
		
		IntBuffer buffer = new IntBuffer(15);
		
		if(rexval != 0) addrex = true;
		if(adsize) buffer.write(0x67);
		if(opsize) buffer.write(0x66);
		if(addrex) buffer.write(0x40 | rexval);
		buffer.write(opcode);
		
		if(regopr) buffer.writeOffset(buffer.read(0) | regval, -1);
		buffer.write(postfix);
		
		return buffer.toArray();
	}
}