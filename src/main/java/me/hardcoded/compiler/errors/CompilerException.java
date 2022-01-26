package me.hardcoded.compiler.errors;

/**
 * This is a custom exception thrown when the compiler has
 * found errors in the code.
 * 
 * @author HardCoded
 */
public class CompilerException extends Error {
	/**
	 * Construct a new compiler exception with no message.
	 */
	public CompilerException() {
		super("");
	}
	
	/**
	 * Construct a new compiler exception with a message.
	 * @param message
	 */
	public CompilerException(String message) {
		super(message);
	}
	
	/**
	 * Construct a new compiler exception with a formatted message.
	 * @param format
	 * @param args
	 */
	public CompilerException(String format, Object... args) {
		super(format.formatted(args));
	}
}
