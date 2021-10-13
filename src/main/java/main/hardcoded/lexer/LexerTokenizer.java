package hardcoded.lexer;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

import hardcoded.compiler.context.AmpleLexer;
import hardcoded.lexer.Token.Type;

public class LexerTokenizer {
	public static List<Token> parse(byte[] bytes) {
		String text = new String(bytes, StandardCharsets.UTF_8);
		List<Token> tokenList = new ArrayList<>();
		int offset = 0;
		int line = 0;
		int column = 0;
		int length = text.length();
		String input = text;
		
		while(offset < length) {
			GenericLexerContext<Type>.LexerToken lexerToken = AmpleLexer.LEXER.nextToken(input);
			if(lexerToken == null) {
				// Throw error
				break;
			}
			
			if(lexerToken.length + offset > length) break;
			
			if(lexerToken.type != Type.WHITESPACE) {
				tokenList.add(new Token(
					lexerToken.type,
					lexerToken.content,
					offset,
					line,
					column
				));
			}
			
			for(int i = offset; i < offset + lexerToken.length; i++) {
				char c = text.charAt(i);
				
				if(c == '\n') {
					line ++;
					column = 0;
				} else {
					column += (c == '\t') ? 4:1;
				}
			}
			
			input = input.substring(lexerToken.length);
			offset += lexerToken.length;
		}
		
		return tokenList;
	}
}
