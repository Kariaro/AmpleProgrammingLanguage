package hc.token;

public class Symbol {
	protected final String value;
	protected Symbol prev;
	protected Symbol next;
	
	protected Symbol(String value) {
		this.value = value;
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
	 * @param count a value of zero will give the same result as calling {@link #next()}
	 * @return returns the nth-next symbol or null if the count was greater than the length of the chain
	 */
	public Symbol next(int count) {
		Symbol symbol = this;
		for(int i = 0; i <= count; i++) {
			symbol = symbol.next;
			if(symbol == null) return null;
		}
		return symbol;
	}
	
	/**
	 * Returns the nth-previous symbol.
	 * @param count a value of zero will give the same result as calling {@link #prev()}
	 * @return returns the nth-previous symbol or null if the count was greater than the length of the chain
	 */
	public Symbol prev(int count) {
		Symbol symbol = this.prev;
		for(int i = 0; i <= count; i++) {
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
	 * Returns the value that this symbol holds.
	 */
	public String toString() {
		return value;
	}
	
	/**
	 * Returns the values of the next count amount of symbols concatinated together.
	 * @param count a value of zero will give the same result as calling {@link #toString()}
	 * @return the amount of symbols to be concatenated
	 */
	public String toString(int count) {
		StringBuilder sb = new StringBuilder();
		Symbol symbol = this;
		for(int i = 0; i <= count; i++) {
			sb.append(symbol.value);
			symbol = symbol.next;
			if(symbol == null) break;
		}
		return sb.toString();
	}
}
