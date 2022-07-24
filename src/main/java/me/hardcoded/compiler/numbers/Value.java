package me.hardcoded.compiler.numbers;

import java.util.Arrays;

public class Value {
	private static final String RADIX = "0123456789abcdefghijklmnopqrstuvwxyz";
	private static final int[] EMPTY = new int[0];
	private final int[] data;
	private final int bits;
	private final Type type;
	
	public enum Type {
		SIGNED,
		UNSIGNED,
		FLOATING
	}
	
	public Value(int[] array, int bits, Type type) {
		this.bits = bits;
		this.type = type;
		this.data = new int[(bits + 31) >> 5];
		this.fillBits(array, bits);
	}
	
	public Value add(Value value) {
		checkEqualType("add", value);
		fillBits(switch (type) {
			case UNSIGNED, SIGNED -> ValueUtil.add(data, data, value.data);
			case FLOATING -> ValueUtil.floatAdd(data, data, value.data, getExponentBits(), getMantissaBits());
		}, bits);
		return this;
	}
	
	public Value sub(Value value) {
		checkEqualType("sub", value);
		fillBits(ValueUtil.sub(data, data, value.data), bits);
		return this;
	}
	
	public Value xor(Value value) {
		checkEqualType("xor", value);
		fillBits(ValueUtil.xor(data, data, value.data), bits);
		return this;
	}
	
	public Value and(Value value) {
		checkEqualType("and", value);
		fillBits(ValueUtil.and(data, data, value.data), bits);
		return this;
	}
	
	public Value or(Value value) {
		checkEqualType("or", value);
		fillBits(ValueUtil.or(data, data, value.data), bits);
		return this;
	}
	
	public Value mul(Value value) {
		checkEqualType("mul", value);
		fillBits(ValueUtil.mul(new int[data.length], data, value.data), bits);
		return this;
	}
	
	public Value div(Value value) {
		checkEqualType("div", value);
		fillBits(switch (type) {
			case UNSIGNED -> ValueUtil.unsignedDiv(new int[data.length], data, value.data);
			case SIGNED -> ValueUtil.signedDiv(new int[data.length], data, value.data);
			case FLOATING -> throw new UnsupportedOperationException();
		}, bits);
		return this;
	}
	
	public Value remainder(Value value) {
		checkEqualType("remainder", value);
		fillBits(switch (type) {
			case UNSIGNED -> ValueUtil.unsignedRemainder(new int[data.length], data, value.data);
			case SIGNED -> ValueUtil.signedRemainder(new int[data.length], data, value.data);
			case FLOATING -> throw new UnsupportedOperationException();
		}, bits);
		return this;
	}
	
	public Value shiftLeft(int steps) {
		fillBits(ValueUtil.shiftLeft(data, data, steps), bits);
		return this;
	}
	
	public Value shiftRight(int steps) {
		fillBits(ValueUtil.shiftRight(data, data, steps), bits);
		return this;
	}
	
	public Value neg() {
		fillBits(ValueUtil.neg(data, data), bits);
		return this;
	}
	
	public int compare(Value value) {
		checkEqualType("compare", value);
		int result = switch (type) {
			case UNSIGNED -> ValueUtil.compareUnsigned(data, value.data);
			case SIGNED   -> ValueUtil.compareSigned(data, value.data);
			case FLOATING -> throw new UnsupportedOperationException();
		};
		
		int[] array = new int[data.length];
		ValueUtil.set(array, result);
		fillBits(array, bits);
		return result;
	}
	
	public boolean isZero() {
		return ValueUtil.isZero(data);
	}
	
	/**
	 * Cast this value to another size and type and return the new value
	 * @param newBits the new size
	 * @param type the new type
	 */
	public Value cast(int newBits, Type type) {
		Value value = new Value(EMPTY, newBits, type);
		
		if (type == Type.FLOATING) {
			// TODO: Only certain bits can be converted to floating
		} else {
			// Check if the last bit is set (if negative)
			if (type == Type.SIGNED && hasBit(bits - 1)) {
				Arrays.fill(value.data, -1);
			}
			
			// Fill bit data
			value.fillBits(data, bits);
		}
		
		return value;
	}
	
	public Value copy() {
		return new Value(data, bits, type);
	}
	
	private int getMantissaBits() {
		// Used when calculating IEEE 754 floating point arithmetics
		return switch (bits) {
			case 16 -> 10;
			case 32 -> 23;
			case 64 -> 52;
			case 128 -> 113;
			case 256 -> 237;
			default -> throw new IllegalStateException("Cannot get mantissa of invalid bit size");
		};
	}
	
	private int getExponentBits() {
		// Used when calculating IEEE 754 floating point arithmetics
		return switch (bits) {
			case 16 -> 5;
			case 32 -> 8;
			case 64 -> 11;
			case 128 -> 15;
			case 256 -> 19;
			default -> throw new IllegalStateException("Cannot get exponent of invalid bit size");
		};
	}
	
	public String toString() {
		return toHexString();
	}
	
	public String toString(int radix) {
		if (radix < 2 || radix > 36) {
			throw new IllegalArgumentException("Invalid radix. Must be between 2 and 36");
		}
		
		StringBuilder sb = new StringBuilder();
		
		switch (type) {
			case UNSIGNED, SIGNED -> {
				int[] radixArray = new int[data.length];
				int[] remainderArray = new int[data.length];
				int[] valueArray = data.clone();
				int[] valueTempArray = data.clone();
				ValueUtil.set(radixArray, radix);
				
				boolean signed = (type == Type.SIGNED);
				if (signed && ValueUtil.isNegative(data)) {
					ValueUtil.neg(valueArray, valueArray);
				}
				
				do {
					ValueUtil.quotientRemainder(valueTempArray, remainderArray, valueArray, radixArray, true);
					ValueUtil.set(valueArray, valueTempArray);
					sb.append(RADIX.charAt(remainderArray[0]));
				} while (!ValueUtil.isZero(valueArray));
				
				if (signed && ValueUtil.isNegative(data)) {
					sb.append('-');
				}
				
				sb.reverse();
			}
			case FLOATING -> {
				int exponentBits = getExponentBits();
				int mantissaBits = getMantissaBits();
				int[] exponent = ValueUtil.copyBits(new int[(exponentBits + 31) >> 5], data, (data.length << 5) - exponentBits - 1, exponentBits);
				int[] mantissa = ValueUtil.copyBits(new int[(mantissaBits + 32) >> 5], data, 0, mantissaBits);
				int sign = data[data.length - 1] >>> 31;
				
				// Clone the whole mantissa and create a ten array
				int[] man = ValueUtil.copyBits(new int[(mantissaBits + 31 + 10) >> 5], mantissa, 0, mantissaBits);
				int[] rdx = ValueUtil.createArray(radix, mantissaBits + 10);
				int[] tmp = ValueUtil.createArray(mantissaBits + 10);
				int[] one = ValueUtil.createArray(1, exponent.length << 5);
				
				// The mantissa highest bit is always set to one. This is done manually
				ValueUtil.setBit(man, man, mantissaBits, true);
				
				// Calculate the true exponent value
				{
					int[] arr = ValueUtil.createArray(mantissaBits, exponent.length << 5);
					ValueUtil.sub(exponent, exponent, arr);
					ValueUtil.set(arr, -1);
					ValueUtil.shiftLeft(arr, arr, exponentBits - 1);
					ValueUtil.flipBits(arr, arr);
					ValueUtil.sub(exponent, exponent, arr);
				}
				
				// Check if the exponent is negative or positive
				int cmp = ValueUtil.signum(exponent, false);
				
				// Two to the power of anything is just a left shift n steps
				// int highestMantissaBit = ValueUtil.highestSetBit(mantissa);
				// Now find the largest x < log radix (2 ^ highestMantissaBit)
				
				double logRd2 = Math.log(2.0) / Math.log(radix);
				double twoPtr = 0;
				int    rdxPtr = 0;
				int    rdxOff = -1;
				// TODO: Position (rdxOff) such that we loose as few bits from the mantissa
				//       during the base conversion
				
				while (!ValueUtil.isZero(exponent)) {
					boolean chooseTwo = (twoPtr) < (rdxPtr + rdxOff);
					if (chooseTwo) {
						twoPtr += logRd2;
						if (cmp > 0) {
							ValueUtil.sub(exponent, exponent, one);
							ValueUtil.shiftLeft(man, man, 1);
						} else {
							ValueUtil.add(exponent, exponent, one);
							ValueUtil.shiftRight(man, man, 1);
						}
					} else {
						rdxPtr += 1;
						if (cmp > 0) {
							ValueUtil.unsignedDiv(tmp, man, rdx);
						} else {
							ValueUtil.mul(tmp, man, rdx);
						}
						ValueUtil.set(man, tmp);
					}
				}
				
				{
					int[] remainderArray = new int[man.length];
					
					do {
						ValueUtil.quotientRemainder(tmp, remainderArray, man, rdx, true);
						ValueUtil.set(man, tmp);
						sb.append(RADIX.charAt(remainderArray[0]));
					} while (!ValueUtil.isZero(man));
					
					if (cmp > 0) {
						sb.insert(0, "0".repeat(rdxPtr));
					} else {
						if (rdxPtr >  sb.length()) sb.append("0".repeat(rdxPtr - sb.length()));
						if (rdxPtr == sb.length()) sb.append(0);
						
						// Insert dot
						sb.insert(rdxPtr, '.');
						if (sb.charAt(0) == '.') {
							sb.insert(0, '0');
						}
					}
				
					if (sign != 0) {
						sb.append('-');
					}
					
					sb.reverse();
					
					// Approximation of the precision of how many digits the float displays correctly
					//long digitsPrecision = Math.round(Math.log10(2.0) * (mantissaBits + 1.0));
				}
			}
		}
		
		return sb.toString();
	}
	
	public String toHexString() {
		StringBuilder sb = new StringBuilder();
		
		for (int i = 0; i < ((bits + 3) / 4); i++) {
			long value = (data[i >> 3] >>> (4 * (i & 15))) & 0xf;
			sb.append(Long.toHexString(value));
		}
		
		return sb.reverse().toString();
	}
	
	public String toBinaryString() {
		StringBuilder sb = new StringBuilder();
		
		for (int i = 0; i < bits; i++) {
			sb.append((data[i >> 5] >>> (i & 31)) & 1);
		}
		
		return sb.reverse().toString();
	}
	
	private boolean hasBit(int index) {
		if (index < 0 || index > bits) {
			throw new RuntimeException("(%d) bit out of bounds".formatted(index));
		}
		
		return ((data[index >> 5] >>> (index & 31)) & 1) == 1;
	}
	
	private void fillBits(int[] array, int copyBits) {
		// Calculate the whole indexes and bits to be copied
		int wholeBits = (copyBits >>> 5);
		int smallBits = (copyBits & 31);
		int highMask = -1 << smallBits;
		
		// First copy the whole bits
		if (array.length > wholeBits) {
			System.arraycopy(array, 0, data, 0, wholeBits);
			
			// If the small bits are not equal to zero
			if (smallBits != 0) {
				data[wholeBits] = (data[wholeBits] & highMask) | (array[wholeBits] & (~highMask));
			}
		} else {
			System.arraycopy(array, 0, data, 0, array.length);
			Arrays.fill(data, array.length, wholeBits, 0);
			
			// If the small bits are not equal to zero
//			if (smallBits != 0) {
//				data[wholeBits] &= highMask;
//			}
		}
	}
	
	private boolean checkEqualType(String name, Value value) {
		if (!equalType(value)) {
			throw new RuntimeException("Value types does not match. (%s)".formatted(name));
		}
		
		return true;
	}
	
	/**
	 * Returns {@code true} if the type is equal
	 */
	public boolean equalType(Value value) {
		return this.bits == value.bits
			&& this.type == value.type;
	}
	
	public static Value valueOf(int value, int bits, Type type) {
		return new Value(new int[] { value }, bits, type);
	}
	
	public static Value valueOf(long value, int bits, Type type) {
		return new Value(new int[] { (int) value, (int) (value >>> 32) }, bits, type);
	}
	
	// TODO: Make it possible to convert between different float bit sizes
	public static Value valueOf(float value) {
		return valueOf(Float.floatToRawIntBits(value), 32, Type.FLOATING);
	}
	
	public static Value valueOf(double value) {
		return valueOf(Double.doubleToRawLongBits(value), 64, Type.FLOATING);
	}
}
