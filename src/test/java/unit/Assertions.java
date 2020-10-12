package unit;

import java.util.Objects;

public class Assertions {
	private Assertions() {}
	
	public static boolean assertEquals(Object a, Object b) {
		return assertEquals(a, b, null);
	}
	
	public static boolean assertEquals(Object a, Object b, String message) {
		if(!Objects.equals(a, b))
			throw new AssertionError(message + " (" + a + ":" + b + ")");
		
		return true;
	}
	
	public static boolean assertEquals(double a, double b) { return assertEquals(a == b, null); }
	public static boolean assertEquals(double a, double b, String message) {
		return assertEquals(a == b, message);
	}
	
	public static boolean assertEquals(long a, long b) { return assertEquals(a == b, null); }
	public static boolean assertEquals(long a, long b, String message) {
		return assertEquals(a == b, message);
	}
	
	public static boolean assertEquals(boolean b, String message) {
		if(!b) throw new AssertionError(message);
		return true;
	}
}
