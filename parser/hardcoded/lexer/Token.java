package hardcoded.lexer;

import java.util.Objects;

/**
 * This is a dual linked list.
 * 
 * @author HardCoded
 */
public class Token {
	// TODO: Maybe use the words trailing? leading?
	protected final String value;
	protected String group;
	protected Token prev;
	protected Token next;
	
	protected int line;
	protected int column;
	protected int fileOffset;
	
	protected Token(String value, String group) {
		this.value = value;
		this.group = group;
	}
	
	public int line() {
		return line;
	}
	
	public int column() {
		return column;
	}
	
	/**
	 * Get the offset from the start of the file that this token was read from.
	 */
	public int fileOffset() {
		return fileOffset;
	}
	
	public String group() {
		return group;
	}
	
	public String value() {
		return value;
	}
	
	
	/**
	 * Get the next token.
	 */
	public Token next() {
		return next;
	}
	
	/**
	 * Get the nth-next token.
	 * @param count a value of one will give the same result as calling {@link #next()}
	 * @return the nth-next token or null if the count was greater than the number of remaining tokens
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
	 * Get the previous token.
	 */
	public Token prev() {
		
		return prev;
	}
	
	/**
	 * Get the nth-previous token.
	 * @param count a value of one will give the same result as calling {@link #prev()}
	 * @return the nth-previous token or null if the count was greater than the length of the chain
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
	 * Get the relative index of the passed token to this token.
	 * @return -1 if the token was not found in the chain
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
	 * Get the number of remaining tokens in the chain.
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
	
	public boolean equals(Object obj) {
		if(obj instanceof String) return obj.equals(value);
		return this == obj;
	}
	
	public boolean groupEquals(String group) {
		return Objects.equals(this.group, group);
	}
	
	public boolean valueEquals(String value) {
		return Objects.equals(this.value, value);
	}
	
	public boolean equals(String group, String value) {
		return groupEquals(group) && valueEquals(value);
	}
	
	public String toString() {
		return value;
	}
	
	/**
	 * Returns the values of the next count amount of tokens concatinated together.
	 * @param count a value of one will give the same result as calling {@link #toString()}
	 * @return returns a string of the concatinated tokens.
	 */
	public String toString(int count) {
		return toString("", count);
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
		
		int max = Math.min(remaining(), count) + 1;
		for(int i = 0; i < max; i++) {
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
