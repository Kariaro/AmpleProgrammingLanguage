package hardcoded.errors;

/**
 * This is a custom exception thrown when the compiler has
 * found errors in the code.
 * 
 * @author HardCoded
 */
public class CompilerException extends RuntimeException {
	private static final long serialVersionUID = -3780487404643365332L;
	
	private final String message;
	
	/**
	 * Construct a new compiler exception with no message.
	 */
	public CompilerException() {
		this.message = "";
	}
	
	/**
	 * Construct a new compiler exception with a message.
	 * @param message
	 */
	public CompilerException(String message) {
		this.message = message;
	}
	
	@Override
	public String getMessage() {
		return message;
	}
}
