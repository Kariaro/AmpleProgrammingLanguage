package hardcoded.compiler.context;

import java.util.LinkedList;
import java.util.List;
import java.util.Objects;

import hardcoded.compiler.errors.CompilerException;
import hardcoded.lexer.Token;

public class Lang {
	private final List<Token> list;
	private int index;
	private LinkedList<Integer> markedTokens;
	private LinkedList<Integer> marked;
	
	// These are used to remove some NPE
	// The start and end will be replaced by these
	// tokens to ensure non null tokens.
	private final Token START;
	private final Token END;
	
	private Lang(List<Token> list) {
		this.markedTokens = new LinkedList<>();
		this.marked = new LinkedList<>();
		this.list = list;
		
		if(!list.isEmpty()) {
			Token t1 = list.get(list.size() - 1);
			START = new Token("", ":null", 0, 0, 0);
			END = new Token("", ":null", t1.offset, t1.line, t1.column);
		} else {
			START = END = new Token("", ":null", 0, 0, 0);
		}
	}
	
	public int remaining() {
		return list.size() - index;
	}
	
//	public void markPoint() {
//		markedTokens.add(index);
//	}
//	
//	public Token getMarkedPoint() {
//		if(markedTokens.isEmpty()) return null;
//		
//		int index = markedTokens.pollLast();
//		return list.get(index);
//	}
	
	public Token token() {
		return peak(0);
	}
	
	public Lang mark() {
		marked.push(index);
		return this;
	}
	
	public Lang reset() {
		index = marked.poll();
		return this;
	}
	
	public Lang resetMarked() {
		marked.clear();
		markedTokens.clear();
		return this;
	}
	
	public Lang next() {
		index++;
		if(index + 1 >= list.size()) throw new CompilerException("Reader is outside of bounds");
		return this;
	}
	
	public Lang nextClear() {
		index++;
		resetMarked();
		return this;
	}
	
	public Lang prev() {
		index--;
		return this;
	}
	
	/**
	 * Returns the current value and increments the index.
	 * @return the current value and increments the index
	 */
	public String valueAdvance() {
		String value = token().value;
		index++;
		return value;
	}
	
	public String value() {
		return token().value;
	}
	
	public String group() {
		return token().group;
	}
	
	public int fileOffset() {
		return token().offset;
	}
	
	public int line() {
		return token().line;
	}
	
	public int column() {
		return token().column;
	}
	
	public String toString() {
		return value();
	}
	
	public boolean valueEquals(String value) {
		return Objects.equals(value(), value);
	}
	
	public boolean valueEqualsAdvance(String value) {
		boolean eq = valueEquals(value);
		if(eq) next();
		return eq;
	}
	
	public boolean groupEquals(String group) {
		return Objects.equals(group(), group);
	}
	
	public boolean equals(String group, String value) {
		return groupEquals(group)
			&& valueEquals(value);
	}
	
	public static Lang wrap(List<Token> list) {
		return new Lang(list);
	}
	
	
	
	
	public Token peak(int offset) {
		int idx = index + offset;
		if(idx < 0) return START;
		if(idx >= list.size()) return END;
		return list.get(idx);
	}
	
	public String peakString(int offset, int count) {
		StringBuilder sb = new StringBuilder();
		
		for(int i = 0; i < count; i++) {
			Token token = peak(offset + i);
			sb.append(token.value).append(" ");
		}
		
		return sb.toString().trim();
	}
}
