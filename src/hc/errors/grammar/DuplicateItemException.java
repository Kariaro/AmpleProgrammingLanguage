package hc.errors.grammar;

public class DuplicateItemException extends GrammarException {
	private static final long serialVersionUID = 4328459110279355950L;
	
	public DuplicateItemException(String message) {
		super(message);
	}
}
