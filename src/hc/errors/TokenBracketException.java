package hc.errors;

public class TokenBracketException extends TokenException {
	private static final long serialVersionUID = 1L;
	
	public TokenBracketException() {
		super(null);
	}
	
	public TokenBracketException(String message) {
		super(message);
	}
}
