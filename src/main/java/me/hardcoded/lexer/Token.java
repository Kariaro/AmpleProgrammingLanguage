package me.hardcoded.lexer;

import me.hardcoded.compiler.impl.ISyntaxPos;

import java.util.Objects;

/**
 * A token class.
 *
 * @author HardCoded
 */
public class Token {
	public final ISyntaxPos syntaxPosition;
	public final String value;
	public final Type type;
	
	public Token(Type type, String value, ISyntaxPos syntaxPosition) {
		this.syntaxPosition = Objects.requireNonNull(syntaxPosition);
		this.value = Objects.requireNonNull(value);
		this.type = Objects.requireNonNull(type);
	}
	
	public Token(Type type, ISyntaxPos syntaxPosition) {
		this(type, "", syntaxPosition);
	}
	
	public String toString() {
		return "{ type: " + type + ", value: '" + value + "' }";
	}
	
	public enum Type {
		// Whitespace
		WHITESPACE,
		EOF,
		
		// Test
		FUNC,
		COMMA,
		
		// Comparisons
		EQUALS,
		NOT_EQUALS,
		LESS_THAN,
		LESS_EQUAL,
		MORE_THAN,
		MORE_EQUAL,
		CAND,
		COR,
		
		// Brackets
		L_PAREN,
		R_PAREN,
		L_SQUARE,
		R_SQUARE,
		L_CURLY,
		R_CURLY,
		
		// Arithmetics
		NOT,
		NOR,
		AND,
		OR,
		XOR,
		PLUS,
		MINUS,
		MUL,
		DIV,
		MOD,
		SHIFT_RIGHT,
		SHIFT_LEFT,
		
		// Memory Operations
		ASSIGN,
		
		// Atoms
		IDENTIFIER,
		CHARACTER,
		BOOLEAN,
		STRING,
		ULONG,
		UINT,
		LONG,
		INT,
		FLOAT,
		DOUBLE,
		NULL,
		
		// Keywords
		IF,
		ELSE,
		FOR,
		WHILE,
		CONTINUE,
		BREAK,
		RETURN,
		NAMESPACE,
		
		// Compiler annotations
		COMPILER,
		
		// Function Modifiers
		EXPORT,
		INLINE,
		
		// Delimiters
		SEMICOLON,
		QUESTION_MARK,
		VARARGS,
		COLON,
		
		// Classes
		NAMESPACE_OPERATOR,
		
		// Preprocessors
		LINK,
		
		// Reserved
		RESERVED,
	}
}
