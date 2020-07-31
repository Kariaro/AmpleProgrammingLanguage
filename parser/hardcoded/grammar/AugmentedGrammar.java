package hardcoded.grammar;

import java.util.*;

import hardcoded.grammar.Grammar.Rule;
import hc.errors.grammar.DuplicateItemException;


class AugmentedGrammar {
	protected final Map<String, IItemToken> tokens;
	protected final Map<String, IItem> items;
	
	public AugmentedGrammar() {
		tokens = new LinkedHashMap<>();
		items = new LinkedHashMap<>();
	}
	
	protected void addItem(IItem type) {
		if(containsItem(type.name)) {
			throw new DuplicateItemException("Item was already defined '" + type + "'");
		}
		
		if(type instanceof IItemToken) {
			tokens.put(type.name, (IItemToken)type);
		} else {
			items.put(type.name, type);
		}
	}
	
	protected boolean containsItem(String name) {
		return items.containsKey(name) || tokens.containsKey(name);
	}
	
	public Set<IItem> getItems() {
		return new LinkedHashSet<IItem>(items.values());
	}
	
	public Set<IItemToken> getTokens() {
		return new LinkedHashSet<IItemToken>(tokens.values());
	}
	
	public IItem getItem(String itemName) {
		if(items.containsKey(itemName)) {
			return items.get(itemName);
		} else if(tokens.containsKey(itemName)) {
			return tokens.get(itemName);
		}
		
		return null;
	}
	
	public class IItem {
		protected final List<IRuleList> matches;
		protected String name;
		
		public IItem(String name) {
			this.matches = new ArrayList<>();
			this.name = name;
		}
		
		public String getName() {
			return name;
		}
		
		public String toString() {
			return name;
		}
	}
	
	public class IItemToken extends IItem {
		public IItemToken(String name) {
			super(name);
		}
	}
	
	public class IRule {
		private final Rule rule;
		
		public IRule(Rule rule) {
			this.rule = rule;
		}
		
		public String toString() {
			return rule.toString();
		}
	}
	
	public class IRuleList {
		protected final List<IRule> rules;
		protected String line;
		protected int ruleId;
		
		public IRuleList(int ruleId, String line) {
			rules = new ArrayList<>();
			this.ruleId = ruleId;
			this.line = line;
		}
		
		public IRuleList() {
			rules = new ArrayList<>();
		}
		
		public int size() {
			return rules.size();
		}
		
		public IRule get(int index) {
			return rules.get(index);
		}
		
		public int getRuleId() {
			return ruleId;
		}
		
		public boolean isEmpty() {
			return rules.isEmpty();
		}
		
		public String toString() {
			String string = line;
			string = rules.toString();
			return string.substring(1, string.length() - 1);
		}
	}
}
