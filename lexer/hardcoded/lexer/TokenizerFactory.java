package hardcoded.lexer;

import java.io.*;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.List;

import hardcoded.lexer.Tokenizer.SymbolGroup;

public class TokenizerFactory {
	private static final Tokenizer READER;
	
	static {
		Tokenizer lexer = new Tokenizer();
		READER = lexer.getImmutableTokenizer();
		
		lexer.add("WHITESPACE", true).addRegexes("[ \t\r\n]", "#[^\r\n]*");
		lexer.add("SPECIAL").addStrings("%DISCARD", "%DELIMITER");
		lexer.add("DELIMITER").addStrings("[", "]", "(", ")", ",", ":");
		lexer.add("ITEMNAME").addRegex("[a-zA-Z0-9_]+([ \t\r\n]*)(?=:)");
		lexer.add("LITERAL").addRegexes(
			"\'[^\'\\\\]*(?:\\\\.[^\'\\\\]*)*\'",
			"\"[^\"\\\\]*(?:\\\\.[^\"\\\\]*)*\""
		);
	}
	
	private TokenizerFactory() {
		
	}
	
	/**
	 * Create a new instance of a tokenizer.
	 * @return The tokenizer.
	 */
	public static Tokenizer createNew() {
		return new Tokenizer();
	}
	
	/**
	 * Create a tokenizer from the content of a file.
	 * 
	 * @param filePath the path to the lexer file.
	 * @return A parsed lexer.
	 * @throws IOException
	 */
	public static Tokenizer loadFromFile(String filePath) throws IOException {
		return loadFromFile(new File(filePath));
	}
	
	/**
	 * Create a tokenizer from the content of a file.
	 * 
	 * @param parent the path of the parent folder.
	 * @param fileName the name of the lexer file.
	 * @return A parsed lexer.
	 * @throws IOException
	 */
	public static Tokenizer loadFromFile(File parent, String fileName) throws IOException {
		return loadFromFile(new File(parent, fileName));
	}
	
	/**
	 * Create a tokenizer from the content of a file.
	 * 
	 * @param file the lexer file.
	 * @return A parsed lexer.
	 * @throws IOException
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
	 * Create a tokenizer from the content of a string.
	 * This uses the ISO_8859_1 charset to decode the string.
	 * 
	 * @param content the string containing the lexer data.
	 * @return The parsed lexer.
	 * @throws NullPointerException if the content string was null.
	 */
	public static Tokenizer loadFromString(String content) {
		return loadFromString(content, StandardCharsets.ISO_8859_1);
	}
	
	/**
	 * Create a tokenizer from the content of a string using the specified charset.
	 * 
	 * @param content the string containing the lexer data.
	 * @param charset the charset that will be used to decode this string.
	 * @return The parsed lexer.
	 * @throws NullPointerException if the content string was null.
	 */
	public static Tokenizer loadFromString(String content, Charset charset) {
		return parseLexer(content.getBytes(charset));
	}
	
	/**
	 * Parses the input from a inputStream and returns the parsed lexer.<br>
	 * This function does not close the stream.
	 * 
	 * @param stream the inputStream that contains the data.
	 * @return A parsed lexer.
	 * @throws IOException
	 * @throws NullPointerException if the stream was null.
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
	 * This method parses reads from a byte array and creates a new tokenizer.
	 */
	private static Tokenizer parseLexer(byte[] bytes) throws LexicalException {
		List<TokenizerSymbol> list = READER.parse(bytes);
		
		Tokenizer lexer = new Tokenizer();
		SymbolGroup symbolGroup = null;
		
		boolean hasDiscard = false;
		for(int i = 0; i < list.size(); i++) {
			TokenizerSymbol sym = list.get(i);
			
			String group = sym.group();
			String value = sym.value();
			
			if(group == null) throw new LexicalException(sym, "Invalid syntax '" + value + "'");
			if(hasDiscard && !group.equals("ITEMNAME")) throw new LexicalException(sym, "Invalid placement of the discard keyword.");
			
			if(group.equals("ITEMNAME")) {
				symbolGroup = lexer.add(value.trim());
				if(symbolGroup == null) throw new LexicalException(sym, "Multiple definitions of with the same group name. '" + value.trim() + "'");
				
				symbolGroup.setDiscard(hasDiscard);
				hasDiscard = false;
				i++;
			} else if(group.equals("SPECIAL")) {
				if(value.equals("%DISCARD")) {
					if(hasDiscard) {
						throw new LexicalException(sym, "Multiple definitions of the discard keyword.");
					}
					
					if(i + 1 > list.size()) {
						throw new LexicalException(sym, "Invalid placement of the discard keyword.");
					}
					
					hasDiscard = true;
				} else if(value.equals("%DELIMITER")) {
					if(i + 7 > list.size()) {
						throw new LexicalException(sym, "Not enough arguments to create a delimiter.");
					}
					
					if(!list.get(i + 1).equals("DELIMITER", "(")) {
						throw new LexicalException(sym, "Invalid delimiter start character. '" + list.get(i + 1) + "'");
					}
					
					if(!list.get(i + 7).equals("DELIMITER", ")")) {
						throw new LexicalException(sym, "Invalid delimiter end character. '" + list.get(i + 7) + "'");
					}
					
					String[] args = new String[3];
					for(int j = 0; j < 3; j++) {
						TokenizerSymbol item = list.get(i + 2 + j * 2);
						if(!item.groupEquals("LITERAL")) {
							throw new LexicalException(sym, "Invalid delimiter item. Expected a literal, got '" + item + "'");
						}
						
						args[j] = item.value().substring(1, item.value().length() - 1);
						
						if(j < 2) {
							TokenizerSymbol sep = list.get(i + 3 + j * 2);
							
							if(!sep.equals("DELIMITER", ",")) {
								throw new LexicalException(sym, "Invalid delimiter separator. Expected a comma, got '" + sep + "'");
							}
						}
					}
					
					symbolGroup.addDelimiter(args[0], args[1], args[2]);
					i += 7;
				}
			} else if(sym.equals("DELIMITER", "[")) {
				if(symbolGroup == null) {
					throw new LexicalException(sym, "Invalid placement of a string literal.");
				}
				
				if(i + 2 > list.size()) {
					throw new LexicalException(sym, "Not enough arguments to create a regex bracket.");
				}
				
				if(!list.get(i + 2).equals("DELIMITER", "]")) {
					throw new LexicalException(list.get(i + 2), "Invalid regex close character. '" + list.get(i + 2) + "'");
				}
				
				TokenizerSymbol item = list.get(i + 1);
				if(!item.groupEquals("LITERAL")) {
					throw new LexicalException(item, "The regex match can only contain string literals.");
				}
				
				symbolGroup.addRegex(item.value().substring(1, item.value().length() - 1));
				i += 2;
			} else if(sym.groupEquals("LITERAL")) {
				if(symbolGroup == null) {
					throw new LexicalException(sym, "Invalid placement of a string literal.");
				}
				
				symbolGroup.addString(value.substring(1, value.length() - 1));
			} else {
				throw new LexicalException(sym, "Invalid character '" + value + "'");
			}
		}
		
		return lexer;
	}
}
