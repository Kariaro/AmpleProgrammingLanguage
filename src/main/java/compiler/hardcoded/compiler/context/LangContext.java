package hardcoded.compiler.context;

import java.util.List;

import hardcoded.lexer.Token;
import hardcoded.lexer.Token.Type;

public class LangContext {
	private final List<Token> list;
	private final Token start;
	private final Token end;
	private int index;
	
	private LangContext(List<Token> list) {
		this.list = list;
		
		start = new Token(Type.WHITESPACE, 0, 0, 0);
		if(!list.isEmpty()) {
			Token t1 = list.get(list.size() - 1);
			end = new Token(Type.EOF, t1.offset, t1.line, t1.column);
		} else {
			end = new Token(Type.EOF, 0, 0, 0);
		}
	}
	
	public int readerIndex() {
		return index;
	}
	
	public void advance() {
		index++;
	}
	
	public void recede() {
		index--;
	}
	
	public int indexOf(Token token) {
		return list.indexOf(token);
	}
	
	public int remaining() {
		return list.size() - index;
	}
	
	public Token token() {
		Token token = peak(0);
		System.out.printf("[%s] (Line: %d, Column: %d), %s\n", Thread.getAllStackTraces().get(Thread.currentThread())[4], token.line, token.column, token);
		
		return peak(0);
	}

	public String value() {
		return token().value;
	}
	
	public Type type() {
		return token().type;
	}
	
	public boolean consumeType(Type expected) {
		if(expected == type()) {
			advance();
			return true;
		}
		
		return false;
	}
	
	public int offset() {
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
	
	public static LangContext wrap(List<Token> list) {
		return new LangContext(list);
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
		if(idx < 0) return start;
		if(idx >= list.size()) return end;
		return list.get(idx);
	}
	
	public String peakString(int offset, int count) {
		StringBuilder sb = new StringBuilder();
		
		for(int i = 0; i < count; i++) {
			sb.append(peak(offset + i).value).append(" ");
		}
		
		return sb.toString().trim();
	}
}
