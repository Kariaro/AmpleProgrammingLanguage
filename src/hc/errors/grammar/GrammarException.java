package hc.errors.grammar;

public class GrammarException extends RuntimeException {
	private static final long serialVersionUID = 3119831781029246673L;
	
	private final String message;
	public GrammarException(String message) {
		this.message = message;
	}
	
	public String getMessage() {
		return message;
	}
}
