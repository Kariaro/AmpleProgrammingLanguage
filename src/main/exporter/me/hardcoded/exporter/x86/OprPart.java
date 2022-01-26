package me.hardcoded.exporter.x86;

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
 *   <dt>{@link Num}</dt>
 *   <dd>Used when defining numbers.</dd>
 * </dl>
 * 
 * @author HardCoded
 */
abstract class OprPart {
	
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

		@Override
		int size() {
			return -1;
		}

		@Override
		Object value() {
			return symbol;
		}

		@Override
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

		@Override
		int size() {
			return register.bits;
		}
		
		@Override
		Object value() {
			return register;
		}

		@Override
		public String toString() {
			return register.toString();
		}
	}
	
	/**
	 * Used to define a number value inside an assembly operator.
	 * 
	 * @see OprPart
	 */
	static class Num extends OprPart {
		private final long value;
		private final int bits;
		
		Num(long value) {
			this.bits = NumberUtils.getBitsSize(value);
			this.value = value;
		}

		@Override
		int size() {
			return bits;
		}

		@Override
		Object value() {
			return value;
		}

		@Override
		public String toString() {
			if(value < 0) return String.format("-0x%01x", -value);
			return String.format("0x%01x", value);
		}
	}
}
