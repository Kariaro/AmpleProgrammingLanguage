package hardcoded.lexer;

/**
 * 
 * @author HardCoded
 */
public class Token {
	protected final String value;
	protected String group;
	protected Token prev;
	protected Token next;
	
	protected int line;
	protected int column;
	
	protected Token(String value, String group) {
		this.value = value;
		this.group = group;
	}
	
	/**
	 * Get the group that this token belongs to.
	 */
	public String getGroup() {
		return group;
	}
	
	/**
	 * Get the line that this token was read on.
	 */
	public int getLineIndex() {
		return line;
	}
	
	/**
	 * Get the column index of this token.
	 */
	public int getColumnIndex() {
		return column;
	}
	
	/**
	 * Returns the next token.
	 */
	public Token next() {
		return next;
	}
	
	/**
	 * Returns the previous token.
	 */
	public Token prev() {
		return prev;
	}
	
	/**
	 * Returns the nth-next token.
	 * @param count a value of one will give the same result as calling {@link #next()}
	 * @return returns the nth-next token or null if the count was greater than the length of the chain
	 */
	public Token next(int count) {
		Token token = this;
		for(int i = 0; i < count; i++) {
			token = token.next;
			if(token == null) return null;
		}
		return token;
	}
	
	/**
	 * Returns the nth-previous token.
	 * @param count a value of one will give the same result as calling {@link #prev()}
	 * @return returns the nth-previous token or null if the count was greater than the length of the chain
	 */
	public Token prev(int count) {
		Token token = this.prev;
		for(int i = 0; i < count; i++) {
			token = token.prev;
			if(token == null) return null;
		}
		return token;
	}
	
	/**
	 * Returns the relative index of the given token.
	 * @return returns -1 if the token was not found in the chain
	 */
	public int indexOf(Token token) {
		if(token == null) return -1;
		if(token == this) return 0;
		Token t = this;
		
		int index = 0;
		while(t.next != null) {
			t = t.next;
			index++;
			if(token == t) return index;
		}
		
		return -1;
	}
	
	/**
	 * Returns the remaining number of tokens in the chain.
	 */
	public int remaining() {
		Token token = this;
		int index = 0;
		while(token.next != null) {
			token = token.next;
			index++;
		}
		return index;
	}
	
	/**
	 * Checks if the string is equal to the value held by this token.
	 */
	public boolean equals(Object object) {
		if(object instanceof String) {
			return ((String)object).equals(value);
		}
		return this == object;
	}
	
	/**
	 * Clone this token. This will not copy the next and previous values.
	 * @deprecated This function could be removed in the future.
	 */
	@Deprecated
	public Token clone() {
		return new Token(value, group);
	}
	
	/**
	 * Clone this token and count tokens after this one.
	 * @param count  a value of one will give the same result as calling {@link #clone()}
	 * @return a cloned chain of count tokens
	 * 
	 * @deprecated This function could be removed in the future.
	 */
	@Deprecated
	public Token clone(int count) {
		Token start = clone();
		Token token = start;
		Token t = this;
		for(int i = 0; i < count; i++) {
			t = t.next;
			if(t == null) return start;
			
			Token next = t.clone();
			next.prev = token;
			token.next = next;
			token = next;
		}
		
		return start;
	}
	
	/**
	 * Returns the value that this token holds.
	 */
	public String toString() {
		return value;
	}
	
	/**
	 * Returns the values of the next count amount of tokens concatinated together.
	 * @param count a value of one will give the same result as calling {@link #toString()}
	 * @return returns a string of the concatinated tokens.
	 */
	public String toString(int count) {
		StringBuilder sb = new StringBuilder();
		Token token = this;
		for(int i = 0; i < count; i++) {
			sb.append(token.value);
			token = token.next;
			if(token == null) break;
		}
		return sb.toString();
	}
	
	/**
	 * Returns the values of the next count amount of tokens concatinated together.
	 * @param separator the string that will separate the concatinated tokens.
	 * @param count a value of one will give the same result as calling {@link #toString()}
	 * @return returns a string of the concatinated tokens.
	 */
	public String toString(CharSequence separator, int count) {
		StringBuilder sb = new StringBuilder();
		Token token = this;
		for(int i = 0; i < count; i++) {
			sb.append(token.value).append(separator);
			token = token.next;
			if(token == null) break;
		}
		
		if(sb.length() > separator.length()) {
			sb.delete(sb.length() - separator.length(), sb.length());
		}
		
		return sb.toString();
	}
}
