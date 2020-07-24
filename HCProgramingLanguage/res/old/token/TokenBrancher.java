package hc.token;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import hc.errors.TokenBracketException;

import static hc.token.BracketType.*;
import static hc.token.TokenType.*;

@Deprecated
class TokenBrancher {
	public TokenBrancher() {
		
	}
	
	protected Token removeComments(Token tree) {
		// Remove leading whitespaces and comments
		while(tree.hasNext() && tree.getType().isComment()) tree = tree.next();
		if(tree.getType().isComment()) return new Token();
		
		Token tokenStart = tree;
		do {
			Token token = tree.next();
			
			if(token.getType().isComment()) {
				// Remove this token from the chain
				tree.next = token.next;
				token.next.prev = tree;
				continue;
			}
			
			tree = token;
		} while(tree.hasNext());
		
		return tokenStart;
	}
	
	protected Token removeDebugCharacters(Token tree) {
		// Remove leading whitespaces and comments
		while(tree.hasNext() && tree.getType().isDebug()) tree = tree.next();
		if(tree.getType().isDebug()) return new Token();
		
		Token tokenStart = tree;
		do {
			Token token = tree.next();
			
			if(token.getType().isDebug()) {
				// Remove this token from the chain
				tree.next = token.next;
				if(token.next == null) break;
				
				token.next.prev = tree;
				continue;
			}
			
			tree = token;
		} while(tree.hasNext());
		
		return tokenStart;
	}
	
	protected boolean isEOL(Token token) { // End of line
		if(!token.hasNext()) return true;
		String value = token.value;
		
		if(value == null || value.length() != 1) return false;
		char c = value.charAt(0);
		
		return c == '\r' || c == '\n';
	}
	
	protected Token mergeCompileTokens(Token tree) {
		List<Token> tokens = new ArrayList<>();
		
		Token start = null;
		TokenType type = TOKEN;
		
		int count = 0;
		while(tree.hasNext()) {
			Token token = tree.next();
			
			switch(type) {
				case COMPILER: {
					if(isEOL(token)) {
						tokens.add(new TokenGroup(start, count, type));
						type = TOKEN;
						tree = token.next(1);
						continue;
					}
					
					count ++;
					break;
				}
				
				default: {
					count = 0;
					
					if(token.toString().equals("#")) {
						type = COMPILER;
						start = token;
						break;
					}
					
					tokens.add(token);
				}
			}
			
			tree = token;
		}
		
		Token token = new Token();
		start = token;
		for(Token t : tokens) {
			token.next = t;
			t.prev = token;
			token = t;
		}
		
		return start;
	}
	
	public Token combineBranches(Token tree) {
		LinkedList<Token> starts = new LinkedList<>();
		tree = removeComments(tree);
		tree = mergeCompileTokens(tree);
		tree = removeDebugCharacters(tree);
		Token tokenStart = tree;
		
		while(tree != null && tree.hasNext()) {
			Token token = tree.next();
			
			String value = token.toSimpleString();
			BracketType type = getBracketType(value);
			
			if(isOpenBracket(value)) {
				starts.add(token);
				// System.out.println("[Bracket start] '" + value + "'");
				tree = token;
				continue;
			}
			
			if(isClosedBracket(value)) {
				Token start = starts.pollLast();
				BracketType stype = getBracketType(start.toSimpleString());
				
				if(stype != type)
					throw new TokenBracketException("Bracket types does not match '" + stype + "/" + type + "', '" + start + "/" + token + "'");
				
				int index = start.indexOf(token);
				// System.out.println("index = " + index + ", " + (start.toSimpleString(index + 1)));
				
				TokenGroup group = new TokenGroup(start, index, TokenType.BRACKET);
				
				Token prev = start.prev;
				prev.next = group;
				group.prev = prev;
				group.next = token.next();
				if(token.next != null) {
					token.next.prev = group;
				}
				
				// System.out.println("group -> '" + group.toSimpleString() + "'");
				// System.out.println("    [Bracket end] " + (prev.next).toSimpleString());
				tree = token;
				continue;
			}
			
			// System.out.println("  read: '" + token + "'");
			tree = token;
		}
		
		// int size = tokenStart.remainingTokens();
		// System.out.println("TokenBrancher: " + tokenStart.toSimpleString(size));
		
		return tokenStart;
	}
}
