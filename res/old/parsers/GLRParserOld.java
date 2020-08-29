package hardcoded.parser;

import static hardcoded.utils.StringUtils.*;

import java.util.LinkedList;
import java.util.List;

import hardcoded.grammar.Grammar.*;
import hardcoded.lexer.Token;
import hardcoded.parser.GLRParserGenerator.*;
import hardcoded.tree.ParseTree;
import hardcoded.tree.ParseTree.PNode;
import hc.errors.grammar.ParserException;

/**
 * https://en.wikipedia.org/wiki/GLR_parser
 * 
 * @author HardCoded
 */
// This is more of a backup file
public class GLRParserOld {
	private final ITable table;
	
	protected GLRParserOld(ITable table) {
		this.table = table;
		
		// System.out.println(table);
		// System.out.println("");
	}
	
	private class LastState {
		private LinkedList<StateToken> reductionStack;
		private ParseTree tree;
		
		private LastState() {
			this.reductionStack = new LinkedList<>();
			this.tree = new ParseTree();
		}
		
		private LastState(LastState ls) {
			this.reductionStack = new LinkedList<>(ls.reductionStack);
			this.tree = new ParseTree(ls.tree);
		}
		
		@Override
		public String toString() {
			return '(' + join(", ", reductionStack) + ')';
		}
	}
	
	private class StateToken {
		private IAction[] actions;
		private int index;
		private String item;
		private Token input;
		
		private StateToken(IAction... actions) {
			this.actions = actions;
		}
		
		public String value() {
			if(item != null) return item;
			return input == null ? null:input.toString();
		}
		
		public IAction getAction() {
			if(actions == null || index >= actions.length) return null;
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
	
	private boolean checkMatch(IRule rule, StateToken state) {
		if(rule == null) return false;
		
		if(rule.isItemToken()) {
			if(state.item != null) return rule.value().equals(state.value());
			return match(rule, state.input);
		}
		
		// This checks if the value are equal nad that the state is the same type as the rule.
		return rule.value().equals(state.value()) && rule.isItemType() != (state.item == null);
	}
	
	private IAction[] getState(IRow row, StateToken state) {
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
	
	// TODO: Check if this is correct
	private boolean canDoReduction(IRuleList set, List<StateToken> reduction) {
		if(reduction.size() < set.size()) return false; // TODO: Check..
		
		int start = reduction.size() - set.size();
		for(int i = 0; i < set.size(); i++) {
			StateToken state = reduction.get(i + start);
			
			IRule rule = set.get(i);
			if(!checkMatch(rule, state)) {
				return false;
			}
			
			// System.out.println("--(" + i + "): " + state + " --- (" + rule + ")");
		}
		
		return true;
	}
	
	// TODO: Fix operator precedence
	public ParseTree parse(Token token) {
		System.out.println("Tokens: '" + token.toString(" ", Integer.MAX_VALUE) + "'");
		
		LinkedList<LastState> stateStack = new LinkedList<>();
		
		LastState ls = new LastState();
		{
			StateToken state = new StateToken(table.start());
			state.input = token.prev();
			
			ls.reductionStack.push(state);
		}
		stateStack.add(new LastState(ls));
		
		int max = 1000000;
		while(max-- > 0) {
			System.out.println();
			System.out.println("Stack: " + ls.reductionStack + ", size=" + stateStack.size());
			// for(int i = Math.max(0, stateStack.size() - 10); i < stateStack.size(); i++) System.out.println("--" + stateStack.get(i));
			
			
			// The first action is always a shift action...
			StateToken state = ls.reductionStack.getLast();
			System.out.println("  State: " + state);
			
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
				StateToken nextState = new StateToken();
				nextState.input = state.input.next();
				
				System.out.println("  ShiftState : state='" + current + "', input='" + nextState.input + "', i=" + state.index);
				
				IRow row = table.getRow(current.index);
				
				// TODO: Sometimes there are more ways to understand a token..
				IAction[] actions = getState(row, nextState);
				if(actions == null || actions.length == 0 || state.index >= actions.length) {
					System.out.println("    \"A shift is not valid for the input '" + nextState.input + "'\"");
					System.out.println("    \"Going back into search tree\"");
					
					// TODO: If the state at the last position in the stateStack has its index
					//       greater than the amount of actions it has. It should be removed from
					//       the list.
					LastState last = stateStack.getLast();
					StateToken st = last.reductionStack.getLast();
					st.index++;
					
					// TODO: Check if this works as expected.
					if(st.index == st.actions.length - 1) {
						// If we have reached the last index of a state we remove that state because if it's wrong we will never enter it again..
						
						// System.out.println("    \"The index is at the end of the actions (" + st.index + ") --- (" + st.actions.length + ")\"");
						ls = new LastState(stateStack.pollLast());
					} else if(st.index >= st.actions.length) {
						// This removes the problem of tying to printing an action outside the actions array.
						stateStack.removeLast();
						
						if(stateStack.isEmpty()) {
							// We have failed to parse this input
							System.out.println("-- FAILED TO PARSE THE INPUT --");
							break;
						}
						
						ls = new LastState(stateStack.getLast());
					} else {
						ls = new LastState(last);
					}
					
					
					continue;
				}
				
				System.out.println("    Actions: " + join(", ", actions));
				System.out.println("    Index  : " + state.index);
				
				if(state.index >= actions.length) {
					System.out.println("    \"State index was outside bounds of actions array\"");
				}
				
				IAction action = actions[state.index];
				System.out.println("    Action : " + action);
				
				nextState.actions = actions;
				nextState.index = state.index;
				
				if(action.isShift()) {
					// TODO: Check for null values!!!
					nextState.input = state.input.next();
				}
				
				ls.reductionStack.add(nextState);
				
				ls.tree.add(new PNode(nextState.value()));
				
				if(actions == null || actions.length > 1) {
					stateStack.add(new LastState(ls));
				}
				
				continue;
			}
			
			if(current.isReduce()) {
				System.out.println("  ReduceState : state='" + current + "', i=" + state.index + ", stack=" + ls.reductionStack);
				System.out.println("    IRuleList: [" + current.rl.itemName + " -> " + current.rl + "]");
				
				// TODO: Check if the rule matches the end of the stream and then compute the next state
				// FIXME: For now lets assume that the rule does match
				IRuleList rule = current.rl;
				
				if(!canDoReduction(rule, ls.reductionStack)) {
					// TODO: What do we do here?
					System.out.println("    \"The reduction rule did not match the current reductionStack\"");
					System.out.println("    \"Going back in the search tree\"");
					
					LastState last = stateStack.getLast();
					StateToken st = last.reductionStack.getLast();
					st.index++;
					
					// TODO: Check if this works as expected.
					if(st.index == st.actions.length - 1) {
						// If we have reached the last index of a state we remove that state because if it's wrong we will never enter it again..
						
						// System.out.println("    \"The index is at the end of the actions (" + st.index + ") --- (" + st.actions.length + ")\"");
						ls = new LastState(stateStack.pollLast());
					} else if(st.index >= st.actions.length) {
						// This removes the problem of tying to printing an action outside the actions array.
						stateStack.removeLast();
						
						if(stateStack.isEmpty()) {
							// We have failed to parse this input
							System.out.println("-- FAILED TO PARSE THE INPUT --");
							break;
						}
						
						ls = new LastState(stateStack.getLast());
					} else {
						ls = new LastState(last);
					}
					
					continue;
				}
				
				for(int i = 0; i < rule.size(); i++) ls.reductionStack.pollLast();
				StateToken nextState = new StateToken();
				nextState.item = current.rl.itemName;
				nextState.input = state.input;
				
				state = ls.reductionStack.getLast();
				
				System.out.println("    State    : " + state);
				
				IRow row = table.getRow(state.rowIndex());
				IAction[] actions = getState(row, nextState);
				
				if(actions != null) {
					System.out.println("    Actions: " + join(", ", actions));
					System.out.println("    Index  : " + state.index);
					nextState.actions = actions;
				}
				
				System.out.println("    Next     : " + nextState);
				
				ls.reductionStack.add(nextState);
				ls.tree.reduce(new PNode(nextState.value()), rule.size());
				
				
				if(actions == null || actions.length > 1) {
					stateStack.add(new LastState(ls));
				}
				
				continue;
			}
			
			// This was unexpected
			throw new ParserException("Error at token: '" + token + "' (line=" + token.line() + ", column=" + token.column() + ")");
		}
		
		System.out.println();
		System.out.println();
		System.out.println("END: " + ls.reductionStack);
		System.out.println("   : " + stateStack);
		System.out.println();
		System.out.println();
		
		return ls.tree;
	}
	
	private boolean match(IRule rule, Token token) {
		if(token == null) return false; // TODO: This should only return true if we are expecting the {EOF} rule
		
		switch(rule.type()) {
			case INVALID: throw new ParserException("Found a invalid matching rule...");
			case STRING: return token.toString().equals(rule.value());
			case REGEX: return token.toString().equals(rule.value());
			case ITEM: {
				if(rule.isItemToken()) {
					// This could match... This
					ItemToken item = (ItemToken)rule.asItem();
					
					for(RuleList set : item.getRules()) {
						if(_match(set, token)) return true;
					}
				}
				
				return false; // Cannot match a token and an item
			}
			case SPECIAL: {
				if(rule.value().equals("{EOF}")) return token == null;
				return false;
			}
		}
		
		return false;
	}
	
	// TODO: This is a cheaty method.. This should be replaced...
	private boolean _match(RuleList set, Token token) {
		if(token == null) return false; // TODO: This should only return true if we are expecting the {EOF} rule
		
		for(Rule rule : set.getRules()) {
			if(rule instanceof ItemRule) {
				throw new ParserException("Cannot match item rules");
			} else if(rule instanceof StringRule) {
				return token.toString().equals(rule.value());
			} else if(rule instanceof RegexRule) {
				return token.toString().matches(rule.value());
			} else if(rule instanceof SpecialRule) {
				// SpecialRule sr = (SpecialRule)rule;
				
				// TODO: Check for {EOF} and other special tokens
				return false;
			}
		}
		
		return false;
	}
}
