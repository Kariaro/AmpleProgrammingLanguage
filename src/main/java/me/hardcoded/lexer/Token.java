package me.hardcoded.lexer;

import java.util.Objects;

import me.hardcoded.compiler.impl.ISyntaxPosition;

/**
 * A token class.
 * 
 * @author HardCoded
 */
public class Token {
	public final ISyntaxPosition syntaxPosition;
	public final String value;
	public final Type type;
	
	public Token(Type type, String value, ISyntaxPosition syntaxPosition) {
		this.syntaxPosition = Objects.requireNonNull(syntaxPosition);
		this.value = Objects.requireNonNull(value);
		this.type = Objects.requireNonNull(type);
	}
	
	public Token(Type type, ISyntaxPosition syntaxPosition) {
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
		PROC, // Procedure
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
		INCREMENT,
		DECREMENT,
		SHIFT_RIGHT,
		SHIFT_LEFT,
		
		// Memory Operations
		ASSIGN,
		ADD_ASSIGN,
		SUB_ASSIGN,
		MUL_ASSIGN,
		DIV_ASSIGN,
		MOD_ASSIGN,
		XOR_ASSIGN,
		OR_ASSIGN,
		AND_ASSIGN,
		SHIFT_LEFT_ASSIGN,
		SHIFT_RIGHT_ASSIGN,
		REFERENCE,
		DEREFERENCE,
		
		// Atoms
		IDENTIFIER,
		CHARACTER,
		BOOLEAN,
		STRING,
		INT,
		LONG,
		FLOAT,
		DOUBLE,
		NULL,
		
		// Variable types
		DOUBLE_TYPE,
		FLOAT_TYPE,
		BYTE_TYPE,
		BOOL_TYPE,
		CHAR_TYPE,
		SHORT_TYPE,
		INT_TYPE,
		LONG_TYPE,
		VOID_TYPE,
		
		// Keywords
		IF,
		ELSE,
		FOR,
		WHILE,
		DO,
		SWITCH,
		CASE,
		DEFAULT,
		CONTINUE,
		BREAK,
		RETURN,
		GOTO,
		NAMESPACE,
		
		// Compiler annotations
		COMPILER,
		
		// Function Modifiers
		EXPORT,
		INLINE,
		
		// Delimiters
		SEMICOLON,
		QUESTION_MARK,
		COLON,
		DOT,
		AT,
		
		// Classes
		NAMESPACE_OPERATOR,
		MEMBER,
		POINTER,
		
		// Preprocessors
		LINK,
		
		// Type prefixes
		UNSIGNED,
		SIGNED,
		VOLATILE,
		CONST,
	}
}
