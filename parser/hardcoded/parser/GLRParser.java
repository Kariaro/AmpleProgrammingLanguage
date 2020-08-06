package hardcoded.parser;

import java.util.*;

import hardcoded.grammar.Grammar.*;
import hardcoded.parser.GLRParserGenerator.*;
import hardcoded.tree.AbstractSyntaxTree;
import hc.errors.grammar.ParserException;
import hc.token.Symbol;
import static hardcoded.utils.StringUtils.*;

/**
 * https://en.wikipedia.org/wiki/GLR_parser
 * 
 * @author HardCoded
 */
public class GLRParser {
	private final ITable table;
	
	protected GLRParser(ITable table) {
		this.table = table;
		
		System.out.println(table);
		System.out.println("");
	}
	
	private class LastState {
		private LinkedList<StateSymbol> reductionStack;
		
		public LastState() {
			this.reductionStack = new LinkedList<>();
		}
		
		public LastState(LastState ls) {
			this.reductionStack = new LinkedList<>(ls.reductionStack);
		}
		
		@Override
		public String toString() {
			return '(' + listToString(", ", reductionStack) + ')';
		}
	}
	
	private class StateSymbol {
		private IAction[] actions;
		private int index;
		private String item;
		private Symbol input;
		
		public StateSymbol(IAction... actions) {
			this.actions = actions;
		}
		
		public StateSymbol() {
			
		}
		
		
		public String value() {
			if(item != null) return item;
			return input == null ? null:input.toString();
		}
		
		public IAction getAction() {
			if(actions == null) return null;
			return actions[index];
		}
		
		public int rowIndex() {
			if(actions == null) return -1;
			return getAction().index;
		}
		
		@Override
		public String toString() {
			if(item == null) {
				if(input == null || input.toString() == null) return getAction() + "";
				return getAction() + "/" + input;
			}
			
			return getAction() + "/" + item;
		}
	}
	
	private boolean checkMatch(IRule rule, StateSymbol state) {
		if(rule == null) return false;
		
		if(rule.isItemToken()) return match(rule, state.input);
		
		// This checks if the value are equal nad that the state is the same type as the rule.
		return rule.value().equals(state.value()) && rule.isItemType() != (state.item == null);
	}
	
	private IAction[] getState(IRow row, StateSymbol state) {
		for(int i = 0; i < row.size(); i++) {
			IAction[] actions = row.get(i);
			if(actions == null) continue;
			
			IRule rule = table.set.get(i);
			// System.out.println("(" + rule + ") --- (" + state.value() + "), " + state.toString());
			
			if(checkMatch(rule, state)) {
				return actions;
			}
		}
		
		return null;
	}
	
	private boolean canDoReduction(IRuleList rule, List<StateSymbol> reduction) {
		// TODO: Implement this method...
		return true;
	}
	
	// TODO: Fix operator precedence
	public AbstractSyntaxTree parse(Symbol symbol) {
		System.out.println("Tokens: '" + symbol.toString(" ", Integer.MAX_VALUE) + "'");
		
		LinkedList<LastState> stateStack = new LinkedList<>();
		
		LastState ls = new LastState();
		{
			StateSymbol state = new StateSymbol(table.start());
			state.input = symbol.prev();
			
			ls.reductionStack.push(state);
		}
		stateStack.add(new LastState(ls));
		
		int max = 500;
		while(max-- > 0) {
			// TODO: Fix crashes from printing the action with invalid index...
			
			System.out.println();
			System.out.println("Stack: " + ls.reductionStack);
			for(int i = Math.max(0, stateStack.size() - 10); i < stateStack.size(); i++) System.out.println("--" + stateStack.get(i));
			
			// The first action is always a shift action...
			StateSymbol state = ls.reductionStack.getLast();
			System.out.println("  State: " + state);
			
			// NOTE: Reductions grabs from the reductionStack
			// NOTE: Shifts grabs from the inputStack
			{
				IAction current = state.getAction();
				
				// Check if we have reached the end of the stream
				if(current == null) {
					// To know that we have finished the stream we need to have two items in the reductionStack
					// First we need the default state S0 and the START item that we specified in the grammar.
					if(ls.reductionStack.size() == 2) {
						String value = state.value();
						
						if(table.acceptItem().equals(value)) {
							System.out.println("-- PARSED THE INPUT SUCCESSFULLY --");
							break;
						}
					}
				}
				
				if(current.isShift()) {
					StateSymbol nextState = new StateSymbol();
					nextState.input = state.input.next();
					
					System.out.println("  ShiftState : state='" + current + "', input='" + nextState.input + "', i=" + state.index);
					
					IRow row = table.getRow(current.index);
					IAction[] actions = getState(row, nextState);
					if(actions == null || actions.length == 0) {
						System.out.println("    \"This shift is not valid for the input '" + nextState.input + "'\"");
						System.out.println("    \"Going back into search tree\"");
						
						LastState last = stateStack.getLast();
						last.reductionStack.getLast().index++;
						ls = new LastState(last);
						
						continue;
					}
					
					System.out.println("    Actions: " + arrayToString(", ", actions));
					System.out.println("    Index  : " + state.index);
					
					if(state.index >= actions.length) {
						System.out.println("    \"State index was outside bounds of actions array\"");
					}

					IAction action = actions[state.index];
					System.out.println("    Action : " + action);
					
					// TODO: If this action is a shift we should give the next state the next input value...
					
					
					nextState.actions = actions;
					nextState.index = state.index;
					
					if(action.isShift()) {
						// TODO: Check for null values!!!
						nextState.input = state.input.next();
					} else {
						
					}
					
					ls.reductionStack.add(nextState);
					stateStack.add(new LastState(ls));
					continue;
				}
				
				if(current.isReduce()) {
					System.out.println("  ReduceState : state='" + current + "', i=" + state.index + ", stack=" + ls.reductionStack);
					System.out.println("    IRuleList: [" + current.rl.itemName + " -> " + current.rl + "]");
					
					// TODO: Check if the rule matches the end of the stream and then compute the next state
					// FIXME: Lets assume that the rule does match
					
					IRuleList rule = current.rl;
					
					if(!canDoReduction(rule, ls.reductionStack)) {
						// TODO: What do we do here?
						System.out.println("    \"The reduction rule did not match the current reductionStack\"");
						System.out.println("    \"Going back in the search tree\"");
						break;
					}
					
					for(int i = 0; i < rule.size(); i++) ls.reductionStack.pollLast();
					StateSymbol nextState = new StateSymbol();
					nextState.item = current.rl.itemName;
					nextState.input = state.input;
					
					state = ls.reductionStack.getLast();
					
					System.out.println("    State    : " + state);
					
					IRow row = table.getRow(state.rowIndex());
					IAction[] actions = getState(row, nextState);
					
					if(actions != null) {
						System.out.println("    Actions: " + arrayToString(", ", actions));
						System.out.println("    Index  : " + state.index);
						
						nextState.actions = actions;
					}
					
					System.out.println("    Next     : " + nextState);
					
					ls.reductionStack.add(nextState);
					stateStack.add(new LastState(ls));
					continue;
				}
			}
			
			
			
			System.out.println();
			System.out.println("Stack: " + ls.reductionStack);
			
			
			if(true) break;
			// throw new ParserException("Error at token: '" + symbol + "' (line=" + symbol.getLineIndex() + ", column=" + symbol.getColumnIndex() + ")");
		}
		
		// TODO: Check if the end of the stack is the correct type...
		System.out.println();
		System.out.println();
		System.out.println("ENDST: " + ls.reductionStack + ", " + stateStack);
		
		System.out.println();
		System.out.println("Break");
		return null;
	}
	
	private boolean match(IRule rule, Symbol symbol) {
		if(symbol == null) return false; // TODO: This should only return true if we are expecting the {EOF} rule
		
		switch(rule.type()) {
			case INVALID: throw new ParserException("Found a invalid matching rule...");
			case STRING: return symbol.toString().equals(rule.value());
			case REGEX: return symbol.toString().equals(rule.value());
			case ITEM: {
				if(rule.isItemToken()) {
					// This could match... This
					ItemToken item = (ItemToken)rule.asItem();
					
					for(RuleList set : item.getRules()) {
						if(_match(set, symbol)) return true;
					}
				}
				
				return false; // Cannot match a symbol and an item
			}
			case SPECIAL: {
				if(rule.value().equals("{EOF}")) return symbol == null;
				return false;
			}
		}
		
		return false;
	}
	
	// TODO: This is a cheaty method.. This should be replaced...
	private boolean _match(RuleList set, Symbol symbol) {
		if(symbol == null) return false; // TODO: This should only return true if we are expecting the {EOF} rule
		
		for(Rule rule : set.getRules()) {
			if(rule instanceof ItemRule) {
				throw new ParserException("Cannot match item rules");
			} else if(rule instanceof StringRule) {
				return symbol.toString().equals(rule.value());
			} else if(rule instanceof RegexRule) {
				return symbol.toString().matches(rule.value());
			} else if(rule instanceof SpecialRule) {
				// SpecialRule sr = (SpecialRule)rule;
				
				// TODO: Check for {EOF} and other special tokens
				return false;
			}
		}
		
		return false;
	}
}
