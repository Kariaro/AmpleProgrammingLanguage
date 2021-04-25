package com.hardcoded.utils;

/**
 * A util assertion class
 * 
 * @author HardCoded
 * @since 0.2.0
 */
public final class Require {
	public static final boolean equal(long a, long b) {
		if(a != b) throw new AssertException("a != b");
		return true;
	}
	
	public static class AssertException extends RuntimeException {
		private static final long serialVersionUID = 2537918261267323888L;
		
		public AssertException(String message) {
			super(message);
		}
	}
}
