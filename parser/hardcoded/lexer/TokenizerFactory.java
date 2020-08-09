package hardcoded.lexer;

import java.io.*;
import java.util.*;
import java.util.regex.Pattern;

import hardcoded.lexer.Tokenizer.SymbolGroup;
import hardcoded.utils.StringUtils;
import hc.errors.lexer.EscapedCharacterException;
import hc.errors.lexer.LexicalException;
import hc.errors.lexer.UnclosedQuoteException;

public class TokenizerFactory {
	private static byte[] readFileBytes(File file) {
		ByteArrayOutputStream bs = new ByteArrayOutputStream();
		
		try(DataInputStream stream = new DataInputStream(new FileInputStream(file))) {
			byte[] buffer = new byte[65536];
			int readBytes = 0;
			
			while((readBytes = stream.read(buffer)) != -1) {
				bs.write(buffer, 0, readBytes);
			}
		} catch(IOException e) {
			e.printStackTrace();
		}
		
		return bs.toByteArray();
	}
	
	public static void main(String[] args) {
		byte[] bytes = readFileBytes(new File("res/lexer/hc.lex"));
		
		TokenizerFactory factory = new TokenizerFactory();
		
		try {
			Tokenizer lexer = factory.parse(bytes);
			
			lexer.dump();
			
		} catch(Exception e) {
			e.printStackTrace();
		}
	}
	
	public TokenizerFactory() {
		
	}
	
	public Tokenizer parse(byte[] bytes) {
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
		
		System.out.println("=======================================================");
		{
			String[] lines = string.split("\n");
			int num = (int)(Math.log10(lines.length + 1)) + 1;
			int index = 1;
			for(String line : string.split("\n")) {
				System.out.printf("%" + num + "d: %s\n", index++, line);
			}
		}
		System.out.println("=======================================================");
		
		return parseLexer(string.toCharArray());
	}
	
	private static final boolean isDelimiter(char c) {
		return !Character.isJavaIdentifierPart(c);
	}
	
	private static final boolean isEscapable(char c) {
		return c == 'x' || c == 'u' || c == 'r' || c == 'n' || c == 't' || c == 'b' || c == '\\' || c == '\'' || c == '\"';
	}
	
	private static final boolean isWhitespace(char c) {
		return Character.isWhitespace(c);
	}
	
//	private static final boolean containsWhitespace(String string) {
//		for(int i = 0; i < string.length(); i++) {
//			if(Character.isWhitespace(string.charAt(i))) return true;
//		}
//		return false;
//	}
	
	private static final String escape(char c) {
		return StringUtils.escapeString(Character.toString(c));
	}
	
	/**
	 * 
	 * @param chars
	 * @return
	 * @throws EscapedCharacterException
	 * @throws UnclosedQuoteException
	 * @throws LexicalException
	 */
	private Tokenizer parseLexer(char[] chars) {
		List<Object> delimiter = new ArrayList<Object>();
		
		boolean hasLine = true;
		boolean hasRegex = false;
		boolean hasDiscard = false;
		boolean hasDelimiter = false;
		boolean hasDelimiterStart = false;
		
		String regexString = null;
		
		Tokenizer lexer = new Tokenizer();
		SymbolGroup group = null;
		
		int lineIndex = 1;
		for(int i = 0; i < chars.length; i++) {
			char c = chars[i];
			
			if(isWhitespace(c)) {
				if(c == '\n') {
					hasLine = true;
					lineIndex++;
				}
				
				continue;
			}
			
			if(hasDelimiter) {
				if(!hasDelimiterStart) {
					if(c != '(') throw new LexicalException("(line:" + lineIndex + " column:" + getColumn(chars, i) + ") Illegal marco start character '" + escape(c) + "' expected '(' \"" + getLine(chars, i) + "\"");
					hasDelimiterStart = true;
					continue;
				} else if(c == ')') {
					hasDelimiterStart = false;
					hasDelimiter = false;
					
					if(delimiter.size() != 3) throw new LexicalException("(line:" + lineIndex + ") Invalid delimiter size. Expected 3 arguments. \"" + getLine(chars, i) + "\"");
					
					group.addDelimiter(
						delimiter.get(0),
						delimiter.get(1),
						delimiter.get(2)
					);
					
					delimiter.clear();
					continue;
				}
			}
			
			if(c == '\'' || c == '\"') {
				String string = getStringLiteral(chars, lineIndex, i);
				
				if(hasRegex) {
					if(regexString != null) throw new LexicalException("(line:" + lineIndex + " column:" + getColumn(chars, i) + ") Too many strings inside regex bracket. \"" + getLine(chars, i) + "\"");
					regexString = string;
				} else if(hasDelimiter) {
					delimiter.add(string);
				} else {
					group.addString(string);
				}
				
				i += string.length() + 1;
			} else if(c == '[') {
				if(hasRegex) throw new LexicalException("(line:" + lineIndex + " column:" + getColumn(chars, i) + ") Only strings are allowed inside regex brackets. \"" + getLine(chars, i) + "\"");
				hasRegex = true;
				regexString = null;
			} else if(c == ']') {
				if(!hasRegex) throw new LexicalException("(line:" + lineIndex + " column:" + getColumn(chars, i) + ") Invalid character '" + escape(c) + "' \"" + getLine(chars, i) + "\"");
				if(regexString == null) throw new LexicalException("(line:" + lineIndex + " column:" + getColumn(chars, i) + ") Empty regex bracket. \"" + getLine(chars, i) + "\"");
				hasRegex = false;
				
				if(hasDelimiter) {
					delimiter.add(Pattern.compile(regexString));
				} else {
					group.addRegex(regexString);
				}
			} else if(c == '%') {
				String string = getSpecialLiteral(chars, lineIndex, i);
				i += string.length();
				
				switch(string) {
					case "DISCARD": {
						if(!hasLine) throw new LexicalException("(line:" + lineIndex + " column:" + getColumn(chars, i) + ") Invalid placement of the discard keyword. \"" + getLine(chars, i) + "\"");
						if(hasDiscard) throw new LexicalException("(line:" + lineIndex + " column:" + getColumn(chars, i) + ") Multiple instances of the discard keyword. \"" + getLine(chars, i) + "\"");
						hasDiscard = true;
						continue;
					}
					case "DELIMITER": {
						if(hasDelimiter) throw new LexicalException("(line:" + lineIndex + " column:" + getColumn(chars, i) + ") You cannot nest the delimiter keyword. \"" + getLine(chars, i) + "\"");
						hasDelimiter = true;
						break;
					}
					case "NONE": {
						if(!hasDelimiter) throw new LexicalException("(line:" + lineIndex + " column:" + getColumn(chars, i) + ") The none keyword only works inside a delimiter. \"" + getLine(chars, i) + "\"");
						delimiter.add(null);
						break;
					}
					default: throw new LexicalException("(line:" + lineIndex + " column:" + getColumn(chars, i) + ") Invalid special keyword '" + string + "' \"" + getLine(chars, i) + "\"");
				}
			} else if(hasDelimiter) {
				if(c != ',') throw new LexicalException("(line:" + lineIndex + " column:" + getColumn(chars, i) + ") Invalid character '" + escape(c) + "' \"" + getLine(chars, i) + "\"");
			} else if(hasLine) {
				String string = getItemName(chars, lineIndex, i);
				
				String name = string.trim();
				if(lexer.contains(name)) throw new LexicalException("(line:" + lineIndex + ") Duplicate definitions. \"" + getLine(chars, i) + "\"");
				
				group = lexer.addGroup(name).setDiscard(hasDiscard);
				
				hasDiscard = false;
				lineIndex += StringUtils.countInstances(string, '\n');
				i += string.length();
			} else throw new LexicalException("(line:" + lineIndex + " column:" + getColumn(chars, i) + ") Invalid character '" + escape(c) + "' \"" + getLine(chars, i) + "\"");
			
			if(hasDiscard) throw new LexicalException("(line:" + lineIndex + " column:" + getColumn(chars, i) + ") Invalid placement of the discard keyword. \"" + getLine(chars, i) + "\"");
			
			hasLine = false;
		}
		
		return lexer;
	}
	
	private String getStringLiteral(char[] chars, int lineIndex, int index) {
		StringBuilder literal = new StringBuilder();
		char quoteType = chars[index];
		
		for(int i = index + 1; i < chars.length; i++) {
			char c = chars[i];
			
			if(c == '\n') throw new UnclosedQuoteException("(line:" + lineIndex + ") Quote was not closed properly. \"" + getLine(chars, i) + "\"");
			if(c == '\\') {
				if(i + 1 >= chars.length) {
					throw new UnclosedQuoteException("(line:" + lineIndex + ") Quote was not closed properly. \"" + getLine(chars, i) + "\"");
				}
				
				char next = chars[i + 1];
				
				if(!isEscapable(next)) {
					throw new EscapedCharacterException("(line:" + lineIndex + " column:" + getColumn(chars, i + 1) + ") The character '" + next + "' is not a valid escape character. \"" + getLine(chars, i) + "\"");
				}
				
				literal.append('\\').append(next);
				i++;
			} else if(c == quoteType) {
				quoteType = 0;
				break;
			} else {
				literal.append(c);
			}
		}
		
		if(quoteType != 0) {
			throw new UnclosedQuoteException("(line:" + lineIndex + ") Quote was not closed properly. \"" + getLine(chars, index) + "\"");
		}
		
		return literal.toString();
	}
	
	private String getItemName(char[] chars, int lineIndex, int index) {
		StringBuilder itemName = new StringBuilder();
		boolean readName = true;
		
		for(int i = index; i < chars.length; i++) {
			char c = chars[i];
			
			if(c == '\n') {
				lineIndex++;
			}
			
			itemName.append(c);
			if(readName && isDelimiter(c)) readName = false;
			
			if(!readName) {
				if(!isWhitespace(c)) {
					if(c != ':') {
						throw new LexicalException("(line:" + lineIndex + " column:" + getColumn(chars, i) + ") Expected a semicolon but got '" + c + "' \"" + getLine(chars, i) + "\"");
					}
					
					if(itemName.length() < 2) {
						throw new LexicalException("(line:" + lineIndex + ") A itemName cannot be empty.");
					}
					
					itemName.deleteCharAt(itemName.length() - 1);
					return itemName.toString();
				}
			}
		}
		
		throw new LexicalException("(line:" + lineIndex + ") ItemName was never closed properly.");
	}
	
	private String getSpecialLiteral(char[] chars, int lineIndex, int index) {
		StringBuilder literal = new StringBuilder();
		
		for(int i = index + 1; i < chars.length; i++) {
			char c = chars[i];
			
			if(isDelimiter(c)) break;
			literal.append(c);
		}
		
		return literal.toString();
	}
	
	private int getColumn(char[] chars, int index) {
		int start = index;
		for(; start > 0; start--) {
			if(chars[start - 1] == '\n') break;
		}
		
		return index - start + 1;
	}
	
	private String getLine(char[] chars, int index) {
		for(; index > 0; index--) {
			if(chars[index - 1] == '\n') break;
		}
		
		StringBuilder line = new StringBuilder();
		
		for(; index < chars.length; index++) {
			if(chars[index] == '\n') break;
			line.append(chars[index]);
		}
		
		return line.toString();
	}
	
	public static TokenizerBuilder create() {
		return new TokenizerBuilder();
	}
}
