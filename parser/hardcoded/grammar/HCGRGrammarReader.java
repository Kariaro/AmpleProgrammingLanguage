package hardcoded.grammar;

import java.io.*;

import hc.errors.grammar.GrammarSyntaxException;
import hc.token.Symbol;
import hc.token.Tokenizer;
import hardcoded.grammar.Grammar.*;

/**
 * This class is used to read grammar files that follows the same rules AS
 * a <a href="https://en.wikipedia.org/wiki/Context-free_grammar">(CFG) context free grammar</a> does.<br>
 * 
 * A context free grammar is an unambiguous grammar, meaning that there is only
 * one way of deriving a production rule from a given input string.<br><br>
 * 
 * This parser allows grammars that follow this syntax.
 *<pre># Syntax
 *#    Comments must be placed on the start of a line.
 *#    Hashtags are not allowed in item or token names.
 *#    Multiple whitespaces are allowed between and infront of rules.
 *#
 *#    Each new item starts with its name followed by a colon and then
 *#    the rules.
 *#
 *#    You can define any item to become the starting item by writing
 *#    'START <itemName>' at any position in the file.
 *#
 *#    If the word TOKEN is placed before a item it becomes a
 *#    single token matching rule and will only accept regex.
 *#
 *#    For each new rule you add you must add a new line with
 *#    a or character followed by the new rule set.
 *#
 *#    String rules can use either double or single quotes.
 *
 *# Matching Types
 *#    A optional single match value is written ( RULES )
 *#    A optional repeated match value is written [ RULES ]
 *#    A regex match is written {"REGEX"} and only works on single tokens
 *#      Be aware that a regex matching could make a grammar ambiguous
 *#      depending on what you are matching. This will result with the
 *#      generator refusing to create a LR(k) parser for that grammar.
 *#      
 *#      The regex match operation should only be used when defining a
 *#      new token and not inside statements.
 *#    A empty match is written {EMPTY} and is a null capturing group
 *#    meaning that it can be skipped and still work.
 *#      If this is put in the middle of a set of rules it will remove
 *#      all rules after it.
 *#    To match the end of a file you use {EOF}.
 *
 *TOKEN NUMBER: {"[0-9]+"}
 *
 *STAT: '(' EXPR [ ',' EXPR ] ')'
 *    | '[' EXPR ( ',' EXPR ) ']'
 *
 *EXPR: NUMBER</pre>
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
public final class HCGRGrammarReader implements GrammarReaderImpl {
	
	@Override
	public Grammar load(Reader reader) throws IOException {
		Grammar grammar = new Grammar();
		
		BufferedReader bufferedReader = new BufferedReader(reader);
		int ruleIndex = 1;
		
		Item type = null;
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
			
			boolean isToken = false;
			if(!first.endsWith(":")) {
				switch(first) {
					case "START": {
						grammar.setStartItem(line);
						continue; // Read next line
					}
					case "TOKEN": {
						isToken = true;
						
						String[] split = line.split("[ \t]+");
						first = split[0];
						line = line.substring(first.length()).trim();
						
						break;
					}
				}
			}
			
			if(first.endsWith(":")) { // Item start
				if(type != null) grammar.addItem(type);
				
				first = first.substring(0, first.length() - 1);
				
				if(first.contains("#")) {
					throw new GrammarSyntaxException("Hashtags cannot be used inside item or token names. \"" + first + "\"");
				}
				
				if(isToken) {
					type = new ItemToken(first);
				} else {
					type = new Item(first);
				}
				
				// Make this into a add pattern
				if(!line.isEmpty()) first = "|";
			}
			
			// Add new pattern
			if(first.equals("|")) {
				if(type instanceof ItemToken) {
					type.matches.add(createRuleList(0, grammar, line));
				} else {
					type.matches.add(createRuleList(ruleIndex++, grammar, line));
				}
			}
		}
		
		if(type != null) {
			grammar.addItem(type);
		}
		
		if(grammar.getStartItem() != null && !grammar.containsItem(grammar.getStartItem())) {
			throw new GrammarSyntaxException("That start item does not exist '" + grammar.getStartItem() + "'");
		}
		
		bufferedReader.close();
		
		return grammar;
	}
	
	private RuleList createRuleList(int ruleId, Grammar grammar, String line) {
		Symbol start = Tokenizer.generateSymbolChain(line.getBytes());
		
		RuleList list = grammar.new RuleList(ruleId);
		Symbol symbol = start;
		
		while(symbol != null) {
			String value = symbol.toString();
			
			if(value.equals("{")) {
				Symbol pattern = symbol.next();
				if(!pattern.next().equals("}")) return null;
				if(pattern.toString().startsWith("\"")) {
					list.add(grammar.new RegexRule(pattern));
				} else {
					list.add(grammar.new SpecialRule(pattern));
				}
				symbol = pattern.next(2);
				continue;
			}
			
			if(value.equals("[") || value.equals("(")) {
				BracketRule match = createBracketRule(grammar, symbol);
				
				list.add(match);
				symbol = symbol.next(match.symbol.remaining() + 3);
				continue;
			}
			
			if(value.startsWith("\"") || value.startsWith("\'")) {
				// Must be a string
				list.add(grammar.new StringRule(symbol));
			} else {
				// Must be a type
				list.add(grammar.new ItemRule(symbol));
			}
			
			symbol = symbol.next();
		}
		
		return list;
	}
	
	private BracketRule createBracketRule(Grammar grammar, Symbol start) {
		boolean repeat = start.equals("[");
		BracketRule bracket = grammar.new BracketRule(start, repeat);
		start = start.next();
		
		Symbol symbol = start;
		int count = -1;
		while(symbol != null) {
			String value = symbol.toString();
			count++;
			
			if(value.equals("{")) {
				Symbol pattern = symbol.next();
				if(!pattern.next().equals("}")) return null;
				bracket.add(grammar.new RegexRule(pattern));
				symbol = pattern.next(2);
				continue;
			}
			
			if(value.equals("[") || value.equals("(")) {
				BracketRule match = createBracketRule(grammar, symbol);
				
				bracket.add(match);
				symbol = symbol.next(match.symbol.remaining() + 3);
				count += match.symbol.remaining() + 2;
				continue;
			}
			
			if(repeat && value.equals("]") || !repeat && value.equals(")")) {
				bracket.symbol = start.clone(count - 1);
				break;
			}
			
			if(value.startsWith("\"") || value.startsWith("\'")) {
				// Must be a string
				bracket.add(grammar.new StringRule(symbol));
			} else {
				// Must be a type
				bracket.add(grammar.new ItemRule(symbol));
			}
			
			symbol = symbol.next();
		}
		
		return bracket;
	}
}