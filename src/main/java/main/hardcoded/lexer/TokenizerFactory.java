package hardcoded.lexer;

import java.io.*;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;

import hardcoded.lexer.Tokenizer.SymbolGroup;
import hardcoded.utils.StringUtils;

public final class TokenizerFactory {
	private static final Tokenizer READER;
	
	static {
		Tokenizer lexer = new Tokenizer();
		READER = lexer.getImmutableTokenizer();
		
		lexer.add("WHITESPACE", true).addRegexes("[ \t\r\n]", "#[^\r\n]*");
		lexer.add("SPECIAL").addStrings("%DISCARD", "%DELIMITER");
		lexer.add("DELIMITER").addStrings("[", "]", "(", ")", ",", ":");
		lexer.add("ITEMNAME").addRegex("[a-zA-Z0-9_]+([ \t\r\n]*):");
		lexer.add("LITERAL").addRegexes(
			"\'[^\'\\\\]*(?:\\\\.[^\'\\\\]*)*\'",
			"\"[^\"\\\\]*(?:\\\\.[^\"\\\\]*)*\""
		);
	}
	
	private TokenizerFactory() {}
	
	/**
	 * Returns a new tokenizer.
	 * @return a new tokenizer
	 */
	public static Tokenizer createNew() {
		return new Tokenizer();
	}
	
	/**
	 * Returns a new tokenizer from a file.
	 * 
	 * @param	pathname	a pathname string
	 * @return	a new tokenizer from a file
	 */
	public static Tokenizer loadFromFile(String pathname) throws IOException {
		return loadFromFile(new File(pathname));
	}
	
	/**
	 * Returns a new tokenizer from a file.
	 * 
	 * @param	parent		the path of the parent folder
	 * @param	fileName	the name of the file
	 * @return	a new tokenizer from a file
	 */
	public static Tokenizer loadFromFile(File parent, String fileName) throws IOException {
		return loadFromFile(new File(parent, fileName));
	}
	
	/**
	 * Returns a new tokenzier from a file.
	 * 
	 * @param	file	the file to read
	 * @return	a new tokenizer from a file
	 */
	public static Tokenizer loadFromFile(File file) throws IOException {
		FileInputStream stream = new FileInputStream(file);
		
		try {
			return load(stream);
		} finally {
			stream.close();
		}
	}
	
	/**
	 * Returns a new tokenizer from a input string using {@code StandardCharsets.ISO_8859_1} as the charset.
	 * 
	 * @param	content	a string containing the tokenizer
	 * @return	a new tokenizer
	 * @throws	NullPointerException
	 * 			if the string was {@code null}
	 */
	public static Tokenizer loadFromString(String content) {
		return loadFromString(content, StandardCharsets.ISO_8859_1);
	}
	
	/**
	 * Returns a new tokenizer from a input string using a specified charset.
	 * 
	 * @param	content	a string containing the tokenizer
	 * @param	charset	the charset of the string
	 * @return	a new tokenizer
	 * @throws	NullPointerException
	 * 			if the string was {@code null}
	 */
	public static Tokenizer loadFromString(String content, Charset charset) {
		return parseLexer(content.getBytes(charset));
	}
	
	/**
	 * Returns a new tokenizer from a {@code InputStream}.
	 * This function does not close the stream.
	 * 
	 * @param	stream	the inputstring containing the tokenizer
	 * @return	a new tokenizer
	 * @throws	NullPointerException
	 * 			if the stream was {@code null}
	 */
	public static Tokenizer load(InputStream stream) throws IOException {
		if(stream == null) throw new NullPointerException("The input stream was null.");
		
		ByteArrayOutputStream bs = new ByteArrayOutputStream();
		
		byte[] buffer = new byte[65536];
		int readBytes = 0;
		
		while((readBytes = stream.read(buffer)) != -1) {
			bs.write(buffer, 0, readBytes);
		}
		
		return parseLexer(bs.toByteArray());
	}
	
	/**
	 * Returns a new tokenizer from a byte array.
	 * @param	bytes	the array to create the tokenizer from
	 * @return	a new tokenizer from a byte array
	 * @throws	LexicalException
	 * 			failed to parse the lexer
	 */
	private static Tokenizer parseLexer(byte[] bytes) throws LexicalException {
		Token token = READER.parse(bytes);
		Tokenizer lexer = new Tokenizer();
		SymbolGroup group = null;
		
		Token stuckSafe = null;
		while(token != null) {
			if(stuckSafe == token) {
				throw new LexicalException(stuckSafe, "While loop got stuck so aborting reading this lexer!!!");
			}
			
			stuckSafe = token;
			
			boolean hasDiscard = false;
			if(token.equals("SPECIAL", "%DISCARD")) {
				if(!token.next().groupEquals("ITEMNAME"))
					throw new LexicalException(token, "Expected a item group after the discard keyword");
				
				hasDiscard = true;
				token = token.next();
			}
			
			if(token.groupEquals("ITEMNAME")) {
				String name = token.value();
				// The name ends with a colon so we need to remove it
				name = name.substring(0, name.length() - 1).trim();
				
				group = lexer.add(name);
				if(group == null) throw new LexicalException(token, "Item group has already been defined");
				
				group.setDiscard(hasDiscard);
				token = token.next(1);
				continue;
			}
			
			if(group == null)
				throw new LexicalException(token, "Invalid syntax '" + token + "'");
			
			if(token.valueEquals("[")) {
				if(token.remaining() < 2)
					throw new LexicalException(token, "Invalid regex syntax. Expected [ 'regex' ]");
				
				if(!token.next(2).equals("DELIMITER", "]"))
					throw new LexicalException(token.next(2), "Invalid regex syntax. Expected closing bracket ']'");
				
				Token item = token.next(1);
				if(!item.groupEquals("LITERAL"))
					throw new LexicalException(item, "Invalid regex syntax. Expected a string literal");
				
				String literal = item.value.substring(1, item.value.length() - 1);
				group.addRegex(StringUtils.unescapeString(literal));
				token = token.next(3);
				continue;
			} else if(token.groupEquals("LITERAL")) {
				String value = token.value();
				value = value.substring(1, value.length() - 1);
				group.addString(StringUtils.unescapeString(value));
				token = token.next();
				continue;
			} else if(token.equals("SPECIAL", "%DELIMITER")) {
				if(token.remaining() < 7) {
					throw new LexicalException(token, "Not enough arguments to create a delimiter.");
				}
				
				if(!token.next().equals("DELIMITER", "(")) {
					throw new LexicalException(token, "Invalid delimiter start character. '" + token.next(1) + "'");
				}
				
				if(!token.next(7).equals("DELIMITER", ")")) {
					throw new LexicalException(token, "Invalid delimiter end character. '" + token.next(7) + "'");
				}
				
				String[] args = new String[3];
				for(int j = 0; j < 3; j++) {
					Token item = token.next(2 + j * 2);
					if(!item.groupEquals("LITERAL"))
						throw new LexicalException(token, "Invalid delimiter item. Expected a literal");
					
					args[j] = StringUtils.unescapeString(item.value.substring(1, item.value.length() - 1));
					
					if(j < 2) {
						Token sep = token.next(3 + j * 2);
						
						if(!sep.equals("DELIMITER", ","))
							throw new LexicalException(token, "Invalid delimiter separator. Expected a comma");
					}
				}
				
				group.addDelimiter(args[0], args[1], args[2]);
				token = token.next(8);
				continue;
			}
			
			throw new LexicalException(token, "Invalid syntax '" + token + "'");
		}
		
		return lexer;
	}
}
