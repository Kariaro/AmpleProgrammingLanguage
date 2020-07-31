package hardcoded.parser;

import java.util.*;
import java.util.stream.Collectors;

import hardcoded.grammar.Grammar;
import hardcoded.grammar.Grammar.*;
import hardcoded.grammar.OptimizedGrammar;
import hc.errors.grammar.GrammarException;

public class OLD_LR0_ParserGenerator {
	
	public LR0_ParserGenerator() {
	}
	
	private Grammar grammar;
	
	// https://en.wikipedia.org/wiki/Finite-state_machine
	public Object parse(Grammar grammar) {
		if(!(grammar instanceof OptimizedGrammar)) throw new GrammarException("This grammar is not optimized");
		
		this.grammar = grammar;
		
		//ITable table = new ITable();
		//table.types = getSymbolSet();
		System.out.println(getSymbolSet());
		
		// TODO: Let the user change the start of the grammar effectivly making it into a
		//       augmented grammar.
		
		String startGroupName = "S";
		IState i0 = createEntrySet(startGroupName);
		
		return step(i0);
	}
	
	private int groupIndex = 1;
	public Object step(IState i0) {
		dumpState(i0);
		
		INextStep step;
		while((step = getNextRun(i0)).selector != null) {
			System.out.println("group -> I" + (groupIndex++));
			closure(step);
			System.out.println(step);
			System.out.println();
		}
		
		return null;
	}
	
	// TODO: Calculate the closure of the main set.
	// TODO: To be able to do this, first get all the items of each unique tokens in the set 
	public Object closure(INextStep step) {
		System.out.println("step -> " + step);
		
		// TODO: Do not handle logic in here.
		//       This method should only create a new IState from step and
		//       add to the global state list...
		
		for(int i = 0; i < step.items.size(); i++) {
			IItem item = step.items.get(i);
			
			System.out.println("  Item: " + item);
			for(IRuleList set : item.list) {
				if(i == 0) {
					if(!step.selector.equals(set.cursor())) continue;
				}
				
				// TODO: We have now found the IRuleList that coresponds to the correct 
				
				if(set.index == set.size()) {
					// TODO: Add to the state stuff.......
					// TODO: Add a reduction action to the table....
					
					// System.out.println("[Close] " + set);
					System.out.println("      \\ Reduction Action [" + item + " -> " + set + "]");
					
					if(step.items.size() == 1) {
						System.out.println("[Close] This state is complete");
					}
					
					break;
				} else if(set.index > set.size()) {
					
				} else {
					// System.out.println("      | " + set);
					
					IRule rule = set.cursor();
					if(rule.isItemType()) {
						if(rule.isTokenItem()) {
							System.out.println("      \\ Shift Action [" + item + " -> " + set + "]");
						} else {
							System.out.println("      \\ Goto Action [" + item + " -> " + set + "]");
							// This should continue........
							// Recursivly call this tree now.. 
						}
					}
				}
			}
		}
		
		return null;
	}
	
	private INextStep getNextRun(IState state) {
		INextStep step = new INextStep();
		
		// First we get the next selector that we need
		for(IItem item : state.list) {
			for(IRuleList set : item.list) {
				IRule rule = set.cursor();
				if(rule == null) continue; // Prevents loops
				
				if(step.selector == null) {
					step.selector = rule;
					break;
				}
			}
		}
		
		if(step.selector == null) {
			// We failed to find the next item of the current state
			return step;
		}
		
		for(IItem item : state.list) {
			for(IRuleList set : item.list) {
				IRule rule = set.cursor();
				
				// Find all items that requires the next states token
				if(step.selector.equals(rule)) {
					set.index ++;
					if(step.items.indexOf(item) < 0) {
						step.items.add(item);
					}
					
					IRule next = set.cursor();
					// System.out.println("Item -> " + item);
					// System.out.println("      > " + set);
					// System.out.println("      > " + next);
					
					// DONE: If the next token is a non-terminal value
					//       it should be added to the items list as a
					//       new set...
					
					// DONE: If the next token is a non-terminal then
					//       it could belong to a set not pressent inside
					//       the current state.. This should be found inside
					//       the grammar instead...
					
					if(next != null && next.isItemType() && !next.isTokenItem()) {
						String name = next.value();
						
						if(!step.hasItem(name)) {
							step.items.add(getGrammarItem(name));
						}
					}
				}
			}
		}
		
		return step;
	}
	
	private class INextStep {
		// The rules that match this selector
		// private List<IIRuleList> rules = new ArrayList<>();
		
		// All items that should be rested... (Creates new IRuleLists)
		private List<IItem> items = new ArrayList<>();
		
		
		private IRule selector;
		
		public boolean hasItem(String name) {
			return items.parallelStream().anyMatch(x -> x.name.equals(name));
		}
		
		public String toString() {
			return "C='" + selector + "', L=" + items;
		}
	}
	
	
	// TODO: Replace all items with this
	private class NItem {
		public List<NRuleList> rules;
		public String name;
		public boolean token;
		
		public NItem(Item item) {
			if(item instanceof ItemToken) token = true;
			name = item.getName();
			rules = item.matches.stream()
				.map(x -> new NRuleList(name, x))
				.collect(Collectors.toList());
		}
		
		public NItem(NItem item) {
			token = item.token;
			name = item.name;
			rules = item.rules.stream()
				.map(x -> x.clone())
				.collect(Collectors.toList());
		}
		
		@Override
		public boolean equals(Object obj) {
			if(!(obj instanceof NItem)) return false;
			return name.equals(((NItem)obj).name);
		}
		public String getName() { return name; }
		public boolean isToken() { return token; }
		
		public String toString() {
			return name;
		}
	}
	
	private class NRuleList {
		public List<NRule> rules;
		public String itemName;
		public int index;
		
		public NRuleList(String itemName, RuleList set) {
			this.itemName = itemName;
			this.rules = set.getRules().stream()
				.map(x -> new NRule(x))
				.collect(Collectors.toList());
		}
		
		public NRuleList(NRuleList list) {
			itemName = list.itemName;
			rules = new ArrayList<>(list.rules);
		}
		
		public int size() { return rules.size(); }
		public NRule get(int index) { return rules.get(index); }
		public boolean isEmpty() { return rules.isEmpty(); }
		
		public NRule cursor() { return cursor(0); }
		public NRule cursor(int offset) {
			if(index + offset >= rules.size()) return null;
			return rules.get(index + offset);
		}
		
		public NRuleList clone() { return new NRuleList(this); }
		
		public String toString() {
			StringBuilder sb = new StringBuilder();
			for(int i = 0; i < rules.size(); i++) {
				NRule rule = rules.get(i);
				
				if(i == index) {
					sb.append("\u2022 ");
				}
				
				sb.append(rule).append(" ");
			}
			
			if(index >= rules.size()) {
				sb.append("\u2022");
				if(!rules.isEmpty()) sb.append(" ");
			}
			
			if(!rules.isEmpty()) {
				sb.deleteCharAt(sb.length() - 1);
			}
			
			return sb.toString();
		}
	}
	
	public enum NType { ITEM, STRING, REGEX, SPECIAL, INVALID }
	private class NRule {
		protected NType type = NType.INVALID;
		protected String value = null;
		public NRule() {}
		public NRule(Rule rule) {
			value = rule.value();
			if(rule instanceof ItemRule) type = NType.ITEM;
			else if(rule instanceof StringRule) type = NType.STRING;
			else if(rule instanceof RegexRule) type = NType.REGEX;
			else if(rule instanceof SpecialRule) type = NType.ITEM;
			else throw new GrammarException("Invalid group type -> " + rule.getClass());
		}
		
		public NType type() { return type; }
		public String value() { return value; }
		public String toString() { return value; }
	}
	
	// ===============================================================
	
	
	private void dumpState(IState state) {
		System.out.println("Item: " + state);
		for(IItem item : state.list) {
			System.out.println("    > " + item);
			for(IRuleList set : item.list) {
				System.out.println("      | " + set);
			}
		}
		System.out.println();
	}
	
	private IState createEntrySet(String startGroupName) {
		IState state = new IState("I0");
		Item start = new Item(startGroupName + "'"); {
			RuleList fin = grammar.new RuleList();
			fin.add(grammar.new ItemRule(startGroupName));
			start.matches.add(fin);
		}
		
		Set<String> searched = new HashSet<>();
		List<Item> search = new ArrayList<>();
		search.add(start);
		while(!search.isEmpty()) {
			Item item = search.get(0);
			search.remove(0);
			
			state.addAll(item);
			
			for(RuleList set : item.getRules()) {
				if(set.isEmpty()) continue;
					
				Rule rule = set.get(0);
				if(rule instanceof ItemRule) {
					ItemRule ir = (ItemRule)rule;
					
					// TODO: Create a isToken method instead of this.
					if(!ir.toString().startsWith("token")) {
						if(!searched.contains(ir.getName())) {
							search.add(grammar.getItem(ir.getName()));
							searched.add(ir.getName());
						}
					}
				}
			}
		}
		
		System.out.println();
		
		return state;
	}
	
	private IItem getGrammarItem(String name) {
		Item item = grammar.getItem(name);
		IItem result = new IItem(name);
		
		for(RuleList set : item.getRules()) {
			result.add(set);
		}
		
		return result;
	}
	
	public List<IType> getSymbolSet() {
		List<IType> t_list = grammar.getItems().stream()
			.map(x -> new IType(x))
			.collect(Collectors.toList());
		List<IType> s_list = grammar.getItems().stream()
			.flatMap(x -> x.matches.stream())
			.flatMap(x -> x.getRules().stream())
			.map(x -> new IType(x))
			.collect(Collectors.toList());
		List<IType> types = new ArrayList<>();
		for(IType type : t_list) if(!types.contains(type)) types.add(type);
		for(IType type : s_list) if(!types.contains(type)) types.add(type);
		return types;
	}
	
//	private class ITable implements Serializable {
//		private static final long serialVersionUID = 551802914179978138L;
//		
//		private List<IType> types; // TypeValues
//		/**
//		 * The states are the individual groups that can be found....
//		 */
//		private int states;
//		private IAction[][] actionTable;
//	}
	
	private class IType {
		/**
		 * 1: Item
		 * 2: Token
		 * 3: String
		 * 4: Special
		 */
		private int type;
		private String value;
		
		public IType(Rule rule) {
			if(rule instanceof ItemRule) {
				ItemRule ir = (ItemRule)rule;
				value = ir.getName();
				
				Item item = grammar.getItem(value);
				if(item instanceof ItemToken) {
					type = 2;
				} else {
					type = 1;
				}
			} else if(rule instanceof RegexRule) {
				value = rule.value();
				type = 3;
			} else if(rule instanceof StringRule) {
				value = rule.value();
				type = 3;
			} else if(rule instanceof SpecialRule) {
				value = rule.value();
				type = 4;
			} else {
				// TODO: Throw some random error
				type = -1;
			}
		}
		
		public IType(Item item) {
			value = item.getName();
			
			if(item instanceof ItemToken) {
				type = 2;
			} else {
				type = 1;
			}
		}
		
		public boolean equals(Object obj) {
			if(!(obj instanceof IType)) return false;
			IType t = (IType)obj;
			return type == t.type && value.equals(t.value);
		}
		
		public String toString() {
			return type + ">" + value;
		}
	}
	
	private interface IAction {}
	
//	private class IActionGoto implements IAction {
//		
//	}
//	
//	private class IActionShift implements IAction {
//		
//	}
//	
//	private class IActionReduce implements IAction {
//		
//	}
	
	// TODO: Too compilcated right now....
	public class IItem {
		protected final List<IRuleList> list;
		private String name;
		
		public IItem(Item item) {
			list = new ArrayList<>();
			name = item.getName();
			
			// TODO: Fill list
		}
		
		public IItem(String name) {
			this.list = new ArrayList<>();
			this.name = name;
		}
		
		public IItem(IItem item) {
			list = new ArrayList<>();
			name = item.name;
			for(IRuleList set : item.list) {
				IRuleList cpy = new IRuleList(set);
				cpy.index = 0;
				list.add(cpy);
			}
		}

		public void add(RuleList set) {
			list.add(new IRuleList(set));
		}
		
		public boolean equals(Object obj) {
			if(!(obj instanceof IItem)) return false;
			return name.equals(((IItem)obj).name);
		}
		
		public String getName() {
			return name;
		}
		
		public String toString() {
			return name;
		}
	}
	
	public class IState {
		protected final List<IItem> list;
		protected String name;
		
		public IState(String name, Item item) {
			this.list = new ArrayList<>();
			this.name = name;
			
			list.add(new IItem(item));
		}
		
		public IState(String name) {
			this.list = new ArrayList<>();
			this.name = name;
		}
		
		public void add(Item item, RuleList set) {
			IItem found = get(item.getName());
			if(found == null) {
				found = new IItem(item.getName());
				list.add(found);
			}
			
			found.add(set);
		}
		
		public void addFirst(IItem item, IRuleList set) {
			IItem found = get(item.getName());
			if(found == null) {
				found = new IItem(item.getName());
				list.add(0, found);
			}
			
			found.list.add(0, new IRuleList(set));
		}
		
		public void addAll(Item item) {
			IItem found = get(item.getName());
			if(found == null) {
				found = new IItem(item.getName());
				list.add(found);
				
				for(RuleList set : item.getRules()) {
					found.add(set);
				}
			}
		}
		
		public IItem get(String name) {
			return list.stream().filter(x -> x.name.equals(name)).findFirst().orElse(null);
		}
		
		public String getName() {
			return name;
		}
		
		public String toString() {
			return name;
		}
	}
	
	public class IRule {
		private final Rule rule;
		
		public IRule(Rule rule) {
			this.rule = rule;
		}
		
		public boolean equals(Object obj) {
			if(obj instanceof IRule) {
				return toString().equals(obj.toString());
			}
			
			return false;
		}
		
		public boolean isTokenItem() {
			if(!isItemType()) return false;
			return (grammar.getItem(((ItemRule)rule).getName())) instanceof ItemToken;
		}
		
		public boolean isItemType() {
			return rule instanceof ItemRule;
		}
		
		public Class<? extends Rule> getRuleClass() {
			return rule.getClass();
		}
		
		public String value() {
			return rule.value();
		}
		
		public String toString() {
			return rule.toString();
		}
	}
	
	public class IRuleList {
		protected final List<IRule> rules;
		protected String line;
		protected int ruleId;
		protected int index; // TODO: Token index...
		
		public IRuleList(int ruleId, String line) {
			rules = new ArrayList<>();
			this.ruleId = ruleId;
			this.line = line;
		}
		
		public IRuleList(RuleList set) {
			rules = new ArrayList<>(set.getRules().stream().map(x -> new IRule(x)).collect(Collectors.toList()));
		}
		
		public IRuleList(IRuleList set) {
			rules = set.rules;
			line = set.line;
			ruleId = set.ruleId;
			index = set.index;
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
		
		protected IRuleList clone() {
			return new IRuleList(this);
		}
		
		public IRule cursor() {
			return cursor(0);
		}
		
		public IRule cursor(int offset) {
			if(index + offset >= rules.size()) {
				return null;
			}
			
			return rules.get(index + offset);
		}
		
		public String toString() {
			StringBuilder sb = new StringBuilder();
			for(int i = 0; i < rules.size(); i++) {
				IRule rule = rules.get(i);
				
				if(i == index) {
					sb.append("\u2022 ");
				}
				
				sb.append(rule).append(" ");
			}
			
			if(index >= rules.size()) {
				sb.append("\u2022");
				if(!rules.isEmpty()) sb.append(" ");
			}
			
			if(!rules.isEmpty()) {
				sb.deleteCharAt(sb.length() - 1);
			}
			
			return sb.toString();
		}
	}
}
