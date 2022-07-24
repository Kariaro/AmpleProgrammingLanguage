package unit;

import me.hardcoded.compiler.numbers.Value;
import org.junit.Assert;
import org.junit.Test;

public final class ValueTest {
	private static final Value.Type[] INTEGER_TYPES = {
		Value.Type.SIGNED,
		Value.Type.UNSIGNED
	};
	
	private static final long[] INTEGER_ADD = {
		0xffffffff_ffffffffL, 0x00000000_00000001L, 0x00000000_00000000L,
		0x00000000_00000001L, 0xffffffff_ffffffffL, 0x00000000_00000000L,
		0xffffffff_00000000L, 0x00000000_ffffffffL, 0xffffffff_ffffffffL,
	};
	
	private static final long[] INTEGER_SUB = {
		0x00000000_00000000L, 0x00000000_00000001L, 0xffffffff_ffffffffL,
		0x00000000_00000000L, 0xffffffff_ffffffffL, 0x00000000_00000001L,
		0xffffffff_00000000L, 0x00000000_ffffffffL, 0xfffffffe_00000001L,
	};
	
	private static final long[] INTEGER_MUL = {
		0xffffffff_ffffffffL, 0x00000000_00000000L, 0x00000000_00000000L,
		0x43234234_00000001L, 0x00000000_23400000L, 0xa9000000_23400000L,
		0x00000258_cf1f764dL, 0x00000000_0000000aL, 0x00001778_173a9f02L,
	};
	
	private static final long[] UNSIGNED_DIV_REM = {
		0xa9000000_23400000L, 0x00123d87_6da876daL, 0x00000000_00000943L, 0x001022b3_83fb3af2L,
		0xa9000000_23400000L, 0x80123d87_6da876daL, 0x00000000_00000001L, 0x28edc278_b5978926L,
		0xffffffff_ffffffffL, 0x43234234_00000001L, 0x00000000_00000003L, 0x36963963_fffffffcL,
		0xffffffff_ffffffffL, 0xffffffff_ffffffffL, 0x00000000_00000001L, 0x00000000_00000000L,
	};
	
	private static final long[] SIGNED_DIV_REM = {
		0xa9000000_23400000L, 0x00123d87_6da876daL, 0xffffffff_fffffb3bL, 0xffff76ee_27beddc2L,
		0xa9000000_23400000L, 0x80123d87_6da876daL, 0x00000000_00000000L, 0xa9000000_23400000L,
		0x80123d87_6da876daL, 0xa9000000_23400000L, 0x00000000_00000001L, 0xd7123d87_4a6876daL,
		0x7f123d87_6da876daL, 0xfffff423_23400000L, 0xffffffff_fff549c8L, 0x000000c6_a3a876daL,
		0xffffffff_ffffffffL, 0x00000000_00000001L, 0xffffffff_ffffffffL, 0x00000000_00000000L,
		0xffffffff_ffffffffL, 0xffffffff_ffffffffL, 0x00000000_00000001L, 0x00000000_00000000L,
		0x00000000_00000001L, 0xffffffff_ffffffffL, 0xffffffff_ffffffffL, 0x00000000_00000000L,
	};
	
	private static final long[] INTEGER_COMPARE = {
		0xa9000000_23400000L, 0x00123d87_6da876daL,  1, -1,
		0xa9000000_23400000L, 0x80123d87_6da876daL,  1,  1,
		0x80123d87_6da876daL, 0xa9000000_23400000L, -1, -1,
		0x7f123d87_6da876daL, 0xfffff423_23400000L, -1,  1,
		0xffffffff_ffffffffL, 0x00000000_00000001L,  1, -1,
		0xffffffff_ffffffffL, 0xffffffff_ffffffffL,  0,  0,
		0x00000000_00000001L, 0xffffffff_ffffffffL, -1,  1,
	};
	
	/**
	 * Integer {@code add}, {@code sub}, {@code mul} are not sign dependant and will
	 * work the same regardless of sign
	 */
	@Test
	public void testIntegerAddSubMul() {
		Value a, b, c, d;
		for (Value.Type type : INTEGER_TYPES) {
			for (int i = 0; i < INTEGER_ADD.length; i += 3) {
				a = Value.valueOf(INTEGER_ADD[i    ], 64, type);
				b = Value.valueOf(INTEGER_ADD[i + 1], 64, type);
				c = Value.valueOf(INTEGER_ADD[i + 2], 64, type);
				d = a.copy().add(b);
				Assert.assertTrue("%s + %s = %s (expected: %s)".formatted(a, b, d, c), d.sub(c).isZero());
			}
			
			for (int i = 0; i < INTEGER_SUB.length; i += 3) {
				a = Value.valueOf(INTEGER_SUB[i    ], 64, type);
				b = Value.valueOf(INTEGER_SUB[i + 1], 64, type);
				c = Value.valueOf(INTEGER_SUB[i + 2], 64, type);
				d = a.copy().sub(b);
				Assert.assertTrue("%s - %s = %s (expected: %s)".formatted(a, b, d, c), d.sub(c).isZero());
			}
			
			for (int i = 0; i < INTEGER_MUL.length; i += 3) {
				a = Value.valueOf(INTEGER_MUL[i    ], 64, type);
				b = Value.valueOf(INTEGER_MUL[i + 1], 64, type);
				c = Value.valueOf(INTEGER_MUL[i + 2], 64, type);
				d = a.copy().mul(b);
				Assert.assertTrue("%s * %s = %s (expected: %s)".formatted(a, b, d, c), d.sub(c).isZero());
			}
		}
	}
	
	@Test
	public void testIntegerDiv() {
		Value a, b, c, d, e;
		for (int i = 0; i < UNSIGNED_DIV_REM.length; i += 4) {
			a = Value.valueOf(UNSIGNED_DIV_REM[i    ], 64, Value.Type.UNSIGNED);
			b = Value.valueOf(UNSIGNED_DIV_REM[i + 1], 64, Value.Type.UNSIGNED);
			c = Value.valueOf(UNSIGNED_DIV_REM[i + 2], 64, Value.Type.UNSIGNED);
			d = Value.valueOf(UNSIGNED_DIV_REM[i + 3], 64, Value.Type.UNSIGNED);
			e = a.copy().div(b);
			Assert.assertTrue("unsigned: %s / %s = %s (expected: %s)".formatted(a, b, e, c), e.sub(c).isZero());
			e = a.copy().remainder(b);
			Assert.assertTrue("unsigned: %s %% %s = %s (expected: %s)".formatted(a, b, e, d), e.sub(d).isZero());
		}
		
		for (int i = 0; i < SIGNED_DIV_REM.length; i += 4) {
			a = Value.valueOf(SIGNED_DIV_REM[i    ], 64, Value.Type.SIGNED);
			b = Value.valueOf(SIGNED_DIV_REM[i + 1], 64, Value.Type.SIGNED);
			c = Value.valueOf(SIGNED_DIV_REM[i + 2], 64, Value.Type.SIGNED);
			d = Value.valueOf(SIGNED_DIV_REM[i + 3], 64, Value.Type.SIGNED);
			e = a.copy().div(b);
			Assert.assertTrue("signed: %s / %s = %s (expected: %s)".formatted(a, b, e, c), e.sub(c).isZero());
			e = a.copy().remainder(b);
			Assert.assertTrue("signed: %s %% %s = %s (expected: %s)".formatted(a, b, e, d), e.sub(d).isZero());
		}
	}
	
	@Test
	public void testIntegerCompare() {
		Value a, b;
		for (int i = 0; i < INTEGER_COMPARE.length; i += 4) {
			a = Value.valueOf(INTEGER_COMPARE[i    ], 64, Value.Type.UNSIGNED);
			b = Value.valueOf(INTEGER_COMPARE[i + 1], 64, Value.Type.UNSIGNED);
			long c = INTEGER_COMPARE[i + 2];
			int d = a.copy().compare(b);
			Assert.assertEquals("unsigned: %s <=> %s = %s (expected: %s)".formatted(a, b, d, c), d, c);
		}
		
		for (int i = 0; i < INTEGER_COMPARE.length; i += 4) {
			a = Value.valueOf(INTEGER_COMPARE[i    ], 64, Value.Type.SIGNED);
			b = Value.valueOf(INTEGER_COMPARE[i + 1], 64, Value.Type.SIGNED);
			long c = INTEGER_COMPARE[i + 3];
			int d = a.copy().compare(b);
			Assert.assertEquals("signed: %s <=> %s = %s (expected: %s)".formatted(a, b, d, c), d, c);
		}
	}
	
	@Test
	public void testToString() {
		Assert.assertEquals("-7fedc27892578926", Value.valueOf(0x80123d87_6da876daL, 64, Value.Type.SIGNED).toString(16));
		Assert.assertEquals("80123d876da876da", Value.valueOf(0x80123d87_6da876daL, 64, Value.Type.UNSIGNED).toString(16));
	}
}
