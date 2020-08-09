package hc.errors.lexer;

public class EscapedCharacterException extends LexicalException {
	private static final long serialVersionUID = 3119831781029246673L;
	
	public EscapedCharacterException(String message) {
		super(message);
	}
}
