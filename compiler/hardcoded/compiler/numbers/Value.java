package hardcoded.compiler.numbers;

import static hardcoded.compiler.constants.Atom.*;

import java.math.BigDecimal;
import java.util.Random;

import hardcoded.compiler.constants.Atom;

/**
 * This is a number utility class for <b>signed</b>, <b>unsigned</b> and <b>floating</b> operations.
 * 
 * @author HardCoded
 */
public class Value {
	
	/**
	 * Container for the number value.
	 */
	private BigNum value;
	
	/**
	 * The type of this value.
	 */
	private Atom type;
	
	
	
	private Value(long value, Atom type) {
		this.value = new BigNum(type, value);
		this.type = type;
	}
	
	private Value(double value, Atom type) {
		this.value = new BigNum(type, value);
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
		return value.decimal;
	}
	
	/**
	 * Returns the long value of this number.
	 * @return the long value of this number
	 */
	public long longValue() {
		return value.integer;
	}
	
	@Override
	public String toString() {
		return value.toString();
	}
	
	
	public Value add(Value a) { return next(value.add(a.value)); }
	public Value sub(Value a) { return next(value.sub(a.value)); }
	public Value div(Value a) { return next(value.div(a.value)); }
	public Value mul(Value a) { return next(value.mul(a.value)); }
	public Value mod(Value a) { return next(value.mod(a.value)); }
	public Value and(Value a) { return next(value.and(a.value)); }
	public Value xor(Value a) { return next(value.xor(a.value)); }
	public Value or(Value a)  { return next(value.or(a.value)); }
	public Value shl(Value a) { return next(value.shl(a.value)); }
	public Value shr(Value a) { return next(value.shr(a.value)); }
	public Value eq(Value a)  { return next(value.eq(a.value)); }
	public Value neq(Value a) { return next(value.neq(a.value)); }
	public Value gt(Value a)  { return next(value.gt(a.value)); }
	public Value gte(Value a) { return next(value.gte(a.value)); }
	public Value lt(Value a)  { return next(value.lt(a.value)); }
	public Value lte(Value a) { return next(value.lte(a.value)); }
	public Value cor(Value a) { return next(value.cor(a.value)); }
	public Value cand(Value a) { return next(value.cand(a.value)); }
	public Value neg() { return next(value.neg()); }
	public Value not() { return next(value.not()); }
	public Value nor() { return next(value.nor()); }
	public Value convert(Atom type) { return next(value.convert(type)); }
	
	public static Value sbyte(long value) { return new Value(value, i8); }
	public static Value word(long value) { return new Value(value, i16); }
	public static Value dword(long value) { return new Value(value, i32); }
	public static Value qword(long value) { return new Value(value, i64); }
	public static Value ubyte(long value) { return new Value(value, u8); }
	public static Value uword(long value) { return new Value(value, u16); }
	public static Value udword(long value) { return new Value(value, u32); }
	public static Value uqword(long value) { return new Value(value, u64); }
	public static Value ifloat(double value) { return new Value(value, f32); }
	public static Value idouble(double value) { return new Value(value, f64); }
	
	Value next(BigNum val) {
		return val.type.isFloating() ?
			new Value(val.decimal, val.type):
			new Value(val.integer, val.type);
	}
	
	static class BigNum {
		private final double decimal;
		private final long integer;
		private final Atom type;
		
		BigNum(Atom type, long value) {
			this.type = type;
			
			if(type.isFloating()) {
				switch(type.size()) {
					case 8: decimal = value; break;
					case 4: decimal = (float)value; break;
					default: throw new ArithmeticException();
				}
				
				integer = value;
			} else {
				if(type.isSigned()) {
					switch(type.size()) {
						case 8: break;
						case 4: value = (int)value; break;
						case 2: value = (short)value; break;
						case 1: value = (byte)value; break;
						default: throw new ArithmeticException();
					}
					
					decimal = value;
				} else {
					value &= (-1L) >>> (8L * (8 - type.size()));
					
					if(value < 0) {
						decimal = 1.0 + 2.0 * ((double)(value >>> 1L));
					} else {
						decimal = value;
					}
				}

				integer = value;
			}
		}
		
		BigNum(Atom type, double value) {
			if(!type.isFloating()) throw new ArithmeticException(type + " does not support floating point values");
			this.type = type;
			
			switch(type.size()) {
				case  4: decimal = (float)value; break;
				default: decimal = value;
			}
			
			integer = (long)value;
		}
		
		BigNum(BigNum a, BigNum b, double value) {
			this(Atom.largest(a.type, b.type), value);
		}
		
		BigNum(BigNum a, BigNum b, long value) {
			this(Atom.largest(a.type, b.type), value);
		}
		
		BigNum add(BigNum val) {
			if(type.isFloating() || val.type.isFloating()) {
				return new BigNum(this, val, decimal + val.decimal);
			}

//			TODO: Check if not checking for unsigned or signed causes any errors.
//			if(type.isSigned() && val.type.isSigned()) {
//				return new BigNum(this, val, integer + val.integer);
//			}
//			
//			long v0 =     integer & ((-1L) >>> (8L * (8 -     type.size())));
//			long v1 = val.integer & ((-1L) >>> (8L * (8 - val.type.size())));
//			return new BigNum(this, val, v0 + v1);
			
			return new BigNum(this, val, integer + val.integer);
		}
		
		BigNum sub(BigNum val) {
			if(type.isFloating() || val.type.isFloating()) {
				return new BigNum(this, val, decimal - val.decimal);
			}

//			TODO: Check if not checking for unsigned or signed causes any errors.
//			if(type.isSigned() && val.type.isSigned()) {
//				return new BigNum(this, val, integer - val.integer);
//			}
//			
//			long v0 =     integer & ((-1L) >>> (8L * (8 -     type.size())));
//			long v1 = val.integer & ((-1L) >>> (8L * (8 - val.type.size())));
//			return new BigNum(this, val, v0 - v1);

			return new BigNum(this, val, integer - val.integer);
		}
		
		BigNum div(BigNum val) {
			if(type.isFloating() || val.type.isFloating()) {
				return new BigNum(this, val, decimal / val.decimal);
			}
			
			if(type.isSigned() && val.type.isSigned()) {
				return new BigNum(this, val, integer / val.integer);
			}
			
			// NOTE: Values should already be bit checked so this is not needed.
//			long v0 =     integer & ((-1L) >>> (8L * (8 -     type.size())));
//			long v1 = val.integer & ((-1L) >>> (8L * (8 - val.type.size())));
			
			// TODO: Rewrite this to use a faster algorithm.
			//     : The internal code is using BigInteger to solve
			//     : some cases and that can become slow if we need
			//     : speed.
			return new BigNum(this, val, Long.divideUnsigned(integer, val.integer));
		}
		
		BigNum mul(BigNum val) {
			if(type.isFloating() || val.type.isFloating()) {
				return new BigNum(this, val, decimal * val.decimal);
			}
			
//			TODO: Check if not checking for unsigned or signed causes any errors.
//			if(type.isSigned() && val.type.isSigned()) {
//				return new BigNum(this, val, integer * val.integer);
//			}
//			
//			long v0 =     integer & ((-1L) >>> (8L * (8 -     type.size())));
//			long v1 = val.integer & ((-1L) >>> (8L * (8 - val.type.size())));
//			return new BigNum(this, val, v0 * v1);
			return new BigNum(this, val, integer * val.integer);
		}
		
		BigNum mod(BigNum val) {
			if(type.isFloating() || val.type.isFloating()) {
				return new BigNum(this, val, decimal % val.decimal);
			}
			
			if(type.isSigned() && val.type.isSigned()) {
				return new BigNum(this, val, integer % val.integer);
			}
			
			// NOTE: Values should already be bit checked so this is not needed.
//			long v0 =     integer & ((-1L) >>> (8L * (8 -     type.size())));
//			long v1 = val.integer & ((-1L) >>> (8L * (8 - val.type.size())));
			
			// TODO: Rewrite this to use a faster algorithm.
			//     : The internal code is using BigInteger to solve
			//     : some cases and that can become slow if we need
			//     : speed.
			return new BigNum(this, val, Long.remainderUnsigned(integer, val.integer));
		}
		
		
		// 
		// Invalid for floating numbers
		// 
		
		BigNum xor(BigNum val) {
			if(type.isFloating() || val.type.isFloating())
				throw new ArithmeticException("'xor' is invalid for floating point values");
			
//			TODO: Check if not checking for unsigned or signed causes any errors.
//			if(type.isSigned() && val.type.isSigned()) {
//				return new BigNum(this, val, integer ^ val.integer);
//			}
//			long v0 =     integer & ((-1L) >>> (8L * (8 -     type.size())));
//			long v1 = val.integer & ((-1L) >>> (8L * (8 - val.type.size())));
//			return new BigNum(this, val, v0 ^ v1);
			return new BigNum(this, val, integer ^ val.integer);
		}
		
		BigNum and(BigNum val) {
			if(type.isFloating() || val.type.isFloating())
				throw new ArithmeticException("'and' is invalid for floating point values");
			
//			TODO: Check if not checking for unsigned or signed causes any errors.
//			if(type.isSigned() && val.type.isSigned()) {
//				return new BigNum(this, val, integer & val.integer);
//			}
//			long v0 =     integer & ((-1L) >>> (8L * (8 -     type.size())));
//			long v1 = val.integer & ((-1L) >>> (8L * (8 - val.type.size())));
//			return new BigNum(this, val, v0 & v1);
			return new BigNum(this, val, integer & val.integer);
		}
		
		BigNum or(BigNum val) {
			if(type.isFloating() || val.type.isFloating())
				throw new ArithmeticException("'or' is invalid for floating point values");
			
//			TODO: Check if not checking for unsigned or signed causes any errors.
//			if(type.isSigned() && val.type.isSigned()) {
//				return new BigNum(this, val, integer | val.integer);
//			}
//			long v0 =     integer & ((-1L) >>> (8L * (8 -     type.size())));
//			long v1 = val.integer & ((-1L) >>> (8L * (8 - val.type.size())));
//			return new BigNum(this, val, v0 | v1);
			return new BigNum(this, val, integer | val.integer);
		}
		
		BigNum shr(BigNum val) {
			if(type.isFloating() || val.type.isFloating())
				throw new ArithmeticException("'shr' is invalid for floating point values");
			
			if(type.isSigned() && val.type.isSigned()) {
				return new BigNum(this, val, integer >> val.integer);
			}

			// NOTE: Values should already be bit checked so this is not needed.
//			long v0 =     integer & ((-1L) >>> (8L * (8 -     type.size())));
//			long v1 = val.integer & ((-1L) >>> (8L * (8 - val.type.size())));
//			return new BigNum(this, val, v0 >>> v1);
			return new BigNum(this, val, integer >>> val.integer);
		}
		
		BigNum shl(BigNum val) {
			if(type.isFloating() || val.type.isFloating())
				throw new ArithmeticException("'shl' is invalid for floating point values");
			
			if(type.isSigned() && val.type.isSigned()) {
				return new BigNum(this, val, integer << val.integer);
			}
			

			// NOTE: Values should already be bit checked so this is not needed.
//			long v0 =     integer & ((-1L) >>> (8L * (8 -     type.size())));
//			long v1 = val.integer & ((-1L) >>> (8L * (8 - val.type.size())));
//			return new BigNum(this, val, v0 << v1);
			if(integer == 0 || val.integer > 0) return new BigNum(this, val, 0);
			return new BigNum(this, val, integer << val.integer);
		}
		
		BigNum not() {
			if(type.isFloating()) return new BigNum(i32, decimal == 0 ? 1:0);
			return new BigNum(i32, integer == 0 ? 1:0);
		}
		
		BigNum nor() {
			if(type.isFloating())
				throw new ArithmeticException("'nor' is invalid for floating point values");
			return new BigNum(this, this, ~integer);
		}
		
		BigNum neg() {
			if(!type.isSigned()) throw new ArithmeticException("'neg' is invalid for unsigned values");
			if(type.isFloating()) return new BigNum(this, this, -decimal);
			return new BigNum(this, this, -integer);
		}
		
		BigNum cor(BigNum val)  {
			boolean a = (type.isFloating() ? (decimal != 0):(integer != 0));
			boolean b = (val.type.isFloating() ? (val.decimal != 0):(val.integer != 0));
			return new BigNum(i32, (a || b) ? 1:0);
		}
		
		BigNum cand(BigNum val)  {
			boolean a = (type.isFloating() ? (decimal != 0):(integer != 0));
			boolean b = (val.type.isFloating() ? (val.decimal != 0):(val.integer != 0));
			return new BigNum(i32, (a && b) ? 1:0);
		}
		
		BigNum eq(BigNum val)  {
			// TODO: Could cause problems if decimal and integer is not written together
			if(type.isFloating() || val.type.isFloating())
				return new BigNum(i32, decimal == val.decimal ? 1:0);
			return new BigNum(i32, integer == val.integer ? 1:0);
		}
		
		BigNum neq(BigNum val)  {
			// TODO: Could cause problems if decimal and integer is not written together
			if(type.isFloating() || val.type.isFloating())
				return new BigNum(i32, decimal != val.decimal ? 1:0);
			return new BigNum(i32, integer != val.integer ? 1:0);
		}
		
		BigNum gt(BigNum val)  {
			// TODO: Could cause problems if decimal and integer is not written together
			if(type.isFloating() || val.type.isFloating())
				return new BigNum(i32, decimal > val.decimal ? 1:0);
			return new BigNum(i32, integer > val.integer ? 1:0);
		}
		
		BigNum gte(BigNum val)  {
			// TODO: Could cause problems if decimal and integer is not written together
			if(type.isFloating() || val.type.isFloating())
				return new BigNum(i32, decimal >= val.decimal ? 1:0);
			return new BigNum(i32, integer >= val.integer ? 1:0);
		}
		
		BigNum lt(BigNum val)  {
			// TODO: Could cause problems if decimal and integer is not written together
			if(type.isFloating() || val.type.isFloating())
				return new BigNum(i32, decimal < val.decimal ? 1:0);
			return new BigNum(i32, integer < val.integer ? 1:0);
		}
		
		BigNum lte(BigNum val)  {
			// TODO: Could cause problems if decimal and integer is not written together
			if(type.isFloating() || val.type.isFloating())
				return new BigNum(i32, decimal <= val.decimal ? 1:0);
			return new BigNum(i32, integer <= val.integer ? 1:0);
		}
		
		BigNum convert(Atom atom) {
			if(atom.isFloating())
				return new BigNum(atom, decimal);
			return new BigNum(atom, integer);
		}
		
		public String toString() {
			if(type.isFloating()) {
				if(type.size() == 4) return Float.toString((float)decimal);
				return Double.toString(decimal);
			}
			
			if(type.isSigned()) return Long.toString(integer);
			return Long.toUnsignedString(integer);
		}
	}
	
	
	public static void main(String[] args) throws Exception {
		Random random = new Random();
		
		while(true) {
			System.out.println("Outer");
			long[] array = new long[2000000];
			for(int i = 0; i < array.length; i++) {
				array[i] = random.nextLong();
			}
			
			long tick = System.currentTimeMillis();
			for(int i = 0; i < 1000000; i++) {
				long a = array[i];
				long b = array[i + 1000000];
				
				Value _a = uqword(a);
				Value _b = uqword(b);
				
				long d_java = (a * b * b * b * b * b) + a + a + a - 0xffffffffffffffL;
				long d_bnum = _a.mul(_b).mul(_b).mul(_b).mul(_b).mul(_b).add(_a).add(_a).add(_a).sub(qword(0xffffffffffffffL)).longValue();
				
				double error = d_java - d_bnum;
				if(error != 0) {
					System.out.println("Error: " + error);
					System.out.println(BigDecimal.valueOf(d_java));
					System.out.println(BigDecimal.valueOf(d_bnum));
				}
			}
			long time = (System.currentTimeMillis() - tick);
			System.out.printf("Took %d ms to run (1000000 iterations), %.5f ms\n", time, (time / 1000000.0D));
		}
// 		double a = (100000D / 16000D) * 3.231235123D;
		
//		System.out.println(val + ", " + val.mul(qword(3)).add(sbyte(3200)));
		
		// BigNum num = new BigNum(Atom.f64, -3230389471289374918237489123748912734892D);
		// System.out.println(num);
		
		// System.out.println(new BigDecimal(a));
		// System.out.println(new BigDecimal(idouble(100000).div(idouble(16000)).mul(idouble(3.231235123D)).doubleValue()));
	}
}
