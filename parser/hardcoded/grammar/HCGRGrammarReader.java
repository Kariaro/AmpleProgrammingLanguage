package hardcoded.grammar;

import java.io.*;
import java.util.*;

import hardcoded.grammar.Grammar.*;
import hardcoded.lexer.Symbol;
import hardcoded.lexer.Tokenizer;
import hardcoded.lexer.TokenizerFactory;
import hc.errors.grammar.GrammarException;

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
	private static final hardcoded.lexer.Tokenizer READER;
	static {
		Tokenizer lexer = TokenizerFactory.createNew();
		READER = lexer.unmodifiableTokenizer();
		
		lexer.addGroup("WHITESPACE", true).addRegexes("[ \t\r\n]", "#[^\r\n]*");
		lexer.addGroup("KEYWORD").addStrings("TOKEN", "START");
		lexer.addGroup("DELIMITER").addStrings("(", ")", "[", "]", "{", "}", ":", "|");
		lexer.addGroup("ITEMNAME").addRegex("[a-zA-Z0-9_]+([ \t\r\n]*)(?=:)");
		lexer.addGroup("NAME").addRegex("[a-zA-Z0-9_]+");
		lexer.addGroup("LITERAL").addRegexes(
			"\'[^\'\\\\]*(?:\\\\.[^\'\\\\]*)*\'",
			"\"[^\"\\\\]*(?:\\\\.[^\"\\\\]*)*\""
		);
	}
	
	public Grammar load(Reader reader) throws IOException {
		CharArrayWriter writer = new CharArrayWriter();
		char[] buffer = new char[65536];
		int readChars = 0;
		while((readChars = reader.read(buffer)) != -1) {
			writer.write(buffer, 0, readChars);
		}
		
		Grammar grammar = new Grammar();
		Item itemGroup = null;
		RuleList set = null;
		
		List<Symbol> list = READER.parse(writer.toString());
		LinkedList<BracketRule> brackets = new LinkedList<>();
		
		for(int i = 0; i < list.size(); i++) {
			Symbol sym = list.get(i);
			
			String group = sym.group();
			String value = sym.value();
			
			if(group == null) {
				throw new GrammarException("(line:" + sym.line() + " column:" + sym.column() + ") Invalid syntax '" + value + "'");
			}
			
			// Creating new itemGroups and specifying the start item.
			if(group.equals("KEYWORD")) {
				if(i + 1 > list.size()) {
					throw new GrammarException("(line:" + sym.line() + " column:" + sym.column() + ") Invalid placement of the " + value.toLowerCase() + " keyword.");
				}

				Symbol item = list.get(i + 1);
				if(value.equals("TOKEN")) {
					if(!item.groupEquals("ITEMNAME")) {
						throw new GrammarException("(line:" + item.line() + " column:" + item.column() + ") Invalid token argument. Expected a item name got '" + item + "'");
					}
					
					String name = item.value().trim();
					if(grammar.containsItem(name)) throw new GrammarException("(line:" + sym.line() + " column:" + sym.column() + ") Multiple definitions of the same item name. '" + name + "'");
					if(set != null && set.isEmpty()) throw new GrammarException("(line:" + sym.line() + " column:" + sym.column() + ") Empty rules are not allowed.");
					
					itemGroup = new ItemToken(name);
					grammar.addItem(itemGroup);
					set = grammar.new RuleList();
					itemGroup.matches.add(set);
					
					i += 2;
				} else if(value.equals("START")) {
					if(!item.groupEquals("NAME")) {
						throw new GrammarException("(line:" + item.line() + " column:" + item.column() + ") Invalid start argument. Expected a item name got '" + item + "'");
					}
					
					grammar.setStartItem(item.value());
					i++;
				}

				continue;
			} else if(group.equals("ITEMNAME")) {
				if(grammar.containsItem(value.trim())) throw new GrammarException("(line:" + sym.line() + " column:" + sym.column() + ") Multiple definitions of the same item name. '" + value.trim() + "'");
				if(set != null && set.isEmpty()) throw new GrammarException("(line:" + sym.line() + " column:" + sym.column() + ") Empty rules are not allowed.");
				
				itemGroup = new Item(value.trim());
				grammar.addItem(itemGroup);
				set = grammar.new RuleList();
				itemGroup.matches.add(set);
				
				i++;
				continue;
			}
			
			if(itemGroup == null) {
				throw new GrammarException("(line:" + sym.line() + " column:" + sym.column() + ") Invalid placement of a regex bracket.");
			}
			
			if(sym.equals("DELIMITER", "|")) {
				if(set.isEmpty()) throw new GrammarException("(line:" + sym.line() + " column:" + sym.column() + ") Empty rules are not allowed.");
				set = grammar.new RuleList();
				itemGroup.matches.add(set);
				continue;
			}
			
			if(sym.equals("DELIMITER", "{")) {
				if(i + 2 > list.size()) {
					throw new GrammarException("(line:" + sym.line() + " column:" + sym.column() + ") Not enough arguments to create a regex bracket.");
				}
				
				if(!list.get(i + 2).equals("DELIMITER", "}")) {
					throw new GrammarException("(line:" + list.get(i + 2).line() + " column:" + list.get(i + 2).column() + ") Invalid regex close character. '" + list.get(i + 2) + "'");
				}
				
				Symbol item = list.get(i + 1);
				if(!item.groupEquals("LITERAL")) {
					throw new GrammarException("(line:" + item.line() + " column:" + item.column() + ") The regex match can only contain string literals.");
				}
				
				RegexRule rule = grammar.new RegexRule(item.value().substring(1, item.value().length() - 1));
				
				if(brackets.isEmpty()) {
					set.add(rule);
				} else {
					brackets.getLast().matches.add(rule);
				}
				
				i += 2;
			} else if(sym.groupEquals("LITERAL")) {
				StringRule rule = grammar.new StringRule(value.substring(1, value.length() - 1));
				
				if(brackets.isEmpty()) {
					set.add(rule);
				} else {
					brackets.getLast().matches.add(rule);
				}
			} else if(sym.groupEquals("NAME")) {
				ItemRule rule = grammar.new ItemRule(value);
				
				if(brackets.isEmpty()) {
					set.add(rule);
				} else {
					brackets.getLast().matches.add(rule);
				}
			} else {
				boolean square_open = sym.equals("DELIMITER", "[");
				boolean square_close = sym.equals("DELIMITER", "]");
				
				if(square_open || sym.equals("DELIMITER", "(")) {
					BracketRule br = grammar.new BracketRule();
					br.repeat = square_open;
					
					if(brackets.isEmpty()) {
						set.add(br);
						brackets.add(br);
					} else {
						brackets.getLast().matches.add(br);
						brackets.add(br);
					}
				} else if(square_close || sym.equals("DELIMITER", ")")) {
					BracketRule br = brackets.getLast();
					
					if(br.repeat != square_close) {
						throw new GrammarException("(line:" + sym.line() + " column:" + sym.column() + ") Invalid bracket close character '" + value + "'");
					}
					
					brackets.removeLast();
				} else {
					throw new GrammarException("(line:" + sym.line() + " column:" + sym.column() + ") Invalid character '" + value + "'");
				}
			}
		}
		
		if(!brackets.isEmpty()) {
			throw new GrammarException("Bracket was not closed properly. > " + brackets);
		}
		
		if(grammar.getStartItem() != null && !grammar.containsItem(grammar.getStartItem())) {
			throw new GrammarException("That start item does not exist '" + grammar.getStartItem() + "'");
		}
		
		return grammar;
	}
}