package hc.token;

import java.util.ArrayList;
import java.util.List;

// FIXME: Create a tokenizer language that can be used to read a set of characters and create the token chain...
// TODO: Clean all classes used to create this token chain
public class Tokenizer {
	private Tokenizer() {}
	
	private static final boolean isDelimiter(char c) {
		return !(Character.isAlphabetic(c)
			   | Character.isDigit(c)
			   | c == '_');
	}
	
	protected static final EarlyToken createTokenChain(byte[] bytes) {
		char[] chars = new char[bytes.length];
		int index = 0;
		
		for(int i = 0; i < bytes.length; i++) {
			chars[i] = (char)Byte.toUnsignedInt(bytes[i]);
		}
		
		EarlyToken start = new EarlyToken();
		EarlyToken token = start;
		
		int lineIndex = 1;
		int linePos = 0;
		
		while(index < chars.length) {
			int startIndex = index;
			
			String buffer = "";
			char c = chars[index++];
			buffer += c;
			if(!isDelimiter(c)) {
				while(index < chars.length) {
					c = chars[index++];
					if(isDelimiter(c)) {
						index--;
						break;
					}
					buffer += c;
				}
			}
			
			EarlyToken next = new EarlyToken();
			next.column = (startIndex - linePos);
			next.line = lineIndex;
			next.value = buffer;
			
			token.next = next;
			next.prev = token;
			token = next;
			
			
			if(chars[startIndex] == '\n') {
				lineIndex++;
				linePos = startIndex;
			}
		}
		
		return start;
	}
	
	protected static EarlyToken combineTokens(EarlyToken token) {
		return new TokenCombiner().combineTokens(token);
	}
	
	protected static EarlyToken cleanTokens(EarlyToken token) {
		return new TokenCleaner().cleanTokens(token);
	}
	
	protected static EarlyToken generateEarlyTokenChain(byte[] bytes) {
		EarlyToken token = createTokenChain(bytes);
		token = combineTokens(token);
		return cleanTokens(token);
	}
	
	public static Token generateTokenChain(byte[] bytes) {
		EarlyToken earlyToken = generateEarlyTokenChain(bytes);
		
		List<Token> list = new ArrayList<>();
		while(earlyToken != null) {
			Token token = new Token(earlyToken.value);
			token.column = earlyToken.column;
			token.line = earlyToken.line;
			list.add(token);
			earlyToken = earlyToken.next();
		}
		
		Token entry = list.get(0);
		Token token = entry;
		for(Token t : list) {
			token.next = t;
			t.prev = token;
			token = t;
		}
		
		return entry.next;
	}
}
