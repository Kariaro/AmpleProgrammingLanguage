package hardcoded.exporter.x86;

import java.util.List;

class AsmOpr {
	private final List<OprPart> parts;
	private final boolean isAddress;
	private final int address_size;
	
	AsmOpr(List<OprPart> parts) {
		this(parts, 0, false);
	}
	
	AsmOpr(List<OprPart> parts, int address_size, boolean isAddress) {
		this.parts = parts;
		this.address_size = address_size;
		this.isAddress = isAddress;
	}
	
	// TODO: Only return OprPart.reg/num from this method
	//       and return the combining operations '+'/'*'
	//       in a separate method!
	
	
	// Get the operand part at the specified index
	private OprPart getPart(int index) {
		return parts.get(index);
	}
	
	// Get the value of an operand part at the specified index
	public Object getObject(int index) {
		return getPart(index).value();
	}
	
	/**
	 * Returns the register at the {@code index} if a register exist otherwise {@code null}.
	 * @param	index	the index of the register operand
	 * @return	the register at the specified {@code index}
	 */
	public RegisterX86 getRegister(int index) {
		return (RegisterX86)getObject(index);
	}
	
	/**
	 * Returns the immediate value located at the given position.
	 * @param	index	the index of the element
	 * @return	the register at the specified {@code index}
	 */
	public long getImmediate(int index) {
		return (long)getObject(index);
	}
	
	public int length() {
		return parts.size();
	}
	
	/**
	 * Returns the size of this assembly operand.
	 * @return the size of this assembly operand
	 */
	public int getSize() {
		if(isAddress) return address_size;
		return getPart(0).size();
	}
	
	/**
	 * Returns {@code true} if this operand is a immediate value.
	 * @return {@code true} if this operand is a immediate value
	 */
	public boolean isImmediate() {
		if(isAddress || parts.size() != 1) return false;
		return getPart(0) instanceof OprPart.Num;
	}
	
	/**
	 * Returns {@code true} if this operand at the position was a immediate value.
	 * @param index the position of the element to check
	 * @return {@code true} if this operand at the position was a immediate value
	 */
	public boolean hasImmediateAt(int index) {
		return getPart(index) instanceof OprPart.Num;
	}
	
	/**
	 * Returns {@code true} if this operand is a register.
	 * @return {@code true} if this operand is a register
	 */
	public boolean isRegister() {
		if(isAddress || parts.size() != 1) return false;
		return getPart(0) instanceof OprPart.Reg;
	}
	
	/**
	 * Returns {@code true} if this operand at the position was a register.
	 * @param index the position of the element to check
	 * @return {@code true} if this operand at the position was a register
	 */
	public boolean hasRegisterAt(int index) {
		return getPart(index) instanceof OprPart.Reg;
	}
	
	/**
	 * Returns {@code true} if this operand is a memory pointer.
	 * @return {@code true} if this operand is a memory pointer
	 */
	public boolean isMemory() {
		return isAddress;
	}
	
	/**
	 * Compare a string with the value of {@code toString()}
	 * 
	 * @param	pattern	the string to check if it matches
	 * @return	{@code true} if the strings match
	 */
	public boolean equalsContent(String string) {
		return toString().equalsIgnoreCase(string);
	}
	
	public String toString() {
		StringBuilder sb = new StringBuilder();
		for(OprPart part : parts) {
			sb.append(part).append(' ');
		}
		
		String value = sb.toString().trim().replace("+ -", "- ").toLowerCase();
		
		if(isAddress) {
			if(address_size == 8) return "byte [" + value + "]";
			if(address_size == 16) return "word [" + value + "]";
			if(address_size == 32) return "dword [" + value + "]";
			if(address_size == 64) return "qword [" + value + "]";
			return "[" + value + "]";
		}
		
		return value;
	}
}
