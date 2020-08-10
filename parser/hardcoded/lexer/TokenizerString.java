package hardcoded.lexer;

import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.regex.Matcher;

public class TokenizerString implements CharSequence {
	private byte[] bytes;
	private int index;
	
	/**
	 * Create a new TokenizerString that is used by the lexer.
	 * 
	 * @param bytes
	 * @throws NullPointerException if the array was null
	 */
	public TokenizerString(byte[] bytes) {
		this.bytes = bytes.clone();
	}
	
	/**
	 * Create a new TokenizerString that is used by the lexer.
	 * The string will be converted to bytes using the <code>ISO_8859_1</code> charset.
	 * 
	 * @param string
	 * @throws NullPointerException if the string was null
	 */
	public TokenizerString(String string) {
		this(string, StandardCharsets.ISO_8859_1);
	}
	
	/**
	 * Create a new TokenizerString that is used by the lexer.
	 * 
	 * @param string
	 * @param charset
	 * @throws NullPointerException if the string was null
	 */
	public TokenizerString(String string, Charset charset) {
		bytes = string.getBytes(charset);
	}
	
	@Override
	public char charAt(int index) {
		return (char)Byte.toUnsignedInt(bytes[this.index + index]);
	}
	
	@Override
	public int length() {
		return bytes.length - index;
	}
	
	int index() {
		return index;
	}
	
	int indexOf(String string) {
		return indexOf(0, string);
	}
	
	int indexOf(int fromIndex, String string) {
		if(string == null || string.isEmpty()) return -1;
		
		int sl = string.length();
		for(int i = fromIndex; i < length() - sl; i++) {
			for(int j = 0; j < sl; j++) {
				if(string.charAt(j) != charAt(i + j)) break;
				if(j == sl - 1) return i;
			}
		}
		
		return -1;
	}
	
	void move(int index) {
		this.index += index;
	}
	
	// Returns the length of the match and -1 if no match
	int matches(Tokenizer.Rule rule) {
		if(rule.isString()) {
			String string = rule.string();
			if(string.length() >= length()) return -1;
			for(int i = 0; i < string.length(); i++) if(string.charAt(i) != charAt(i)) return -1;
			return string.length();
		} else if(rule.isPattern()) {
			Matcher matcher = rule.pattern().matcher(this);
			if(!matcher.lookingAt()) return -1;
			return matcher.end();
		}
		
		return -1;
	}
	
	@Override
	public CharSequence subSequence(int start, int end) {
		byte[] next = new byte[end - start];
		System.arraycopy(bytes, index + start, next, 0, next.length);
		return new TokenizerString(next);
	}
	
	@Override
	public String toString() {
		return new String(bytes, index, length());
	}
}
