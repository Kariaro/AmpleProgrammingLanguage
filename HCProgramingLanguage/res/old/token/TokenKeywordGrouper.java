package hc.token;

import hc.parser.Modifier;
import hc.parser.Primitive;

public class TokenKeywordGrouper {
	public TokenKeywordGrouper() {
		
	}
	
	/**
	 * A keyword is a word that is not allowed to be used as a name for a method or variable.
	 * @param token
	 * @return
	 */
	protected boolean isKeyword(Token token) {
		String value = token.getStringValue();
		if(value == null || token.getType() != TokenType.TOKEN) return false;
		
		switch(value) {
			case "switch":
			case "case":
			case "goto":
			case "for":
			case "while":
			case "else":
			case "do":
			case "true":
			case "false":
			case "null":
				return true;
		}
		
		return false;
	}
	
	public Token modifyTokenType(Token tree) {
		Token treeStart = tree;
		
		while(tree.hasNext()) {
			Token token = tree.next();
			if(isKeyword(token)) token.setType(TokenType.KEYWORD);
			if(Modifier.isModifier(token)) token.setType(TokenType.MODIFIER);
			if(Primitive.isPrimitive(token)) token.setType(TokenType.PRIMITIVE);
			
			tree = token;
		}
		
		return treeStart;
	}
}
