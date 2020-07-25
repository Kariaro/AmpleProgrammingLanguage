package hardcoded.grammar;

import java.io.*;
import java.util.*;

import hc.token.Symbol;
import hc.token.Tokenizer;
import hardcoded.grammar.Grammar.*;

//https://en.wikipedia.org/wiki/LR_parser
//  * This class is not a  (<a href="https://en.wikipedia.org/wiki/Context-sensitive_grammar">CSG</a>) context sensitive grammar on the other hand is a grammar that can
//  * change how it derives a production rule given the information it has already
//  * parsed.<br>

/**
 * This class is used to read grammar files that follows the same rules that
 * a <a href="https://en.wikipedia.org/wiki/Context-free_grammar">(CFG) context free grammar</a> uses.<br>
 * 
 * A CFG is a unambiguous grammar meaning that there is only one way to derive
 * a production rule from a given state.<br><br>
 * 
 * This parser allows grammars that follow this syntax.
 *<pre># Syntax
 *#    Comments must be placed on the start of a line.
 *#    Multiple whitespaces are allowed between and infront of rules. 
 *#
 *#    Each statment starts with a name followed by a colon, then
 *#    followed by the first production rule.
 *#
 *#    For each new production rule you must add a new line with
 *#    a '|' followed by the pattern.
 *#
 *#    A statement must be closed by a semicolon.
 *
 *# Matching Types
 *#    A optional single match value is written ( PRODUCTION RULES )
 *#    A optional repeated match value is written [ PRODUCTION RULES ]
 *#    A regex match is written {"REGEX"} and only works on single tokens
 *NUMBER: {"[0-9]+"}
 *;
 *
 *STAT: "(" EXPR [ "," EXPR ] ")"
 *    | "[" EXPR ( "," EXPR ) "]"
 *;
 *
 *EXPR: NUMBER
 *;</pre>
 *
 * We will test the grammar above with the following strings.
 *
 *<pre>Accepted: "(1, 20, 39)"
 *Accepted: "[3, 43]"
 *Rejected: "[1, 2, 43]"</pre>
 *
 * The last string got rejected because the roundbrackets only allow for at most one
 * match.
 * 
 * @author HardCoded
 */
public final class GrammarFactory {
	private GrammarFactory() {}
	
	/**
	 * Parse a grammar from the content of this file.
	 * 
	 * @param filePath the path to the grammar file.
	 * @return A parsed grammar.
	 * @throws IOException
	 */
	public static Grammar load(String filePath) throws IOException {
		return load(new FileReader(new File(filePath)));
	}
	
	/**
	 * Parse a grammar from the content of this file.
	 * 
	 * @param file the path to the grammar file.
	 * @return A parsed grammar.
	 * @throws IOException
	 */
	public static Grammar load(File file) throws IOException {
		return load(new FileReader(file));
	}
	
	/**
	 * Parse a grammar from the content of this string.
	 * 
	 * @param content the string containing the grammar data.
	 * @return A parsed grammar.
	 * @throws IOException
	 */
	public static Grammar loadFromString(String content) throws IOException {
		return load(new StringReader(content));
	}
	
	/**
	 * Parses the input from a reader and returns the parsed grammar.
	 * 
	 * @param reader the reader that contains the data.
	 * @return A parsed grammar.
	 * @throws IOException
	 */
	public static Grammar load(Reader reader) throws IOException {
		Grammar grammar = new Grammar();
		Map<String, Grammar.Type> types = grammar.types;
		
		BufferedReader bufferedReader = new BufferedReader(reader);
		int ruleIndex = 1;
		
		Type type = null;
		String line;
		while((line = bufferedReader.readLine()) != null) {
			line = line.trim();
			
			// Remove comments and empty lines
			if(line.startsWith("#") || line.isEmpty()) continue;
			
			String first = null;
			{
				String[] split = line.split("[ \t]+");
				first = split[0];
				
				// Remove the first token from the line and
				// set the first to the first token.
				line = line.substring(first.length()).trim();
			}
			
			if(first.endsWith(":")) { // Type start
				type = grammar.new Type(first.substring(0, first.length() - 1));
				
//				if(debug) System.out.println("New Type: '" + type + "'");
				
				// Make this into a add pattern
				if(!line.isEmpty()) first = "|";
			}
			
			// Add new pattern
			if(first.equals("|")) {
				type.matches.add(createMatch(ruleIndex++, grammar, line));
			}
			
			if(first.equals(";")) {
//				if(debug) {
//					for(Match m : type.matches) {
//						System.out.println("    - " + m);
//					}
//					System.out.println("End Type: '" + type + "'");
//					System.out.println();
//				}
				
				types.put(type.name, type);
				type = null;
			}
		}
		bufferedReader.close();
		
		return grammar;
	}
	
	private static MatchList createMatch(int ruleId, Grammar grammar, String line) {
		Symbol start = Tokenizer.generateSymbolChain(line.getBytes());
		
		MatchList list = grammar.new MatchList(ruleId, line);
		Symbol symbol = start;
		
		while(symbol != null) {
			String value = symbol.toString();
			
			if(value.equals("{")) {
				Symbol pattern = symbol.next();
				if(!pattern.next().equals("}")) return null;
				list.add(grammar.new MatchRegex(pattern));
				symbol = pattern.next(2);
				continue;
			}
			
			if(value.equals("[") || value.equals("(")) {
				MatchBracket match = createMatchBracket(grammar, symbol);
				
				list.add(match);
				symbol = symbol.next(match.symbol.remaining() + 3);
				continue;
			}
			
			if(value.startsWith("\"")) {
				// Must be a string
				list.add(grammar.new MatchString(symbol));
			} else {
				// Must be a type
				list.add(grammar.new MatchType(symbol));
			}
			
			symbol = symbol.next();
		}
		
		return list;
	}
	
	private static MatchBracket createMatchBracket(Grammar grammar, Symbol start) {
		boolean repeat = start.equals("[");
		MatchBracket bracket = grammar.new MatchBracket(start, repeat);
		start = start.next();
		
		Symbol symbol = start;
		int count = -1;
		while(symbol != null) {
			String value = symbol.toString();
			count++;
			
			if(value.equals("{")) {
				Symbol pattern = symbol.next();
				if(!pattern.next().equals("}")) return null;
				bracket.add(grammar.new MatchRegex(pattern));
				symbol = pattern.next(2);
				continue;
			}
			
			if(value.equals("[") || value.equals("(")) {
				MatchBracket match = createMatchBracket(grammar, symbol);
				
				bracket.add(match);
				symbol = symbol.next(match.symbol.remaining() + 3);
				count += match.symbol.remaining() + 2;
				continue;
			}
			
			if(repeat && value.equals("]") || !repeat && value.equals(")")) {
				bracket.symbol = start.clone(count - 1);
				break;
			}
			
			if(value.startsWith("\"")) {
				// Must be a string
				bracket.add(grammar.new MatchString(symbol));
			} else {
				// Must be a type
				bracket.add(grammar.new MatchType(symbol));
			}
			
			symbol = symbol.next();
		}
		
		return bracket;
	}
}
