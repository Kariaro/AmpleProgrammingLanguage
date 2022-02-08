package me.hardcoded.compiler.parser.type;

public enum TreeType {
	// Statements
	BREAK,
	CONTINUE,
	EMPTY,
	FOR,
	FUNC,
	GOTO,
	IF,
	LABEL,
	PROG,
	RETURN,
	SCOPE,
	VAR,
	WHILE,
	NAMESPACE,
	
	// Expressions
	BINARY,
	CALL,
	CAST,
	COMMA,
	NAME,
	NULL,
	NUM,
	STR,
	UNARY,
	CONDITIONAL;
	
	public static final TreeType[] VALUES = values();
}
