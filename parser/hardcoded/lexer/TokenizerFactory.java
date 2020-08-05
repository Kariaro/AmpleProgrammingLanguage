package hardcoded.lexer;

public class TokenizerFactory {
	public TokenizerFactory() {
		
	}
	
	public static TokenizerBuilder create() {
		return new TokenizerBuilder();
	}
}
