package hardcoded.utils;

import java.lang.reflect.Array;
import java.util.List;

import hardcoded.utils.error.MalformedEscapeException;

public final class StringUtils {
	private StringUtils() {}
	
	public static String join(CharSequence separator, List<?> list) {
		if(list == null) return null;
		return join(separator, list.toArray());
	}
	
	public static String join(CharSequence separator, Object array) {
		if(array == null) return null;
		
		try {
			int length = Array.getLength(array);
			
			StringBuilder sb = new StringBuilder();
			for(int i = 0; i < length; i++)
				sb.append(separator).append(Array.get(array, i));
			
			if(length > 0)
				sb.delete(0, separator.length());
			
			return sb.toString();
		} catch(IllegalArgumentException e) {
			return null;
		}
	}
	
	/**
	 * Convertes all instances of <code>[\'] [\"] [\\] [\r] [\n] [\b] [\t] [\x..] [&bsol;u....]</code> to the correct character.
	 * 
	 * @param string
	 * @return the unescaped string
	 * @throws MalformedEscapeException
	 */
	public static String unescapeString(String string) {
		if(string == null) return null;
		
		StringBuilder sb = new StringBuilder();
		boolean escape = false;
		
		for(int i = 0; i < string.length(); i++) {
			char c = string.charAt(i);
			
			if(escape) {
				escape = false;
				
				switch(c) {
					case '\'': case '\"': case '\\': sb.append(c); break;
					case '0': sb.append('\0'); break;
					case 'r': sb.append('\r'); break;
					case 'n': sb.append('\n'); break;
					case 'b': sb.append('\b'); break;
					case 't': sb.append('\t'); break;
					case 'x': {
						if(i + 3 > string.length()) {
							throw new MalformedEscapeException("(index:" + i + ") Not enough characters for '\\x..' escape.");
						}
						
						String hex = string.substring(i + 1, i + 3);
						
						try {
							sb.append((char)(int)Integer.valueOf(hex, 16));
						} catch(NumberFormatException e) {
							throw new MalformedEscapeException("(index:" + i + ") Invalid escape '\\x" + hex + "'");
						}
						
						i += 2;
						break;
					}
					case 'u': {
						if(i + 5 > string.length()) {
							throw new MalformedEscapeException("(index:" + i + ") Not enough characters for '\\u....' escape.");
						}
						
						String hex = string.substring(i + 1, i + 5);
						
						try {
							sb.append((char)(int)Integer.valueOf(hex, 16));
						} catch(NumberFormatException e) {
							throw new MalformedEscapeException("(index:" + i + ") Invalid escape '\\u" + hex + "'");
						}
						
						i += 4;
						break;
					}
					
					default: {
						throw new MalformedEscapeException("(index:" + i + ") Invalid character escape '\\" + c + "'");
					}
				}
			} else if(c == '\\') {
				escape = true;
			} else {
				sb.append(c);
			}
		}
		
		return sb.toString();
	}
	
	
	public static String escapeString(String string) {
		if(string == null) return null;
		
		StringBuilder sb = new StringBuilder();
		
		for(int i = 0; i < string.length(); i++) {
			char c = string.charAt(i);
			
			switch(c) { // Normal escapes
				case '\r': sb.append("\\r"); continue;
				case '\n': sb.append("\\n"); continue;
				case '\b': sb.append("\\b"); continue;
				case '\t': sb.append("\\t"); continue;
				case '\'': sb.append("\\\'"); continue;
				case '\"': sb.append("\\\""); continue;
				case '\\': sb.append("\\\\"); continue;
			}
			
			if(c > 0xff) { // Unicode
				sb.append("\\u").append(toHexString(c, 4));
				continue;
			}
			
			if(Character.isISOControl(c)) { // Control character
				sb.append("\\x").append(toHexString(c, 2));
				continue;
			}
			
			sb.append(c);
		}
		
		return sb.toString();
	}
	
	/**
	 * Escapes a string so that it becomes regex safe.
	 * @param string
	 * @return
	 */
	public static String regexEscape(String string) {
		if(string == null) return null;
		
		StringBuilder sb = new StringBuilder();
		
		for(int i = 0; i < string.length(); i++) {
			char c = string.charAt(i);
			
			switch(c) { // Normal escapes
				case '\0': sb.append("\\0"); continue;
				case '\n': sb.append("\\n"); continue;
				case '\r': sb.append("\\r"); continue;
				case '\t': sb.append("\\t"); continue;
				case '\\': sb.append("\\\\"); continue;
				
				case '^': case '$': case '?': case '|':
				case '*': case '/': case '+': case '.':
				case '(': case ')': case '[': case ']': 
					sb.append("\\").append(c); continue;
			}
			
			if(c > 0xff) { // Unicode
				sb.append("\\u").append(toHexString(c, 4));
				continue;
			}
			
			if(Character.isISOControl(c)) { // Control character
				sb.append("\\x").append(toHexString(c, 2));
				continue;
			}
			
			sb.append(c);
		}
		
		return sb.toString();
	}
	
	public static int countInstances(CharSequence string, char c) {
		if(string != null) {
			int count = 0;
			
			for(int i = 0; i < string.length(); i++) {
				count += (string.charAt(i) == c) ? 1:0;
			}
			
			return count;
		}
		
		return 0;
	}
	
	/**
	 * Converts a number into a hex string with a given minimum length.
	 * 
	 * @param value the value to be converted to a hex string.
	 * @param length the minimum length of that hex string.
	 * @return the returned hex string.
	 */
	public static String toHexString(long value, int length) {
		if(length < 1) throw new IllegalArgumentException("The minimum length of the returned string cannot be less than one.");
		return String.format("%0" + length + "x", value);
	}
}
