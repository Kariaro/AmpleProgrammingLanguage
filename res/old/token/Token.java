package hc.token;

public class Token {
	private TokenType type = TokenType.TOKEN;
	protected String value;
	protected Token prev;
	protected Token next;
	
	public Token() {
		
	}
	
	public Object getValue() {
		return value;
	}
	
	public String getStringValue() {
		return value;
	}
	
	/**
	 * Get the previous token.
	 */
	public Token previous() { return prev; }
	
	/**
	 * Get the next token.
	 */
	public Token next() { return next; }
	
	/**
	 * Get the nth token.
	 */
	public Token next(int skip) {
		Token token = this;
		for(int i = 0; i < skip; i++) token = token.next;
		return token;
	}
	
	/**
	 * Check if the next token exists.
	 */
	public boolean hasNext() { return next != null; }
	
	/**
	 * Check if there is atleast {@literal count} remaining tokens.
	 * @param count
	 */
	public boolean hasNext(int count) {
		if(next == null) return count == 0;
		Token token = next;
		for(int i = 0; i < count - 1; i++) {
			if(!token.hasNext()) return false;
			token = token.next;
		}
		return true;
	}
	
	public TokenType getType() {
		return type;
	}
	
	public final Token setType(TokenType type) {
		if(type == null) type = TokenType.TOKEN;
		this.type = type;
		return this;
	}
	
	/**
	 * Get the tokens index relative to this token.
	 * @return -1 if the token was not found
	 */
	public int indexOf(Token token) {
		if(this == token) return 0;
		
		Token t = this;
		int index = 0;
		while(t.hasNext()) {
			t = t.next();
			index++;
			if(t == token) return index;
		} while(t.hasNext());
		
		return -1;
	}
	
	/**
	 * Try convert this token to a tokengroup.
	 */
	public TokenGroup toGroup() throws ClassCastException {
		return (TokenGroup)this;
	}
	
	/**
	 * This will only clone this token and remove all connected tokens.
	 */
	public Token clone() {
		Token token = new Token();
		token.value = value;
		return token;
	}
	
	public int remainingTokens() {
		if(next == null) return 0;
		Token t = this;
		int size = 0;
		while(t.hasNext()) {
			t = t.next();
			size++;
		} while(t.hasNext());
		return size + 1;
	}
	
	public String toString() {
		return value;
	}
	
	/**
	 * Combines a chain of tokens together and returns their combined value.
	 * @param count the amount of tokens to concatenate
	 */
	public String toString(int count) {
		StringBuilder sb = new StringBuilder();
		Token token = this;
		for(int i = 0; i < count; i++) {
			if(token == null) break;
			
			sb.append(token.getStringValue());
			token = token.next;
		}
		
		return sb.toString();
	}
	
	public String toSimpleString() {
		String value = this.toString();
		if(type.isQuoted()) return value;
		return value == null ? null:value.toString().replaceAll("[ \t\n\r]+", " ");
	}
	
	public String toSimpleString(int count) {		
		String value = this.toString(count);
		if(type.isQuoted()) return value;
		return value.replaceAll("[ \t\n\r]+", " ");
	}
}
