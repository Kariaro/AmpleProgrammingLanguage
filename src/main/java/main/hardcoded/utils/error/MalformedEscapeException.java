package hardcoded.utils.error;

public class MalformedEscapeException extends RuntimeException {
	private static final long serialVersionUID = 5574011843596895252L;
	private final String message;
	
	public MalformedEscapeException(String message) {
		this.message = message;
	}
	
	@Override
	public String getMessage() {
		return message;
	}
}
