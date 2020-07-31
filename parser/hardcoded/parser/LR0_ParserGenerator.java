package hardcoded.parser;

import java.util.*;
import java.util.stream.Collectors;

import hardcoded.grammar.Grammar;
import hardcoded.grammar.Grammar.*;
import hardcoded.grammar.OptimizedGrammar;
import hardcoded.visualization.DFAVisualization;
import hc.errors.grammar.GrammarException;

/**
 * This class is more of a proof of concept.
 * 
 * @author HardCoded
 */
public class LR0_ParserGenerator {
	
	public LR0_ParserGenerator() {
		
	}
	
	// The grammar we are generating a parser for
	private Grammar grammar;
	
	// This list will contain all global states..
	private List<IState> globalStates = new ArrayList<>();
	
	// https://en.wikipedia.org/wiki/Finite-state_machine
	public Object parse(Grammar grammar) {
		if(!(grammar instanceof OptimizedGrammar)) {
			throw new GrammarException("This grammar is not optimized");
		}
		
		this.grammar = grammar;
		
		//ITable table = new ITable();
		//table.types = getSymbolSet();
		// System.out.println(getSymbolSet());
		
		String startGroupName = grammar.getStartItem();
		if(startGroupName == null) {
			// Could not find a start item....
			throw new GrammarException("The grammar did not specify any start items");
		}
		
		IState i0 = createNNEntrySet(startGroupName);
		System.out.println();
		System.out.println();
		
		globalStates.add(i0);
		computeClosure(i0);
		
		System.out.println("States -> " + globalStates.size());
		for(IState state : globalStates) {
			prettyPrint(state);
			System.out.println("====================================");
		}
		
		for(int i = 0; i < globalStates.size(); i++) {
			globalStates.get(i).name = "I" + Integer.toString(i);
			globalStates.get(i).accept();
		}
		
		new DFAVisualization().show(globalStates);
		
		return null;
	}
	
	private void computeClosure(IState state) {
		IState current = state.clone();
		
		List<IState> nextSet = new ArrayList<>();
		IState next;
		
		for(int i = 0; i < current.size(); i++) {
			next = createNextState(current, i);
			
			if(next == null) {
				// The end of the chain or no more rules to iterate
				continue;
			}
			
			// Check if this state is already defined. If so do not add it again.
			int index = globalStates.indexOf(next);
			if(index < 0) {
				globalStates.add(next);
				state.name = "I" + globalStates.size();
				state.next.add(next);
				computeClosure(next);
			} else {
				IState found = globalStates.get(index);
				state.next.add(found);
			}
			
			nextSet.add(next);
		}
	}
	
	/**
	 * Creates the next logical state from this state..
	 * 
	 * Returns null if this state was a final state and can't be reduced anymore.
	 * @param state
	 * @param index
	 * @return
	 */
	private IState createNextState(IState state, int index) {
		IState result = new IState("I" + globalStates.size());
		
		{
			IRuleList set = state.getRule(index);
			IRule rule = set.cursor();
			if(rule == null) {
				// This means that this is not a LR(0) grammar
				// and should throw an error
				//
				// Or that this is the end of the chain.
				// We could not find any new states to calculate.
				return null;
			}
			result.action = rule;
		}
		
		for(IRuleList set : state.allRules) {
			IRule rule = set.cursor();
			
			// Find all items that requires the next states token
			if(result.action.equals(rule)) {
				if(result.rules.indexOf(set) < 0) {
					set.index++;
					result.rules.add(set.fullClone());
				}
				
				IRule next = set.cursor();
				if(next != null && next.isItemType() && !next.isItemToken()) {
					String name = next.value;
					
					if(!result.hasItem(name)) {
						// TODO: If this item has a leading non-terminal item then it should also be added
						//       to this list..
						result.list.add(getGrammarItem(name));
					}
				}
			}
		}
		
		return result;
	}
	
	private IState createNNEntrySet(String startGroupName) {
		// TODO: Optimize this function..
		
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
			
			if(item != start) {
				state.addAll(item);
			}
			
			for(RuleList set : item.getRules()) {
				if(set.isEmpty()) continue;
					
				Rule rule = set.get(0);
				if(rule instanceof ItemRule) {
					if(!rule.toString().startsWith("token")) {
						if(!searched.contains(rule.value())) {
							Item i = grammar.getItem(rule.value());
							
							if(i == null) {
								throw new GrammarException("Failed to create entry set. Item '" + rule.value() + "' does not exist!");
							}
							
							search.add(i);
							searched.add(rule.value());
						}
					}
				}
			}
		}
		
		return state.accept();
	}
	
	private void prettyPrint(IState state) {
		System.out.println("state -> " + state);
		for(IRuleList set : state.rules) {
			if(set.index == set.size()) System.out.println("      \\ Reduction Action [" + set.itemName + " -> " + set + "]");
			else if(set.cursor() != null) {
				IRule rule = set.cursor();
				if(rule.isItemType()) {
					if(rule.isItemToken()) System.out.println("      \\ Shift Action [" + set.itemName + " -> " + set + "]");
					else System.out.println("      \\ Goto Action [" + set.itemName + " -> " + set + "]");
				} else System.out.println("      \\ Shift Action [" + set.itemName + " -> " + set + "]");
			}
		}
		for(int i = 0; i < state.list.size(); i++) {
			IItem item = state.list.get(i);
			System.out.println("  Item: " + item);
			for(IRuleList set : item.list) {
				if(set.index == set.size()) {
					System.out.println("      \\ Reduction Action [" + item + " -> " + set + "]");
					if(state.list.size() == 1) System.out.println("[Close] This state is complete");
				} else if(set.cursor() != null) {
					IRule rule = set.cursor();
					if(rule.isItemType()) {
						if(rule.isItemToken()) System.out.println("      \\ Shift Action [" + item + " -> " + set + "]");
						else System.out.println("      \\ Goto Action [" + item + " -> " + set + "]");
					} else System.out.println("      \\ Shift Action [" + item + " -> " + set + "]");
				}
			}
		}
	}
	
	private IItem getGrammarItem(String name) {
		Item item = grammar.getItem(name);
		IItem result = new IItem(name);
		
		for(RuleList set : item.getRules()) {
			result.add(item, set);
		}
		
		return result;
	}
	
	public class IState {
		// Contains current stages...
		protected final List<IRuleList> rules;
		protected final List<IItem> list;
		public final List<IState> next;
		
		// What states this state connects to.
		private final List<IRuleList> allRules;
		
		// What rule is used to connect to this state.
		private IRule action;
		
		// The name of this state.
		private String name;
		
		private IState(String name) {
			this.allRules = new ArrayList<>();
			this.rules = new ArrayList<>();
			this.list = new ArrayList<>();
			this.next = new ArrayList<>();
			this.name = name;
		}
		
		private IState accept() {
			allRules.clear();
			allRules.addAll(rules);
			allRules.addAll(list.stream().flatMap(x -> x.list.stream()).collect(Collectors.toList()));
			return this;
		}
		
		public boolean hasItem(String name) {
			return list.parallelStream().anyMatch(x -> x.name.equals(name));
		}
		
		public void addAll(Item item) {
			IItem found = get(item.getName());
			if(found == null) {
				found = new IItem(item.getName());
				list.add(found);
				
				for(RuleList set : item.getRules()) {
					found.add(item, set);
				}
			}
		}
		
		public IState clone() {
			IState copy = new IState(name);
			copy.action = action;
			copy.rules.addAll(rules.stream().map(x -> x.fullClone()).collect(Collectors.toList()));
			copy.list.addAll(list.stream().map(x -> x.fullClone()).collect(Collectors.toList()));
			return copy.accept();
		}
		
		public IItem get(String name) {
			return list.stream().filter(x -> x.name.equals(name)).findFirst().orElse(null);
		}
		
		public IRuleList getRule(int index) {
			return allRules.get(index);
		}
		
		public IRule action() {
			return action;
		}
		
		public int size() {
			return allRules.size();
		}
		
		@Override
		public boolean equals(Object obj) {
			if(!(obj instanceof IState)) return false;
			IState state = (IState)obj;
			
			if(!action.equals(state.action)) return false;
			if(rules.size() != state.rules.size()) return false;
			
			// We only need to compare the rules because all the items are computed
			// from the rules list.
			for(int i = 0; i < rules.size(); i++) {
				IRuleList a = rules.get(i);
				IRuleList b = state.rules.get(i);
				if(!a.equals(b)) return false;
			}
			
			return true;
		}
		
		public String getName() {
			return name;
		}
		
		public String toString() {
			return "R='" + rules + "', A='" + action + "', L=" + list;
		}
	}
	

	public class IItem {
		public List<IRuleList> list;
		public String name;
		public boolean token;
		
		private IItem(String name) {
			this.list = new ArrayList<>();
			this.name = name;
		}
		
		public IItem(Item item) {
			if(item instanceof ItemToken) token = true;
			name = item.getName();
			list = item.matches.stream()
				.map(x -> new IRuleList(name, x))
				.collect(Collectors.toList());
		}
		
		public void add(Item item, RuleList set) {
			list.add(new IRuleList(item.getName(), set));
		}
		
		public IItem fullClone() {
			IItem copy = new IItem(name);
			copy.token = token;
			copy.list.addAll(list.stream().map(x -> x.fullClone()).collect(Collectors.toList()));
			return copy;
		}
		
		public String getName() { return name; }
		
		@Override
		public boolean equals(Object obj) {
			if(!(obj instanceof IItem)) return false;
			return name.equals(((IItem)obj).name);
		}
		
		public String toString() {
			return name;
		}
	}
	
	public class IRuleList {
		public List<IRule> rules;
		public String itemName;
		public int index;
		
		private IRuleList(String itemName, RuleList set) {
			this.itemName = itemName;
			this.rules = set.getRules().stream()
				.map(x -> new IRule(x))
				.collect(Collectors.toList());
		}
		
		public IRuleList(IRuleList list) {
			itemName = list.itemName;
			rules = new ArrayList<>(list.rules);
		}
		
		public int size() { return rules.size(); }
		public IRule get(int index) { return rules.get(index); }
		
		public IRule cursor() { return cursor(0); }
		public IRule cursor(int offset) {
			if(index + offset >= rules.size()) return null;
			return rules.get(index + offset);
		}
		
		public IRuleList clone() { return new IRuleList(this); }
		public IRuleList fullClone() {
			IRuleList copy = new IRuleList(this);
			copy.index = index;
			return copy;
		}
		
		public boolean equals(Object obj) {
			if(!(obj instanceof IRuleList)) return false;
			IRuleList set = (IRuleList)obj;
			
			if(index != set.index) return false;
			if(!itemName.equals(set.itemName)) return false;
			if(rules.size() != set.size()) return false;
			for(int i = 0; i < rules.size(); i++) {
				IRule a = rules.get(i);
				IRule b = set.get(i);
				if(!a.equals(b)) return false;
			}
			return true;
		}
		
		public String toString() {
			StringBuilder sb = new StringBuilder();
			for(int i = 0; i < rules.size(); i++) {
				IRule rule = rules.get(i);
				
				if(i == index) sb.append("\u2022 ");
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
	
	public enum IType { ITEM, STRING, REGEX, SPECIAL, INVALID }
	public class IRule {
		protected IType type = IType.INVALID;
		protected String value = null;
		
		private IRule(Rule rule) {
			value = rule.value();
			if(rule instanceof ItemRule) type = IType.ITEM;
			else if(rule instanceof StringRule) type = IType.STRING;
			else if(rule instanceof RegexRule) type = IType.REGEX;
			else if(rule instanceof SpecialRule) type = IType.SPECIAL;
			else throw new GrammarException("Invalid group type -> " + rule.getClass());
		}
		
		public boolean isItemType() { return type == IType.ITEM; }
		public boolean isItemToken() { return grammar.getItem(value) instanceof ItemToken; }
		
		public IType type() { return type; }
		public String value() { return value; }
		
		public boolean equals(Object obj) {
			if(!(obj instanceof IRule)) return false;
			IRule rule = (IRule)obj;
			return type == rule.type && (value == null ? (rule.value == null):(value.equals(rule.value)));
		}
		
		public String toString() { return value; }
	}
}
