package hardcoded.compiler;

import java.util.LinkedList;
import java.util.Objects;

import hardcoded.lexer.Token;

public class Sym {
	private LinkedList<Token> marked;
	private Token token;
	
	public Sym(Token token) {
		this.token = token;
		this.marked = new LinkedList<>();
	}
	
	public LinkedList<Token> marked() {
		return marked;
	}
	
	public int remaining() {
		if(token == null) return -1;
		return token.remaining();
	}

	public Token token() {
		return token;
	}
	
	public Sym mark() {
		marked.push(token);
		return this;
	}
	
	public Sym reset() {
		token = marked.poll();
		return this;
	}
	
	public Sym resetMarked() {
		marked.clear();
		return this;
	}
	
	public Sym next() {
		token = token.next();
		return this;
	}
	
	public Sym nextClear() {
		token = token.next();
		marked.clear();
		return this;
	}
	
	public Sym next(int count) {
		token = token.next(count);
		return this;
	}
	
	public Sym prev() {
		token = token.prev();
		return this;
	}
	
	public Sym prev(int count) {
		token = token.prev(count);
		return this;
	}
	
	public String value() {
		if(token == null) return null;
		return token.toString();
	}
	
	public String group() {
		return token.getGroup();
	}
	
	public boolean valueEquals(String string) {
		if(string == null) return value() == null;
		return string.equals(value());
	}
	
	public boolean groupEquals(String groupName) {
		if(groupName == null) return group() == null;
		return groupName.equals(group());
	}
	
	public boolean equals(String groupName, String string) {
		return Objects.equals(groupName, group())
			&& Objects.equals(string, value());
	}
	
	public int line() {
		return token.getLineIndex();
	}
	
	public int column() {
		return token.getColumnIndex();
	}
	
	@Override
	public String toString() {
		return value();
	}
}
