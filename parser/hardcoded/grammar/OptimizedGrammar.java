package hardcoded.grammar;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import hc.errors.grammar.GrammarException;

/**
 * This is a optimized grammar used for generating the LR(k)
 * parser for a grammar class.<br><br>
 *
 * This class works by introducing two reductions.
 * 
 *<pre># Square reduction S: . [ a ] $
 *S: . S0 $
 *S0: a S0
 *  | {EMPTY}</pre>
 *<pre># Round reduction S: . ( a ) $
 *S: . a $
 * | . $</pre> 
 * 
 * The idea is to take a grammar filled with brackets and to
 * change it such that  it does not contain any more bracket
 * capture groups.
 * 
 * @author HardCoded
 */
public class OptimizedGrammar extends Grammar {
	
	public OptimizedGrammar(Grammar grammar) {
		// System.out.println("Tokens");
		grammar.tokens.values().forEach((t) -> {
			// System.out.println("Token: token:" + t);
			
			ItemToken token = new ItemToken(t.name);
			
			for(RuleList rule : t.matches) {
				// System.out.println("     | " + rule);
				token.matches.add(cloneRuleList(rule));
			}
			
			tokens.put(t.name, token);
		});
		
		// System.out.println();
		// System.out.println("Items");
		grammar.items.values().forEach((i) -> {
			// System.out.println("Item: i:" + i);
			
			ItemToken token = new ItemToken(i.name);
			
			for(RuleList rule : i.matches) {
				// System.out.println("    | " + rule);
				token.matches.add(cloneRuleList(rule));
			}
			
			items.put(i.name, token);
		});
		// System.out.println();
		
		optimize();
	}
	
	private RuleList cloneRuleList(RuleList set) {
		RuleList list = new RuleList();
		list.rules.addAll(cloneList(set.rules));
		return list;
	}
	
	private List<Rule> cloneList(List<Rule> set) {
		List<Rule> list = new ArrayList<>();
		
		for(Rule rule : set) {
			Rule clone = null;
			if(rule instanceof RegexRule) {
				clone = new RegexRule(((RegexRule)rule).pattern.pattern());
			} else if(rule instanceof StringRule) {
				clone = new StringRule(((StringRule)rule).value);
			} else if(rule instanceof BracketRule) {
				clone = cloneBracket((BracketRule)rule);
			} else if(rule instanceof ItemRule) {
				clone = new ItemRule(((ItemRule)rule).name);
			} else if(rule instanceof SpecialRule) {
				clone = new SpecialRule(((SpecialRule)rule).type);
			}
			
			if(clone == null) {
				throw new GrammarException("Failed to clone set -> " + set);
			}
			
			list.add(clone);
		}
		
		return list;
	}
	
	private BracketRule cloneBracket(BracketRule set) {
		BracketRule rule = new BracketRule();
		rule.repeat = set.repeat;
		rule.matches.addAll(cloneList(set.matches));
		return rule;
	}
	
	
	
	
	/**
	 * All ruleLists that contain brackets should be optimized away..
	 */
	private BracketRule firstBracket(RuleList list) {
		Optional<Rule> rule = list.rules.stream().filter(x -> x instanceof BracketRule).findFirst();
		return rule.isPresent() ? (BracketRule)rule.get():null;
	}
	
	private void optimize() {
		List<Item> result = new ArrayList<>(this.items.values());
		long subRules = this.items.values().parallelStream().flatMap((i) -> i.matches.stream()).count();
		long totRules = this.items.values().parallelStream().count();
		
		// DONE: Optimize this so that it does not contain any more bracket matches.
		//       This should not be done while loading the grammar but when creating the
		//       LRParser with the LRParserGenerator.
		
		do {
			System.out.println("Items -> " + result);
			result = reduce(result);
		} while(!result.isEmpty());
		
		long subRules2 = this.items.values().parallelStream().flatMap((i) -> i.matches.stream()).count();
		long totRules2 = this.items.values().parallelStream().count();
		
		
		System.out.println("Done with reduction: ");
		System.out.println("    : " + subRules + "/" + subRules2);
		System.out.println("    : " + totRules + "/" + totRules2);
		
		// TODO: If multiple RuleLists contains the exact same operators then it should
		//       be optimized to only one ruleList.. This should only happen for rules
		//       inside the same item. This should be applied after the bracket reduction
		//       has been made.
		
	}
	
	/**
	 * Simplifies all brackets found inside the list.
	 * 
	 * @param items
	 * @return all the items that has changed during the reduction.
	 */
	private List<Item> reduce(List<Item> items) {
		List<Item> changed = new ArrayList<>();
		
		items.forEach((i) -> {
			for(int index = 0; index < i.matches.size(); index++) {
				RuleList set = i.matches.get(index);
				
				BracketRule bracket = firstBracket(set);
				if(bracket == null) {
					//System.out.println("    | " + set);
					continue;
				}
				
				if(!changed.contains(i)) changed.add(i);
				
				System.out.println("    | " + set);
				if(bracket.repeat) {
					Item created = reduceSquare(i, set);
					this.items.put(created.name, created);
					
					for(RuleList l : created.matches) System.out.println("    + -> " + l);
					
					changed.add(created);
				} else {
					List<RuleList> rules = reduceRound(set);
					
					for(RuleList l : rules) System.out.println("    + -> " + l);
					
					i.matches.set(index    , rules.get(0));
					i.matches.add(index + 1, rules.get(1));
					index++;
				}
				
				System.out.println();
			}
			
		});
		
		return changed;
	}
	
	/**
	 * Reduce the round capture group.
	 */
	private List<RuleList> reduceRound(RuleList set) {
		List<RuleList> list = new ArrayList<>();
		RuleList a = set;
		RuleList b = cloneRuleList(set);
		
		//   S > . ( a ) $
		// Becomes
		//   S  > . a $
		//      > . $
		
		for(int i = 0; i < set.size(); i++) {
			Rule rule = set.rules.get(i);
			if(rule instanceof BracketRule) {
				BracketRule br = (BracketRule)rule;
				
				a.rules.remove(i);
				a.rules.addAll(i, br.matches);
				b.rules.remove(i);
				break;
			}
		}
		// This is just going to change the current ruleset and nothing else.
		
		list.add(a);
		list.add(b);
		return list;
	}
	
	private int squareIndex = 0;
	
	/**
	 * Reduce the square capture group.
	 */
	private Item reduceSquare(Item parent, RuleList set) {
		// This is going to add a new instance to the items list..
		squareIndex++;
		
		String name = "#" + parent.name + squareIndex;
		//   S > . [ a ] $
		// Becomes
		//   S  > . S0 $
		//   S0 > a S0
		//      > {EMPTY}
		
		for(int i = 0; i < set.size(); i++) {
			Rule rule = set.rules.get(i);
			if(rule instanceof BracketRule) {
				set.rules.set(i, new ItemRule(name));
				
				RuleList a = new RuleList();
				a.rules.addAll(((BracketRule)rule).matches);
				a.add(new ItemRule(name));
				
				RuleList b = new RuleList();
				b.add(new SpecialRule(SPECIAL_EMPTY));
				
				Item item = new Item(name);
				item.matches.add(a);
				item.matches.add(b);
				
				return item;
			}
		}
		
		return null;
	}
}
