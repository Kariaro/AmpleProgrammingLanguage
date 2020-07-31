package hardcoded.grammar;

import java.util.*;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import hc.errors.grammar.DuplicateItemException;
import hc.errors.grammar.UndefinedMatchType;
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
	protected final Map<String, ItemToken> tokens;
	protected final Map<String, Item> items;
	protected String startItem;
	
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
	
	// TODO: For this to make any sense this should be an augmented grammar instead of
	//       being just a grammar.
	
	/**
	 * If there was a item in the language that used the START
	 * keyword. Then this will return that item.
	 * 
	 * @return The name of the item that this grammar starts with.
	 */
	public String getStartItem() {
		return startItem;
	}
	
	public static class Item {
		protected String name;
		public final List<RuleList> matches;
		
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
	
	public static class ItemToken extends Item {
		public ItemToken(String name) { super(name); }
	}
	
	public abstract class Rule {
		protected int ruleId;
		
		protected Rule() {
			
		}
		
		protected Rule(int ruleId) {
			this.ruleId = ruleId;
		}
		
		public String getRuleString() {
			return "Rule" + ruleId;
		}
		
		public int getRuleId() {
			return ruleId;
		}
		
		public String value() {
			return null;
		}
		
		@Override
		public boolean equals(Object obj) {
			if(obj instanceof Rule) {
				return toString().equals(obj.toString());
			}
			
			return this == obj;
		}
		
		/** Represent this hash with a unique hash that can only be
		 *
		 */
		public String hash() {
			return null;
		}
	}
	
	public class RuleList extends Rule {
		protected final List<Rule> rules;
		protected String line;
		
		public RuleList(int ruleId, String line) {
			super(ruleId);
			
			rules = new ArrayList<>();
			this.line = line;
		}
		
		public RuleList() {
			rules = new ArrayList<>();
		}
		
		public void add(Rule rule) {
			rules.add(rule);
		}
		
		public int size() {
			return rules.size();
		}
		
		public Rule get(int index) {
			return rules.get(index);
		}
		
		public boolean isEmpty() {
			return rules.isEmpty();
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
		
		public BracketRule() {
			matches = new ArrayList<>();
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
		protected String name;
		public ItemRule(Symbol symbol) {
			this(symbol.toString());
		}
		
		public ItemRule(String name) {
			this.name = name;
		}
		
		public String getName() {
			return name;
		}
		
		public String value() {
			return name;
		}
		
		public String toString() {
			if(tokens.containsKey(name)) return "token:" + name;
			return "i:" + name;
		}
	}
	
	public class StringRule extends Rule {
		protected String value;
		public StringRule(Symbol symbol) {
			value = symbol.toString();
			value = value.substring(1, value.length() - 1);
		}
		
		public StringRule(String value) {
			this.value = value;
		}
		
		public String value() {
			return value;
		}
		
		public String toString() {
			return "s:\"" + value + "\"";
		}
	}
	
	public class RegexRule extends Rule {
		protected Pattern pattern;
		public RegexRule(Symbol symbol) {
			String regex = symbol.toString();
			regex = regex.substring(1, regex.length() - 1);
			pattern = Pattern.compile(regex);
		}
		
		public RegexRule(String regex) {
			pattern = Pattern.compile(regex);
		}
		
		public String value() {
			return pattern.pattern();
		}
		
		public String toString() {
			return "r:" + pattern;
		}
	}
	
	protected static final int SPECIAL_EOF = 1;
	protected static final int SPECIAL_EMPTY = 2;
	public class SpecialRule extends Rule {
		protected int type;
		public SpecialRule(Symbol symbol) {
			switch(symbol.toString()) {
				case "EOF": {
					type = SPECIAL_EOF;
					break;
				}
				case "EMPTY": {
					type = SPECIAL_EMPTY;
					break;
				}
				default: {
					throw new UndefinedMatchType("The match type {" + symbol + "} does not exist.");
				}
			}
		}
		
		public SpecialRule(int type) {
			this.type = type;
		}
		
		public String value() {
			return toString();
		}
		
		public String toString() {
			switch(type) {
				case SPECIAL_EOF: return "{EOF}";
				case SPECIAL_EMPTY: return "{EMPTY}";
			}
			return null;
		}
	}
	
	public Grammar expand() {
		return new OptimizedGrammar(this);
	}
}
