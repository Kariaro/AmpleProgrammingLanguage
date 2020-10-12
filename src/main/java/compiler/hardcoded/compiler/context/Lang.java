package hardcoded.compiler.context;

import java.util.LinkedList;
import java.util.Objects;

import hardcoded.lexer.Token;

public class Lang {
	private LinkedList<Token> marked;
	private Token token;
	
	public Lang(Token token) {
		this.token = token;
		this.marked = new LinkedList<>();
	}
	
	public LinkedList<Token> marked() {
		return marked;
	}
	
	public int fileOffset() {
		return token.fileOffset();
	}
	
	public int remaining() {
		if(token == null) return -1;
		return token.remaining();
	}

	public Token token() {
		return token;
	}
	
	public Lang mark() {
		marked.push(token);
		return this;
	}
	
	public Lang reset() {
		token = marked.poll();
		return this;
	}
	
	public Lang resetMarked() {
		marked.clear();
		return this;
	}
	
	public Lang next() {
		token = token.next();
		return this;
	}
	
	public Lang nextClear() {
		token = token.next();
		marked.clear();
		return this;
	}
	
	public Lang next(int count) {
		token = token.next(count);
		return this;
	}
	
	public Lang prev() {
		token = token.prev();
		return this;
	}
	
	public Lang prev(int count) {
		token = token.prev(count);
		return this;
	}
	
	public String value() {
		if(token == null) return null;
		return token.toString();
	}
	
	public String group() {
		return token.group();
	}
	
	public int line() {
		return token.line();
	}
	
	public int column() {
		return token.column();
	}
	
	public String toString() {
		return value();
	}
	
	public boolean valueEquals(String string) {
		return Objects.equals(string, value());
	}
	
	public boolean valueEqualsAdvance(String string) {
		boolean eq = Objects.equals(string, value());
		if(eq) next();
		return eq;
	}
	
	public boolean groupEquals(String groupName) {
		return Objects.equals(groupName, group());
	}
	
	public boolean equals(String groupName, String string) {
		return Objects.equals(groupName, group())
			&& Objects.equals(string, value());
	}
}
