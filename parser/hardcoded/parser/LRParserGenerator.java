package hardcoded.parser;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import hardcoded.grammar.Grammar;
import hardcoded.grammar.Grammar.*;
import hardcoded.grammar.Rule;

/**
 * Sources:<br>
 *  - https://www.cs.ru.ac.za/compilers/pdfvers.pdf<br>
 *  - http://www.orcca.on.ca/~watt/home/courses/2007-08/cs447a/notes/LR1%20Parsing%20Tables%20Example.pdf<br>
 *  - https://en.wikipedia.org/wiki/Canonical_LR_parser<br><br><br>
 * 
 * This is a LR(k) parser generator.
 * 
 * @author HardCoded
 */
public class LRParserGenerator {
	public LRParserGenerator() {
		
	}
	
	/**
	 * Generate a LR(k) parse from a given grammar.<br>
	 * 
	 * A higher k value can drastically increase the amount
	 * of states that the parser needs to check.
	 * 
	 * @param k the amount of tokens to look ahead
	 * @param grammar the grammar to generate a parser to
	 * @return a LR(k) parser generated from the grammar.
	 */
	public LRParser generateParser(int k, Grammar grammar) {
		
		
		return null;
	}
	
	/**
	 * This function might be a bit more tricky... This is really complicated.
	 * @param grammar
	 * @return A set of rules that can be found infront of each typeSet.
	 */
	public Set<Rule> FIRST(Grammar grammar) {
		// TODO: Complex stuff with bracket items GRRRR
		// TODO: Solve how to understand bracket items......
		
		Set<Item> items = grammar.getItems();
		
		System.out.println("Items: " + items);
		items.forEach((t) -> {
			System.out.println("Item: i:" + t);
			for(RuleList set : t.getRules()) {
				System.out.println("    | " + set.getRuleString() + " > " + set);
				
			}
			System.out.println();
		});
		System.out.println("==================================");
		System.out.println("==================================");
		System.out.println("==================================");
		System.out.println();
		System.out.println();
		
		items.forEach(a -> {
			System.out.println("Item: i:" + a);
			
			// NOTE: Special cases
			//   If a item "A" has a rule that only contains another item "B" then
			//   tream "A" as if it was also the item "B".
			
			getBeforeItem(a, grammar);
			System.out.println("==================================");
			
			/*
			for(Item b : grammar.getItems()) {
				// Compare a && b
				
				for(RuleList set : b.getRules()) {
					System.out.println("    | " + set.getRuleString() + " > " + set);
					
					List<Rule> rules = set.getRules();
					if(rules.size() == 1) {
						Rule rule = rules.get(0);
						
						if(rule instanceof ItemRule) {
							ItemRule ir = (ItemRule)rule;
							// Check that we have not already searched this rule.
							
						}
					}
					for(int j = 1; j < set.size(); j++)  {
						Rule rule = rules.get(j);
						
						if(rule instanceof ItemRule) {
							ItemRule ir = (ItemRule)rule;
							
							if(ir.getName().equals(a.getName())) {
								// TODO: If the rule before this item is not a string rule
								//       then search for the terminal value of that rule......
								
								System.out.println("      | Before   > " + rules.get(j - 1));
								System.out.println("      | ItemRule > " + rule);
								System.out.println();
							}
						}
					}
					
				}
			}
			
//			for(RuleList set : a.getRules()) {
//				System.out.println("    | " + set.getRuleString() + " > " + set);
//				
//				List<Rule> rules = set.getRules();
//				for(int j = 1; j < set.size(); j++)  {
//					Rule rule = rules.get(j);
//					
//					if(rule instanceof ItemRule) {
//						System.out.println("      | Before   > " + rules.get(j - 1));
//						System.out.println("      | ItemRule > " + rule);
//					}
//				}
//				
//				System.out.println();
//			}
			
			// Rules...
//			for(RuleList set : i.getRules()) {
//				System.out.println("    | " + set.getRuleString() + " > " + set);
//			}
			System.out.println();
			
			// Find all nonterminals a where "a i" is a valid syntax
			
			*/
		});
		/*
		types.forEach((t) -> {
			System.out.println("Item: t:" + t);
			for(RuleList set : t.getRules()) {
				System.out.println("    | " + set.getRuleString() + " > " + set);
				
			}
			System.out.println();
		});
		*/
		
		Set<ItemToken> tokens = grammar.getTokens();
		
		System.out.println(tokens);
		tokens.forEach((t) -> {
			System.out.println("Token: t:" + t);
			for(Rule match : t.getRules()) {
				System.out.println("     | " + match);
			}
			System.out.println();
		});
		
		return null;
	}
	
	// TODO: Get all trivial cases for each new found case that matches....
	protected List<Rule> getBeforeItem(Item a, Grammar grammar) {
		List<Rule> trivial = new ArrayList<>();
		
		// TODO: Fix bracket cases
		// TODO: First find trivial cases
		for(Item b : grammar.getItems()) {
			System.out.println("    | " + b);
			
			for(RuleList set : b.getRules()) {
				boolean once = false;
				// System.out.println("      | " + set.getRuleString() + " > " + set);
				
				List<Rule> rules = set.getRules();
				for(int j = 1; j < set.size(); j++) {
					Rule rule = rules.get(j);
					
					if(rule instanceof ItemRule) {
						ItemRule ir = (ItemRule)rule;
						
						if(ir.getName().equals(a.getName())) {
							if(!once) {
								System.out.println("      | " + set.getRuleString() + " > " + set);
								once = true;
							}
							
							Rule before = rules.get(j - 1);
							
							System.out.println("        | Before > " + before);
							System.out.println("        | Rule   > " + ir);
							
							if(trivial.contains(before)) {
							
							} else {
								trivial.add(before);
							}
						}
					}
				}
			}
		}
		
		System.out.println("Trivial: " + trivial);
		System.out.println();
		System.out.println();
		
		// NOTE: Special cases
		//   If a item "A" has a rule that starts with item "B"
		//   then treat "A" as if it was also the item "B".
		List<Item> special = new ArrayList<>();
		List<Item> search = new ArrayList<>();
		search.add(a);
		
		
		for(int j = 0; j < search.size(); j++) {
			Item b = search.get(j);
			System.out.println("    | " + b);
			
			for(RuleList set : b.getRules()) {
				System.out.println("      | " + set.getRuleString() + " > " + set);
				
				List<Rule> rules = set.getRules();
				if(!rules.isEmpty()) {
					Rule rule = rules.get(0);
					
					if(rule instanceof ItemRule) {
						ItemRule ir = (ItemRule)rule;
						
						Item item = grammar.getItem(ir.getName());
						if(special.contains(item)) {
							// Do not add it again
						} else {
							special.add(item);
							search.add(item);
						}
					}
				}
			}
		}
		
		System.out.println("Special: " + special);
		System.out.println();
		System.out.println();
		
		
		
		return null;
	}
	
	/**
	 * We need to calculate the FOLLOW(b) and FIRST(a, b) for
	 * all sets 'a' and production rule 'b'<br><br>
	 * 
	 * For this grammar:<br>
	 * 
	 *<pre># $ Is the terminal of the file EOF
	 *# n is a token
	 *
	 *S -> E $
	 *
	 *E -> T
	 *  -> ( E )
	 *
	 *T -> n
	 *  -> + T
	 *  -> T + n</pre>
	 *
	 * The result of calling FIRST on a production rule should give all tokens
	 * that could be infront of that production rule legaly in this grammar.<br>
	 * 
	 * The result of calling FOLLOW on a production rule should give all tokens
	 * where this production rule could legaly be proceded by that token.<br>
	 *
	 *<pre>FIRST(S) = { E>T>n, E>T>+, E>( }
	 *FIRST(E) = {   T>n,   T>+,   ( }
	 *FIRST(T) = {     n,     + }
	 *
	 *FOLLOW(S) = { $, }
	 *FOLLOW(E) = { $, +, ), n }
	 *FOLLOW(T) = { $, +, ), }</pre>
	 *
	 *Sources: <a href="https://en.wikipedia.org/wiki/Canonical_LR_parser#FIRST_and_FOLLOW_sets">Wikipedia Canonical LR parser (FIRST and FOLLOW sets)</a>
	 */
	public void generateTable() {
		
	}
}
