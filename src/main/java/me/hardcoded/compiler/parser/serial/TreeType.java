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
	STRUCT,
	
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
	SIZEOF,
	COMPILER,
	
	// Special
	BUILTIN,
	;
	
	public static final TreeType[] VALUES = values();
}
