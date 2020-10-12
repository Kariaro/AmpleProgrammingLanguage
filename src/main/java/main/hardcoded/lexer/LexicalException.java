package hardcoded.lexer;

public class LexicalException extends RuntimeException {
	private static final long serialVersionUID = 3119831781029246673L;
	
	private final String message;
	public LexicalException(String message) {
		this.message = message;
	}
	
	public LexicalException(TokenizerSymbol symbol, String message) {
		this.message = "(line:" + symbol.line() + " column:" + symbol.column() + ") " + message;
	}
	
	public String getMessage() {
		return message;
	}
}
