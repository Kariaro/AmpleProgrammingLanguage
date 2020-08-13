package hardcoded.parser;

import java.util.*;
import java.util.stream.Collectors;

import hardcoded.grammar.Grammar;
import hardcoded.grammar.Grammar.*;
import hardcoded.grammar.OptimizedGrammar;
import hardcoded.visualization.DFAVisualization;
import hc.errors.grammar.GrammarException;

/**
 * This class will create a LR0_Parser for a specified grammar.<br>
 * 
 * https://en.wikipedia.org/wiki/Finite-state_machine
 * 
 * @author HardCoded
 */
@Deprecated
public class LR0_ParserGenerator {
	
	public LR0_ParserGenerator() {
		
	}
	
	// The grammar we are generating a parser for
	private Grammar grammar;
	
	// This list will contain all global states..
	private List<IState> globalStates = new ArrayList<>();
	
	public LR0_Parser generateParser(Grammar grammar) {
		if(!(grammar instanceof OptimizedGrammar)) {
			throw new GrammarException("This grammar is not optimized");
		}
		
		this.grammar = grammar;
		
		String startGroupName = grammar.getStartItem();
		if(startGroupName == null) {
			// Could not find a valid start item....
			throw new GrammarException("The grammar did not specify any start items");
		}
		
		IState state = createEntrySet(startGroupName);
		globalStates.add(state);
		computeClosure(state);
		
		for(int i = 0; i < globalStates.size(); i++) {
			globalStates.get(i).name = "I" + Integer.toString(i);
			globalStates.get(i).id = i;
			globalStates.get(i).accept();
			
			// prettyPrint(globalStates.get(i));
		}
		
		new DFAVisualization().show(globalStates);
		
		// NOTE: This could be a nice addition.. Only for visual stuff though.
		//       Sort the set list so that all terminals are on the left and all non-terminals are on the right inside this list.
		List<IRule> set = globalStates.stream().map(x -> x.action).filter(x -> x != null).distinct().collect(Collectors.toList());
		
		return new LR0_Parser(new ITable(set, globalStates));
	}
	
	public class ITable {
		public List<IRule> set;
		public List<IRow> rows;
		private IAction entry;
		
		private ITable(List<IRule> set, List<IState> states) {
			this.set = set;
			entry = new IAction(null, 0, 0);
			rows = new ArrayList<>();
			
			for(int i = 0; i < states.size(); i++) {
				rows.add(new IRow(set, states.get(i)));
			}
		}
		
		@Override
		public String toString() {
			StringBuilder sb = new StringBuilder();
			sb.append("ITable: ").append(rows.size()).append(" ").append(rows.size() == 1 ? "entry\n":"entries\n");
			
			sb.append("state: ");
			for(int i = 0; i < set.size(); i++) {
				String string = set.get(i).toString();
				if(string.length() > 7) string = string.substring(0, 7);
				
				sb.append(String.format("%7s ", string));
			}
			
			if(set.size() > 0) sb.deleteCharAt(sb.length() - 1);
			
			sb.append("\n");
			
			for(int i = 0; i < rows.size(); i++) {
				sb.append(String.format("%5d: %s\n", i, rows.get(i)));
			}
			
			if(rows.size() > 0) sb.deleteCharAt(sb.length() - 1);
			return sb.toString();
		}
		
		public IRow getRow(int state) {
			return rows.get(state);
		}

		public IAction start() {
			return entry;
		}
	}
	
	public class IRow {
		private final IAction[] actions;
		
		public IRow(List<IRule> set, IState states) {
			actions = new IAction[set.size()];
			
//			System.out.println("Create Row: " + states);
//			for(IState state : states.next) {
//				System.out.println("    : " + state);
//			}
			
			if(states.next.isEmpty()) {
				IAction action = new IAction(states, 1, states.id);
				
				for(int i = 0; i < actions.length; i++) {
					actions[i] = action;
				}
			} else {
				for(IState state : states.next) {
					int index = set.indexOf(state.action);
					
					if(index == -1) continue;
					actions[index] = new IAction(state);
				}
			}
		}
		
		public IAction[] actions() {
			return actions;
		}
		
		public String toString() {
			StringBuilder sb = new StringBuilder();
			for(IAction action : actions) sb.append(String.format("%7s ", (action == null ? "":action)));
			if(actions.length > 0) sb.deleteCharAt(sb.length() - 1);
			if(actions[0] != null && actions[0].type == 1) {
				sb.append(" ").append(actions[0].state.rules);
				IState state = actions[0].state;
				
				System.out.println("State: " + state.name + "/" + state);
				System.out.println("     : " + state.id);
				System.out.println("     : " + state.action);
				System.out.println("     : " + state.allRules);
			}
			return sb.toString();
		}
	}
	
	public class IAction {
		/**
		 * 0: Shift<br>
		 * 1: Reduce
		 */
		public int type;
		public int index;
		public IRule rule;
		public IState state;
		
		private IAction(IState state, int type, int index) {
			this.state = state;
			this.index = index;
			this.type = type;
		}
		
		public IAction(int type, int index) {
			this.index = index;
			this.type = type;
		}
		
		private IAction(IState state) {
			this.state = state;
			rule = state.action;
			index = globalStates.indexOf(state);
		}
		
		@Override
		public String toString() {
			if(type == 0) return "S" + index;
			if(type == 1) return "r" + index;
			return "?ERR?" + index;
		}
	}
	
	// TODO: If there is a chain with only only terminals left, find the start
	//       of the chain and reduce it to only one state.
	private void computeClosure(IState state) {
		IState current = state.clone();
		
		List<IState> nextSet = new ArrayList<>();
		IState next;
		
		Set<Integer> visited = new HashSet<>();
		for(int i = 0; i < current.size(); i++) {
			next = createNextState(current, i, visited);
			
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
	 * Creates the next logical state from a given state.
	 * 
	 * @param state
	 * @param index
	 * @param visited The indexes that has already been searched.
	 * @return
	 */
	private IState createNextState(IState state, int index, Set<Integer> visited) {
		IState result;
		
		{
			IRule rule = state.getRule(index).cursor();
			if(rule == null) {
				// This means that this is not a LR(0) grammar
				// and should throw an error
				//
				// Or that this is the end of the chain.
				// We could not find any new states to calculate.
				return null;
			}
			
			// There is a possibility that we have already searched this rule..
			// We need to check if we have used this rule already.
			if(visited.contains(index)) return null;
			
			result = new IState("I" + globalStates.size());
			result.action = rule;
		}
		
		for(int i = 0; i < state.size(); i++) {
			IRuleList set = state.allRules.get(i);
			IRule rule = set.cursor();
			
			// Find all items that requires the next states token
			if(result.action.equals(rule)) {
				if(result.rules.indexOf(set) < 0) {
					if(!visited.contains(i)) {
						// FIXME: This could break some checks and I'm not sure which ones it does break.
						
						visited.add(i);
						set.index++;
					}
					
					result.rules.add(set.fullClone());
				}
				
				IRule next = set.cursor();
				if(next != null && next.isItemType() && !next.isItemToken()) {
					String name = next.value;
					
					if(!result.hasItem(name)) {
						// DONE: If this item has a leading non-terminal item then it should also be added
						//       to this list..
						result.list.add(getGrammarItem(name));
					}
				}
			}
		}
		
		for(int i = 0; i < result.list.size(); i++) {
			IItem item = result.list.get(i);
			
			for(IRuleList set : item.list) {
				IRule next = set.cursor();
				if(next != null && next.isItemType() && !next.isItemToken()) {
					String name = next.value;
					
					if(!result.hasItem(name)) {
						result.list.add(getGrammarItem(name));
					}
				}
			}
		}
		
		return result;
	}
	
	private IState createEntrySet(String startGroupName) {
		// TODO: Optimize this function..
		
		IState state = new IState("I0");
		Set<String> searched = new HashSet<>();
		List<Item> search = new ArrayList<>();
		
		{
			Item i = grammar.getItem(startGroupName);
			
			if(i == null)
				throw new GrammarException("Failed to create entry set. Item '" + startGroupName + "' does not exist!");
			
			search.add(i);
			searched.add(startGroupName);
		}
		
		while(!search.isEmpty()) {
			Item item = search.get(0);
			search.remove(0);
			state.addAll(item);
			
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
		
		if(state == null) {
			System.out.println();
			return;
		}
		
		for(IRuleList set : state.rules) {
			if(set.index == set.size()) {
				System.out.println("      R [" + set.itemName + " -> " + set + "]");
			} else if(set.cursor() != null) {
				System.out.println("      S [" + set.itemName + " -> " + set + "]");
			}
		}
		
		for(int i = 0; i < state.list.size(); i++) {
			IItem item = state.list.get(i);
			System.out.println("  Item: " + item);
			for(IRuleList set : item.list) {
				if(set.index == set.size()) {
					System.out.println("      R [" + item + " -> " + set + "]");
					if(state.list.size() == 1) System.out.println("[Close] This state is complete");
				} else if(set.cursor() != null) {
					System.out.println("      S [" + item + " -> " + set + "]");
				}
			}
		}
		
		System.out.println();
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
		
		// Used to create the ITable
		private int id;
		
		// What rule is used to connect to this state.
		private IRule action;
		
		// The name of this state.
		// XXX: Change the name to be ("I" + this.id)
		private String name;
		
		private IState(String name) {
			this.allRules = new ArrayList<>();
			this.rules = new ArrayList<>();
			this.list = new ArrayList<>();
			this.next = new ArrayList<>();
			this.name = name;
		}
		
		private IState accept() {
			// NOTE: Sort all the items such that the sets inside the rules list is combined iwth the rules inside the list to create
			//       a more even pattern..
			
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
		
		public List<IRuleList> getRules() {
			return Collections.unmodifiableList(rules);
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
			list = item.getRules().stream()
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
		private IType type = IType.INVALID;
		private String value = null;
		
		private IRule(Rule rule) {
			value = rule.value();
			if(rule instanceof ItemRule) type = IType.ITEM;
			else if(rule instanceof StringRule) type = IType.STRING;
			else if(rule instanceof RegexRule) type = IType.REGEX;
			else if(rule instanceof SpecialRule) type = IType.SPECIAL;
			else throw new GrammarException("Invalid group type -> " + rule.getClass());
		}
		
		public boolean match(String string) {
			if(type == IType.ITEM) {
				
				IItem item = getGrammarItem(value);
				
				if(item.token) {
					for(IRuleList set : item.list) {
						if(set.rules.get(0).match(string)) return true;
					}
					
					return false;
				} else {
					// Can't do anything here
				}
			}
			
			if(type == IType.REGEX) {
				System.out.println(value);
				return string.matches(value);
			}
			
			if(type == IType.STRING) {
				return value.equals(string);
			}
			
			return false;
		}

		public boolean isItemType() { return type == IType.ITEM; }
		public boolean isItemToken() { return grammar.getItem(value) instanceof ItemToken; }
		
		public Item asItem() { return grammar.getItem(value); }
		
		public IType type() { return type; }
		public String value() { return value; }
		
		public int hashCode() { return value.hashCode() * (type.ordinal() + 1); }
		public boolean equals(Object obj) {
			if(!(obj instanceof IRule)) return false;
			IRule rule = (IRule)obj;
			return type == rule.type && (value == null ? (rule.value == null):(value.equals(rule.value)));
		}
		
		public String toString() { return value; }
	}
}
