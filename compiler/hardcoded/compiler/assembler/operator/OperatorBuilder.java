package hardcoded.compiler.assembler.operator;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * A builder for assembly operators.
 * 
 * @author HardCoded
 */
public final class OperatorBuilder {
	final List<OperatorPart> parts = new ArrayList<>();
	
	/**
	 * Returns the next builder to continue.
	 * @param value the value of the displacement
	 * @return the next builder to continue
	 */
	public OperatorBuilder disp8(long value) {
		if(value < Byte.MIN_VALUE || value > Byte.MAX_VALUE)
			throw new RuntimeException("Disp value is outside range for 8 bits. (" + value + ")");
		
		parts.add(new OperatorPart.Disp(8, (byte)value));
		return this;
	}
	
	/**
	 * Returns the next builder to continue.
	 * @param value the value of the displacement
	 * @return the next builder to continue
	 */
	public OperatorBuilder disp16(long value) {
		if(value < Short.MIN_VALUE || value > Short.MAX_VALUE)
			throw new RuntimeException("Disp value is outside range for 16 bits. (" + value + ")");

		parts.add(new OperatorPart.Disp(16, (short)value));
		return this;
	}
	
	/**
	 * Returns the next builder to continue.
	 * @param value the value of the displacement
	 * @return the next builder to continue
	 */
	public OperatorBuilder disp32(long value) {
		if(value < Integer.MIN_VALUE || value > Integer.MAX_VALUE)
			throw new RuntimeException("Disp value is outside range for 32 bits. (" + value + ")");
		
		parts.add(new OperatorPart.Disp(32, (int)value));
		return this;
	}
	
	
	
	/**
	 * Returns a new {@code AsmOperator} with a immediate value.
	 * @param value the value of the immediate value
	 * @return a new {@code AsmOperator} with a immediate value
	 */
	public AsmOperator imm8(long value) {
		if(value < Byte.MIN_VALUE || value > Byte.MAX_VALUE)
			throw new RuntimeException("Immediate value is outside range for 8 bits. (" + value + ")");
		
		return new AsmOperator(Arrays.asList(new OperatorPart.Imm(8, (byte)value)));
	}
	
	/**
	 * Returns a new {@code AsmOperator} with a immediate value.
	 * @param value the value of the immediate value
	 * @return a new {@code AsmOperator} with a immediate value
	 */
	public AsmOperator imm16(long value) {
		if(value < Short.MIN_VALUE || value > Short.MAX_VALUE)
			throw new RuntimeException("Immediate value is outside range for 16 bits. (" + value + ")");
		
		return new AsmOperator(Arrays.asList(new OperatorPart.Imm(16, (short)value)));
	}
	
	/**
	 * Returns a new {@code AsmOperator} with a immediate value.
	 * @param value the value of the immediate value
	 * @return a new {@code AsmOperator} with a immediate value
	 */
	public AsmOperator imm32(long value) {
		if(value < Integer.MIN_VALUE || value > Integer.MAX_VALUE)
			throw new RuntimeException("Immediate value is outside range for 32 bits. (" + value + ")");
		
		return new AsmOperator(Arrays.asList(new OperatorPart.Imm(32, (int)value)));
	}
	
	/**
	 * Returns a new {@code AsmOperator} with a immediate value.
	 * @param value the value of the immediate value
	 * @return a new {@code AsmOperator} with a immediate value
	 */
	public AsmOperator imm64(long value) {
		return new AsmOperator(Arrays.asList(new OperatorPart.Imm(64, value)));
	}
	
	
	
	
	/**
	 * Returns the next builder to continue.
	 * @return the next builder to continue
	 */
	public OperatorBuilder scalar(int value) {
		parts.add(new OperatorPart.Scalar(value));
		return this;
	}
	
	
	
	/**
	 * Returns the next builder to continue.
	 * @return the next builder to continue
	 */
	public OperatorBuilder add() {
		parts.add(new OperatorPart.Sym('+'));
		return this;
	}
	
	/**
	 * Returns the next builder to continue.
	 * @return the next builder to continue
	 */
	public OperatorBuilder mul() {
		parts.add(new OperatorPart.Sym('*'));
		return this;
	}
	
	
	
	/**
	 * Returns a new {@code AsmOperator}.
	 * @return a new {@code AsmOperator}
	 */
	public AsmOperator get() {
		return new AsmOperator(parts);
	}
	
	/**
	 * Returns a new {@code AsmOperator} as a byte pointer.
	 * @return a new {@code AsmOperator} as a byte pointer
	 */
	public AsmOperator ptrByte() {
		return new AsmOperator(parts, 8, true);
	}
	
	/**
	 * Returns a new {@code AsmOperator} as a word pointer.
	 * @return a new {@code AsmOperator} as a word pointer
	 */
	public AsmOperator ptrWord() { 
		return new AsmOperator(parts, 16, true);
	}
	
	/**
	 * Returns a new {@code AsmOperator} as a dword pointer.
	 * @return a new {@code AsmOperator} as a dword pointer
	 */
	public AsmOperator ptrDword() {
		return new AsmOperator(parts, 32, true);
	}
	
	/**
	 * Returns a new {@code AsmOperator} as a qword pointer.
	 * @return a new {@code AsmOperator} as a qword pointer
	 */
	public AsmOperator ptrQword() {
		return new AsmOperator(parts, 64, true);
	}
	
	/**
	 * Returns a new {@code AsmOperator} as a pointer.
	 * @return a new {@code AsmOperator} as a pointer
	 */
	public AsmOperator ptr() {
		return new AsmOperator(parts, 0, true);
	}
}