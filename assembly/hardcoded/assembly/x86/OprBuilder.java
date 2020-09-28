package hardcoded.assembly.x86;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

// TODO: Disp and Imm should not be inputed as 'imm8 16 32' but instead
//       be variable and changed to the correct size later. Then the
//       compiler should throw an error if it's illegal to use that combination.

/**
 * A builder for assembly operators.
 * 
 * @author HardCoded
 */
public final class OprBuilder {
	final List<OprPart> parts = new ArrayList<>();
	
	/**
	 * Returns a assembly operator from a string.
	 * @param value the string to convert into an assembly operator
	 * @return a assembly operator from a string
	 */
	public AsmOpr fromString(String value) {
		return OprStringBuilder.fromString(this, value);
	}
	
	
	
	/**
	 * Returns the next builder to continue.
	 * @param register the assembly register to add
	 * @return the next builder to continue
	 */
	public OprBuilder reg(RegisterX86 register) {
		if(register == null)
			throw new NullPointerException();
		
		parts.add(new OprPart.Reg(register));
		return this;
	}
	
	/**
	 * Returns the next builder to continue.
	 * @param	value	the value of the number
	 * @return the next builder to continue
	 */
	public OprBuilder num(long value) {
		parts.add(new OprPart.Num(value));
		return this;
	}
	
	/**
	 * Returns the next builder to continue.
	 * @param value the value of the displacement
	 * @return the next builder to continue
	 */
	public AsmOpr imm(long value) {
		return new AsmOpr(Arrays.asList(new OprPart.Num(value)));
	}
	
	/**
	 * Returns the next builder to continue.
	 * @return the next builder to continue
	 */
	public OprBuilder scalar(int value) {
		parts.add(new OprPart.Num((byte)value));
		return this;
	}
	
	
	
	/**
	 * Returns the next builder to continue.
	 * @return the next builder to continue
	 */
	public OprBuilder add() {
		parts.add(new OprPart.Sym('+'));
		return this;
	}
	
	/**
	 * Returns the next builder to continue.
	 * @return the next builder to continue
	 */
	public OprBuilder mul() {
		parts.add(new OprPart.Sym('*'));
		return this;
	}
	
	
	
	/**
	 * Returns a new {@code AsmOperator}.
	 * @return a new {@code AsmOperator}
	 */
	public AsmOpr get() {
		return new AsmOpr(parts);
	}
	
	/**
	 * Returns a new {@code AsmOperator} as a byte pointer.
	 * @return a new {@code AsmOperator} as a byte pointer
	 */
	public AsmOpr ptrByte() { return ptr(8); }
	
	/**
	 * Returns a new {@code AsmOperator} as a word pointer.
	 * @return a new {@code AsmOperator} as a word pointer
	 */
	public AsmOpr ptrWord() { return ptr(16); }
	
	/**
	 * Returns a new {@code AsmOperator} as a dword pointer.
	 * @return a new {@code AsmOperator} as a dword pointer
	 */
	public AsmOpr ptrDword() { return ptr(32); }
	
	/**
	 * Returns a new {@code AsmOperator} as a qword pointer.
	 * @return a new {@code AsmOperator} as a qword pointer
	 */
	public AsmOpr ptrQword() { return ptr(64); }
	
	/**
	 * Returns a new {@code AsmOperator} as a pointer.
	 * @return a new {@code AsmOperator} as a pointer
	 */
	public AsmOpr ptr() { return ptr(0); }
	
	/**
	 * Returns a new {@code AsmOperator} as a pointer with a specified size.
	 * @param	bits	the size of the pointer
	 * @return a new {@code AsmOperator} as a pointer with a specified size
	 */
	public AsmOpr ptr(int bits) {
		return new AsmOpr(parts, bits, true);
	}
}