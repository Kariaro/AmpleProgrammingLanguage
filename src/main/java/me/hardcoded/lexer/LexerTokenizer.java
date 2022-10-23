package me.hardcoded.lexer;

import me.hardcoded.compiler.context.AmpleLexer;
import me.hardcoded.compiler.impl.ISyntaxPos;
import me.hardcoded.lexer.Token.Type;
import me.hardcoded.utils.Position;
import me.hardcoded.utils.error.ErrorUtil;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

public class LexerTokenizer {
	public static List<Token> parse(String path, byte[] bytes) {
		List<Token> tokenList = parseKeepWhitespace(path, bytes);
		tokenList.removeIf(token -> token.type == Type.WHITESPACE);
		return tokenList;
	}
	
	public static List<Token> parseKeepWhitespace(String path, byte[] bytes) {
		String text = new String(bytes, StandardCharsets.UTF_8);
		List<Token> tokenList = new ArrayList<>();
		int offset = 0;
		int line = 0;
		int column = 0;
		int length = text.length();
		String input = text;
		
		while (offset < length) {
			Position startPos = new Position(column, line); // offset
			
			GenericLexerContext<Type>.LexerToken lexerToken = AmpleLexer.LEXER.nextToken(input);
			if (lexerToken == null) {
				throw new RuntimeException(ErrorUtil.createFullError(
					ISyntaxPos.of(path, startPos, startPos),
					text,
					"Could not parse token"
				));
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
			
			Position endPos = new Position(column, line); // offset + lexerToken.length
			tokenList.add(new Token(
				lexerToken.type,
				lexerToken.content,
				ISyntaxPos.of(path, startPos, endPos)
			));
			
			input = input.substring(lexerToken.length);
			offset += lexerToken.length;
		}
		
		return tokenList;
	}
}
