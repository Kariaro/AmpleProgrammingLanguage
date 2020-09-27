package hardcoded;

public class Test2 {
	public static void main(String[] args) {
		B[] array = new B[1];
		testing(array);
	}
	
//	public static boolean testing(Comparable<?>[] test) {
//		return false;
//	}
	
	public static boolean testing(A[] test) {
		return false;
	}
	
	private static class A {
		
	}
	
	private static class B extends A {
		
	}
}
