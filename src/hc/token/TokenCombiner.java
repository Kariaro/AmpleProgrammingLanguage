package hc.token;

import static hc.token.TokenType.*;

import java.util.ArrayList;
import java.util.List;

class TokenCombiner {
	public TokenCombiner() {
		
	}
	
	protected boolean isWhitespace(EarlyToken token) {
		String value = token.value;
		
		if(value == null) return true;
		return value.length() == 1 && Character.isWhitespace(value.charAt(0));
	}
	
	protected boolean isEOL(EarlyToken token) { // End of line
		if(!token.hasNext()) return true;
		String value = token.value;
		
		if(value == null || value.length() != 1) return false;
		char c = value.charAt(0);
		
		return c == '\r' || c == '\n';
	}
	
	protected boolean isEscaped(EarlyToken token) {
		if(!token.hasNext()) return false;
		String value = token.value;
		
		if(value == null || value.length() < 1) return false;
		char c = value.charAt(0);
		if(c != '\\') return false;
		
		EarlyToken next = token.next();
		c = next.value.charAt(0);
		
		if(c == '"'
		|| c == '\''
		|| c == 'r'
		|| c == 'n'
		|| c == 't') return true;
		
		return false;
	}
	
	public EarlyToken combineTokens(EarlyToken tree) {
		List<EarlyToken> tokens = new ArrayList<>();
		
		EarlyToken start = null;
		TokenType type = TOKEN;
		
		int count = 0;
		while(tree.hasNext()) {
			EarlyToken token = tree.next();
			
			switch(type) {
				case DOUBLEQUOTE: {
					if(!token.prev.toSimpleString().equals("\\")) {
						if(token.toSimpleString().equals("\"")) {
							tokens.add(new TokenGroup(start, count + 1, type).convertToToken());
							type = TOKEN;
							break;
						}
					}
					
					count++;
					break;
				}
				
				case SINGLEQUOTE: {
					if(!token.prev.toSimpleString().equals("\\")) {
						if(token.toSimpleString().equals("\'")) {
							tokens.add(new TokenGroup(start, count + 1, type).convertToToken());
							type = TOKEN;
							break;
						}
					}
					
					count++;
					break;
				}
				
				case COMMENT: {
					if(isEOL(token)) {
						tokens.add(new TokenGroup(start, count, type).convertToToken());
						type = TOKEN;
						tree = token.next(1);
						continue;
					}
					
					count ++;
					break;
				}
				
				case MLCOMMENT: {
					if(token.hasNext(2)) {
						String value = token.toSimpleString(2);
						
						if(value.equals("*/")) {
							tokens.add(new TokenGroup(start, count + 2, type).convertToToken());
							type = TOKEN;
							tree = token.next(1);
							continue;
						}
					}

					count ++;
					break;
				}
				
				default: {
					count = 0;
					
					if(token.hasNext(2)) {
						String value = token.toString(2);
						
						if(value.equals("//")) {
							type = COMMENT;
							start = token;
							break;
						}
						
						if(value.equals("/*")) {
							type = MLCOMMENT;
							start = token;
							break;
						}
					}
					
					if(token.toString().equals("\"")) {
						type = DOUBLEQUOTE;
						start = token;
						break;
					}
					
					if(token.toString().equals("\'")) {
						type = SINGLEQUOTE;
						start = token;
						break;
					}
					
					if(isWhitespace(token)) {
						tokens.add(token.clone().setType(WHITESPACE));
					} else {
						tokens.add(token.clone());
					}
				}
			}
			
			tree = token;
		}
		
		EarlyToken token = new EarlyToken();
		start = token;
		for(EarlyToken t : tokens) {
			token.next = t;
			t.prev = token;
			token = t;
		}
		
		start = compactSpaces(start);
		return start;
	}
	
	protected boolean isSpace(EarlyToken token) {
		String value = token.value;
		
		if(value == null || value.length() != 1) return false;
		char c = value.charAt(0);
		return c == ' ' || c == '\t';
	}
	
	protected EarlyToken compactSpaces(EarlyToken tree) {
		EarlyToken treeStart = tree;
		EarlyToken start = null;
		
		int spaces = 0;
		while(tree.hasNext()) {
			EarlyToken token = tree.next();
			
			if(isSpace(token)) {
				if(spaces == 0) start = token;
				spaces++;
			} else {
				if(spaces > 0) {
					start.prev.next = token;
//					start.next = token;
//					start.value = " ";
					start = null;
					spaces = 0;
				}
			}
			
			tree = token;
		}
		
		return treeStart;
	}
}
