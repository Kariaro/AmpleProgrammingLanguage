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
	//STR,
	//CONDITIONAL;
	;
	
	public static final TreeType[] VALUES = values();
}
