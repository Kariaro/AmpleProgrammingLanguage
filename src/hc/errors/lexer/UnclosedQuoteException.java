package hc.errors.lexer;

public class UnclosedQuoteException extends LexicalException {
	private static final long serialVersionUID = 3119831781029246673L;
	
	public UnclosedQuoteException(String message) {
		super(message);
	}
}
