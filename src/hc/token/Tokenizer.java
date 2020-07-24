package hc.token;

import java.util.ArrayList;
import java.util.List;

public class Tokenizer {
	private Tokenizer() {}
	
//	private char[] chars;
//	private int index;
//	
//	public Token createTokenChain(byte[] bytes) {
//		chars = new char[bytes.length];
//		index = 0;
//		
//		for(int i = 0; i < bytes.length; i++) {
//			chars[i] = (char)Byte.toUnsignedInt(bytes[i]);
//		}
//		
//		Token start = new Token();
//		Token prev = start;
//		
//		Token token;
//		while((token = next()) != null) {
//			prev.next = token;
//			token.prev = prev;
//			prev = token;
//		}
//		
//		return start;
//	}
	
	private static final boolean isDelimiter(char c) {
		return !(Character.isAlphabetic(c)
			   | Character.isDigit(c)
			   | c == '_');
	}
	
//	protected Token next() {
//		if(index >= chars.length) return null;
//		
//		String buffer = "";
//		char c = chars[index++];
//		buffer += c;
//		if(!isDelimiter(c)) {
//			while(index < chars.length) {
//				c = chars[index++];
//				if(isDelimiter(c)) {
//					index--;
//					break;
//				}
//				buffer += c;
//			}
//		}
//		
//		Token token = new Token();
//		token.value = buffer;
//		
//		return token;
//	}
	
	protected static final Token createTokenChain(byte[] bytes) {
		char[] chars = new char[bytes.length];
		int index = 0;
		
		for(int i = 0; i < bytes.length; i++) {
			chars[i] = (char)Byte.toUnsignedInt(bytes[i]);
		}
		
		Token start = new Token();
		Token token = start;
		
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
			
			// System.out.println("Token: '" + token + "' line=" + lineIndex + ", col=" + (startIndex - linePos) + ", index=" + index);
			
			Token next = new Token();
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
	
	protected static Token combineTokens(Token token) {
		return new TokenCombiner().combineTokens(token);
	}
	
	protected static Token cleanTokens(Token token) {
		return new TokenCleaner().cleanTokens(token);
	}
	
	public static Token generateTokenChain(byte[] bytes) {
		Token token = createTokenChain(bytes);
		token = combineTokens(token);
		return cleanTokens(token);
	}
	
	public static Symbol generateSymbolChain(byte[] bytes) {
		Token token = generateTokenChain(bytes);
		
		List<Symbol> list = new ArrayList<>();
		while(token != null) {
			Symbol symbol = new Symbol(token.value);
			symbol.column = token.column;
			symbol.line = token.line;
			list.add(symbol);
			token = token.next();
		}
		
		Symbol entry = list.get(0);
		Symbol symbol = entry;
		for(Symbol s : list) {
			symbol.next = s;
			s.prev = symbol;
			symbol = s;
		}
		
		return entry.next;
	}
}
