package hc.token;

public class TokenCleaner {
	public TokenCleaner() {
		
	}
	
//	protected boolean shouldRemove(Token token) {
//		TokenType type = token.getType();
//		
//		return type.isWhitespace() || type.isComment();
//	}
//	
//	protected boolean shouldCompact(Token token) {
//		if(!(token instanceof TokenGroup)) return false;
//		
//		TokenType type = token.getType();
//		return type == DOUBLEQUOTE
//			|| type == SINGLEQUOTE;
//	}
	
//	protected Token compactSpaces(Token tree) {
//		while(tree.hasNext()) {
//			if(!shouldRemove(tree)) break;
//			tree = tree.next();
//		}
//		if(tree.getType().isDebug()) return new Token();
//		
//		Token tokenStart = tree;
//		do {
//			Token token = tree.next();
//			
//			if(shouldRemove(token)) {
//				// Remove this token from the chain
//				tree.next = token.next;
//				token.next.prev = tree;
//				continue;
//			}
//			
//			if(shouldCompact(token)) {
//				Token compacted = token.toGroup().convertToToken();
//				tree.next = compacted;
//				compacted.next = token.next;
//				compacted.prev = tree;
//				continue;
//			}
//			
//			tree = token;
//		} while(tree.hasNext());
//		
//		return tokenStart;
//	}
	
	protected Token cleanCommentsAndSpaces(Token tree) {
		while(tree.hasNext() && tree.getType().isDebug()) tree = tree.next();
		if(tree.getType().isDebug()) return new Token();
		
		Token tokenStart = tree;
		do {
			Token token = tree.next();
			
			if(token.getType().isDebug()) {
				// Remove this token from the chain
				tree.next = token.next;
				token.next.prev = tree;
				continue;
			}
			
			tree = token;
		} while(tree.hasNext());
		
		return tokenStart;
	}
	
	public Token cleanTokens(Token tree) {
		tree = cleanCommentsAndSpaces(tree);
		return tree;
	}
}
