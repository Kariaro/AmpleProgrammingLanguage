package me.hardcoded.compiler.context;

import me.hardcoded.lexer.GenericLexerContext;
import me.hardcoded.lexer.Token;
import me.hardcoded.lexer.Token.Type;

public class AmpleLexer {
	public static final GenericLexerContext<Token.Type> LEXER;
	
	static {
		LEXER = new GenericLexerContext<Token.Type>()
			// Whitespaces
			.addRule(Type.WHITESPACE, i -> i
				.addMultiline("/*", "*/")
				.addRegex("//[^\\r\\n]*")
				.addRegex("[ \t\r\n]+")
			)
			
			// Comparisons
			.addRule(Type.EQUALS, i -> i.addString("=="))
			.addRule(Type.NOT_EQUALS, i -> i.addString("!="))
			.addRule(Type.LESS_EQUAL, i -> i.addString("<="))
			.addRule(Type.MORE_EQUAL, i -> i.addString(">="))
			.addRule(Type.LESS_THAN, i -> i.addString("<"))
			.addRule(Type.MORE_THAN, i -> i.addString(">"))
			.addRule(Type.CAND, i -> i.addString("&&"))
			.addRule(Type.COR, i -> i.addString("||"))
			
			// Arithmetics
			.addRule(Type.NOT, i -> i.addString("!"))
			.addRule(Type.NOR, i -> i.addString("~"))
			.addRule(Type.XOR, i -> i.addString("^"))
			.addRule(Type.AND, i -> i.addString("&"))
			.addRule(Type.OR, i -> i.addString("|"))
			.addRule(Type.MINUS, i -> i.addString("-"))
			.addRule(Type.PLUS, i -> i.addString("+"))
			.addRule(Type.MUL, i -> i.addString("*"))
			.addRule(Type.DIV, i -> i.addString("/"))
			.addRule(Type.MOD, i -> i.addString("%"))
			.addRule(Type.SHIFT_LEFT, i -> i.addString("<<"))
			.addRule(Type.SHIFT_RIGHT, i -> i.addString(">>"))
			
			// Atoms
			.addRule(Type.IDENTIFIER, i -> i.addRegex("[a-zA-Z_][a-zA-Z0-9_]*"))
			.addRule(Type.BOOLEAN, i -> i.addStrings("true", "false"))
			.addRule(Type.CHARACTER, i -> i.addMultiline("'", "\\", "'"))
			.addRule(Type.STRING, i -> i.addMultiline("\"", "\\", "\""))
			.addRule(Type.DOUBLE, i -> i.addRegex("[0-9]+(\\.[0-9]+)?[dD]?"))
			.addRule(Type.FLOAT, i -> i.addRegex("[0-9]+(\\.[0-9]+)?[fF]"))
			.addRule(Type.ULONG, i -> i.addRegexes(
				"0x[0-9a-fA-F]+[Uu][Ll]",
				"0b[0-1]+[Uu][Ll]",
				"[0-9]+[Uu][Ll]"
			))
			.addRule(Type.UINT, i -> i.addRegexes(
				"0x[0-9a-fA-F]+[Uu]",
				"0b[0-1]+[Uu]",
				"[0-9]+[Uu]"
			))
			.addRule(Type.LONG, i -> i.addRegexes(
				"0x[0-9a-fA-F]+[Ll]",
				"0b[0-1]+[Ll]",
				"[0-9]+[Ll]"
			))
			.addRule(Type.INT, i -> i.addRegexes(
				"0x[0-9a-fA-F]+",
				"0b[0-1]+",
				"[0-9]+"
			))
			
			// Memory operations
			.addRule(Type.ASSIGN, i -> i.addString("="))
			
			// Brackets
			.addRule(Type.L_PAREN, i -> i.addString("("))
			.addRule(Type.R_PAREN, i -> i.addString(")"))
			.addRule(Type.L_SQUARE, i -> i.addString("["))
			.addRule(Type.R_SQUARE, i -> i.addString("]"))
			.addRule(Type.L_CURLY, i -> i.addString("{"))
			.addRule(Type.R_CURLY, i -> i.addString("}"))
			
			// Delimiters
			.addRule(Type.SEMICOLON, i -> i.addString(";"))
			.addRule(Type.QUESTION_MARK, i -> i.addString("?"))
			
			// Classes
			.addRule(Type.NAMESPACE_OPERATOR, i -> i.addString("::"))
			
			// Preprocessors
			.addRule(Type.FUNC, i -> i.addString("func"))
			.addRule(Type.PROC, i -> i.addString("proc"))
			.addRule(Type.LINK, i -> i.addString("@link"))
			.addRule(Type.RETURN, i -> i.addString("ret"))
			.addRule(Type.COLON, i -> i.addString(":"))
			.addRule(Type.COMMA, i -> i.addString(","))
			.addRule(Type.COMPILER, i -> i.addString("compiler"))
			
			// Keywords
			.addRule(Type.IF, i -> i.addString("if"))
			.addRule(Type.FOR, i -> i.addString("for"))
			.addRule(Type.ELSE, i -> i.addString("else"))
			.addRule(Type.WHILE, i -> i.addString("while"))
			.addRule(Type.CONTINUE, i -> i.addString("continue"))
			.addRule(Type.BREAK, i -> i.addString("break"))
			.addRule(Type.NAMESPACE, i -> i.addString("namespace"))
			
			.toImmutable();
	}
}
