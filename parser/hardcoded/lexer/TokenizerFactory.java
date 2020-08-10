package hardcoded.lexer;

import java.io.*;
import java.util.*;

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
			
			System.out.println("=======================================================");
			
			byte[] lang = readFileBytes(new File("res/lexer/hc_specify.hc"));
			lexer.parse(lang);
			
			System.out.println("=======================================================");
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
	
	private static final boolean isEscapable(char c) {
		return c == 'x' || c == 'u' || c == 'r' || c == 'n' || c == 't' || c == 'b' || c == '\\' || c == '\'' || c == '\"';
	}
	
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
		List<String> delimiter = new ArrayList<String>();
		
		boolean hasLine = true;
		boolean hasRegex = false;
		boolean hasDiscard = false;
		boolean hasDelimiter = false;
		boolean hasDelimiterStart = false;
		boolean hasDelimiterComma = false;
		
		String regexString = null;
		
		Tokenizer lexer = new Tokenizer();
		SymbolGroup group = null;
		
		int lineIndex = 1;
		for(int i = 0; i < chars.length; i++) {
			char c = chars[i];
			
			if(Character.isWhitespace(c)) {
				if(c == '\n') {
					hasLine = !hasDelimiter;
					lineIndex++;
				}
				
				continue;
			}
			
			if(hasDelimiter) {
				if(!hasDelimiterStart) {
					if(c != '(') throw new LexicalException("(line:" + lineIndex + " column:" + getColumn(chars, i) + ") Illegal delimiter start character '" + escape(c) + "' expected '(' \"" + getLine(chars, i) + "\"");
					hasDelimiterStart = true;
					hasDelimiterComma = true;
					continue;
				} else if(c == ')') {
					hasDelimiterStart = false;
					hasDelimiterComma = false;
					hasDelimiter = false;
					
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
					if(!hasDelimiterComma) throw new LexicalException("(line:" + lineIndex + " column:" + getColumn(chars, i) + ") Delimiter items needs to be comma separated. \"" + getLine(chars, i) + "\"");
					hasDelimiterComma = false;
					
					delimiter.add(string);
				} else {
					group.addString(string);
				}
				
				i += string.length() + 1;
			} else if(c == '[' || c == ']') {
				if(hasDelimiter) throw new LexicalException("(line:" + lineIndex + " column:" + getColumn(chars, i) + ") Only strings are allowed inside delimiters. \"" + getLine(chars, i) + "\"");
				
				if(c == ']') {
					if(!hasRegex) throw new LexicalException("(line:" + lineIndex + " column:" + getColumn(chars, i) + ") Invalid character '" + escape(c) + "' \"" + getLine(chars, i) + "\"");
					if(regexString == null) throw new LexicalException("(line:" + lineIndex + " column:" + getColumn(chars, i) + ") Empty regex bracket. \"" + getLine(chars, i) + "\"");
					
					group.addRegex(regexString);
				} else if(hasRegex) {
					throw new LexicalException("(line:" + lineIndex + " column:" + getColumn(chars, i) + ") Only strings are allowed inside regex brackets. \"" + getLine(chars, i) + "\"");
				}
				
				hasRegex = (c == '['); 
				regexString = null;
			} else if(c == '%') {
				String string = getSpecialLiteral(chars, lineIndex, i);
				i += string.length();
				
				if(string.equals("DELIMITER")) {
					if(hasDelimiter) throw new LexicalException("(line:" + lineIndex + " column:" + getColumn(chars, i) + ") You cannot nest the delimiter keyword. \"" + getLine(chars, i) + "\"");
					hasDelimiter = true;
				} else if(string.equals("DISCARD")) {
					if(!hasLine) throw new LexicalException("(line:" + lineIndex + " column:" + getColumn(chars, i) + ") Invalid placement of the discard keyword. \"" + getLine(chars, i) + "\"");
					if(hasDiscard) throw new LexicalException("(line:" + lineIndex + " column:" + getColumn(chars, i) + ") Multiple instances of the discard keyword. \"" + getLine(chars, i) + "\"");
					hasDiscard = true;
					continue;
				} else {
					throw new LexicalException("(line:" + lineIndex + " column:" + getColumn(chars, i) + ") Invalid special keyword '" + string + "' \"" + getLine(chars, i) + "\"");
				}
			} else {
				if(hasLine) {
					String string = getItemName(chars, lineIndex, i);
					
					String name = string.trim();
					if(lexer.contains(name)) throw new LexicalException("(line:" + lineIndex + ") Duplicate definitions. \"" + getLine(chars, i) + "\"");
					group = lexer.addGroup(name).setDiscard(hasDiscard);
					
					hasDiscard = false;
					lineIndex += StringUtils.countInstances(string, '\n');
					i += string.length();
				} else {
					if(hasDelimiter && c == ',') {
						if(hasDelimiterComma) throw new LexicalException("(line:" + lineIndex + " column:" + getColumn(chars, i) + ") Empty delimiter element. \"" + getLine(chars, i) + "\"");
						hasDelimiterComma = true;
						
						if(delimiter.size() > 2) throw new LexicalException("(line:" + lineIndex + ") Invalid delimiter size. Expected 3 arguments. \"" + getLine(chars, i) + "\"");
					} else {
						throw new LexicalException("(line:" + lineIndex + " column:" + getColumn(chars, i) + ") Invalid character '" + escape(c) + "' \"" + getLine(chars, i) + "\"");
					}
				}
			}
			
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
				return literal.toString();
			} else {
				literal.append(c);
			}
		}
		
		throw new UnclosedQuoteException("(line:" + lineIndex + ") Quote was not closed properly. \"" + getLine(chars, index) + "\"");
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
			if(readName && !Character.isJavaIdentifierPart(c)) readName = false;
			
			if(!readName) {
				if(!Character.isWhitespace(c)) {
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
			
			if(!Character.isJavaIdentifierPart(c)) break;
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
