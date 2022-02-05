package me.hardcoded.compiler.numbers;

import me.hardcoded.compiler.constants.Atom;
import me.hardcoded.compiler.expression.LowType;

import static me.hardcoded.compiler.constants.Atom.*;

/**
 * This is a number utility class for <b>signed</b>, <b>unsigned</b> and <b>floating</b> operations.
 * 
 * @author HardCoded
 */
public class ValueNew {
	// TODO: Make this class work for arbitrary sizes
	// https://en.wikipedia.org/wiki/IEEE_754
	// TODO: Allow from 16 to 256 bit floating point values
	// TODO: Allow from 8 to 512 bit integer values
	private final double decimal;
	private final long integer;
	
	/**
	 * The type of this value.
	 */
	private Atom type;
	
	
	private ValueNew(ValueNew a, ValueNew b, double value) {
		this(Atom.largest(a.type, b.type), value);
	}
	
	private ValueNew(ValueNew a, ValueNew b, long value) {
		this(Atom.largest(a.type, b.type), value);
	}
	
	private ValueNew(Atom type, long value) {
		this.type = type;
		
		if (isFloating()) {
			switch (size()) {
				case 8 -> decimal = value;
				case 4 -> decimal = (float)value;
				default -> throw new ArithmeticException();
			}
			
			integer = value;
		} else {
			if (isSigned()) {
				switch (size()) {
					case 8 -> {}
					case 4 -> value = (int)value;
					case 2 -> value = (short)value;
					case 1 -> value = (byte)value;
					default -> throw new ArithmeticException();
				}
				
				decimal = value;
			} else {
				value &= (-1L) >>> (8L * (8 - size()));
				
				if (value < 0) {
					decimal = 1.0 + 2.0 * ((double)(value >>> 1L));
				} else {
					decimal = value;
				}
			}

			integer = value;
		}
	}
	
	private ValueNew(Atom type, double value) {
		if (!isFloating()) throw new ArithmeticException(type + " does not support floating point values");
		this.type = type;
		
		switch (size()) {
			case 4 -> decimal = (float)value;
			default -> decimal = value;
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
	
	
	public static ValueNew sbyte(long value) { return new ValueNew(i8, value); }
	public static ValueNew word(long value) { return new ValueNew(i16, value); }
	public static ValueNew dword(long value) { return new ValueNew(i32, value); }
	public static ValueNew qword(long value) { return new ValueNew(i64, value); }
	public static ValueNew ubyte(long value) { return new ValueNew(u8, value); }
	public static ValueNew uword(long value) { return new ValueNew(u16, value); }
	public static ValueNew udword(long value) { return new ValueNew(u32, value); }
	public static ValueNew uqword(long value) { return new ValueNew(u64, value); }
	public static ValueNew ifloat(double value) { return new ValueNew(f32, value); }
	public static ValueNew idouble(double value) { return new ValueNew(f64, value); }
	public static ValueNew get(long value, Atom type) { return new ValueNew(type, value); }
	public static ValueNew get(double value, Atom type) { return new ValueNew(type, value); }
	
	public ValueNew add(ValueNew val) {
		if (isFloating() || val.isFloating()) {
			return new ValueNew(this, val, decimal + val.decimal);
		}
		
		return new ValueNew(this, val, integer + val.integer);
	}
	
	public ValueNew sub(ValueNew val) {
		if (isFloating() || val.isFloating()) {
			return new ValueNew(this, val, decimal - val.decimal);
		}
		
		return new ValueNew(this, val, integer - val.integer);
	}
	
	public ValueNew div(ValueNew val) {
		if (isFloating() || val.isFloating()) {
			return new ValueNew(this, val, decimal / val.decimal);
		}
		
		if (isSigned() && val.isSigned()) {
			return new ValueNew(this, val, integer / val.integer);
		}
		
//		NOTE: Values should already be bit checked so this is not needed.
//		long v0 =     integer & ((-1L) >>> (8L * (8 -     size())));
//		long v1 = val.integer & ((-1L) >>> (8L * (8 - val.size())));
		
		// TODO: Rewrite this to use a faster algorithm.
		//     : The internal code is using BigInteger to solve
		//     : some cases and that can become slow if we need
		//     : speed.
		return new ValueNew(this, val, Long.divideUnsigned(integer, val.integer));
	}
	
	public ValueNew mul(ValueNew val) {
		if (isFloating() || val.isFloating()) {
			return new ValueNew(this, val, decimal * val.decimal);
		}
		
		// TODO: Check if not checking for unsigned or signed causes any errors.
		return new ValueNew(this, val, integer * val.integer);
	}
	
	public ValueNew mod(ValueNew val) {
		if (isFloating() || val.isFloating()) {
			return new ValueNew(this, val, decimal % val.decimal);
		}
		
		if (isSigned() && val.isSigned()) {
			return new ValueNew(this, val, integer % val.integer);
		}
		
//		NOTE: Values should already be bit checked so this is not needed.
//		long v0 =     integer & ((-1L) >>> (8L * (8 -     size())));
//		long v1 = val.integer & ((-1L) >>> (8L * (8 - val.size())));
		
		// TODO: Rewrite this to use a faster algorithm.
		//     : The internal code is using BigInteger to solve
		//     : some cases and that can become slow if we need
		//     : speed.
		return new ValueNew(this, val, Long.remainderUnsigned(integer, val.integer));
	}
	
	
	// 
	// Invalid for floating numbers
	// 
	
	public ValueNew xor(ValueNew val) {
		if (isFloating() || val.isFloating()) {
			throw new ArithmeticException("'xor' is invalid for floating point values");
		}
		
		// TODO: Check if not checking for unsigned or signed causes any errors.
		return new ValueNew(this, val, integer ^ val.integer);
	}
	
	public ValueNew and(ValueNew val) {
		if (isFloating() || val.isFloating()) {
			throw new ArithmeticException("'and' is invalid for floating point values");
		}
		
		return new ValueNew(this, val, integer & val.integer);
	}
	
	public ValueNew or(ValueNew val) {
		if (isFloating() || val.isFloating()) {
			throw new ArithmeticException("'or' is invalid for floating point values");
		}
		
		return new ValueNew(this, val, integer | val.integer);
	}
	
	public ValueNew shr(ValueNew val) {
		if (isFloating() || val.isFloating()) {
			throw new ArithmeticException("'shr' is invalid for floating point values");
		}
		
		if (isSigned() && val.isSigned()) {
			return new ValueNew(this, val, integer >> val.integer);
		}
		
		return new ValueNew(this, val, integer >>> val.integer);
	}
	
	public ValueNew shl(ValueNew val) {
		if (isFloating() || val.isFloating()) {
			throw new ArithmeticException("'shl' is invalid for floating point values");
		}
		
		if (isSigned() && val.isSigned()) {
			return new ValueNew(this, val, integer << val.integer);
		}
		
		// NOTE: Values should already be bit checked so this is not needed.
		if (integer == 0 || val.integer > 0) {
			return new ValueNew(this, val, 0);
		}
		return new ValueNew(this, val, integer << val.integer);
	}
	
	public ValueNew not() {
		if (isFloating()) {
			return new ValueNew(i32, decimal == 0 ? 1:0);
		}
		return new ValueNew(i32, integer == 0 ? 1:0);
	}
	
	public ValueNew nor() {
		if (isFloating()) {
			throw new ArithmeticException("'nor' is invalid for floating point values");
		}
		return new ValueNew(this, this, ~integer);
	}
	
	public ValueNew neg() {
		if (!isSigned()) {
			throw new ArithmeticException("'neg' is invalid for unsigned values");
		}
		if (isFloating()) {
			return new ValueNew(this, this, -decimal);
		}
		return new ValueNew(this, this, -integer);
	}
	
	public ValueNew cor(ValueNew val)  {
		boolean a = (isFloating() ? (decimal != 0):(integer != 0));
		boolean b = (val.isFloating() ? (val.decimal != 0):(val.integer != 0));
		return new ValueNew(i32, (a || b) ? 1:0);
	}
	
	public ValueNew cand(ValueNew val)  {
		boolean a = (isFloating() ? (decimal != 0):(integer != 0));
		boolean b = (val.isFloating() ? (val.decimal != 0):(val.integer != 0));
		return new ValueNew(i32, (a && b) ? 1:0);
	}
	
	// TODO: Could cause problems if decimal and integer is not written together
	public ValueNew eq(ValueNew val)  {
		if (isFloating() || val.isFloating()) {
			return new ValueNew(i32, decimal == val.decimal ? 1:0);
		}
		return new ValueNew(i32, integer == val.integer ? 1:0);
	}

	// TODO: Could cause problems if decimal and integer is not written together
	public ValueNew neq(ValueNew val)  {
		if (isFloating() || val.isFloating()) {
			return new ValueNew(i32, decimal != val.decimal ? 1:0);
		}
		return new ValueNew(i32, integer != val.integer ? 1:0);
	}
	
	// TODO: Could cause problems if decimal and integer is not written together
	public ValueNew gt(ValueNew val)  {
		if (isFloating() || val.isFloating()) {
			return new ValueNew(i32, decimal > val.decimal ? 1:0);
		}
		
		if (isSigned() && val.isSigned()) {
			return new ValueNew(i32, integer > val.integer ? 1:0);
		}
		
		return new ValueNew(i32, Long.compareUnsigned(integer, val.integer) > 0 ? 1:0);
	}
	
	// TODO: Could cause problems if decimal and integer is not written together
	public ValueNew gte(ValueNew val)  {
		if (isFloating() || val.isFloating()) {
			return new ValueNew(i32, decimal >= val.decimal ? 1:0);
		}
		
		if (isSigned() && val.isSigned()) {
			return new ValueNew(i32, integer >= val.integer ? 1:0);
		}
		
		return new ValueNew(i32, Long.compareUnsigned(integer, val.integer) >= 0 ? 1:0);
	}
	
	// TODO: Could cause problems if decimal and integer is not written together
	public ValueNew lt(ValueNew val)  {
		if (isFloating() || val.isFloating()) {
			return new ValueNew(i32, decimal < val.decimal ? 1:0);
		}
		
		if (isSigned() && val.isSigned()) {
			return new ValueNew(i32, integer < val.integer ? 1:0);
		}
		
		return new ValueNew(i32, Long.compareUnsigned(integer, val.integer) < 0 ? 1:0);
	}
	
	// TODO: Could cause problems if decimal and integer is not written together
	public ValueNew lte(ValueNew val)  {
		if (isFloating() || val.isFloating()) {
			return new ValueNew(i32, decimal <= val.decimal ? 1:0);
		}
		
		if (isSigned() && val.isSigned()) {
			return new ValueNew(i32, integer <= val.integer ? 1:0);
		}
		
		return new ValueNew(i32, Long.compareUnsigned(integer, val.integer) <= 0 ? 1:0);
	}
	
	public ValueNew convert(Atom atom) {
		if (atom.isFloating()) {
			return new ValueNew(atom, decimal);
		}
		
		return new ValueNew(atom, integer);
	}
	
	public ValueNew convert(LowType type) {
		if (type.isPointer()) {
			return convert(Atom.i64);
		}
		
		return convert(type.type());
	}
	
	@Override
	public String toString() {
		if (isFloating()) {
			if(size() == 4) {
				return Float.toString((float)decimal);
			}
			
			return Double.toString(decimal);
		}
		
		if (isSigned()) {
			return Long.toString(integer);
		}
		
		return Long.toUnsignedString(integer);
	}
}
