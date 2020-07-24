package hc.token;

import java.util.ArrayList;
import java.util.List;

public class TokenGroup extends Token {
	protected List<Token> group;
	protected TokenType type;
	
	public TokenGroup() {
		group = new ArrayList<>();
	}
	
	public TokenGroup(Token start, int count) {
		this(start, count, null);
	}
	
	public TokenGroup(Token start, int count, TokenType type) {
		group = new ArrayList<>();
		setType(type);
		
		for(int i = 0; i <= count; i++) {
			group.add(start.clone());
			start = start.next;
		}
	}
	
	public String getStringValue() {
		StringBuilder sb = new StringBuilder();
		for(Token token : group) sb.append(token.getValue());
		return sb.toString();
	}
	
	/**
	 * This will only clone this token and remove all connected tokens.
	 */
	public Token clone() {
		TokenGroup token = new TokenGroup();
		for(Token t : group) token.group.add(t.clone());
		return token;
	}
	
	/**
	 * Convert this group into one token
	 */
	public Token convertToToken() {
		Token token = new Token();
		token.setType(getType());
		token.value = toString();
		return token;
	}
	
	public List<Token> getValue() {
		return group;
	}
	
	public String toString() {
		StringBuilder sb = new StringBuilder();
		for(Token token : group) sb.append(token.getValue());
		return sb.toString();
	}
}
