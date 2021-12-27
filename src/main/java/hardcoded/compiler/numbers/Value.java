package hardcoded.compiler.numbers;

import static hardcoded.compiler.constants.Atom.*;

import hardcoded.compiler.constants.Atom;
import hardcoded.compiler.expression.LowType;

/**
 * This is a number utility class for <b>signed</b>, <b>unsigned</b> and <b>floating</b> operations.
 * 
 * @author HardCoded
 */
public class Value {
	private final double decimal;
	private final long integer;
	
	/**
	 * The type of this value.
	 */
	private Atom type;
	
	
	private Value(Value a, Value b, double value) {
		this(Atom.largest(a.type, b.type), value);
	}
	
	private Value(Value a, Value b, long value) {
		this(Atom.largest(a.type, b.type), value);
	}
	
	private Value(Atom type, long value) {
		this.type = type;
		
		if(isFloating()) {
			switch(size()) {
				case 8: decimal = value; break;
				case 4: decimal = (float)value; break;
				default: throw new ArithmeticException();
			}
			
			integer = value;
		} else {
			if(isSigned()) {
				switch(size()) {
					case 8: break;
					case 4: value = (int)value; break;
					case 2: value = (short)value; break;
					case 1: value = (byte)value; break;
					default: throw new ArithmeticException();
				}
				
				decimal = value;
			} else {
				value &= (-1L) >>> (8L * (8 - size()));
				
				if(value < 0) {
					decimal = 1.0 + 2.0 * ((double)(value >>> 1L));
				} else {
					decimal = value;
				}
			}

			integer = value;
		}
	}
	
	private Value(Atom type, double value) {
		if(!isFloating()) throw new ArithmeticException(type + " does not support floating point values");
		this.type = type;
		
		switch(size()) {
			case 4 -> {
				decimal = (float)value;
			}
			
			default -> {
				decimal = value;
			}
		}
		
		integer = (long)value;
	}
	
	/**
	 * Returns {@code true} if this value is signed.
	 */
	public boolean isSigned() {
		return type.isSigned();
	}
	
	/**
	 * Returns {@code true} if this value is a floating point number.
	 */
	public boolean isFloating() {
		return type.isFloating();
	}
	
	/**
	 * Returns the amount of bytes this number takes.
	 */
	public int size() {
		return type.size();
	}
	
	/**
	 * Returns the atom type of this number.
	 */
	public Atom type() {
		return type;
	}
	
	/**
	 * Returns the double value of this number.
	 */
	public double doubleValue() {
		return decimal;
	}
	
	/**
	 * Returns the long value of this number.
	 */
	public long longValue() {
		return integer;
	}
	
	
	public static Value sbyte(long value) { return new Value(i8, value); }
	public static Value word(long value) { return new Value(i16, value); }
	public static Value dword(long value) { return new Value(i32, value); }
	public static Value qword(long value) { return new Value(i64, value); }
	public static Value ubyte(long value) { return new Value(u8, value); }
	public static Value uword(long value) { return new Value(u16, value); }
	public static Value udword(long value) { return new Value(u32, value); }
	public static Value uqword(long value) { return new Value(u64, value); }
	public static Value ifloat(double value) { return new Value(f32, value); }
	public static Value idouble(double value) { return new Value(f64, value); }
	public static Value get(long value, Atom type) { return new Value(type, value); }
	public static Value get(double value, Atom type) { return new Value(type, value); }
	
	public Value add(Value val) {
		if(isFloating() || val.isFloating()) {
			return new Value(this, val, decimal + val.decimal);
		}
		
		return new Value(this, val, integer + val.integer);
	}
	
	public Value sub(Value val) {
		if(isFloating() || val.isFloating()) {
			return new Value(this, val, decimal - val.decimal);
		}
		
		return new Value(this, val, integer - val.integer);
	}
	
	public Value div(Value val) {
		if(isFloating() || val.isFloating()) {
			return new Value(this, val, decimal / val.decimal);
		}
		
		if(isSigned() && val.isSigned()) {
			return new Value(this, val, integer / val.integer);
		}
		
//		NOTE: Values should already be bit checked so this is not needed.
//		long v0 =     integer & ((-1L) >>> (8L * (8 -     size())));
//		long v1 = val.integer & ((-1L) >>> (8L * (8 - val.size())));
		
		// TODO: Rewrite this to use a faster algorithm.
		//     : The internal code is using BigInteger to solve
		//     : some cases and that can become slow if we need
		//     : speed.
		return new Value(this, val, Long.divideUnsigned(integer, val.integer));
	}
	
	public Value mul(Value val) {
		if(isFloating() || val.isFloating()) {
			return new Value(this, val, decimal * val.decimal);
		}
		
		// TODO: Check if not checking for unsigned or signed causes any errors.
		return new Value(this, val, integer * val.integer);
	}
	
	public Value mod(Value val) {
		if(isFloating() || val.isFloating()) {
			return new Value(this, val, decimal % val.decimal);
		}
		
		if(isSigned() && val.isSigned()) {
			return new Value(this, val, integer % val.integer);
		}
		
//		NOTE: Values should already be bit checked so this is not needed.
//		long v0 =     integer & ((-1L) >>> (8L * (8 -     size())));
//		long v1 = val.integer & ((-1L) >>> (8L * (8 - val.size())));
		
		// TODO: Rewrite this to use a faster algorithm.
		//     : The internal code is using BigInteger to solve
		//     : some cases and that can become slow if we need
		//     : speed.
		return new Value(this, val, Long.remainderUnsigned(integer, val.integer));
	}
	
	
	// 
	// Invalid for floating numbers
	// 
	
	public Value xor(Value val) {
		if(isFloating() || val.isFloating()) {
			throw new ArithmeticException("'xor' is invalid for floating point values");
		}
		
		// TODO: Check if not checking for unsigned or signed causes any errors.
		return new Value(this, val, integer ^ val.integer);
	}
	
	public Value and(Value val) {
		if(isFloating() || val.isFloating()) {
			throw new ArithmeticException("'and' is invalid for floating point values");
		}
		
		return new Value(this, val, integer & val.integer);
	}
	
	public Value or(Value val) {
		if(isFloating() || val.isFloating()) {
			throw new ArithmeticException("'or' is invalid for floating point values");
		}
		
		return new Value(this, val, integer | val.integer);
	}
	
	public Value shr(Value val) {
		if(isFloating() || val.isFloating()) {
			throw new ArithmeticException("'shr' is invalid for floating point values");
		}
		
		if(isSigned() && val.isSigned()) {
			return new Value(this, val, integer >> val.integer);
		}
		
		return new Value(this, val, integer >>> val.integer);
	}
	
	public Value shl(Value val) {
		if(isFloating() || val.isFloating()) {
			throw new ArithmeticException("'shl' is invalid for floating point values");
		}
		
		if(isSigned() && val.isSigned()) {
			return new Value(this, val, integer << val.integer);
		}
		
		// NOTE: Values should already be bit checked so this is not needed.
		if(integer == 0 || val.integer > 0) return new Value(this, val, 0);
		return new Value(this, val, integer << val.integer);
	}
	
	public Value not() {
		if(isFloating()) return new Value(i32, decimal == 0 ? 1:0);
		return new Value(i32, integer == 0 ? 1:0);
	}
	
	public Value nor() {
		if(isFloating()) throw new ArithmeticException("'nor' is invalid for floating point values");
		return new Value(this, this, ~integer);
	}
	
	public Value neg() {
		if(!isSigned()) throw new ArithmeticException("'neg' is invalid for unsigned values");
		if(isFloating()) return new Value(this, this, -decimal);
		return new Value(this, this, -integer);
	}
	
	public Value cor(Value val)  {
		boolean a = (isFloating() ? (decimal != 0):(integer != 0));
		boolean b = (val.isFloating() ? (val.decimal != 0):(val.integer != 0));
		return new Value(i32, (a || b) ? 1:0);
	}
	
	public Value cand(Value val)  {
		boolean a = (isFloating() ? (decimal != 0):(integer != 0));
		boolean b = (val.isFloating() ? (val.decimal != 0):(val.integer != 0));
		return new Value(i32, (a && b) ? 1:0);
	}
	
	// TODO: Could cause problems if decimal and integer is not written together
	public Value eq(Value val)  {
		if(isFloating() || val.isFloating()) return new Value(i32, decimal == val.decimal ? 1:0);
		return new Value(i32, integer == val.integer ? 1:0);
	}

	// TODO: Could cause problems if decimal and integer is not written together
	public Value neq(Value val)  {
		if(isFloating() || val.isFloating()) return new Value(i32, decimal != val.decimal ? 1:0);
		return new Value(i32, integer != val.integer ? 1:0);
	}
	
	// TODO: Could cause problems if decimal and integer is not written together
	public Value gt(Value val)  {
		if(isFloating() || val.isFloating()) return new Value(i32, decimal > val.decimal ? 1:0);
		if(isSigned() && val.isSigned()) return new Value(i32, integer > val.integer ? 1:0);
		
		return new Value(i32, Long.compareUnsigned(integer, val.integer) > 0 ? 1:0);
	}
	
	// TODO: Could cause problems if decimal and integer is not written together
	public Value gte(Value val)  {
		if(isFloating() || val.isFloating()) return new Value(i32, decimal >= val.decimal ? 1:0);
		if(isSigned() && val.isSigned()) return new Value(i32, integer >= val.integer ? 1:0);
		
		return new Value(i32, Long.compareUnsigned(integer, val.integer) >= 0 ? 1:0);
	}
	
	// TODO: Could cause problems if decimal and integer is not written together
	public Value lt(Value val)  {
		if(isFloating() || val.isFloating()) {
			return new Value(i32, decimal < val.decimal ? 1:0);
		}
		
		if(isSigned() && val.isSigned()) {
			return new Value(i32, integer < val.integer ? 1:0);
		}
		
		return new Value(i32, Long.compareUnsigned(integer, val.integer) < 0 ? 1:0);
	}
	
	// TODO: Could cause problems if decimal and integer is not written together
	public Value lte(Value val)  {
		if(isFloating() || val.isFloating()) {
			return new Value(i32, decimal <= val.decimal ? 1:0);
		}
		
		if(isSigned() && val.isSigned()) {
			return new Value(i32, integer <= val.integer ? 1:0);
		}
		
		return new Value(i32, Long.compareUnsigned(integer, val.integer) <= 0 ? 1:0);
	}
	
	public Value convert(Atom atom) {
		if(atom.isFloating()) return new Value(atom, decimal);
		return new Value(atom, integer);
	}
	
	public Value convert(LowType type) {
		if(type.isPointer()) return convert(Atom.i64);
		return convert(type.type());
	}
	
	@Override
	public String toString() {
		if(isFloating()) {
			if(size() == 4) return Float.toString((float)decimal);
			return Double.toString(decimal);
		}
		
		if(isSigned()) return Long.toString(integer);
		return Long.toUnsignedString(integer);
	}
}
