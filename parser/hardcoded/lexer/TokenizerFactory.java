package hardcoded.lexer;

import java.io.*;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.List;

import hardcoded.lexer.Tokenizer.SymbolGroup;
import hc.errors.lexer.LexicalException;

public class TokenizerFactory {
	private static final Tokenizer DEFAULT;
	private static final Tokenizer READER;
	static {
		{
			Tokenizer lexer = new Tokenizer();
			READER = lexer.unmodifiableTokenizer();
			
			lexer.addGroup("WHITESPACE", true).addRegexes("[ \t\r\n]", "#[^\r\n]*");
			lexer.addGroup("SPECIAL").addStrings("%DISCARD", "%DELIMITER");
			lexer.addGroup("DELIMITER").addStrings("[", "]", "(", ")", ",", ":");
			lexer.addGroup("ITEMNAME").addRegex("[a-zA-Z0-9_]+([ \t\r\n]*)(?=:)");
			lexer.addGroup("LITERAL").addRegexes(
				"\'[^\'\\\\]*(?:\\\\.[^\'\\\\]*)*\'",
				"\"[^\"\\\\]*(?:\\\\.[^\"\\\\]*)*\""
			);
		}
		
		{
			Tokenizer lexer = new Tokenizer();
			DEFAULT = lexer.unmodifiableTokenizer();
			lexer.addGroup("COMMENT", true).addRegexes("\\/\\*.*?\\*\\/", "//[^\r\n]*");
			lexer.addGroup("SPACE", true).addRegex("[ \t\r\n]");
			lexer.addGroup("WORD").addRegex("[a-zA-Z0-9_]+");
			lexer.addGroup("LITERAL").addRegexes(
				"\'[^\'\\\\]*(?:\\\\.[^\'\\\\]*)*\'",
				"\"[^\"\\\\]*(?:\\\\.[^\"\\\\]*)*\""
			);
			lexer.addGroup("TOKEN").addRegex(".");
			lexer.setDefaultGroup("TOKEN");
		}
	}
	
	public static Tokenizer getDefaultLexer() {
		return DEFAULT;
	}
	
//	private static byte[] readFileBytes(File file) {
//		ByteArrayOutputStream bs = new ByteArrayOutputStream();
//		
//		try(DataInputStream stream = new DataInputStream(new FileInputStream(file))) {
//			byte[] buffer = new byte[65536];
//			int readBytes = 0;
//			
//			while((readBytes = stream.read(buffer)) != -1) {
//				bs.write(buffer, 0, readBytes);
//			}
//		} catch(IOException e) {
//			e.printStackTrace();
//		}
//		
//		return bs.toByteArray();
//	}
	
	public static void main(String[] args) {
		// byte[] bytes = readFileBytes(new File("res/lexer/hc.lex"));
		// bytes = readFileBytes(new File("res/lexer/hclexer.lex"));
		
		
		try {
			Tokenizer lexer = TokenizerFactory.loadFromFile("res/lexer/hc.lex");
			System.out.println("=======================================================");
			lexer.dump();
			System.out.println("=======================================================");
			
			
			// System.out.println("=======================================================");
			
			// byte[] lang = readFileBytes(new File("res/lexer/hc_specify.hc"));
			// lang = readFileBytes(new File("res/lexer/hc.lex"));
			// for(TSym sym : lexer.parse(lang)) System.out.printf("[%s] %s\n", sym.group(), sym.value());
			
			// System.out.println("=======================================================");
			
		} catch(Exception e) {
			e.printStackTrace();
		}
	}
	
	private TokenizerFactory() {
		
	}
	
	/**
	 * Creates a new instance of a tokenizer.
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
		return load(new FileInputStream(filePath));
	}
	
	/**
	 * Create a tokenizer from the content of a file.
	 * 
	 * @param file the lexer file.
	 * @return A parsed lexer.
	 * @throws IOException
	 */
	public static Tokenizer loadFromFile(File file) throws IOException {
		return load(new FileInputStream(file));
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
	 * Parses the input from a inputStream and returns the parsed lexer.
	 * 
	 * @param stream the inputStream that contains the data.
	 * @return A parsed lexer.
	 * @throws IOException
	 * @throws NullPointerException if the stream was null.
	 */
	public static Tokenizer load(InputStream stream) throws IOException {
		if(stream == null) throw new NullPointerException("The inputStream cannot be null.");
		
		ByteArrayOutputStream bs = new ByteArrayOutputStream();
		
		byte[] buffer = new byte[65536];
		int readBytes = 0;
		
		while((readBytes = stream.read(buffer)) != -1) {
			bs.write(buffer, 0, readBytes);
		}
		
		return parseLexer(bs.toByteArray());
	}
	
	/**
	 * 
	 * @param bytes
	 * @return
	 * @throws LexicalException
	 */
	private static Tokenizer parseLexer(byte[] bytes) {
		List<Symbol> list = READER.parse(bytes);
		
		Tokenizer lexer = new Tokenizer();
		SymbolGroup symbolGroup = null;
		
		boolean hasDiscard = false;
		for(int i = 0; i < list.size(); i++) {
			Symbol sym = list.get(i);
			
			String group = sym.group();
			String value = sym.value();
			
			if(group == null) {
				throw new LexicalException("(line:" + sym.line() + " column:" + sym.column() + ") Invalid syntax '" + value + "'");
			}
			
			if(hasDiscard && !group.equals("ITEMNAME")) {
				throw new LexicalException("(line:" + sym.line() + " column:" + sym.column() + ") Invalid placement of the discard keyword.");
			}
			
			if(group.equals("ITEMNAME")) {
				symbolGroup = lexer.addGroup(value.trim());
				if(symbolGroup == null) throw new LexicalException("(line:" + sym.line() + " column:" + sym.column() + ") Multiple definitions of with the same group name. '" + value.trim() + "'");
				
				symbolGroup.setDiscard(hasDiscard);
				hasDiscard = false;
				i++;
			} else if(group.equals("SPECIAL")) {
				if(value.equals("%DISCARD")) {
					if(hasDiscard) {
						throw new LexicalException("(line:" + sym.line() + " column:" + sym.column() + ") Multiple definitions of the discard keyword.");
					}
					
					if(i + 1 > list.size()) {
						throw new LexicalException("(line:" + sym.line() + " column:" + sym.column() + ") Invalid placement of the discard keyword.");
					}
					
					hasDiscard = true;
				} else if(value.equals("%DELIMITER")) {
					if(i + 7 > list.size()) {
						throw new LexicalException("(line:" + sym.line() + " column:" + sym.column() + ") Not enough arguments to create a delimiter.");
					}
					
					if(!list.get(i + 1).equals("DELIMITER", "(")) {
						throw new LexicalException("(line:" + sym.line() + " column:" + sym.column() + ") Invalid delimiter start character. '" + list.get(i + 1) + "'");
					}
					
					if(!list.get(i + 7).equals("DELIMITER", ")")) {
						throw new LexicalException("(line:" + sym.line() + " column:" + sym.column() + ") Invalid delimiter end character. '" + list.get(i + 7) + "'");
					}
					
					String[] args = new String[3];
					for(int j = 0; j < 3; j++) {
						Symbol item = list.get(i + 2 + j * 2);
						if(!item.groupEquals("LITERAL")) {
							throw new LexicalException("(line:" + sym.line() + " column:" + sym.column() + ") Invalid delimiter item. Expected a literal, got '" + item + "'");
						}
						
						args[j] = item.value().substring(1, item.value().length() - 1);
						
						if(j < 2) {
							Symbol sep = list.get(i + 3 + j * 2);
							
							if(!sep.equals("DELIMITER", ",")) {
								throw new LexicalException("(line:" + sym.line() + " column:" + sym.column() + ") Invalid delimiter separator. Expected a comma, got '" + sep + "'");
							}
						}
					}
					
					symbolGroup.addDelimiter(args[0], args[1], args[2]);
					i += 7;
				}
			} else if(sym.equals("DELIMITER", "[")) {
				if(symbolGroup == null) {
					throw new LexicalException("(line:" + sym.line() + " column:" + sym.column() + ") Invalid placement of a string literal.");
				}
				
				if(i + 2 > list.size()) {
					throw new LexicalException("(line:" + sym.line() + " column:" + sym.column() + ") Not enough arguments to create a regex bracket.");
				}
				
				if(!list.get(i + 2).equals("DELIMITER", "]")) {
					throw new LexicalException("(line:" + list.get(i + 2).line() + " column:" + list.get(i + 2).column() + ") Invalid regex close character. '" + list.get(i + 2) + "'");
				}
				
				Symbol item = list.get(i + 1);
				if(!item.groupEquals("LITERAL")) {
					throw new LexicalException("(line:" + item.line() + " column:" + item.column() + ") The regex match can only contain string literals.");
				}
				
				symbolGroup.addRegex(item.value().substring(1, item.value().length() - 1));
				i += 2;
			} else if(sym.groupEquals("LITERAL")) {
				if(symbolGroup == null) {
					throw new LexicalException("(line:" + sym.line() + " column:" + sym.column() + ") Invalid placement of a string literal.");
				}
				
				symbolGroup.addString(value.substring(1, value.length() - 1));
			} else {
				throw new LexicalException("(line:" + sym.line() + " column:" + sym.column() + ") Invalid character '" + value + "'");
			}
		}
		
		return lexer;
	}
	
	@SuppressWarnings("unused")
	@Deprecated private static final void printContent(byte[] bytes) {
		System.out.println("=======================================================");
		StringBuilder sb = new StringBuilder(bytes.length + 2);
		sb.append('\n').append(new String(bytes)).append('\n');
		
		// These regex replaces changes all line endings to '\n',
		// removes all comments and removes all empty lines.
		String string = sb.toString()
			.replaceAll("(\r\n|\r|\n)", "\n")
			.replaceAll("(?<=\n)([ \t]*)#.*?\n", "\n");
//			.replaceAll("(?<=\n)\n", "")
//			.trim();
		
		// Remove the characters we added before
		string = string.substring(1, string.length() - 1);
		
		String[] lines = string.split("\n");
		int num = (int)(Math.log10(lines.length + 1)) + 1;
		int index = 1;
		for(String line : string.split("\n")) {
			System.out.printf("%" + num + "d: %s\n", index++, line);
		}
		
		System.out.println("=======================================================");
	}
}
