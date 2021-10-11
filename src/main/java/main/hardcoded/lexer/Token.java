package hardcoded.lexer;

import java.util.Objects;

/**
 * A token class.
 * 
 * @author HardCoded
 */
public class Token {
	public final Type type;
	public final String value;
	public final int offset;
	public final int line;
	public final int column;
	
	public Token(Type type, String value, int offset, int line, int column) {
		this.type = Objects.requireNonNull(type);
		this.value = Objects.requireNonNull(value);
		this.offset = offset;
		this.line = line;
		this.column = column;
	}
	
	public Token(Type type, int offset, int line, int column) {
		this(type, "", offset, line, column);
	}

	@Deprecated
	public boolean valueEquals(String string) {
		return value.equals(string);
	}
	
	@Deprecated
	public boolean equals(String group, Type type) {
		return valueEquals(value)
			&& (this.type == type);
	}
	
	@Deprecated
	public boolean isInvalid() {
		return column < 0 || offset < 0 || line < 0;
	}
	
	public String toString() {
		return "{ type: %s, value: '%s' }".formatted(type, value);
	}
	
	public enum Type {
		// Whitespace
		WHITESPACE,
		EOF,
		
		// Comparisons
		EQUALS,
		NOT_EQUALS,
		LESS_THAN,
		LESS_THAN_EQUAL,
		MORE_THAN,
		MORE_THAN_EQUAL,
		CAND,
		COR,
		
		// Brackets
		LEFT_PARENTHESIS,
		RIGHT_PARENTHESIS,
		LEFT_SQUARE_BRACKET,
		RIGHT_SQUARE_BRACKET,
		LEFT_CURLY_BRACKET,
		RIGHT_CURLY_BRACKET,
		
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
		INC,
		DEC,
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
		
		// Atoms
		IDENTIFIER,
		CHARACTER,
		BOOLEAN,
		STRING,
		NULL,
		
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
		
		// Function Modifiers
		EXPORT,
		INLINE,
		
		// Delimiters
		SEMICOLON,
		COMMA,
		QUESTION_MARK,
		COLON,
		AT,
		
		// Classes
		NAMESPACE,
		MEMBER,
		POINTER,
		
		// Preprocessors
		DEFINE_TYPE,
		DEFINE,
		UNDEFINE,
		IMPORT,
		
		// Variable types
		DOUBLE,
		BYTE,
		CHAR,
		SHORT,
		INT,
		LONG,
		VOID,
		
		// Type prefixes
		UNSIGNED,
		SIGNED,
		CONST,
	}
}
