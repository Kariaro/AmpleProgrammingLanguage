package hardcoded.compiler.assembler.operator;

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
 *   <dt>{@link Disp}</dt>
 *   <dd>Used when specifying a displacement value.</dd>
 *   <dd><code>[r/m + disp]</code></dd>
 *   
 *   <dt>{@link Scalar}</dt>
 *   <dd>Used when specifying a SIB operator.</dd>
 *   <dd><code>[base + index * scalar]</code></dd>
 * </dl>
 * 
 * @author HardCoded
 */
abstract class OperatorPart {
	
	/**
	 * Used to define addition or multiplication inside an assembly operator.
	 * 
	 * @see OperatorPart
	 */
	static class Sym extends OperatorPart {
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
	 * @see OperatorPart
	 */
	static class Reg extends OperatorPart {
		private final Register register;
		
		Reg(Register register) {
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
	
	/**
	 * Used to define a immediate value inside an assembly operator.
	 * 
	 * @see OperatorPart
	 */
	static class Imm extends OperatorPart {
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
	 * Used to define a displacement value inside an assembly operator.
	 * 
	 * @see OperatorPart
	 */
	static class Disp extends OperatorPart {
		private final int bits;
		private final Number value;
		
		Disp(int bits, Number value) {
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
			return String.format("disp%d=0x%0" + (bits / 4) + "x", bits, value);
		}
	}
	
	/**
	 * Used to define a scalar value inside an assembly operator.
	 * 
	 * @see OperatorPart
	 */
	static class Scalar extends OperatorPart {
		private final int value;
		
		Scalar(int value) {
			this.value = value;
		}

		int size() {
			return 0;
		}
		
		Object value() {
			return value;
		}
		
		public String toString() {
			return String.format("0x%02x", value);
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
