package hardcoded.assembly.x86;

/**
 * This class defines all objects used to define assembly operators.
 * 
 * <dl>
 *   <dt>{@link Sym}</dt>
 *   <dd>Used when defining additon or multiplication inside the operator.</dd>
 *   
 *   <dt>{@link Reg}</dt>
 *   <dd>Used when referencing a register.</dd>
 *   
 *   <dt>{@link Imm}</dt>
 *   <dd>Used when creating a immediate value.</dd>
 *   
 *   <dt>{@link Num}</dt>
 *   <dd>Used when defining numbers.</dd>
 * </dl>
 * 
 * @author HardCoded
 */
abstract class OprPart {
	
	/**
	 * Used to define addition or multiplication inside an assembly operator.
	 * 
	 * @see OprPart
	 */
	static class Sym extends OprPart {
		private final char symbol;
		
		Sym(char symbol) {
			this.symbol = symbol;
		}

		int size() {
			return -1;
		}
		
		Object value() {
			return symbol;
		}
		
		public String toString() {
			return Character.toString(symbol);
		}
	}
	
	/**
	 * Used to define register an assembly operator.
	 * 
	 * @see OprPart
	 */
	static class Reg extends OprPart {
		private final RegisterX86 register;
		
		Reg(RegisterX86 register) {
			this.register = register;
		}

		int size() {
			return register.bits;
		}
		
		Object value() {
			return register;
		}
		
		public String toString() {
			return register.toString();
		}
	}
	
	// TODO: Remove Imm and replace with Num.. A number is a Immediate value if it's the only part of a register and is not a pointer.
	/**
	 * Used to define a immediate value inside an assembly operator.
	 * 
	 * @see OprPart
	 */
	static class Imm extends OprPart {
		private final int bits;
		private final Number value;
		
		Imm(int bits, Number value) {
			this.bits = bits;
			this.value = value;
		}

		int size() {
			return bits;
		}
		
		Object value() {
			return value;
		}
		
		public String toString() {
			return String.format("imm%d=0x%0" + (bits / 4) + "x", bits, value);
		}
	}
	
	/**
	 * Used to define a number value inside an assembly operator.
	 * 
	 * @see OprPart
	 */
	static class Num extends OprPart {
		private final Number value;
		private final int bits;
		
		Num(int bits, Number value) {
			this.bits = bits;
			if(bits < 9) value = value.byteValue();
			else if(bits < 17) value = value.shortValue();
			else if(bits < 33) value = value.intValue();
			else if(bits < 65) value = value.longValue();
			
			this.value = value;
		}

		int size() {
			return bits;
		}
		
		Object value() {
			return value;
		}
		
		public String toString() {
			// return String.format("num0x%0" + (bits / 4) + "x", value);
			return String.format("0x%01x", value);
		}
	}
	
	/**
	 * Returns the size of this operator part.
	 * @return the size of this operator part
	 */
	abstract int size();
	
	/**
	 * Returns the value of this operator part.
	 * @return the value of this operator part
	 */
	abstract Object value();
}
