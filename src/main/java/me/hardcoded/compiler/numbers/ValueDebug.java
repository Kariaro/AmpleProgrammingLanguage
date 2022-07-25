package me.hardcoded.compiler.numbers;

import java.math.BigDecimal;
import java.math.MathContext;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;

public class ValueDebug {
	private static final DecimalFormat FORMAT;
	private static final MathContext CONTEXT = new MathContext(4096);
	static {
		DecimalFormatSymbols symbols = new DecimalFormatSymbols();
		symbols.setDecimalSeparator('.');
		symbols.setMinusSign('-');
		symbols.setInfinity("Inf");
		symbols.setNaN("NaN");
		FORMAT = new DecimalFormat("#.################################", symbols);
	}
	
	static Value fromHex(String str, int bits) {
		int[] array = new int[(bits + 31) >> 5];
		final int len = Math.min(array.length, (str.length() + 7) >> 3);
		
		for (int i = 0; i < len; i++) {
			int takeEnd = str.length() - i * 8;
			String section = str.substring(Math.max(0, takeEnd - 8), takeEnd);
			array[i] = (int) Long.parseLong(section, 16);
		}
		
		return new Value(array, bits, Value.Type.UNSIGNED);
	}
	
	static void dumpFloat(float value) {
		int bits = Float.floatToRawIntBits(value);
		int sign = (bits & 0x80000000) >>> 31;
		int exp  = (bits & 0x7f800000) >>> 23;
		int man  = (bits & 0x007fffff);
		BigDecimal decimal = BigDecimal
			.valueOf((1 << 23) | man)
			.multiply(BigDecimal
				.valueOf(2)
				.pow(exp - 127 - 23, CONTEXT)
			);
		decimal = sign != 0 ? decimal.negate() : decimal;
		
		System.out.println("=".repeat(100));
		System.out.printf("sign: %d, exp: %d, man: %d\n", sign, exp, man);
		System.out.println(FORMAT.format(value));
		System.out.println(decimal.toPlainString());
	}
	
	static void dumpDouble(double value) {
		long bits = Double.doubleToRawLongBits(value);
		long sign = (bits & 0x80000000_00000000L) >>> 63;
		long exp  = (bits & 0x7ff00000_00000000L) >>> 52;
		long man  = (bits & 0x000fffff_ffffffffL);
		BigDecimal decimal = BigDecimal
			.valueOf((1L << 52) | man)
			.multiply(BigDecimal
				.valueOf(2)
				.pow((int) (exp - 1023 - 52), CONTEXT)
			);
		decimal = sign != 0 ? decimal.negate() : decimal;
		
		System.out.println("=".repeat(100));
		System.out.printf("sign: %d, exp: %d, man: %d\n", sign, exp, man);
		System.out.println(FORMAT.format(value));
		System.out.println(decimal.toPlainString());
	}
	
	static void testFloat(float value) {
		int bits = Float.floatToRawIntBits(value);
		int exp  = (bits & 0x7f800000) >>> 23;
		BigDecimal pow = BigDecimal.valueOf(2).pow(exp - 127 - 23, CONTEXT);
		String min = BigDecimal.valueOf(0xffffff).multiply(pow).toPlainString();
		String max = BigDecimal.valueOf(0x800000).multiply(pow).toPlainString();
		System.out.println("=".repeat(100));
		System.out.printf("min: %s\nmax: %s\n", min, max);
	}
	
	public static void main(String[] args) {
		float ts;
		ts = (float) Math.PI;
		ts = 0.1f;
		// ts = 0.00000000000000000000000000000000000000000000000000000000000000001012312312839128039128312;
		// ts = 0.0000000000000000000000000000000000000123;
		
		Value a = Value.valueOf(10F);
		Value b = Value.valueOf(1234567890F);
		
		try {
			System.out.printf("hex: %s, (%s)\n", a, a.toString(10));
			System.out.printf("hex: %s, (%s)\n", b, b.toString(10));
			Value c = a.add(b);
			System.out.printf("hex: %s, (%s)\n", c, c.toString(10));
			Value d = c.cast(64, Value.Type.SIGNED);
			System.out.printf("hex: %s, (%s)\n", d, d.toString(10));
			Value e = Value.valueOf(1234567890, 64, Value.Type.SIGNED).cast(64, Value.Type.FLOATING);
			System.out.printf("hex: %s, (%s)\n", e, e.toString(10));
			Value f = Value.valueOf(1234567890.0).rawCast(64, Value.Type.UNSIGNED);
			System.out.printf("hex: %s, (%s)\n", f, f.toString(10));
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		dumpFloat(10F);
		dumpFloat(20F);
		dumpFloat(30F);
	}
}
