package hardcoded.utils.error;

public class MalformedEscapeException extends RuntimeException {
	private static final long serialVersionUID = 5574011843596895252L;
	
	public MalformedEscapeException(String message) {
		super(message);
	}
}
