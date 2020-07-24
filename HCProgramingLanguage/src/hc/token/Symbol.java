package hc.token;

public class Symbol {
	protected final String value;
	protected Symbol prev;
	protected Symbol next;
	
	protected int line;
	protected int column;
	
	protected Symbol(String value) {
		this.value = value;
	}
	
	/**
	 * Get the line that this symbol was read on.
	 */
	public int getLineIndex() {
		return line;
	}
	
	/**
	 * Get the column index of this symbol.
	 */
	public int getColumnIndex() {
		return column;
	}
	
	/**
	 * Returns the next symbol.
	 */
	public Symbol next() {
		return next;
	}
	
	/**
	 * Returns the previous symbol.
	 */
	public Symbol prev() {
		return prev;
	}
	
	/**
	 * Returns the nth-next symbol.
	 * @param count a value of one will give the same result as calling {@link #next()}
	 * @return returns the nth-next symbol or null if the count was greater than the length of the chain
	 */
	public Symbol next(int count) {
		Symbol symbol = this;
		for(int i = 0; i < count; i++) {
			symbol = symbol.next;
			if(symbol == null) return null;
		}
		return symbol;
	}
	
	/**
	 * Returns the nth-previous symbol.
	 * @param count a value of one will give the same result as calling {@link #prev()}
	 * @return returns the nth-previous symbol or null if the count was greater than the length of the chain
	 */
	public Symbol prev(int count) {
		Symbol symbol = this.prev;
		for(int i = 0; i < count; i++) {
			symbol = symbol.prev;
			if(symbol == null) return null;
		}
		return symbol;
	}
	
	/**
	 * Returns the relative index of the given symbol.
	 * @return returns -1 if the symbol was not found in the chain
	 */
	public int indexOf(Symbol symbol) {
		if(symbol == null) return -1;
		if(symbol == this) return 0;
		Symbol s = this;
		
		int index = 0;
		while(s.next != null) {
			s = s.next;
			index++;
			if(symbol == s) return index;
		}
		
		return -1;
	}
	
	/**
	 * Returns the remaining number of symbols in the chain.
	 */
	public int remaining() {
		Symbol symbol = this;
		int index = 0;
		while(symbol.next != null) {
			symbol = symbol.next;
			index++;
		}
		return index;
	}
	
	/**
	 * Checks if the string is equal to the value held by this symbol.
	 */
	public boolean equals(Object object) {
		if(object instanceof String) {
			return ((String)object).equals(value);
		}
		return this == object;
	}
	
	/**
	 * Clone this symbol. This will not copy the next and previous values.
	 */
	public Symbol clone() {
		return new Symbol(value);
	}
	
	/**
	 * Clone this symbol and count symbols after this one.
	 * @param count  a value of one will give the same result as calling {@link #clone()}
	 * @return a cloned chain of count symbols
	 */
	public Symbol clone(int count) {
		Symbol start = clone();
		Symbol symbol = start;
		Symbol s = this;
		for(int i = 0; i < count; i++) {
			s = s.next;
			if(s == null) return start;
			
			Symbol next = s.clone();
			next.prev = symbol;
			symbol.next = next;
			symbol = next;
		}
		
		return start;
	}
	
	/**
	 * Returns the value that this symbol holds.
	 */
	public String toString() {
		return value;
	}
	
	/**
	 * Returns the values of the next count amount of symbols concatinated together.
	 * @param count a value of one will give the same result as calling {@link #toString()}
	 * @return returns a string of the concatinated symbols.
	 */
	public String toString(int count) {
		StringBuilder sb = new StringBuilder();
		Symbol symbol = this;
		for(int i = 0; i < count; i++) {
			sb.append(symbol.value);
			symbol = symbol.next;
			if(symbol == null) break;
		}
		return sb.toString();
	}
	
	/**
	 * Returns the values of the next count amount of symbols concatinated together.
	 * @param separator the string that will separate the concatinated symbols.
	 * @param count a value of one will give the same result as calling {@link #toString()}
	 * @return returns a string of the concatinated symbols.
	 */
	public String toString(CharSequence separator, int count) {
		StringBuilder sb = new StringBuilder();
		Symbol symbol = this;
		for(int i = 0; i < count; i++) {
			sb.append(symbol.value).append(separator);
			symbol = symbol.next;
			if(symbol == null) break;
		}
		
		if(sb.length() > separator.length()) {
			sb.delete(sb.length() - separator.length(), sb.length());
		}
		
		return sb.toString();
	}
}
