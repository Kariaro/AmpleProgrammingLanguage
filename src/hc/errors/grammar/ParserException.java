package hc.errors.grammar;

public class ParserException extends RuntimeException {
	private static final long serialVersionUID = 3119831781029246673L;
	
	private final String message;
	public ParserException(String message) {
		this.message = message;
	}
	
	public String getMessage() {
		return message;
	}
}
