package me.hardcoded.compiler.numbers;

import java.util.Arrays;

/**
 * Utility class for extended value size algorithms.
 *
 * This class was written to make it easier to port BigInteger to other languages.
 *
 * @author HardCoded
 */
class ValueUtilBck {
	static int[] add(int[] dst, int[] x, int[] y) {
		long carry = 0L;
		
		for (int i = 0, len = dst.length; i < len; i++) {
			long a = x[i] & 0xffffffffL;
			long b = y[i] & 0xffffffffL;
			long c = a + b + carry;
			
			// Calculate overflow
			carry = c >>> 32;
			
			// Update array
			dst[i] = (int) c;
		}
		
		return dst;
	}
	
	static int[] sub(int[] dst, int[] x, int[] y) {
		long carry = 0L;
		
		for (int i = 0, len = dst.length; i < len; i++) {
			long a = x[i] & 0xffffffffL;
			long b = y[i] & 0xffffffffL;
			long c = a - b - carry;
			
			// Calculate overflow
			carry = (-(c >>> 32)) & 0xffffffffL;
			
			// Update array
			dst[i] = (int) c;
		}
		
		return dst;
	}
	
	static int[] add(int[] dst, int[] x, long value) {
		long carry = 0L;
		
		for (int i = 0, len = dst.length; i < len; i++) {
			long a = x[i] & 0xffffffffL;
			long b = (i == 0 ? (value) : (i == 1 ? (value >>> 32) : 0)) & 0xffffffffL;
			long c = a + b + carry;
			carry = c >>> 32;
			
			// Update array
			dst[i] = (int) c;
		}
		
		return dst;
	}
	
	static int[] xor(int[] dst, int[] x, int[] y) {
		for (int i = 0, len = dst.length; i < len; i++) {
			dst[i] = x[i] ^ y[i];
		}
		
		return dst;
	}
	
	static int[] and(int[] dst, int[] x, int[] y) {
		for (int i = 0, len = dst.length; i < len; i++) {
			dst[i] = x[i] & y[i];
		}
		
		return dst;
	}
	
	static int[] or(int[] dst, int[] x, int[] y) {
		for (int i = 0, len = dst.length; i < len; i++) {
			dst[i] = x[i] | y[i];
		}
		
		return dst;
	}
	
	static int[] neg(int[] dst, int[] x) {
		long carry = 0L;
		
		for (int i = 0, len = dst.length; i < len; i++) {
			long a = (~x[i]) & 0xffffffffL;
			long b = (i == 0 ? 1L : 0L);
			long c = a + b + carry;
			
			// Calculate overflow
			carry = c >>> 32;
			
			// Update array
			dst[i] = (int) c;
		}
		
		return dst;
	}
	
	/**
	 * @apiNote {@code dst} must not be the same as {@code x} or {@code y}
	 */
	static int[] mul(int[] dst, int[] x, int[] y) {
		assert dst != x && dst != y : "dst must not be the same as x or y";
		Arrays.fill(dst, 0);
		
		// For each element in x multiply with all of y
		for (int i = 0, len = dst.length; i < len; i++) {
			long carry = 0L;
			long dstCarry = 0L;
			
			for (int j = 0; j < len - i; j++) {
				long a = x[i] & 0xffffffffL;
				long b = y[j] & 0xffffffffL;
				long c = a * b + carry;
				
				// Calculate overflow
				carry = c >>> 32;
				
				// Add the value to the destination
				long d = (dst[j + i] & 0xffffffffL) + (c & 0xffffffffL) + dstCarry;
				dstCarry = d >>> 32;
				dst[j + i] = (int) d;
			}
		}
		
		return dst;
	}
	
	static int[] shiftLeft(int[] dst, int[] x, int steps) {
		final int len = x.length;
		
		// How many steps to the left that we need to load
		final int lid = steps >> 5;
		final int fid = steps & 31;
		
		if (fid == 0) {
			// We can move the array over one step
			System.arraycopy(x, 0, dst, lid, len - lid);
			Arrays.fill(dst, 0, lid, 0);
			return dst;
		}
		
		for (int i = len - 1; i >= 0; i--) {
			long a = (i - lid >= 0)
				? ((x[i - lid] & 0xffffffffL) << fid)
				: 0;
			
			long b = (i - lid - 1 >= 0)
				? ((x[i - lid - 1] & 0xffffffffL) >> (32 - fid))
				: 0;
			
			dst[i] = (int) (a | b);
		}
		
		return dst;
	}
	
	static int[] shiftRight(int[] dst, int[] x, int steps) {
		final int len = x.length;
		
		// How many steps to the right that we need to load
		final int rid = (steps + 31) >> 5;
		final int fid = steps & 31;
		
		if (fid == 0) {
			// We can move the array over one step
			System.arraycopy(x, rid, dst, 0, len - rid);
			Arrays.fill(dst, len - rid, len, 0);
			return dst;
		}
		
		for (int i = 0; i < len; i++) {
			long a = (i + rid < len)
				? ((x[i + rid] & 0xffffffffL) << (32 - fid))
				: 0;
			
			long b = (i + rid - 1 < len && i - 1 + rid >= 0)
				? ((x[i + rid - 1] & 0xffffffffL) >> fid)
				: 0;
			
			dst[i] = (int) (a | b);
		}
		
		return dst;
	}
	
	/**
	 * @apiNote {@code Q}, {@code R} are modified
	 */
	static void quotientRemainder(int[] Q, int[] R, int[] x, int[] y, boolean unsigned) {
		quotientRemainder(Q, R, y.clone(), x, y, unsigned);
	}
	
	/**
	 * @apiNote {@code Q}, {@code R}, {@code tmp_y} are modified
	 */
	static void quotientRemainder(int[] Q, int[] R, int[] tmp_y, int[] x, int[] y, boolean unsigned) {
		Arrays.fill(Q, 0);
		System.arraycopy(x, 0, R, 0, x.length);
		
		int cmp = compareUnsigned(x, y);
//		System.out.printf("%s <=> %s == %d\n", toString(x), toString(y), cmp);
		if (cmp == 0) {
			Q[0] = 1;
			Arrays.fill(R, 0);
			return;
		}
		
		boolean x_neg = isNegative(x);
		boolean y_neg = isNegative(y);
		if (!unsigned) {
			if (x_neg) neg(R, R);
			if (y_neg) neg(tmp_y, tmp_y);
		}
		
		int highest = (x.length << 5) - highestSetBit(tmp_y);
		int index = 0;
		while (index < highest && compareUnsigned(R, tmp_y) > 0) {
			shiftLeft(tmp_y, tmp_y, 1);
			index ++;
		}
		
		// For each index in the bit list
		for (int i = index; i >= 0; i--) {
			if (compareUnsigned(R, tmp_y) >= 0) {
//				String prev = toString(R);
				sub(R, R, tmp_y);
				setBit(Q, Q, i, true);
//				System.out.printf(
//					"%s  -  %s = %s (%s)\n",
//					prev,
//					toString(tmp_y),
//					toString(R),
//					toString(Q)
//				);
			}
			
			shiftRight(tmp_y, tmp_y, 1);
		}
		
		if (!unsigned) {
			if (x_neg ^ y_neg) {
				neg(Q, Q);
			}
			
			if (x_neg) {
				neg(R, R);
			}
		}
	}
	
	/**
	 * @apiNote {@code dst} must not be the same as {@code x} or {@code y}
	 *        - Requires two memory allocations
	 */
	static int[] unsignedDiv(int[] dst, int[] x, int[] y) {
		assert dst != x && dst != y : "dst must not be the same as x or y";
		
		// Allocate two temporary arrays
		int[] tmp_x = x.clone();
		int[] tmp_y = y.clone();
		
		// Calculate quotient and remainder
		quotientRemainder(dst, tmp_x, tmp_y, x, y, true);
		return dst;
	}
	
	/**
	 * @apiNote {@code dst} must not be the same as {@code x} or {@code y}
	 *        - Requires two memory allocations
	 */
	static int[] signedDiv(int[] dst, int[] x, int[] y) {
		assert dst != x && dst != y : "dst must not be the same as x or y";
		
		// Allocate two temporary arrays
		int[] tmp_x = x.clone();
		int[] tmp_y = y.clone();
		
		// Calculate quotient and remainder
		quotientRemainder(dst, tmp_x, tmp_y, x, y, false);
		return dst;
	}
	
	/**
	 * @apiNote {@code dst} must not be the same as {@code x} or {@code y}
	 *        - Requires two memory allocation
	 */
	static int[] unsignedRemainder(int[] dst, int[] x, int[] y) {
		assert dst != x && dst != y : "dst must not be the same as x or y";
		
		// Allocate two temporary arrays
		int[] tmp_x = x.clone();
		int[] tmp_y = y.clone();
		
		// Calculate quotient and remainder
		quotientRemainder(dst, tmp_x, tmp_y, x, y, true);
		return tmp_x;
	}
	
	/**
	 * @apiNote {@code dst} must not be the same as {@code x} or {@code y}
	 *        - Requires two memory allocation
	 */
	static int[] signedRemainder(int[] dst, int[] x, int[] y) {
		assert dst != x && dst != y : "dst must not be the same as x or y";
		
		// Allocate two temporary arrays
		int[] tmp_x = x.clone();
		int[] tmp_y = y.clone();
		
		// Calculate quotient and remainder
		quotientRemainder(dst, tmp_x, tmp_y, x, y, false);
		return tmp_x;
	}
	
	static int[] setBit(int[] dst, int[] x, int index, boolean enable) {
		final int idx = index >> 5;
		final int ibt = index & 31;
		
		if (dst != x) {
			System.arraycopy(x, 0, dst, 0, x.length);
		}
		
		dst[idx] = enable
			? (x[idx] |  (1 << ibt))
			: (x[idx] & ~(1 << ibt));
		
		return dst;
	}
	
	static boolean getBit(int[] x, int index) {
		final int idx = index >> 5;
		final int ibt = index & 31;
		return (x[idx] & (1 << ibt)) != 0;
	}
	
	static int[] set(int[] x, int[] y) {
		System.arraycopy(y, 0, x, 0, x.length);
		return x;
	}
	
	static int[] set(int[] x, long value) {
		if (value < 0) {
			Arrays.fill(x, -1);
		} else {
			Arrays.fill(x, 0);
		}
		x[0] = (int) value;
		if (x.length > 1) {
			x[1] = (int) (value >>> 32);
		}
		return x;
	}
	
	static boolean isNegative(int[] x) {
		return (x[x.length - 1] >>> 31) != 0;
	}
	
	static boolean isZero(int[] x) {
		for (int i = 0, len = x.length; i < len; i++) {
			if (x[i] != 0) {
				return false;
			}
		}
		
		return true;
	}
	
	static int highestSetBit(int[] x) {
		final int len = x.length;
		
		for (int i = len - 1; i >= 0; i--) {
			int mask = Integer.highestOneBit(x[i]);
			
			if (mask != 0) {
				// Remove one and calculate bit count
				return Integer.bitCount(mask - 1) + 1 + (i << 5);
			}
		}
		
		return -1;
	}
	
	static int signum(int[] x, boolean unsigned) {
		if (x[x.length - 1] < 0) {
			return unsigned ? 1 : -1;
		}
		
		for (int i = x.length - 1; i >= 0; i--) {
			if (x[i] != 0) {
				return 1;
			}
		}
		
		return 0;
	}
	
	static int compareSigned(int[] x, int[] y) {
		boolean x_neg = isNegative(x);
		boolean y_neg = isNegative(y);
		int cmp = compareUnsigned(x, y);
		return (x_neg == y_neg) ? cmp : -cmp;
	}
	
	static int compareUnsigned(int[] x, int[] y) {
		for (int i = x.length - 1; i >= 0; i--) {
			long a = x[i] & 0xffffffffL;
			long b = y[i] & 0xffffffffL;
			
			if (a != b) {
				return a < b ? -1 : 1;
			}
		}
		
		return 0;
	}
	
	static int[] flipBits(int[] dst, int[] x) {
		for (int i = 0, len = dst.length; i < len; i++) {
			dst[i] = ~x[i];
		}
		
		return dst;
	}
	
	static int[] copyBits(int[] dst, int[] src, int offset, int bits) {
		// TODO: this should not overwrite any data inside dst after bits...
		
		final int len = src.length;
		
		// How many steps to the right that we need to load
		final int rid = (offset + 31) >> 5;
		final int fid = offset & 31;
		final int bid = (bits + 31) >> 5;
		final int mask = ~(-1 << (bits & 31));
		
		if (fid == 0) {
//			// We can move the array over one step
			for (int i = 0; i < bid; i++) {
				long a = (i + rid < len)
					? (src[i + rid] & 0xffffffffL)
					: 0;
				
				if (bid == i + 1) {
					dst[i] = (dst[i] & ~mask) | (int) (a & mask);
				} else {
					dst[i] = (int) (a);
				}
			}
		} else {
			for (int i = 0; i < bid; i++) {
				long a = (i + rid < len)
					? ((src[i + rid] & 0xffffffffL) << (32 - fid))
					: 0;
				
				long b = (i + rid - 1 < len && i + rid > 0)
					? ((src[i + rid - 1] & 0xffffffffL) >> fid)
					: 0;
				
				if (bid == i + 1) {
					dst[i] = (dst[i] & ~mask) | (int) ((a | b) & mask);
				} else {
					dst[i] = (int) (a | b);
				}
			}
		}
		
		return dst;
	}
	
	static int[] bitcopy(int[] src, int srcOff, int[] dst, int dstOff, int bits) {
		// TODO: Make this faster
		for (int i = 0; i < bits; i++) {
			// Change one bit at the time
			setBit(dst, dst, dstOff + i, getBit(src, srcOff + i));
		}
		
		return dst;
	}
	
	static int[] fromLong(long value) {
		return new int[] { (int) value, (int) (value >>> 32) };
	}
	
	static String toBitsString(int[] value) {
		StringBuilder sb = new StringBuilder();
		sb.append("0b");
		for (int i = value.length - 1; i >= 0; i--) {
			String binary = Integer.toBinaryString(value[i]);
			sb.append("0".repeat(32 - binary.length())).append(binary);
		}
		for (int i = sb.length() - 8; i > 2; i -= 8) {
			sb.insert(i, '_');
		}
		return sb.append('L').toString();
	}
	
	static int toInt(int[] array) {
		if (array.length > 1) {
			throw new IllegalStateException("value cannot be converted to an int");
		}
		
		return array[0];
	}
	
	static String toString(int[] array) {
		return Arrays.stream(array).mapToObj("%08x"::formatted).reduce("", (s0, s1) -> s1 + s0);
	}
	
	static String toPlainString(int[] array) {
		java.math.BigInteger bi = new java.math.BigInteger(toString(array), 16);
		if (isNegative(array)) {
			bi = bi.xor(java.math.BigInteger.TWO.pow(array.length << 5).subtract(java.math.BigInteger.ONE))
				.negate().subtract(java.math.BigInteger.ONE);
		}
		return bi.toString();
	}
	
	private static String toString(long value, int idx, int length) {
		return ((".".repeat((length - idx - 1) * 8)) + "%08x" + (".".repeat(idx * 8))).formatted(value);
	}
	
	// Floating point arithmetics
	private static int[] floatSub(int[] dst, int[] x, int[] y, int exponentBits, int mantissaBits) {
		// Truth table of sign
		// : x : y : expression : action
		// | 0 | 0 | x + y      | add
		// | 0 | 1 | x - y      | sub
		// | 1 | 0 | y - x      | sub
		// | 1 | 1 | -(x + y)   | add (keep negative sign)
		int xSgn = x[x.length - 1] >>> 31;
		int ySgn = y[y.length - 1] >>> 31;
		if (xSgn == ySgn) {
			return floatAdd(dst, x, y, exponentBits, mantissaBits);
		}
		
		throw new UnsupportedOperationException();
	}
	
	/**
	 * @apiNote Requires x memory allocation
	 */
	private static int[] floatAdd(int[] dst, int[] x, int[] y, int exponentBits, int mantissaBits) {
		// Truth table of sign
		// : x : y : expression : action
		// | 0 | 0 | x + y      | add
		// | 0 | 1 | x - y      | sub
		// | 1 | 0 | y - x      | sub
		// | 1 | 1 | -(x + y)   | add (keep negative sign)
		int xSgn = x[x.length - 1] >>> 31;
		int ySgn = y[y.length - 1] >>> 31;
		if (xSgn != ySgn) {
			boolean xFirst = ySgn != 0;
			return floatSub(dst, xFirst ? x : y, xFirst ? y : x, exponentBits, mantissaBits);
		}
		
		int[] xExp = copyBits(new int[(exponentBits + 31) >> 5], x, (x.length << 5) - exponentBits - 1, exponentBits);
		int[] yExp = copyBits(new int[(exponentBits + 31) >> 5], y, (x.length << 5) - exponentBits - 1, exponentBits);
		int[] xMan = copyBits(new int[(mantissaBits + 32) >> 5], x, 0, mantissaBits);
		int[] yMan = copyBits(new int[(mantissaBits + 32) >> 5], y, 0, mantissaBits);
		
		int cmp = compareUnsigned(xExp, yExp);
//		System.out.printf("exp (x=%s, y=%s)\n", toPlainString(xExp), toPlainString(yExp));
		
		// Remove x from y
		sub(xExp, yExp, xExp);
		
		int shift = toInt(xExp);
//		System.out.printf("shift (%s) (%d)\n", cmp < 0 ? "X" : "Y", shift);
		if (cmp < 0) {
			// xExp is smallest
			setBit(xMan, xMan, mantissaBits, true);
			shiftRight(xMan, xMan,  shift);
		} else if (cmp > 0) {
			// yExp is smallest
			setBit(yMan, yMan, mantissaBits, true);
			shiftRight(yMan, yMan, -shift);
		}
		
		add(xMan, xMan, yMan);
		
		if (cmp > 0) {
			sub(yExp, yExp, xExp);
		}
		
//		System.out.println(toBitsString(xMan));
		if (getBit(xMan, mantissaBits)) {
			// Overflow exponent needs to be modified upwards
			add(yExp, yExp, 1);
			shiftRight(xMan, xMan, 1);
		}
//		System.out.println(toBitsString(xMan));
		
		setBit(dst, dst, mantissaBits + exponentBits, xSgn != 0);
		bitcopy(yExp, 0, dst, mantissaBits, exponentBits);
		bitcopy(xMan, 0, dst, 0, mantissaBits);
		
//		System.out.printf("exp: %s\n", toPlainString(yExp));
//		System.out.printf("man: %s\n", toPlainString(xMan));
		
//		throw new UnsupportedOperationException();
		return dst;
	}
	
	/**
	 * @deprecated
	 *     this is a temporary fix to allow me to continue working on this language before I've implemented
	 *     software support for IEEE 754.
	 */
	@Deprecated
	final class Temp {
		private static boolean checkIfFloat(int exponentBits, int mantissaBits) {
			if (!((exponentBits == 8 && mantissaBits == 23) || (exponentBits == 11 && mantissaBits == 52))) {
				throw new UnsupportedOperationException("float has not been implemented for other than 32 and 64 bit");
			}
			
			return (exponentBits == 8 && mantissaBits == 23);
		}
		
		private static float toFloat(int[] x) {
			return Float.intBitsToFloat(x[0]);
		}
		
		private static double toDouble(int[] x) {
			return Double.longBitsToDouble((x[0] & 0xffffffffL) | (((long) x[1]) << 32));
		}
		
		static int[] add(int[] dst, int[] x, int[] y, int exponentBits, int mantissaBits) {
			if (checkIfFloat(exponentBits, mantissaBits)) {
				dst[0] = Float.floatToRawIntBits(toFloat(x) + toFloat(y));
			} else {
				long bits = Double.doubleToRawLongBits(toDouble(x) + toDouble(y));
				dst[0] = (int) bits;
				dst[1] = (int) (bits >>> 32);
			}
			
			return dst;
		}
		
		static int[] sub(int[] dst, int[] x, int[] y, int exponentBits, int mantissaBits) {
			if (checkIfFloat(exponentBits, mantissaBits)) {
				dst[0] = Float.floatToRawIntBits(toFloat(x) - toFloat(y));
			} else {
				long bits = Double.doubleToRawLongBits(toDouble(x) - toDouble(y));
				dst[0] = (int) bits;
				dst[1] = (int) (bits >>> 32);
			}
			
			return dst;
		}
		
		static int[] mul(int[] dst, int[] x, int[] y, int exponentBits, int mantissaBits) {
			if (checkIfFloat(exponentBits, mantissaBits)) {
				dst[0] = Float.floatToRawIntBits(toFloat(x) * toFloat(y));
			} else {
				long bits = Double.doubleToRawLongBits(toDouble(x) * toDouble(y));
				dst[0] = (int) bits;
				dst[1] = (int) (bits >>> 32);
			}
			
			return dst;
		}
		
		static int[] div(int[] dst, int[] x, int[] y, int exponentBits, int mantissaBits) {
			if (checkIfFloat(exponentBits, mantissaBits)) {
				dst[0] = Float.floatToRawIntBits(toFloat(x) / toFloat(y));
			} else {
				long bits = Double.doubleToRawLongBits(toDouble(x) / toDouble(y));
				dst[0] = (int) bits;
				dst[1] = (int) (bits >>> 32);
			}
			
			return dst;
		}
		
		static int[] remainder(int[] dst, int[] x, int[] y, int exponentBits, int mantissaBits) {
			if (checkIfFloat(exponentBits, mantissaBits)) {
				dst[0] = Float.floatToRawIntBits(toFloat(x) % toFloat(y));
			} else {
				long bits = Double.doubleToRawLongBits(toDouble(x) % toDouble(y));
				dst[0] = (int) bits;
				dst[1] = (int) (bits >>> 32);
			}
			
			return dst;
		}
		
		static int[] neg(int[] dst, int[] x, int exponentBits, int mantissaBits) {
			if (checkIfFloat(exponentBits, mantissaBits)) {
				dst[0] = Float.floatToRawIntBits(-toFloat(x));
			} else {
				long bits = Double.doubleToRawLongBits(-toDouble(x));
				dst[0] = (int) bits;
				dst[1] = (int) (bits >>> 32);
			}
			
			return dst;
		}
		
		static int compare(int[] x, int[] y, int exponentBits, int mantissaBits) {
			if (checkIfFloat(exponentBits, mantissaBits)) {
				return Float.compare(toFloat(x), toFloat(y));
			} else {
				return Double.compare(toDouble(x), toDouble(y));
			}
		}
	}
	
	/**
	 * Create an array that can contain the specified amount of bits
	 *
	 * @param bits the amount of bits
	 * @return an array that can contain the specified amount of bits
	 */
	public static int[] createArray(int bits) {
		return new int[(bits + 31) >> 5];
	}
	
	/**
	 * Create an array that can contain the specified amount of bits
	 * with a default value
	 *
	 * @param value the default value
	 * @param bits the amount of bits
	 * @return an array that can contain the specified amount of bits
	 */
	public static int[] createArray(int value, int bits) {
		int[] array = createArray(bits);
		
		// If the value is negative fill the entire array
		if (value < 0) Arrays.fill(array, -1);
		array[0] = value;
		
		return array;
	}
}
