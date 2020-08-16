package hardcoded.grammar;

import java.util.*;
import java.util.regex.Pattern;

import hardcoded.lexer.Token;
import hc.errors.grammar.DuplicateItemException;
import hc.errors.grammar.UndefinedMatchType;

/**
 * This is a grammar file that contains statements and production rules.<br>
 * 
 * A grammar is used when you want to check if a chain of tokens follow a
 * specific pattern. This pattern can take many shapes and many grammars can
 * be used to parse coding language. One such language is java.<br><br>
 * 
 * If a start item was defined inside the grammar this will become an
 * AugmentedGrammar.<br><br>
 * 
 * @author HardCoded
 */
public class Grammar {
	protected final Map<String, ItemToken> tokens;
	protected final Map<String, Item> items;
	protected String startItem;
	
	// TODO: Make it so that if this was implemented somewhere you could modify all the syntax.
	// TODO: Maybe make like a CustomGrammar gramamr or something where you can modify everything.
	// TODO: Make the default grammar item types (String, Regex, Item, Special) mandatory or the only rules that are allowed inside the GLRParser.
	
	protected Grammar() {
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
	
	public boolean containsItem(String name) {
		return items.containsKey(name) || tokens.containsKey(name);
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
	
	/**
	 * If there was a item in the language that used the START
	 * keyword. Then this will return that item.
	 * 
	 * @return The name of the item that this grammar starts with.
	 */
	public String getStartItem() {
		return startItem;
	}
	
	/**
	 * Set what item should be considered the start of this grammar.
	 * 
	 * @param startItem
	 */
	public void setStartItem(String startItem) {
		this.startItem = startItem;
	}
	
	public static class Item {
		protected String name;
		protected final List<RuleList> matches;
		
		protected Item(String name) {
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
		private boolean imported;
		
		public ItemToken(String name) {
			this(name, false);
		}
		
		public ItemToken(String name, boolean imported) {
			super(name);
			this.imported = imported;
		}
		
		public boolean isImported() {
			return imported;
		}
	}
	
	public abstract class Rule {
		protected Rule() {}
		
		public String value() {
			return null;
		}
		
		@Override
		public boolean equals(Object obj) {
			if(obj instanceof Rule) {
				// TODO: This feels a bit cheaty
				return toString().equals(obj.toString());
			}
			
			return this == obj;
		}
	}
	
	public class RuleList extends Rule {
		protected final List<Rule> rules;
		protected RuleList() {
			rules = new ArrayList<>();
		}
		
		protected void add(Rule rule) {
			rules.add(rule);
		}
		
		protected int size() {
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
			String string = rules.toString();
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
		
		protected BracketRule() {
			this.matches = new ArrayList<>();
		}
		
		protected void add(Rule match) {
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
		protected String itemName;
		protected ItemRule(Token token) {
			itemName = token.toString();
		}
		
		protected ItemRule(String name) {
			this.itemName = name;
		}
		
		public String value() {
			return itemName;
		}
		
		public String toString() {
			if(tokens.containsKey(itemName)) return "token:" + itemName;
			return "i:" + itemName;
		}
	}
	
	public class StringRule extends Rule {
		protected String value;
		protected StringRule(Token token) {
			value = token.toString();
			value = value.substring(1, value.length() - 1);
		}
		
		protected StringRule(String value) {
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
		protected RegexRule(Token token) {
			String regex = token.toString();
			regex = regex.substring(1, regex.length() - 1);
			pattern = Pattern.compile(regex);
		}
		
		protected RegexRule(String regex) {
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
		protected SpecialRule(Token token) {
			switch(token.toString()) {
				case "EOF": {
					type = SPECIAL_EOF;
					break;
				}
				case "EMPTY": {
					type = SPECIAL_EMPTY;
					break;
				}
				default: {
					throw new UndefinedMatchType("The match type {" + token + "} does not exist.");
				}
			}
		}
		
		protected SpecialRule(int type) {
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
