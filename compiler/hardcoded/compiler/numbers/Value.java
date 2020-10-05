package hardcoded.compiler.numbers;

import java.math.BigDecimal;
import java.math.BigInteger;

/**
 * This is a value interface for number addition.
 * 
 * @author HardCoded
 */
public interface Value {
	
	/**
	 * Returns {@code true} if this value is signed.
	 * @return {@code true} if this value is signed
	 */
	public boolean isSigned();
	
	/**
	 * Returns {@code true} if this value is a floating point number.
	 * @return {@code true} if this value is a floating point number
	 */
	public boolean isFloating();
	
	/**
	 * Returns the amount of bytes this number takes.
	 * @return the amount of bytes this number takes
	 */
	public default int size() {
		return value().getSize();
	}
	
	/**
	 * Returns the value that this number holds.
	 * @return the value that this number holds
	 */
	public BigVal value();
	
	
	public default Value add(Value a) { return next(value().add(a.value())); }
	public default Value sub(Value a) { return next(value().sub(a.value())); }
	public default Value div(Value a) { return next(value().div(a.value())); }
	public default Value mul(Value a) { return next(value().mul(a.value())); }
	public default Value mod(Value a) { return next(value().mod(a.value())); }
	public default Value and(Value a) { return next(value().and(a.value())); }
	public default Value xor(Value a) { return next(value().xor(a.value())); }
	public default Value or(Value a) { return next(value().or(a.value())); }
	public default Value shl(Value a) { return next(value().shl(a.value())); }
	public default Value shr(Value a) { return next(value().shr(a.value())); }
	
	public default Value eq(Value a) { return next(value().eq(a.value())); }
	public default Value neq(Value a) { return next(value().neq(a.value())); }
	public default Value gt(Value a) { return next(value().gt(a.value())); }
	public default Value gte(Value a) { return next(value().gte(a.value())); }
	public default Value lt(Value a) { return next(value().lt(a.value())); }
	public default Value lte(Value a) { return next(value().lte(a.value())); }
	

	public default Value cor(Value a) { return null; }
	public default Value cand(Value a) { return null; }
	
	public default Value neg() { return null; }
	public default Value not() { return null; } // keeps size and sign but removes floating status
	public default Value nor() { return null; }
	
	public static class IByte implements Value {
		private BigVal value;
		public IByte(long value) {
			this.value = BigVal.signed(1, value);
		}
		
		public boolean isFloating() { return false; }
		public boolean isSigned() { return true; }
		public BigVal value() { return value; }
		public String toString() { return value.toString(); }
	}
	
	public static class Word implements Value {
		private BigVal value;
		public Word(long value) {
			this.value = BigVal.signed(2, value);
		}

		public boolean isFloating() { return false; }
		public boolean isSigned() { return true; }
		public BigVal value() { return value; }
		public String toString() { return value.toString(); }
	}
	
	public static class Dword implements Value {
		private BigVal value;
		public Dword(long value) {
			this.value = BigVal.signed(4, value);
		}
		
		public boolean isFloating() { return false; }
		public boolean isSigned() { return true; }
		public BigVal value() { return value; }
		public String toString() { return value.toString(); }
	}
	
	public static class Qword implements Value {
		private BigVal value;
		public Qword(long value) {
			this.value = BigVal.signed(8, value);
		}

		public boolean isFloating() { return false; }
		public boolean isSigned() { return true; }
		public BigVal value() { return value; }
		public String toString() { return value.toString(); }
	}
	
	public static class UByte implements Value {
		private BigVal value;
		public UByte(long value) {
			this.value = BigVal.unsigned(1, value);
		}

		public boolean isFloating() { return false; }
		public boolean isSigned() { return false; }
		public BigVal value() { return value; }
		public String toString() { return value.toString(); }
	}
	
	public static class UWord implements Value {
		private BigVal value;
		public UWord(long value) {
			this.value = BigVal.unsigned(2, value);
		}

		public boolean isFloating() { return false; }
		public boolean isSigned() { return false; }
		public BigVal value() { return value; }
		public String toString() { return value.toString(); }
	}
	
	public static class UDword implements Value {
		private BigVal value;
		public UDword(long value) {
			this.value = BigVal.unsigned(4, value);
		}

		public boolean isFloating() { return false; }
		public boolean isSigned() { return false; }
		public BigVal value() { return value; }
		public String toString() { return value.toString(); }
	}
	
	public static class UQword implements Value {
		private BigVal value;
		public UQword(long value) {
			this.value = BigVal.unsigned(8, value);
		}

		public boolean isFloating() { return false; }
		public boolean isSigned() { return false; }
		public BigVal value() { return value; }
		public String toString() { return value.toString(); }
	}
	
	
	
	
	public static class IFloat implements Value {
		private BigVal value;
		public IFloat(double value) {
			this.value = BigVal.decimal(4, value);
		}

		public boolean isFloating() { return true; }
		public boolean isSigned() { return true; }
		public BigVal value() { return value; }
		public String toString() { return value.toString(); }
	}
	
	public static class IDouble implements Value {
		private BigVal value;
		public IDouble(double value) {
			this.value = BigVal.decimal(8, value);
		}

		public boolean isFloating() { return true; }
		public boolean isSigned() { return true; }
		public BigVal value() { return value; }
		public String toString() { return value.toString(); }
	}
	
	public static IByte sbyte(long value) { return new IByte(value); }
	public static Word word(long value) { return new Word(value); }
	public static Dword dword(long value) { return new Dword(value); }
	public static Qword qword(long value) { return new Qword(value); }
	public static UByte ubyte(long value) { return new UByte(value); }
	public static UWord uword(long value) { return new UWord(value); }
	public static UDword udword(long value) { return new UDword(value); }
	public static UQword uqword(long value) { return new UQword(value); }
	public static IFloat ifloat(double value) { return new IFloat(value); }
	public static IDouble idouble(double value) { return new IDouble(value); }
	
	static Value next(BigVal val) {
		if(val.isInteger()) {
			if(val.isSigned()) {
				switch(val.getSize()) {
					case 1: return sbyte(val.longValue());
					case 2: return word(val.longValue());
					case 4: return dword(val.longValue());
					case 8: return qword(val.longValue());
				}
			} else {
				switch(val.getSize()) {
					case 1: return ubyte(val.longValue());
					case 2: return uword(val.longValue());
					case 4: return udword(val.longValue());
					case 8: return uqword(val.longValue());
				}
			}
		} else {
			switch(val.getSize()) {
				case 4: return ifloat(val.doubleValue());
				case 8: return idouble(val.doubleValue());
			}
		}
		
		throw new UnsupportedOperationException("Failed to convert BigVal into a type.");
	}
	
	static Value next(Value a, Value b, long value) {
		return next(a.isSigned() && b.isSigned(), a.isFloating() || b.isFloating(), Math.max(a.size(), b.size()), value);
	}
	
	static Value next(boolean signed, int size, long value) { return next(signed, false, size, value); }
	static Value next(boolean signed, boolean floating, int size, long value) {
		if(floating) {
			switch(size) {
				case 4:	return ifloat(Double.longBitsToDouble(value));
				case 8:	return idouble(Double.longBitsToDouble(value));
			}
		} else {
			if(signed) {
				switch(size) {
					case 1: return sbyte(value);
					case 2: return word(value);
					case 4: return dword(value);
					case 8: return qword(value);
				}
			}
			
			switch(size) {
				case 1: return ubyte(value);
				case 2: return uword(value);
				case 4: return udword(value);
				case 8: return uqword(value);
			}
		}
		
		
		throw new UnsupportedOperationException("Invalid creation (" + signed + ", " + size + ", " + value + ")");
	}
	
	static class BigVal {
		private final BigInteger integer;
		private final BigDecimal decimal;
		private final boolean signed;
		private final int size;
		
		private BigVal(boolean signed, int size, BigInteger integer) {
			this.integer = integer;
			this.decimal = null;
			this.signed = signed;
			this.size = size;
		}
		
		private BigVal(int size, BigDecimal decimal) {
			this.decimal = decimal;
			this.integer = null;
			this.signed = true;
			this.size = size;
		}
		
		public boolean isInteger() { return integer != null; }
		public boolean isSigned() { return signed; }
		public int getSize() { return size; }
		
		public long longValue() {
			if(isInteger())
				return integer.longValue();
			return decimal.longValue();
		}
		
		public double doubleValue() {
			if(isInteger())
				return integer.doubleValue();
			return decimal.doubleValue();
		}
		
		public BigVal add(BigVal val) {
			int size = Math.max(this.size, val.size);
			if(!(isInteger() && val.isInteger())) {
				BigDecimal a = val.decimal == null ? new BigDecimal(val.integer):val.decimal;
				return decimal(size, a.add(decimal == null ? new BigDecimal(integer):decimal).doubleValue());
			}
			if(isSigned() && val.isSigned())
				return signed(size, integer.add(val.integer).longValue());
			return unsigned(size, integer.add(val.integer).longValue());
		}
		
		public BigVal sub(BigVal val) {
			int size = Math.max(this.size, val.size);
			if(!(isInteger() && val.isInteger())) {
				BigDecimal a = decimal == null ? new BigDecimal(integer):decimal;
				return decimal(size, a.subtract(val.decimal == null ? new BigDecimal(val.integer):val.decimal).doubleValue());
			}
			if(isSigned() && val.isSigned())
				return signed(size, integer.subtract(val.integer).longValue());
			return unsigned(size, integer.subtract(val.integer).longValue());
		}
		
		public BigVal mul(BigVal val) {
			int size = Math.max(this.size, val.size);
			if(!(isInteger() && val.isInteger())) {
				BigDecimal a = val.decimal == null ? new BigDecimal(val.integer):val.decimal;
				return decimal(size, a.multiply(decimal == null ? new BigDecimal(integer):decimal).doubleValue());
			}
			if(isSigned() && val.isSigned())
				return signed(size, integer.multiply(val.integer).longValue());
			return unsigned(size, integer.multiply(val.integer).longValue());
		}
		
		public BigVal div(BigVal val) {
			int size = Math.max(this.size, val.size);
			if(!(isInteger() && val.isInteger())) {
				BigDecimal a = decimal == null ? new BigDecimal(integer):decimal;
				return decimal(size, a.divide(val.decimal == null ? new BigDecimal(val.integer):val.decimal).doubleValue());
			}
			if(isSigned() && val.isSigned())
				return signed(size, integer.divide(val.integer).longValue());
			return unsigned(size, integer.divide(val.integer).longValue());
		}
		
		public BigVal mod(BigVal val) {
			int size = Math.max(this.size, val.size);
			if(!(isInteger() && val.isInteger())) {
				BigDecimal a = decimal == null ? new BigDecimal(integer):decimal;
				return decimal(size, a.remainder(val.decimal == null ? new BigDecimal(val.integer):val.decimal).doubleValue());
			}
			if(isSigned() && val.isSigned())
				return signed(size, integer.remainder(val.integer).longValue());
			return unsigned(size, integer.remainder(val.integer).longValue());
		}
		
		public BigVal xor(BigVal val) {
			if(!(isInteger() && val.isInteger())) throw new UnsupportedOperationException("Cannot perform 'xor' with float values");
			int size = Math.max(this.size, val.size);
			if(isSigned() && val.isSigned())
				return signed(size, integer.remainder(val.integer).longValue());
			return unsigned(size, integer.remainder(val.integer).longValue());
		}
		
		public BigVal shr(BigVal val) {
			if(!(isInteger() && val.isInteger())) throw new UnsupportedOperationException("Cannot perform 'shr' with float values");
			int size = Math.max(this.size, val.size);
			if(isSigned() && val.isSigned())
				return signed(size, integer.shiftRight(val.integer.intValue()).longValue());
			return unsigned(size, integer.shiftRight(val.integer.intValue()).longValue());
		}
		
		public BigVal shl(BigVal val) {
			if(!(isInteger() && val.isInteger())) throw new UnsupportedOperationException("Cannot perform 'shl' with float values");
			int size = Math.max(this.size, val.size);
			if(isSigned() && val.isSigned())
				return signed(size, integer.shiftLeft(val.integer.intValue()).longValue());
			return unsigned(size, integer.shiftLeft(val.integer.intValue()).longValue());
		}
		
		public BigVal and(BigVal val) {
			if(!(isInteger() && val.isInteger())) throw new UnsupportedOperationException("Cannot perform 'and' with float values");
			int size = Math.max(this.size, val.size);
			if(isSigned() && val.isSigned())
				return signed(size, integer.and(val.integer).longValue());
			return unsigned(size, integer.and(val.integer).longValue());
		}
		
		public BigVal or(BigVal val) {
			if(!(isInteger() && val.isInteger())) throw new UnsupportedOperationException("Cannot perform 'or' with float values");
			int size = Math.max(this.size, val.size);
			if(isSigned() && val.isSigned())
				return signed(size, integer.or(val.integer).longValue());
			return unsigned(size, integer.or(val.integer).longValue());
		}
		
		public BigVal eq(BigVal val) {
			throw new UnsupportedOperationException();
		}
		public BigVal neq(BigVal val) {
			throw new UnsupportedOperationException();
		}
		public BigVal gt(BigVal val) {
			throw new UnsupportedOperationException();
		}
		public BigVal gte(BigVal val) {
			throw new UnsupportedOperationException();
		}
		public BigVal lt(BigVal val) {
			throw new UnsupportedOperationException();
		}
		public BigVal lte(BigVal val) {
			throw new UnsupportedOperationException();
		}
		
		public String toString() {
			if(isInteger())
				return integer.toString();
			
			return Double.toString(decimal.doubleValue());
		}
		
		static BigVal unsigned(int size, long value) {
			long mask = (1L << (size * 8L)) - 1L;
			if(size == 8) mask = -1;
			String str = Long.toUnsignedString(value & mask);
			// System.out.printf("us, %d, %016x : %016x : %016x\n", size, mask, value, Long.valueOf(str, 16));
			return new BigVal(false, size, new BigInteger(str));
		}
		
		static BigVal signed(int size, long value) {
			long mask = (1L << (size * 8L)) - 1L;
			if(size == 8) mask = -1;
			long valu = value & mask;
			// System.out.printf("sg, %d, %016x : %016x : %016x\n", size, mask, value, valu);
			
			if(size == 1) return new BigVal(true, 1, new BigInteger(Byte.toString((byte)valu)));
			if(size == 2) return new BigVal(true, 2, new BigInteger(Short.toString((short)valu)));
			if(size == 4) return new BigVal(true, 4, new BigInteger(Integer.toString((int)valu)));
			if(size == 8) return new BigVal(true, 8, new BigInteger(Long.toString((long)valu)));
			
			throw new UnsupportedOperationException("Invalid size '" + size + "'");
		}
		
		static BigVal decimal(int size, double value) {
			if(size == 4)
				return new BigVal(size, new BigDecimal((float)value));
			
			if(size == 8)
				return new BigVal(size, new BigDecimal(value));
			
			throw new UnsupportedOperationException("Invalid size '" + size + "'");
		}
	}
	
	public static void main(String[] args) {
		Value val = ubyte(0x123).mul(sbyte(-2));
		
		System.out.println(val + ", " + val.mul(qword(3)).add(sbyte(3200)));
	}
}
