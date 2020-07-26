package hardcoded.grammar;

import java.util.*;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import hc.errors.DuplicateItemException;
import hc.token.Symbol;

/**
 * This is a grammar file that contains statements and production rules.<br>
 * 
 * A grammar is used when you want to check if a chain of tokens follow a
 * specific pattern. This pattern can take many shapes and many grammars can
 * be used to parse coding language. One such language is java.<br><br>
 * 
 * @author HardCoded
 */
public class Grammar {
	private final Map<String, ItemToken> tokens;
	private final Map<String, Item> items;
	
	// TODO: ItemRule and TokenRule should be destinguishable.
	//       Currently they do not show up correctly.......
	
	public Grammar() {
		tokens = new LinkedHashMap<>();
		items = new LinkedHashMap<>();
	}
	
	protected void addItem(Item type) {
		if(containsItem(type.name)) {
			throw new DuplicateItemException("Item was already defined '" + type + "'");
		}
		
		if(type instanceof ItemToken) {
			tokens.put(type.name, (ItemToken)type);
		} else {
			items.put(type.name, type);
		}
	}
	
	protected boolean containsItem(String name) {
		return items.containsKey(name) || tokens.containsKey(name);
	}
	
	public Set<RuleList> getAllRules() {
		return new LinkedHashSet<RuleList>(items.values().stream().flatMap(i -> i.matches.stream()).collect(Collectors.toList()));
	}
	
	public Set<Item> getItems() {
		return new LinkedHashSet<Item>(items.values());
	}
	
	public Set<ItemToken> getTokens() {
		return new LinkedHashSet<ItemToken>(tokens.values());
	}
	
	public Item getItem(String itemName) {
		if(items.containsKey(itemName)) {
			return items.get(itemName);
		} else if(tokens.containsKey(itemName)) {
			return tokens.get(itemName);
		}
		
		return null;
	}
	
	public class Item {
		protected final String name;
		protected final List<RuleList> matches;
		
		public Item(String name) {
			this.matches = new ArrayList<>();
			this.name = name;
		}
		
		public List<RuleList> getRules() {
			return Collections.unmodifiableList(matches);
		}
		
		public String getName() {
			return name;
		}
		
		public String toString() {
			return name;
		}
	}
	
	public class ItemToken extends Item {
		public ItemToken(String name) { super(name); }
	}
	
	public class RuleList extends Rule {
		private final List<Rule> rules;
		private final String line;
		
		public RuleList(int ruleId, String line) {
			super(ruleId);
			
			rules = new ArrayList<>();
			this.line = line;
		}
		
		public void add(Rule rule) {
			rules.add(rule);
		}
		
		public int size() {
			return rules.size();
		}
		
		public List<Rule> getRules() {
			return Collections.unmodifiableList(rules);
		}
		
		public String toString() {
			String string = line;
			string = rules.toString();
			return string.substring(1, string.length() - 1);
		}
	}
	
	/**
	 * This rule is an optional match meaning that it can be ignored when parsing the grammar.
	 * This rule has two optinal states where if you use roundbrackets around a expression it's
	 * a single match rule. If you put squarebrackets around an expression it's a repeatable match.
	 * 
	 * @author HardCoded
	 */
	public class BracketRule extends Rule {
		protected List<Rule> matches;
		protected boolean repeat;
		protected Symbol symbol;
		
		public BracketRule(Symbol symbol) {
			this(symbol, false);
		}
		
		public BracketRule(Symbol symbol, boolean repeat) {
			matches = new ArrayList<>();
			this.symbol = symbol;
			this.repeat = repeat;
		}
		
		public void add(Rule match) {
			matches.add(match);
		}
		
		public String toString() {
			StringBuilder sb = new StringBuilder().append(repeat ? "[":"(");
			for(Rule match : matches) sb.append(match.toString()).append(", ");
			if(!matches.isEmpty()) {
				sb.deleteCharAt(sb.length() - 1);
				sb.deleteCharAt(sb.length() - 1);
			}
			return sb.append(repeat ? "]":")").toString();
		}
	}
	
	public class ItemRule extends Rule {
		private String name;
		public ItemRule(Symbol symbol) {
			this.name = symbol.toString();
		}
		
		public String getName() {
			return name;
		}
		
		public String toString() { return "i:" + name; }
	}
	
	public class StringRule extends Rule {
		private String value;
		public StringRule(Symbol symbol) {
			value = symbol.toString();
			value = value.substring(1, value.length() - 1);
		}
		
		public String toString() {
			return "s:\"" + value + "\"";
		}
	}
	
	public class MatchRegex extends Rule {
		private Pattern pattern;
		public MatchRegex(Symbol symbol) {
			String regex = symbol.toString();
			regex = regex.substring(1, regex.length() - 1);
			pattern = Pattern.compile(regex);
		}
		
		public String toString() {
			return "r:" + pattern;
		}
	}
	
	public Grammar expand() {
		Grammar grammar = new Grammar();
		// TODO: Optimize the grammar so that it does not contain any more bracket matches..
		//       this should not be done while loading the grammar but when creating the LRParser
		//       with the LRParserGenerator.
		
		return grammar;
	}
}
