package hc.token;

public class Tokeniser {
	public Tokeniser() {}
	
	private char[] chars;
	private int index;
	public Token createTokenTree(byte[] bytes) {
		chars = new char[bytes.length];
		index = 0;
		
		for(int i = 0; i < bytes.length; i++) {
			chars[i] = (char)Byte.toUnsignedInt(bytes[i]);
		}
		
		Token start = new Token();
		Token prev = start;
		
		Token token;
		while((token = next()) != null) {
			prev.next = token;
			token.prev = prev;
			prev = token;
			// System.out.println("word: '" + token + "'");
		}
		
		return start;
	}
	
	protected boolean isDelimiter(char c) {
		return !(Character.isAlphabetic(c)
			   | Character.isDigit(c)
			   | c == '_');
	}
	
	protected Token next() {
		if(index >= chars.length) return null;
		
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
		
		Token token = new Token();
		token.value = buffer;
		
		return token;
	}
}
