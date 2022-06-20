package me.hardcoded.compiler.parser.serial;

public enum TreeType {
	// Statements
	PROGRAM,
	BREAK,
	CONTINUE,
	EMPTY,
	FOR,
	FUNC,
	GOTO,
	IF,
	LABEL,
	RETURN,
	SCOPE,
	VAR,
	WHILE,
	NAMESPACE,
	
	// Expressions
	BINARY,
	UNARY,
	CALL,
	//CAST,
	//COMMA,
	NAME,
	//NULL,
	NUM,
	NONE,
	STRING,
	//CONDITIONAL;
	STACK_DATA,
	;
	
	public static final TreeType[] VALUES = values();
}
