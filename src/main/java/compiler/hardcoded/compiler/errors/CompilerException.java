package hardcoded.compiler.errors;

/**
 * This is a custom exception thrown when the compiler has
 * found errors in the code.
 * 
 * @author HardCoded
 */
public class CompilerException extends Error {
	private static final long serialVersionUID = -3780487404643365332L;
	
	/**
	 * Construct a new compiler exception with no message.
	 */
	public CompilerException() {
		super("");
	}
	
	/**
	 * Construct a new compiler exception with a message.
	 * @param	message
	 */
	public CompilerException(String message) {
		super(message);
	}
}
