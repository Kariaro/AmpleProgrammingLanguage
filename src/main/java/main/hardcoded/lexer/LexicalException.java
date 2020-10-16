package hardcoded.lexer;

/**
 * This {@code LexicalException} is thrown when a lexer class finds an invalid
 * character or string when parsing a tokenizer.
 *  
 * @author HardCoded
 */
public class LexicalException extends RuntimeException {
	private static final long serialVersionUID = 3119831781029246673L;
	
	private final String message;
	public LexicalException(String message) {
		this.message = message;
	}
	
	public LexicalException(Token symbol, String message) {
		this.message = "(line:" + symbol.line + " column:" + symbol.column + ") " + message;
	}
	
	public String getMessage() {
		return message;
	}
}
