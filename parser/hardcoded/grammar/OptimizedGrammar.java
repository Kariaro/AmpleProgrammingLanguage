package hardcoded.grammar;

import java.util.*;
import java.util.stream.Collectors;

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
 * The idea is to take a grammar that uses complicated bracket
 * captures and change it so that it does the same thing but
 * without any bracket groups.
 * 
 * @author HardCoded
 */
public class OptimizedGrammar extends Grammar {
	
	public OptimizedGrammar(Grammar grammar) {
		grammar.tokens.values().forEach((t) -> {
			ItemToken token = new ItemToken(t.name);
			for(RuleList rule : t.matches) token.matches.add(cloneRuleList(rule));
			tokens.put(t.name, token);
		});
		
		grammar.items.values().forEach((i) -> {
			Item token = new Item(i.name);
			for(RuleList rule : i.matches) token.matches.add(cloneRuleList(rule));
			items.put(i.name, token);
		});
		
		optimize();
		
		int ruleId = 0;
		for(Item item : items.values()) {
			for(Rule rule : item.matches) rule.ruleId = (++ruleId);
		}
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
	 * Compares the content of a and b and compares if a
	 * could replace b.
	 * 
	 * @param a the item to check for equality
	 * @param b the item that you comapre with
	 * @return true if a is equivalent to b
	 */
	private boolean contentEquals(Item a, Item b) {
		if(a.matches.size() != b.matches.size()) return false;
		
		Set<String> searched = new HashSet<String>();
		searched.add(b.name);
		
		return contentEquals(a, b, searched);
	}
	
	/**
	 * Searched contains the items that has already been compared
	 */
	private boolean contentEquals(Item a, Item b, Set<String> searched) {
		if(a.matches.size() != b.matches.size()) return false;
		
		for(int i = 0; i < a.matches.size(); i++) {
			RuleList a_set = a.matches.get(i);
			RuleList b_set = b.matches.get(i);
			if(a_set.size() != b_set.size()) return false;
			
			for(int j = 0; j < a_set.size(); j++) {
				Rule a_rule = a_set.rules.get(j);
				Rule b_rule = b_set.rules.get(j);
				if(a_rule.getClass() != b_rule.getClass()) return false;
				
				if(b_rule instanceof BracketRule) {
					throw new UnsupportedOperationException("Trying to compare two items that contain BracketRules." +
															"There should not be any bracketRules in these items");
				}
				
				if(b_rule instanceof StringRule) {
					StringRule a_r = (StringRule)a_rule;
					StringRule b_r = (StringRule)b_rule;
					if(!a_r.value.equals(b_r.value)) return false;
				} else if(b_rule instanceof SpecialRule) {
					SpecialRule a_r = (SpecialRule)a_rule;
					SpecialRule b_r = (SpecialRule)b_rule;
					if(a_r.type != b_r.type) return false;
				} else if(b_rule instanceof RegexRule) {
					RegexRule a_r = (RegexRule)a_rule;
					RegexRule b_r = (RegexRule)b_rule;
					if(!a_r.pattern.pattern().equals(b_r.pattern.pattern())) return false;
					
					// This capture group should not be allowed in normal items....
				} else if(b_rule instanceof ItemRule) {
					ItemRule a_ir = (ItemRule)a_rule;
					ItemRule b_ir = (ItemRule)b_rule;
					
					// System.out.println(" -> " + a_ir + "/" + b_ir);
					if(a_ir.name.equals(b_ir.name)) {
						continue; // If the item is matching then it should work. 
					}
					
					if(!searched.contains(b_ir.name)) {
						searched.add(b_ir.name);
						
						// There should never be any token reduction so this should never work.
						return contentEquals(getItem(a_ir.name), getItem(b_ir.name));
					}
				}
			}
		}
		
		return true;
	}
	
	
	/**
	 * All ruleLists that contain brackets should be optimized away..
	 */
	private BracketRule firstBracket(RuleList list) {
		Optional<Rule> rule = list.rules.stream().filter(x -> x instanceof BracketRule).findFirst();
		return rule.isPresent() ? (BracketRule)rule.get():null;
	}
	
	/**
	 * Optimizes all items inside the grammar.
	 */
	private void optimize() {
		List<Item> result = new ArrayList<>(this.items.values());
		long subRules = items.values().parallelStream().flatMap((i) -> i.matches.stream()).count();
		long totRules = items.values().parallelStream().count();
		
		// DONE: Optimize this so that it does not contain any more bracket matches.
		//       This should not be done while loading the grammar but when creating the
		//       LRParser with the LRParserGenerator.
		
		do {
			result = reduceBrackets(result);
		} while(!result.isEmpty());
		
		// DONE: If multiple RuleLists contains the exact same operators then it should
		//       be optimized to only one ruleList.. This should only happen for rules
		//       inside the same item. This should be applied after the bracket reduction
		//       has been made.
		
		result = new ArrayList<>(this.items.values());
		for(int i = 0; i < result.size(); i++) {
			Item item = result.get(i);
			
			List<String> names = reduceGroups(item);
			replaceItemsAndRemove(item, names);
			
			result = new ArrayList<>(this.items.values());
		}
		
		System.out.println("=================================================================");
		items.values().forEach(i -> {
			System.out.println("Item: " + i);
			for(RuleList set : i.matches) {
				System.out.println("    | " + set);
			}
			System.out.println();
		});
		
		System.out.println("Done with reduction:");
		System.out.printf("    rules   : %4d / %4d\n", totRules, items.values().parallelStream().count());
		System.out.printf("    elements: %4d / %4d\n", subRules, items.values().parallelStream().flatMap((i) -> i.matches.stream()).count());
	}
	
	private List<String> reduceGroups(Item a) {
		return items.values().parallelStream().filter(b -> (a != b && contentEquals(a, b))).map(i -> i.name).collect(Collectors.toList());
	}
	
	private void replaceItemsAndRemove(Item replace, List<String> items) {
		this.items.values().forEach(i -> {
			for(RuleList set : i.matches) {
				for(Rule rule : set.rules) {
					if(rule instanceof ItemRule) {
						ItemRule ir = (ItemRule)rule;
						
						for(String name : items) {
							if(ir.name.equals(name)) {
								ir.name = replace.name;
							}
						}
					}
				}
			}
		});
		
		for(String name : items) this.items.remove(name);
	}
	
	/**
	 * Simplifies all brackets found inside the list.
	 * 
	 * @param items
	 * @return all the items that has changed during the reduction.
	 */
	private List<Item> reduceBrackets(List<Item> items) {
		List<Item> changed = new ArrayList<>();
		
		items.forEach((i) -> {
			for(int index = 0; index < i.matches.size(); index++) {
				RuleList set = i.matches.get(index);
				
				BracketRule bracket = firstBracket(set);
				if(bracket == null) continue;
				
				if(!changed.contains(i)) changed.add(i);
				
				if(bracket.repeat) {
					Item created = reduceSquare(i, set);
					this.items.put(created.name, created);
					changed.add(created);
				} else {
					List<RuleList> rules = reduceRound(set);
					i.matches.set(index    , rules.get(0));
					i.matches.add(index + 1, rules.get(1));
					index++;
				}
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
		
		list.add(a);
		list.add(b);
		return list;
	}
	
	/**
	 * Reduce the square capture group.
	 */
	private Item reduceSquare(Item parent, RuleList set) {
		long count = items.values().parallelStream().filter(i -> i.name.startsWith("#" + parent.name + " ")).count();
		String name = "#" + parent.name + " " + (count + 1);
		
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
		
		// throw new BracketExpectedException(); ??????
		return null;
	}
}
