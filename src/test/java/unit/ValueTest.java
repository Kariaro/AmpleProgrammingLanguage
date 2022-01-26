package unit;

import static me.hardcoded.compiler.numbers.Value.*;

import org.junit.Test;

public final class ValueTest {
	@Test
	public static void value_add() {
		Assertions.assertEquals(
			idouble(Math.PI).add(ifloat(Math.E)).doubleValue(),
			Math.PI + (float)Math.E,
			"IDouble and IFloat addition did not match"
		);
		
		Assertions.assertEquals(
			qword(-532).add(udword(-1)).longValue(),
			-532L + 0xffffffffL,
			"Qword and UDword addition did not match"
		);
		
		Assertions.assertEquals(
			ubyte(0xff).add(ubyte(0xff)).longValue(),
			0xfe,
			"UByte and UByte addition did not match"
		);
		
	}
	
	public static void test() {
		value_add();
	}
	
	public static void main(String[] args) {
		test();
	}
}
