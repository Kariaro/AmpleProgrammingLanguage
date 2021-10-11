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
	 * This method will convert a number into a string using a custom base. Calling this
	 * method with a base of {@code "012346789abcdef"} will return a hexadecimal string
	 * and will do the same thing as calling {@code Long.toString(value, 16)}.
	 * 
	 * @param	value	the input number
	 * @param	base	the custom base
	 * @return	a {@code number} written with a custom base.
	 * @throws	NullPointerException
	 * 			if the base was {@code null}
	 * @throws	IllegalArgumentException
	 * 			if the base string had a length less than 2
	 */
	public static String toStringCustomBase(long value, String base) {
		return toStringCustomBase(value, base, true);
	}
	
	/**
	 * This method will convert a number into a string using a custom base. Calling this
	 * method with a base of {@code "012346789abcdef"} will return a hexadecimal string
	 * and will do the same thing as calling {@code Long.toString(value, 16)}.
	 * 
	 * <p>If {@code isAlphabetical} is {@code false} then the first character will be
	 * treated as a non zero character and will always be included in the output. The
	 * following is an example of how the base {@code "ab"} and {@code "01"} would convert
	 * the input number depending on the {@code isAlphabetical} parameter.
	 *<PRE>
	 *value:   false          true
	 *   0     [a ]:[0 ]      [a  ]:[0  ]
	 *   1     [b ]:[1 ]      [b  ]:[1  ]
	 *   2     [aa]:[00]      [ba ]:[10 ]
	 *   3     [ab]:[01]      [bb ]:[11 ]
	 *   4     [ba]:[10]      [baa]:[100]
	 *   5     [bb]:[11]      [bab]:[101]
	 *</PRE>
	 *
	 * @param	value	the input number
	 * @param	base	the custom base
	 * @param	isAlphabetical	if {@code false} will always treat the zero character as part of the number
	 * 					
	 * @return	a string of a {@code number} written in a custom base
	 * @throws	NullPointerException
	 * 			if the base string was {@code null}
	 * @throws	IllegalArgumentException
	 * 			if the base string had a length less than 2
	 */
	public static String toStringCustomBase(long value, String base, boolean isAlphabetical) {
		if(base == null) throw new NullPointerException();
		if(base.length() < 2) throw new IllegalArgumentException();
		
		StringBuilder sb = new StringBuilder();
		int length = base.length();
		int offset = isAlphabetical ? 0:1;
		
		// Allow negative values to be outputed. Will always turn a negative value positive
		// because the smallest allowed base is 2 and that base and all bases greater than 2
		// will remove the signed bit from the long value.
		if(value < 0) {
			long v = Long.remainderUnsigned(value, length);
			sb.append(base.charAt((int)v));
			value = Long.divideUnsigned(value - v, length) - offset;
		}
		
		while(true) {
			long v = value % length;
			sb.insert(0, base.charAt((int)v));
			
			if(value >= length) {
				value = ((value - v) / length) - offset;
			} else break;
		}
		
		return sb.toString();
	}
	
	/**
	 * Convertes all instances of <code>[\'] [\"] [\\] [\r] [\n] [\b] [\t] [\x..] [&bsol;u....]</code> to the correct character.
	 * 
	 * @param	string
	 * @return	a unescaped string
	 * @throws	MalformedEscapeException
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
	 * @param	string
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
				case '{': case '}':
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
	 * @param	value	the value to be converted to a hex string
	 * @param	length	the minimum length of that hex string
	 * @return	a hex string
	 */
	public static String toHexString(long value, int length) {
		if(length < 1) throw new IllegalArgumentException("The minimum length of the returned string cannot be less than one.");
		return String.format("%0" + length + "x", value);
	}
	
	// NOTE: Why is this printHexString method not using a byte array?
	public static String printHexString(CharSequence separator, int[] array) {
		if(array == null || array.length == 0) return "";
		StringBuilder sb = new StringBuilder();
		for(int i : array) {
			sb.append(separator).append(String.format("%02x", i & 0xff));
		}
		
		return sb.toString().substring(separator.length()).trim();
	}
}
