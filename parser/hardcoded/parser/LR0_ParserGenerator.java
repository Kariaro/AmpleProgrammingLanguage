package hardcoded.parser;

import java.util.*;
import java.util.stream.Collectors;

import hardcoded.grammar.Grammar;
import hardcoded.grammar.Grammar.*;
import hardcoded.grammar.OptimizedGrammar;
import hc.errors.grammar.GrammarException;

/**
 * 
 * @author HardCoded
 */
public class LR0_ParserGenerator {
	
	public LR0_ParserGenerator() {
	}
	
	private Grammar grammar;
	
	// https://en.wikipedia.org/wiki/Finite-state_machine
	public Object parse(Grammar grammar) {
		if(!(grammar instanceof OptimizedGrammar)) throw new GrammarException("This grammar is not optimized");
		
		this.grammar = grammar;
		
		//ITable table = new ITable();
		//table.types = getSymbolSet();
		// System.out.println(getSymbolSet());
		
		// TODO: Let the user change the start of the grammar effectivly making it into a
		//       augmented grammar.
		String startGroupName = "S";
		// startGroupName = "program";
		NState i0 = createNNEntrySet(startGroupName);
		System.out.println();
		System.out.println();
		
		globalStates.add(i0);
		computeClosure(i0);
		
		System.out.println("States -> " + globalStates.size());
		for(NState state : globalStates) {
			//prettyPrint(state);
			//System.out.println("====================================");
		}
		
		return null;
		
		//IState i0 = createEntrySet(startGroupName); return step(i0);
	}
	
	// This list will contain all global states..
	private List<NState> globalStates = new ArrayList<>();
	
	public Object computeClosure(NState state) {
		// dumpState(state);
		// prettyPrint(state);
		// System.out.println();
		
		NState current = state.clone();
		
		List<NState> nextSet = new ArrayList<>();
		NState next;
		
		while((next = createNextState(current)) != null) {
			// TODO: Before computing the closure of a state check that it is not
			//       already defined inside the global state list.
			
			int index = globalStates.indexOf(next);
			if(index < 0) {
				globalStates.add(next);
				state.next.add(next);
				computeClosure(next);
			} else {
				NState found = globalStates.get(index);
				state.next.add(found);
			}
			
			nextSet.add(next);
			
			// System.out.println();
		}
		
		// System.out.println(nextSet);
		return null;
	}
	
	public void prettyPrint(NState state) {
		System.out.println("state -> " + state);
		
		for(IRuleList set : state.rules) {
			if(set.index > set.size()) continue;
			
			if(set.index == set.size()) {
				System.out.println("      \\ Reduction Action [" + set.itemName + " -> " + set + "]");
			} else {
				// System.out.println("      | " + set);
				
				IRule rule = set.cursor();
				if(rule.isItemType()) {
					if(rule.isItemToken()) {
						System.out.println("      \\ Shift Action [" + set.itemName + " -> " + set + "]");
					} else {
						System.out.println("      \\ Goto Action [" + set.itemName + " -> " + set + "]");
					}
				} else {
					System.out.println("      \\ Shift Action [" + set.itemName + " -> " + set + "]");
				}
			}
		}
		
		for(int i = 0; i < state.list.size(); i++) {
			IItem item = state.list.get(i);
			
			System.out.println("  Item: " + item);
			for(IRuleList set : item.list) {
				// TODO: We have now found the IRuleList that coresponds to the correct 
				
				if(set.index == set.size()) {
					// TODO: Add to the state stuff.......
					// TODO: Add a reduction action to the table....
					
					// System.out.println("[Close] " + set);
					System.out.println("      \\ Reduction Action [" + item + " -> " + set + "]");
					
					if(state.list.size() == 1) {
						System.out.println("[Close] This state is complete");
					}
					
					//break;
				} else if(set.index > set.size()) {
					
				} else {
					// System.out.println("      | " + set);
					
					IRule rule = set.cursor();
					if(rule.isItemType()) {
						if(rule.isItemToken()) {
							System.out.println("      \\ Shift Action [" + item + " -> " + set + "]");
						} else {
							System.out.println("      \\ Goto Action [" + item + " -> " + set + "]");
							// This should continue........
							// Recursivly call this tree now.. 
						}
					} else {
						System.out.println("      \\ Shift Action [" + item + " -> " + set + "]");
					}
				}
			}
		}
	}
	
	/*
	@Deprecated
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
					if(step.rules.indexOf(set) < 0) {
						step.rules.add(set);
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
					
					if(next != null && next.isItemType() && !next.isItemToken()) {
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
	
	private int groupIndex = 1;
	public Object step(IState i0) {
		dumpState(i0);
		
		INextStep step;
		while((step = getNextRun(i0)).selector != null) {
			System.out.println("group -> I" + (groupIndex++));
			closure(step);
			//System.out.println(step);
			System.out.println();
			System.out.println();
		}
		
		return null;
	}
	
	// TODO: Calculate the closure of the main set.
	// TODO: To be able to do this, first get all the items of each unique tokens in the set 
	private Object closure(INextStep step) {
		System.out.println("step -> " + step);
		
		// TODO: Do not handle logic in here.
		//       This method should only create a new IState from step and
		//       add to the global state list...
		
		for(IRuleList set : step.rules) {
			// TODO: This should never happen when getting the next set of rules!
			if(set.index > set.size()) continue;
			
			if(set.index == set.size()) {
				System.out.println("      \\ Reduction Action [" + set.itemName + " -> " + set + "]");
			} else {
				// System.out.println("      | " + set);
				
				IRule rule = set.cursor();
				if(rule.isItemType()) {
					if(rule.isItemToken()) {
						System.out.println("      \\ Shift Action [" + set.itemName + " -> " + set + "]");
					} else {
						System.out.println("      \\ Goto Action [" + set.itemName + " -> " + set + "]");
					}
				} else {
					System.out.println("      \\ Shift Action [" + set.itemName + " -> " + set + "]");
				}
			}
		}
		
		for(int i = 0; i < step.items.size(); i++) {
			IItem item = step.items.get(i);
			
			System.out.println("  Item: " + item);
			for(IRuleList set : item.list) {
				// TODO: We have now found the IRuleList that coresponds to the correct 
				
				if(set.index == set.size()) {
					// TODO: Add to the state stuff.......
					// TODO: Add a reduction action to the table....
					
					// System.out.println("[Close] " + set);
					System.out.println("      \\ Reduction Action [" + item + " -> " + set + "]");
					
					if(step.items.size() == 1) {
						System.out.println("[Close] This state is complete");
					}
					
					//break;
				} else if(set.index > set.size()) {
					
				} else {
					// System.out.println("      | " + set);
					
					IRule rule = set.cursor();
					if(rule.isItemType()) {
						if(rule.isItemToken()) {
							System.out.println("      \\ Shift Action [" + item + " -> " + set + "]");
						} else {
							System.out.println("      \\ Goto Action [" + item + " -> " + set + "]");
							// This should continue........
							// Recursivly call this tree now.. 
						}
					} else {
						System.out.println("      \\ Shift Action [" + item + " -> " + set + "]");
					}
				}
			}
		}
		
		return null;
	}
	*/
	
	/**
	 * Creates the next logical state from this state..
	 * 
	 * Returns null if this state was a final state and can't be reduced anymore.
	 * @param state
	 * @return
	 */
	private NState createNextState(NState state) {
		// TODO: Add some atomic counter to increment this value...
		NState result = new NState("I");
		
		// First we get the next selector that we need
		for(IRuleList set : state.rules) {
			IRule rule = set.cursor();
			if(rule == null) continue; // Prevents loops
			
			if(result.action == null) {
				result.action = rule;
				break;
			}
		}
		
		if(result.action == null) {
			// If there was no new actions found inside the old rule sets...
			
			for(IItem item : state.list) {
				for(IRuleList set : item.list) {
					IRule rule = set.cursor();
					if(rule == null) continue; // Prevents loops
					
					if(result.action == null) {
						result.action = rule;
						break;
					}
				}
			}
		}
		
		if(result.action == null) {
			// We could not find any new states to calculate.
			return null;
		}
		
		// TODO: Ensure that we remove the old rules that are not used.
		for(IRuleList set : state.rules) {
			IRule rule = set.cursor();
			
			// Find all items that requires the next states token
			if(result.action.equals(rule)) {
				if(result.rules.indexOf(set) < 0) {
					set.index++;
					result.rules.add(set.fullClone());
				}
				
				IRule next = set.cursor();
				if(next != null && next.isItemType() && !next.isItemToken()) {
					String name = next.value();
					
					if(!result.hasItem(name)) {
						result.list.add(getGrammarItem(name));
					}
				}
			}
		}
		
		for(IItem item : state.list) {
			for(IRuleList set : item.list) {
				IRule rule = set.cursor();
				
				// Find all items that requires the next states token
				if(result.action.equals(rule)) {
					if(result.rules.indexOf(set) < 0) {
						set.index++;
						result.rules.add(set.fullClone());
					}
					
					IRule next = set.cursor();
					// DONE: If the next token is a non-terminal value
					//       it should be added to the items list as a
					//       new set...
					
					// DONE: If the next token is a non-terminal then
					//       it could belong to a set not pressent inside
					//       the current state.. This should be found inside
					//       the grammar instead...
					
					if(next != null && next.isItemType() && !next.isItemToken()) {
						String name = next.value();
						
						if(!result.hasItem(name)) {
							result.list.add(getGrammarItem(name));
						}
					}
				}
			}
		}
		
		return result;
	}
	
	@Deprecated
	private class INextStep {
		// The rules that match the current selector.
		// This list will never have a IRuleList where the index is zero.
		private List<IRuleList> rules = new ArrayList<>();
		
		// All non-terminal items found at the cursor of the
		// list in rules.
		private List<IItem> items = new ArrayList<>();
		
		
		private IRule selector;
		
		public boolean hasItem(String name) {
			return items.parallelStream().anyMatch(x -> x.name.equals(name));
		}
		
		public String toString() {
			return "R='" + rules + "', C='" + selector + "', L=" + items;
		}
	}
	
	private class IItem {
		public List<IRuleList> list;
		public String name;
		public boolean token;
		
		public IItem(String name) {
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
		
//		public IItem(IItem item) {
//			token = item.token;
//			name = item.name;
//			list = item.list.stream()
//				.map(x -> x.clone())
//				.collect(Collectors.toList());
//		}
		
		public void add(Item item, RuleList set) {
			list.add(new IRuleList(item.getName(), set));
		}
		
		public IItem fullClone() {
			IItem copy = new IItem(name);
			copy.token = token;
			copy.list.addAll(list.stream().map(x -> x.fullClone()).collect(Collectors.toList()));
			return copy;
		}
		
		@Override
		public boolean equals(Object obj) {
			if(!(obj instanceof IItem)) return false;
			return name.equals(((IItem)obj).name);
		}
		public String getName() { return name; }
		public boolean isToken() { return token; }
		
		public String toString() {
			return name;
		}
	}
	
	private class IRuleList {
		public List<IRule> rules;
		public String itemName;
		public int index;
		
		public IRuleList(String itemName, RuleList set) {
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
		// public boolean isEmpty() { return rules.isEmpty(); }
		
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
	private class IRule {
		protected NType type = NType.INVALID;
		protected String value = null;
		
		public IRule(Rule rule) {
			value = rule.value();
			if(rule instanceof ItemRule) type = NType.ITEM;
			else if(rule instanceof StringRule) type = NType.STRING;
			else if(rule instanceof RegexRule) type = NType.REGEX;
			else if(rule instanceof SpecialRule) type = NType.ITEM;
			else throw new GrammarException("Invalid group type -> " + rule.getClass());
		}
		
		public boolean isItemType() { return type == NType.ITEM; }
		public boolean isItemToken() {
			return grammar.getItem(value) instanceof ItemToken;
		}
		
		public boolean equals(Object obj) {
			if(!(obj instanceof IRule)) return false;
			IRule rule = (IRule)obj;
			return type == rule.type && (value == null ? (rule.value == null):(value.equals(rule.value)));
		}
		
		public NType type() { return type; }
		public String value() { return value; }
		public String toString() { return value; }
	}
	
	private NState createNNEntrySet(String startGroupName) {
		// TODO: Use an AtomicInteger to increment the state number.
		NState state = new NState("I");
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
							Item i = grammar.getItem(ir.getName());
							
							if(i == null)
								throw new GrammarException("Failed to create entry set. Item '" + ir.getName() + "' does not exist!");
							
							search.add(i);
							searched.add(ir.getName());
						}
					}
				}
			}
		}
		
		return state;
	}
	
	// ===============================================================
	
	private void dumpState(NState state) {
		System.out.println("State: " + state);
		System.out.println("Rules:");
		for(IRuleList set : state.rules) {
			System.out.println("    > [" + set.itemName + " -> " + set + "]");
		}
		
		System.out.println("Items:");
		for(IItem item : state.list) {
			System.out.println("    > " + item);
			for(IRuleList set : item.list) {
				System.out.println("      | " + set);
			}
		}
		System.out.println();
	}
	
	
	// ===============================================================
	/*
	
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
	
	*/
	
	private IItem getGrammarItem(String name) {
		Item item = grammar.getItem(name);
		IItem result = new IItem(name);
		
		for(RuleList set : item.getRules()) {
			result.add(item, set);
		}
		
		return result;
	}
	
	/*
	private List<IType> getSymbolSet() {
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
	
	// TODO: Remove this class.
	private class IType {
		/**
		 * 1: Item
		 * 2: Token
		 * 3: String
		 * 4: Special
		 *
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
	*/
	
	public class NState {
		// Contains current stages...
		protected final List<IRuleList> rules;
		protected final List<IItem> list;
		
		// What rule is used to connect to this state.
		protected IRule action;
		
		// What states this state connects to.
		protected List<NState> next;
		
		// The name of this state.
		protected String name;
		
		public NState(String name) {
			this.rules = new ArrayList<>();
			this.list = new ArrayList<>();
			this.next = new ArrayList<>();
			this.name = name;
		}
		
		public void add(Item item, RuleList set) {
			IItem found = get(item.getName());
			if(found == null) {
				found = new IItem(item.getName());
				list.add(found);
			}
			
			found.add(item, set);
		}
		
		public void addAll(IItem item) {
			if(!hasItem(item.name)) list.add(item);
		}
		
		public boolean hasItem(String name) {
			return list.parallelStream().anyMatch(x -> x.name.equals(name));
		}
		
		public void addAll(Item item) {
			if(item == null) {
				Thread.dumpStack();
				throw new NullPointerException("Found null item");
			}
			IItem found = get(item.getName());
			if(found == null) {
				found = new IItem(item.getName());
				list.add(found);
				
				for(RuleList set : item.getRules()) {
					found.add(item, set);
				}
			}
		}
		
		public NState clone() {
			NState copy = new NState(name);
			copy.action = action;
			copy.rules.addAll(rules.stream().map(x -> x.fullClone()).collect(Collectors.toList()));
			copy.list.addAll(list.stream().map(x -> x.fullClone()).collect(Collectors.toList()));
			return copy;
		}
		
		public IItem get(String name) {
			return list.stream().filter(x -> x.name.equals(name)).findFirst().orElse(null);
		}
		
		@Override
		public boolean equals(Object obj) {
			if(!(obj instanceof NState)) return false;
			NState state = (NState)obj;
			
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
			// "N='" + name + "', 
			return "R='" + rules + "', A='" + action + "', L=" + list;
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
			
			found.add(item, set);
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
					found.add(item, set);
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
}
