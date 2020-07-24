package hc.token;

class TokenCleaner {
	public TokenCleaner() {
		
	}
	
	protected Token cleanCommentsAndSpaces(Token tree) {
		while(tree.hasNext() && tree.getType().isDebug()) tree = tree.next();
		if(tree.getType().isDebug()) return new Token();
		
		Token tokenStart = tree;
		do {
			Token token = tree.next();
			
			if(token.getType().isDebug()) {
				// Remove this token from the chain
				tree.next = token.next;
				if(token.next != null) {
					token.next.prev = tree;
				}
				
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
