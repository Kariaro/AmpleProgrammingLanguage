package hardcoded.compiler.numbers;

import java.math.*;

import hardcoded.compiler.constants.Atom;
import static hardcoded.compiler.constants.Atom.*;

/**
 * This is a number utility class for <b>signed</b>, <b>unsigned</b> and <b>floating</b> operations.
 * 
 * @author HardCoded
 */
public class ValueOld {
	private static final MathContext CONTEXT = new MathContext(20, RoundingMode.HALF_EVEN);
	
	/**
	 * Container for the number value.
	 */
	private BigVal value;
	
	/**
	 * The type of this value.
	 */
	private Atom type;
	
	private ValueOld(long value, Atom type) {
		this.value = new BigVal(type, new BigDecimal(value));
		this.type = type;
	}
	
	private ValueOld(double value, Atom type) {
		this.value = new BigVal(type, new BigDecimal(value));
		this.type = type;
	}
	
	
	/**
	 * Returns {@code true} if this value is signed.
	 * @return {@code true} if this value is signed
	 */
	public boolean isSigned() {
		return type.isSigned();
	}
	
	/**
	 * Returns {@code true} if this value is a floating point number.
	 * @return {@code true} if this value is a floating point number
	 */
	public boolean isFloating() {
		return type.isFloating();
	}
	
	/**
	 * Returns the amount of bytes this number takes.
	 * @return the amount of bytes this number takes
	 */
	public int size() {
		return type.size();
	}
	
	/**
	 * Returns the value that this number holds.
	 * @return the value that this number holds
	 */
	public BigVal value() {
		return value;
	}
	
	/**
	 * Returns the atom type of this number.
	 * @return the atom type of this number
	 */
	public Atom type() {
		return type;
	}
	
	/**
	 * Returns the double value of this number.
	 * @return the double value of this number
	 */
	public double doubleValue() {
		return value.doubleValue();
	}
	
	/**
	 * Returns the long value of this number.
	 * @return the long value of this number
	 */
	public long longValue() {
		return longValue();
	}
	
	@Override
	public String toString() {
		return value.toString();
	}
	
	
	public ValueOld add(ValueOld a) { return next(value().add(a.value())); }
	public ValueOld sub(ValueOld a) { return next(value().sub(a.value())); }
	public ValueOld div(ValueOld a) { return next(value().div(a.value())); }
	public ValueOld mul(ValueOld a) { return next(value().mul(a.value())); }
	public ValueOld mod(ValueOld a) { return next(value().mod(a.value())); }
	public ValueOld and(ValueOld a) { return next(value().and(a.value())); }
	public ValueOld xor(ValueOld a) { return next(value().xor(a.value())); }
	public ValueOld or(ValueOld a) { return next(value().or(a.value())); }
	public ValueOld shl(ValueOld a) { return next(value().shl(a.value())); }
	public ValueOld shr(ValueOld a) { return next(value().shr(a.value())); }
	public ValueOld eq(ValueOld a)  { return next(value().eq(a.value())); }
	public ValueOld neq(ValueOld a) { return next(value().neq(a.value())); }
	public ValueOld gt(ValueOld a)  { return next(value().gt(a.value())); }
	public ValueOld gte(ValueOld a) { return next(value().gte(a.value())); }
	public ValueOld lt(ValueOld a)  { return next(value().lt(a.value())); }
	public ValueOld lte(ValueOld a) { return next(value().lte(a.value())); }
	public ValueOld cor(ValueOld a) { return next(value().cor(a.value())); }
	public ValueOld cand(ValueOld a) { return next(value().cand(a.value())); }
	public ValueOld neg() { return next(value.neg()); }
	public ValueOld not() { return next(value.not()); }
	public ValueOld nor() { return next(value.nor()); }
	
	public static ValueOld sbyte(long value) { return new ValueOld(value, i8); }
	public static ValueOld word(long value) { return new ValueOld(value, i16); }
	public static ValueOld dword(long value) { return new ValueOld(value, i32); }
	public static ValueOld qword(long value) { return new ValueOld(value, i64); }
	public static ValueOld ubyte(long value) { return new ValueOld(value, u8); }
	public static ValueOld uword(long value) { return new ValueOld(value, u16); }
	public static ValueOld udword(long value) { return new ValueOld(value, u32); }
	public static ValueOld uqword(long value) { return new ValueOld(value, u64); }
	public static ValueOld ifloat(double value) { return new ValueOld(value, f32); }
	public static ValueOld idouble(double value) { return new ValueOld(value, f64); }
	
	ValueOld next(BigVal val) {
//		type = val.type;
//		value = val;
//		return this;
		return isFloating() ?
			new ValueOld(val.doubleValue(), val.type):
			new ValueOld(val.longValue(), val.type);
	}
	
	static class BigVal {
		private final BigDecimal decimal;
		private final Atom type;
		
		BigVal(BigVal a, BigVal b, BigInteger value) {
			this(Atom.largest(a.type, b.type), new BigDecimal(value));
		}
		
		BigVal(BigVal a, BigVal b, BigDecimal value) {
			this(Atom.largest(a.type, b.type), value);
		}
		
		BigVal(Atom atom, BigDecimal decimal) {
			this.type = atom;
			
			if(atom.isFloating()) {
				double value = decimal.doubleValue();
				
				switch(atom.size()) {
					case 4: this.decimal = new BigDecimal((float)value); break;
					default: this.decimal = new BigDecimal(value);
				}
			} else {
				long value = decimal.longValue() & ((-1L) >>> (8L * (8 - atom.size())));
				if(atom.isSigned()) {
					switch(atom.size()) {
						case 4: value = (int)value; break;
						case 2: value = (short)value; break;
						case 1: value = (byte)value; break;
					}
					
					this.decimal = new BigDecimal(BigInteger.valueOf(value));
				} else {
					if(value < 0) {
						this.decimal = new BigDecimal(Long.toUnsignedString(value));
					} else {
						this.decimal = new BigDecimal(value);
					}
				}
			}
		}
		
		long longValue() { return decimal.longValue(); }
		double doubleValue() { return decimal.doubleValue(); }
		
		BigVal add(BigVal val) { return new BigVal(this, val, decimal.add(val.decimal, CONTEXT)); }
		BigVal sub(BigVal val) { return new BigVal(this, val, decimal.subtract(val.decimal, CONTEXT)); }
		BigVal div(BigVal val) { return new BigVal(this, val, decimal.divide(val.decimal, CONTEXT)); }
		BigVal mul(BigVal val) { return new BigVal(this, val, decimal.multiply(val.decimal, CONTEXT)); }
		BigVal mod(BigVal val) { return new BigVal(this, val, decimal.remainder(val.decimal, CONTEXT)); }
		
		// Invalid for floating numbers
		BigVal xor(BigVal val) { return new BigVal(this, val, decimal.toBigIntegerExact().xor(val.decimal.toBigIntegerExact())); }
		BigVal and(BigVal val) { return new BigVal(this, val, decimal.toBigIntegerExact().and(val.decimal.toBigIntegerExact())); }
		BigVal or (BigVal val) { return new BigVal(this, val, decimal.toBigIntegerExact().or(val.decimal.toBigIntegerExact())); }
		BigVal shr(BigVal val) { return new BigVal(this, val, decimal.toBigIntegerExact().shiftRight(val.decimal.intValueExact())); }
		BigVal shl(BigVal val) { return new BigVal(this, val, decimal.toBigIntegerExact().shiftLeft(val.decimal.intValueExact())); }
		BigVal not() { return new BigVal(i32, BigDecimal.valueOf(((decimal.toBigIntegerExact().signum() + 1) & 1))); }
		BigVal nor() { return new BigVal(this, this, decimal.toBigIntegerExact().not()); }
		BigVal neg() {
			if(!type.isSigned()) throw new ArithmeticException("You cannot negate a unsigned number");
			return new BigVal(this, this, decimal.toBigIntegerExact().negate());
		}

		BigVal cor(BigVal val)  { return new BigVal(i32, (decimal.signum() != 0 || val.decimal.signum() != 0) ? BigDecimal.ONE:BigDecimal.ZERO); }
		BigVal cand(BigVal val) { return new BigVal(i32, (decimal.signum() != 0 && val.decimal.signum() != 0) ? BigDecimal.ONE:BigDecimal.ZERO); }
		BigVal eq(BigVal val)  { return new BigVal(i32, decimal.compareTo(val.decimal) == 0 ? BigDecimal.ONE:BigDecimal.ZERO); }
		BigVal neq(BigVal val) { return new BigVal(i32, decimal.compareTo(val.decimal) != 0 ? BigDecimal.ONE:BigDecimal.ZERO); }
		BigVal gt(BigVal val)  { return new BigVal(i32, decimal.compareTo(val.decimal) == 1 ? BigDecimal.ONE:BigDecimal.ZERO); }
		BigVal gte(BigVal val) { return new BigVal(i32, decimal.compareTo(val.decimal) >= 0 ? BigDecimal.ONE:BigDecimal.ZERO); }
		BigVal lt(BigVal val)  { return new BigVal(i32, decimal.compareTo(val.decimal) ==-1 ? BigDecimal.ONE:BigDecimal.ZERO); }
		BigVal lte(BigVal val) { return new BigVal(i32, decimal.compareTo(val.decimal) <= 0 ? BigDecimal.ONE:BigDecimal.ZERO); }
		
		public String toString() {
			if(type.isFloating()) {
				if(type.size() == 4) return Float.toString((float)doubleValue());
				return Double.toString(doubleValue());
			}
			
			if(type.isSigned()) return Long.toString(longValue());
			return Long.toUnsignedString(longValue());
		}
	}
	
	public static void main(String[] args) throws Exception {
		ValueOld val = qword(1000).div(qword(29));
		
		
		{
			double a = 1000000000000000000000000D;
			double b = 1249137812734871237461324D;
			double c = Math.PI;
			
			double d_java = (a / b) * a * c * c * c * c * c * c * c * c * c * c * c * c * c * c * c * c;
			double d_bnum = idouble(a).div(idouble(b)).mul(idouble(a)).mul(idouble(c)).mul(idouble(c)).mul(idouble(c)).mul(idouble(c)).mul(idouble(c)).mul(idouble(c)).mul(idouble(c)).mul(idouble(c)).mul(idouble(c)).mul(idouble(c)).mul(idouble(c)).mul(idouble(c)).mul(idouble(c)).mul(idouble(c)).mul(idouble(c)).mul(idouble(c)).doubleValue();
			
			double error = d_java - d_bnum;

			System.out.println(new BigDecimal(d_java));
			System.out.println(new BigDecimal(d_bnum));
			System.out.println(d_java);
			System.out.println("Error: " + new BigDecimal(error));
		}
//		for(double a = 889993; a < 1000000; a++) {
//			System.out.println("Outer");
//			long tick = System.currentTimeMillis();
//			for(double b = 1; b < 1000000; b++) {
//				// System.out.println("Inner " + b);
//				double d_java = (a / b) * a;
//				double d_bnum = idouble(a).div(idouble(b)).mul(idouble(a)).doubleValue();
//				
//				double error = d_java - d_bnum;
//				if(error != 0)
//					System.out.println(error);
//				
//				if(error < -0.00001 || error > 0.00001) {
////					System.out.println(a + ", " + b);
////					System.out.println(BigDecimal.valueOf(d_java));
////					System.out.println(BigDecimal.valueOf(d_bnum));
//				}
//			}
//			long time = (System.currentTimeMillis() - tick);
//			System.out.printf("Took %d ms to run (1000000 iterations), %.5f ms\n", time, (time / 1000000.0D));
//		}
		double a = (100000D / 16000D) * 3.231235123D;
		
		System.out.println(val + ", " + val.mul(qword(3)).add(sbyte(3200)));
		
		System.out.println(new BigDecimal(a));
		System.out.println(new BigDecimal(idouble(100000).div(idouble(16000)).mul(idouble(3.231235123D)).doubleValue()));
	}
}
