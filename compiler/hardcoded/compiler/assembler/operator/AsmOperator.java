package hardcoded.compiler.assembler.operator;

import java.util.List;

public class AsmOperator {
	private final List<OperatorPart> parts;
	private final boolean isAddress;
	private final int address_size;
	
	AsmOperator(List<OperatorPart> parts) {
		this(parts, 0, false);
	}
	
	AsmOperator(List<OperatorPart> parts, int address_size, boolean isAddress) {
		this.parts = parts;
		this.address_size = address_size;
		this.isAddress = isAddress;
	}
	
	// Get the operator part at the specified index
	OperatorPart getPart(int index) {
		return parts.get(index);
	}
	
	// Get the value of an operator part at the specified index
	Object getObject(int index) {
		return getPart(index).value();
	}
	
	/**
	 * Returns the size of this assembly operator.
	 * @return the size of this assembly operator
	 */
	public int getSize() {
		if(isAddress) return address_size;
		return getPart(0).size();
	}
	
	/**
	 * Returns {@code true} if this operator is a immediate value.
	 * @return {@code true} if this operator is a immediate value
	 */
	public boolean isImmediate() {
		if(isAddress || parts.size() != 1) return false;
		return getObject(0) instanceof OperatorPart.Imm;
	}
	
	/**
	 * Returns {@code true} if this operator is a register.
	 * @return {@code true} if this operator is a register
	 */
	public boolean isRegister() {
		if(isAddress || parts.size() != 1) return false;
		return getObject(0) instanceof OperatorPart.Reg;
	}
	
	/**
	 * Returns {@code true} if this operator is a memory pointer.
	 * @return {@code true} if this operator is a memory pointer
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
		for(OperatorPart part : parts) {
			sb.append(part).append(' ');
		}
		
		String value = sb.toString().trim();
		
		if(isAddress) {
			if(address_size == 8) return "byte [" + value + "]";
			else if(address_size == 16) return "word [" + value + "]";
			else if(address_size == 32) return "dword [" + value + "]";
			else if(address_size == 64) return "qword [" + value + "]";
			else return "[" + value + "]";
		}
		
		return value;
	}
}
