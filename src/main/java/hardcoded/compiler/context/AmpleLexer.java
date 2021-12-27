package hardcoded.compiler.context;

import hardcoded.lexer.GenericLexerContext;
import hardcoded.lexer.Token;
import hardcoded.lexer.Token.Type;

public class AmpleLexer {
	public static final GenericLexerContext<Token.Type> LEXER;
	
	static {
		LEXER = new GenericLexerContext<Token.Type>()
			// Whitespaces
			.addRule(Type.WHITESPACE, i -> i
				.addMultiline("/*", "*/")
				.addRegex("//[^\\r\\n]*")
				.addRegex("[ \t\r\n]")
			)

			// Comparisons
			.addRule(Type.EQUALS, i -> i.addString("=="))
			.addRule(Type.NOT_EQUALS, i -> i.addString("!="))
			.addRule(Type.LESS_THAN_EQUAL, i -> i.addString("<="))
			.addRule(Type.MORE_THAN_EQUAL, i -> i.addString(">="))
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
			.addRule(Type.CHARACTER, i -> i.addMultiline("\'", "\\", "\'"))
			.addRule(Type.STRING, i -> i.addMultiline("\"", "\\", "\""))
			.addRule(Type.NULL, i -> i.addStrings("null"))
			.addRule(Type.LONG, i -> i.addRegexes(
				"0x[0-9a-fA-F]+L",
				"0b[0-1]+L",
				"[0-9]+L"
			))
			.addRule(Type.INT, i -> i.addRegexes(
				"0x[0-9a-fA-F]+",
				"0b[0-1]+",
				"[0-9]+"
			))

			// Memory operations
			.addRule(Type.DEC, i -> i.addString("--"))
			.addRule(Type.INC, i -> i.addString("++"))
			.addRule(Type.ASSIGN, i -> i.addString("="))
			.addRule(Type.ADD_ASSIGN, i -> i.addString("+="))
			.addRule(Type.SUB_ASSIGN, i -> i.addString("-="))
			.addRule(Type.MUL_ASSIGN, i -> i.addString("*="))
			.addRule(Type.DIV_ASSIGN, i -> i.addString("/="))
			.addRule(Type.MOD_ASSIGN, i -> i.addString("%="))
			.addRule(Type.XOR_ASSIGN, i -> i.addString("^="))
			.addRule(Type.AND_ASSIGN, i -> i.addString("&="))
			.addRule(Type.OR_ASSIGN, i -> i.addString("|="))
			.addRule(Type.SHIFT_LEFT_ASSIGN, i -> i.addString("<<="))
			.addRule(Type.SHIFT_RIGHT_ASSIGN, i -> i.addString(">>="))

			// Brackets
			.addRule(Type.LEFT_PARENTHESIS, i -> i.addString("("))
			.addRule(Type.RIGHT_PARENTHESIS, i -> i.addString(")"))
			.addRule(Type.LEFT_SQUARE_BRACKET, i -> i.addString("["))
			.addRule(Type.RIGHT_SQUARE_BRACKET, i -> i.addString("]"))
			.addRule(Type.LEFT_CURLY_BRACKET, i -> i.addString("{"))
			.addRule(Type.RIGHT_CURLY_BRACKET, i -> i.addString("}"))

			// Delimiters
			.addRule(Type.SEMICOLON, i -> i.addString(";"))
			.addRule(Type.COMMA, i -> i.addString(","))
			.addRule(Type.QUESTION_MARK, i -> i.addString("?"))
			.addRule(Type.COLON, i -> i.addString(":"))
			.addRule(Type.AT, i -> i.addString("@"))
			
			// Classes
			.addRule(Type.NAMESPACE, i -> i.addString("::"))
			.addRule(Type.MEMBER, i -> i.addString("."))
			.addRule(Type.POINTER, i -> i.addString("->"))
			
			// Preprocessors
			.addRule(Type.DEFINE_TYPE, i -> i.addString("@type"))
			.addRule(Type.DEFINE, i -> i.addString("@set"))
			.addRule(Type.UNDEFINE, i -> i.addString("@unset"))
			.addRule(Type.IMPORT, i -> i.addString("@import"))
			
			// Keywords
			.addRule(Type.IF, i -> i.addString("if"))
			.addRule(Type.FOR, i -> i.addString("for"))
			.addRule(Type.ELSE, i -> i.addString("else"))
			.addRule(Type.DO, i -> i.addString("do"))
			.addRule(Type.WHILE, i -> i.addString("while"))
			.addRule(Type.CONTINUE, i -> i.addString("continue"))
			.addRule(Type.BREAK, i -> i.addString("break"))
			.addRule(Type.RETURN, i -> i.addString("return"))
			.addRule(Type.SWITCH, i -> i.addString("switch"))
			.addRule(Type.CASE, i -> i.addString("case"))
			.addRule(Type.DEFAULT, i -> i.addString("default"))
			.addRule(Type.GOTO, i -> i.addString("goto"))
			.addRule(Type.COMPILER, i -> i.addString("compiler"))
			.addRule(Type.CONSTRUCT, i -> i.addString("construct"))
			
			// Function Modifiers
			.addRule(Type.EXPORT, i -> i.addString("export"))
			.addRule(Type.INLINE, i -> i.addString("inline"))
			
			
			// Variable Types
			.addRule(Type.VOID, i -> i.addString("void"))
			.addRule(Type.CHAR, i -> i.addString("char"))
			.addRule(Type.BYTE, i -> i.addString("byte"))
			.addRule(Type.INT, i -> i.addString("int"))
			.addRule(Type.SHORT, i -> i.addString("short"))
			.addRule(Type.LONG, i -> i.addString("long"))
			
			// Type prefix
			.addRule(Type.UNSIGNED, i -> i.addString("unsigned"))
			.addRule(Type.SIGNED, i -> i.addString("signed"))
			.addRule(Type.CONST, i -> i.addString("const"))
			
		.toImmutable();
	}
}
