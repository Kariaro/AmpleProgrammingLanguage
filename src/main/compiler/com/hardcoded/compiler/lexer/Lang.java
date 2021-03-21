package com.hardcoded.compiler.lexer;

import java.util.List;
import java.util.Objects;

/**
 * 
 * @author HardCoded
 * @since 0.2.0
 */
public class Lang {
	private final List<Token> list;
	private final Token START;
	private final Token END;
	private int index;
	
	private Lang(List<Token> list) {
		this.list = list;
		
		if(!list.isEmpty()) {
			Token t1 = list.get(list.size() - 1);
			START = new Token("", ":null", 0, 0, 0);
			END = new Token("", ":null", t1.offset, t1.line, t1.column);
		} else {
			START = END = new Token("", ":null", 0, 0, 0);
		}
	}
	
	public int readerIndex() {
		return index;
	}
	
	public int indexOf(Token token) {
		return list.indexOf(token);
	}
	
	public int remaining() {
		return list.size() - index;
	}
	
	public Token token() {
		return peak(0);
	}
	
	public Token next() {
		Token token = token();
		index++;
		return token;
	}
	
	public Lang prev() {
		index--;
		return this;
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

	public boolean groupEquals(String group) {
		return Objects.equals(group(), group);
	}
	
	@Deprecated
	public boolean equals(String group, String value) {
		return groupEquals(group)
			&& valueEquals(value);
	}
	
	public static Lang wrap(List<Token> list) {
		return new Lang(list);
	}
	
	
	/**
	 * Returns the token at the specified relative position.
	 * <br>Note that this method will never return {@code null}.
	 * 
	 * @param	offset
	 * @return	the token at the specified relative position
	 */
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

	public boolean hasNext() {
		return remaining() > 0;
	}
}
