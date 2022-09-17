package me.hardcoded.lexer;

import me.hardcoded.compiler.context.AmpleLexer;
import me.hardcoded.compiler.impl.ISyntaxPosition;
import me.hardcoded.lexer.Token.Type;
import me.hardcoded.utils.Position;

import java.io.File;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

public class LexerTokenizer {
	public static List<Token> parse(byte[] bytes) {
		return parse(null, bytes);
	}
	
	public static List<Token> parse(File file, byte[] bytes) {
		List<Token> tokenList = parseKeepWhitespace(file, bytes);
		tokenList.removeIf(token -> token.type == Type.WHITESPACE);
		return tokenList;
	}
	
	public static List<Token> parseKeepWhitespace(File file, byte[] bytes) {
		String text = new String(bytes, StandardCharsets.UTF_8);
		List<Token> tokenList = new ArrayList<>();
		int offset = 0;
		int line = 0;
		int column = 0;
		int length = text.length();
		String input = text;
		
		while (offset < length) {
			Position startPosition = new Position(file, column, line, offset);
			
			GenericLexerContext<Type>.LexerToken lexerToken = AmpleLexer.LEXER.nextToken(input);
			if (lexerToken == null) {
				throw new RuntimeException("(%s) (line: %d, column: %d): Could not parse token".formatted(file, line + 1, column + 1));
			}
			
			if (lexerToken.length + offset > length) {
				break;
			}
			
			for (int i = offset; i < offset + lexerToken.length; i++) {
				char c = text.charAt(i);
				
				if (c == '\n') {
					line++;
					column = 0;
				} else {
					column += (c == '\t') ? 4 : 1;
				}
			}
			
			Position endPosition = new Position(file, column, line, offset + lexerToken.length);
			tokenList.add(new Token(
				lexerToken.type,
				lexerToken.content,
				ISyntaxPosition.of(startPosition, endPosition)
			));
			
			input = input.substring(lexerToken.length);
			offset += lexerToken.length;
		}
		
		return tokenList;
	}
}
