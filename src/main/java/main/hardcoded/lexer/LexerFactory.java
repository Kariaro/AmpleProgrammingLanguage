package hardcoded.lexer;

import java.io.*;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.List;

import hardcoded.lexer.LexerTokenizer.LexerGroup;
import hardcoded.utils.StringUtils;

public final class LexerFactory {
	private static final LexerTokenizer READER;
	
	static {
		LexerTokenizer lexer = new LexerTokenizer();
		lexer.addGroup("WHITESPACE", true).addRegexes("[ \t\r\n]", "#[^\r\n]*");
		lexer.addGroup("SPECIAL").addStrings("%DISCARD", "%DELIMITER");
		lexer.addGroup("DELIMITER").addStrings("[", "]", "(", ")", ",", ":");
		lexer.addGroup("ITEMNAME").addRegex("[a-zA-Z0-9_]+([ \t\r\n]*):");
		lexer.addGroup("LITERAL").addRegexes(
			"\'[^\'\\\\]*(?:\\\\.[^\'\\\\]*)*\'",
			"\"[^\"\\\\]*(?:\\\\.[^\"\\\\]*)*\""
		);
		
		READER = lexer.getImmutableTokenizer();
	}
	
	private LexerFactory() {}
	
	/**
	 * Returns a new tokenizer.
	 * @return a new tokenizer
	 */
	public static LexerTokenizer createNew() {
		return new LexerTokenizer();
	}
	
	/**
	 * Returns a new tokenizer from a file.
	 * 
	 * @param	pathname	a pathname string
	 * @return	a new tokenizer from a file
	 */
	public static LexerTokenizer loadFromFile(String pathname) throws IOException {
		return loadFromFile(new File(pathname));
	}
	
	/**
	 * Returns a new tokenizer from a file.
	 * 
	 * @param	parent	the path of the parent folder
	 * @param	child	the name of the file
	 * @return	a new tokenizer from a file
	 */
	public static LexerTokenizer loadFromFile(File parent, String child) throws IOException {
		return loadFromFile(new File(parent, child));
	}
	
	/**
	 * Returns a new tokenzier from a file.
	 * 
	 * @param	file	the file to read
	 * @return	a new tokenizer from a file
	 */
	public static LexerTokenizer loadFromFile(File file) throws IOException {
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
	public static LexerTokenizer loadFromString(String content) {
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
	public static LexerTokenizer loadFromString(String content, Charset charset) {
		return parseLexer(content.getBytes(charset));
	}
	
	/**
	 * Returns a new tokenizer from a {@code InputStream}.
	 * This function does not close the stream.
	 * 
	 * @param	stream	a inputstream
	 * @return	a new tokenizer
	 * @throws	NullPointerException
	 * 			if the stream was {@code null}
	 */
	public static LexerTokenizer load(InputStream stream) throws IOException {
		if(stream == null) throw new NullPointerException("The input stream was null");
		
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
	 * @param	bytes	a byte array
	 * @return	a new tokenizer
	 * @throws	LexicalException
	 * 			failed to parse the lexer
	 */
	private static LexerTokenizer parseLexer(byte[] bytes) throws LexicalException {
		List<Token> list = READER.parse(bytes);
		LexerTokenizer lexer = new LexerTokenizer();
		LexerGroup group = null;
		
		for(int i = 0; i < list.size(); i++) {
			Token token = list.get(i);
			
			boolean hasDiscard = false;
			if(token.equals("SPECIAL", "%DISCARD")) {
				if(!list.get(i + 1).groupEquals("ITEMNAME"))
					throw new LexicalException(token, "Expected a item group after the discard keyword");
				
				hasDiscard = true;
				token = list.get(++i);
			}
			
			if(token.groupEquals("ITEMNAME")) {
				String name = token.value;
				// The name ends with a colon so we need to remove it
				name = name.substring(0, name.length() - 1).trim();
				
				group = lexer.addGroup(name, hasDiscard);
				continue;
			}
			
			if(group == null)
				throw new LexicalException(token, "Invalid syntax '" + token + "'");
			
			if(token.valueEquals("[")) {
				if(i + 2 >= list.size())
					throw new LexicalException(token, "Invalid regex syntax. Expected [ 'regex' ]");
				
				Token next_1 = list.get(i + 1);
				Token next_2 = list.get(i + 2);
				if(!next_2.equals("DELIMITER", "]"))
					throw new LexicalException(next_2, "Invalid regex syntax. Expected closing bracket ']'");
				
				if(!next_1.groupEquals("LITERAL"))
					throw new LexicalException(next_1, "Invalid regex syntax. Expected a string literal");
				
				String literal = next_1.value.substring(1, next_1.value.length() - 1);
				group.addRegex(StringUtils.unescapeString(literal));
				i += 2;
				continue;
			} else if(token.groupEquals("LITERAL")) {
				String value = token.value;
				group.addString(StringUtils.unescapeString(value.substring(1, value.length() - 1)));
				continue;
			} else if(token.equals("SPECIAL", "%DELIMITER")) {
				if(i + 7 >= list.size()) {
					throw new LexicalException(token, "Not enough arguments to create a delimiter");
				}
				
				if(!list.get(i + 1).equals("DELIMITER", "(")) {
					throw new LexicalException(token, "Invalid delimiter start character. '" + list.get(i + 1) + "'");
				}
				
				if(!list.get(i + 7).equals("DELIMITER", ")")) {
					throw new LexicalException(token, "Invalid delimiter end character. '" + list.get(i + 7) + "'");
				}
				
				String[] args = new String[3];
				for(int j = 0; j < 3; j++) {
					Token item = list.get(i + 2 + j * 2);
					if(!item.groupEquals("LITERAL"))
						throw new LexicalException(token, "Invalid delimiter item. Expected a literal");
					
					args[j] = StringUtils.unescapeString(item.value.substring(1, item.value.length() - 1));
					
					if(j < 2) {
						Token sep = list.get(i + 3 + j * 2);
						if(!sep.equals("DELIMITER", ","))
							throw new LexicalException(token, "Invalid delimiter separator. Expected a comma");
					}
				}
				
				group.addDelimiter(args[0], args[1], args[2]);
				i += 7;
				continue;
			}
			
			throw new LexicalException(token, "Invalid syntax '" + token + "'");
		}
		
		return lexer;
	}
}
