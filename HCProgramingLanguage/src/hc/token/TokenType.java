package hc.token;

public enum TokenType {
	TOKEN,
	WHITESPACE,
	DOUBLEQUOTE,
	SINGLEQUOTE,
	COMMENT,
	MLCOMMENT,
	BRACKET,
	COMPILER,
	
	MODIFIER,
	KEYWORD,
	PRIMITIVE,
	;
	
	public boolean isQuoted() {
		return this == SINGLEQUOTE || this == DOUBLEQUOTE;
	}
	
	public boolean isComment() {
		return this == COMMENT || this == MLCOMMENT;
	}
	
	public boolean isWhitespace() {
		return this == WHITESPACE;
	}
	
	public boolean isBracket() {
		return this == BRACKET;
	}
	
	public boolean isSpecifier() {
		return this == TOKEN || this == PRIMITIVE;
	}
	
	public boolean isDebug() {
		return isComment() || isWhitespace();
	}
}
