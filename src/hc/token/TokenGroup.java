package hc.token;

import java.util.ArrayList;
import java.util.List;

class TokenGroup extends EarlyToken {
	protected List<EarlyToken> group;
	
	public TokenGroup() {
		group = new ArrayList<>();
	}
	
	public TokenGroup(EarlyToken start, int count) {
		this(start, count, null);
	}
	
	public TokenGroup(EarlyToken start, int count, TokenType type) {
		group = new ArrayList<>();
		column = start.column;
		line = start.line;
		setType(type);
		
		for(int i = 0; i <= count; i++) {
			group.add(start.clone());
			start = start.next;
		}
	}
	
	public String getStringValue() {
		StringBuilder sb = new StringBuilder();
		for(EarlyToken token : group) sb.append(token.getValue());
		return sb.toString();
	}
	
	/**
	 * This will only clone this token and remove all connected tokens.
	 */
	public EarlyToken clone() {
		TokenGroup token = new TokenGroup();
		for(EarlyToken t : group) token.group.add(t.clone());
		return token;
	}
	
	/**
	 * Convert this group into one token
	 */
	public EarlyToken convertToToken() {
		EarlyToken token = new EarlyToken();
		token.setType(getType());
		token.value = toString();
		token.column = column;
		token.line = line;
		return token;
	}
	
	public List<EarlyToken> getValue() {
		return group;
	}
	
	public String toString() {
		StringBuilder sb = new StringBuilder();
		for(EarlyToken token : group) sb.append(token.getValue());
		return sb.toString();
	}
}
