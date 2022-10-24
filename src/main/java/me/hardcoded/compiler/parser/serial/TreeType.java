package me.hardcoded.compiler.parser.serial;

public enum TreeType {
	// Statements
	PROGRAM,
	BREAK,
	CONTINUE,
	EMPTY,
	FOR,
	FUNC,
	IF,
	RETURN,
	SCOPE,
	VAR,
	WHILE,
	NAMESPACE,
	
	// Expressions
	BINARY,
	UNARY,
	CALL,
	CAST,
	NAME,
	NUM,
	STR,
	NONE,
	STACK_ALLOC,
	COMPILER,
	;
	
	public static final TreeType[] VALUES = values();
}
