package hc.token;

class TokenCleaner {
	public TokenCleaner() {
		
	}
	
	protected EarlyToken cleanCommentsAndSpaces(EarlyToken tree) {
		while(tree.hasNext() && tree.getType().isDebug()) tree = tree.next();
		if(tree.getType().isDebug()) return new EarlyToken();
		
		EarlyToken tokenStart = tree;
		do {
			EarlyToken token = tree.next();
			
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
	
	public EarlyToken cleanTokens(EarlyToken tree) {
		tree = cleanCommentsAndSpaces(tree);
		return tree;
	}
}
