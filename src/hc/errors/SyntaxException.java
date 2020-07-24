package hc.errors;

public class SyntaxException extends TokenException {
	private static final long serialVersionUID = 1L;
	
	public SyntaxException() {
		super(null);
	}
	
	public SyntaxException(String message) {
		super(message);
	}
}
