package hardcoded.assembly.x86;

import java.util.List;

import hardcoded.assembly.impl.AsmFactory;

public class AsmOpr {
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
	
	// Get the operator part at the specified index
	public OprPart getPart(int index) {
		return parts.get(index);
	}
	
	// Get the value of an operator part at the specified index
	public Object getObject(int index) {
		return getPart(index).value();
	}
	
	/**
	 * Returns the register at the {@code index} if a register exist otherwise {@code null}.
	 * @param	index	the index of the register operand
	 * @return	the register at the specified {@code index}
	 */
	public RegisterX86 getRegister(int index) {
		Object obj = getObject(index);
		if(obj instanceof RegisterX86) return (RegisterX86) obj;
		return null;
	}
	
	public int length() {
		return parts.size();
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
		return getPart(0) instanceof OprPart.Num;
	}
	
	/**
	 * Returns {@code true} if this operator is a register.
	 * @return {@code true} if this operator is a register
	 */
	public boolean isRegister() {
		if(isAddress || parts.size() != 1) return false;
		return getPart(0) instanceof OprPart.Reg;
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
		for(OprPart part : parts) {
			sb.append(part).append(' ');
		}
		
		String value = sb.toString().trim().replace("+ -", "- ");
		
		if(isAddress) {
			if(address_size > 0) return AsmFactory.getSizeString(address_size) + " [" + value + "]";
			return "[" + value + "]";
		}
		
		return value;
	}
}
