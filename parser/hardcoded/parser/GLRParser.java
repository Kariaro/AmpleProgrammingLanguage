package hardcoded.parser;

import java.util.*;

import hardcoded.grammar.Grammar.*;
import hardcoded.parser.GLRParserGenerator.*;
import hardcoded.tree.AbstractSyntaxTree;
import hc.errors.grammar.ParserException;
import hc.token.Symbol;

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
		private int stateIndex;
		private Symbol symbol;
		private LinkedList<StateSymbol> reductionStack;
		private int index = 0;
		private IAction[] actions;
		
		public LastState(int stateIndex, Symbol symbol, LinkedList<StateSymbol> reductionStack, int index) {
			this.stateIndex = stateIndex;
			this.symbol = symbol;
			this.reductionStack = new LinkedList<>(reductionStack);
		}
		
		public LastState(LastState ls) {
			this(ls.stateIndex, ls.symbol, ls.reductionStack, ls.index);
		}

		@Override
		public String toString() {
			StringBuilder sb = new StringBuilder();
			sb.append("(").append(stateIndex).append(" ").append(symbol);
			if(!reductionStack.isEmpty()) sb.append(" ").append(reductionStack);
			if(index > 0) sb.append(" ").append(index);
			return sb.append(")").toString();
		}
	}
	
	// TODO: Fix operator precedence
	public AbstractSyntaxTree parse(Symbol symbol) {
		System.out.println("Tokens: '" + symbol.toString(" ", Integer.MAX_VALUE) + "'");
		
		LinkedList<LastState> stateStack = new LinkedList<>();
		
		LastState ls = new LastState(0, symbol, new LinkedList<>(), 0);
		ls.reductionStack.push(new StateSymbol(table.start()));
		stateStack.add(new LastState(ls));
		
		int max = 10;
		while(max-- > 0) {
			System.out.println();
			System.out.println("Stack: " + ls.reductionStack + ", " + ls.stateIndex + ", " + stateStack);
			
			IRow row = table.getRow(ls.stateIndex);
			StateSymbol state = ls.reductionStack.getLast();
			// ls.actions = getIAction(row, ls.reductionStack.getLast());
			
			/*
			if(ls.actions == null) {
				// TODO: Here we have three different cases
				//   Case 1: The action is at the end of the stream and is fully done.
				//   Case 2: We should have reduced the last group but we shifted it instead..
				//   Case 3: Some other error.
				
				System.out.println("Actions error grrr.... symbol=" + ls.symbol);
				System.out.println("  Serrr: " + ls.symbol + ", We need to go backwards...");
				System.out.println();
				
				// The symbol was not a valid token.
				// This means that the last pushed state was wrong..
				// We fix this by removing the last state and entering the one before the invalid push.
				// If there is no state before our push we know that the input text has invalid syntax.
				
				// LastState info = stateStack.removeLast();
				// stateStack.removeLast();
				
				// TODO: Sometimes this gets stuck in a loop because it cannot recover from a state...
				LastState last = stateStack.getLast();
				last.index++;
				
				// TODO: Go back one more level if index is greater than the size of the actions array.
				ls = new LastState(last);
				ls.index = last.index;
				
				// Everything that appears inside the visited set should be skiped when looking for the
				// next action.
				
				
				// TODO: What do we do here?
				continue;
			}
			
			{
				StringBuilder sb = new StringBuilder();
				for(IAction a : ls.actions) sb.append(a).append("|");
				if(ls.actions.length > 0) sb.deleteCharAt(sb.length() - 1);
				
				System.out.println("  String: " + sb.toString());
			}
			
			if(ls.index >= ls.actions.length) {
				// TODO: We need to remove this state and go back one more....
				stateStack.removeLast();
				
				break;
			}
			
			IAction action = ls.actions[ls.index];
			if(action == null) {
				// TODO: What do we do here?
				System.out.println("WEIRD: Action was null?!?");
				break;
			}
			*/
			
			IAction action = state.action;
			
			if(action.isShift()) {
				System.out.println("  Shift: " + ls.symbol + ", " + action);
				ls.actions = getIAction(row, ls.symbol);
				
				if(ls.actions == null) {
					System.out.println("WEIRD: ls.actions  was null?!?");
					
					LastState last = stateStack.getLast();
					last.index++;
					ls = new LastState(last);
					ls.index = last.index;
					
					state = ls.reductionStack.getLast();
					
					continue;
				}
				
				// TODO: We could reuse the variable action.
				IAction act = ls.actions[ls.index];
				if(act == null) {
					// TODO: What do we do here?
					System.out.println("WEIRD: Action was null?!?");
					break;
				}
				
				ls.reductionStack.add(new StateSymbol(act, ls.symbol));
				ls.stateIndex = act.index;
				stateStack.add(new LastState(ls));
				ls.symbol = ls.symbol.next();
			} else if(action.isReduce()) {
				// ls.reductionStack.add(new StateSymbol(action, ls.symbol));
				ls.symbol = ls.symbol.next();
				
				System.out.println("  Reduce: " + ls.reductionStack + ", " + action);
				
				// TODO: There should still only be one rule in this list
				// TODO: More error checks for lengths and sizes.....
				
				System.out.println("    IRuleList: " + action.rl);
				System.out.println("    SSymbol  : " + state);
				
				// TODO: Check for this rule to be defined and to be non zero sized.
				
				IRuleList rule = action.rl;
				int length = rule.size();
				
				for(int i = 0; i < length; i++) ls.reductionStack.pollLast();
				state = ls.reductionStack.getLast();
				
				StateSymbol next = new StateSymbol(action, rule.itemName);
				ls.reductionStack.add(next);
				ls.stateIndex = state.action.index;
				stateStack.add(new LastState(ls));
				ls.stateIndex = action.index;
			}
			
			// throw new ParserException("Error at token: '" + symbol + "' (line=" + symbol.getLineIndex() + ", column=" + symbol.getColumnIndex() + ")");
		}
		
		// TODO: Check if the end of the stack is the correct type...

		System.out.println("ENDST: " + ls.reductionStack + ", " + ls.stateIndex + ", " + stateStack);
		
		System.out.println();
		System.out.println("Break");
		return null;
	}
	
	private int getMatchColumn(StateSymbol state) {
		int index = 0;
		// TODO: If it's a token type then we need to check for equality....... (maybe)
		
		for(IRule rule : table.set) {
			//System.out.println("(" + rule + ")/(" + state.value() + ") == " + index);
			
			if(rule.value().equals(state.value())
			&& state.isSymbol() != rule.isItemType()) return index;
			
			index++;
		}
		
		return -1;
	}
	
	private IAction[] getIAction(IRow row, StateSymbol state) {
		int col = getMatchColumn(state);
		
		if(col < 0) return null;
		return row.actions()[col];
	}
	
	private int getMatchColumn(Symbol symbol) {
		if(symbol == null) return -1;
		
		int index = 0;
		for(IRule rule : table.set) {
			if(!rule.isItemType() && rule.value().equals(symbol.toString())) return index;
			index++;
		}
		
		return -1;
	}
	
	private IAction[] getIAction(IRow row, Symbol symbol) {
		if(symbol == null) return null; // TODO: What do we do here?
		int col = getMatchColumn(symbol);
		
		if(col < 0) return null;
		return row.actions()[col];
	}
	
	private int indexOf(IRow row, IAction action) {
		IAction[] actions = row.actions()[0];
		for(int i = 0; i < actions.length; i++) {
			if(actions[i] == action) return i;
		}
		
		return -1;
	}
	
	private int rowSize(IRow row) {
		int size = 0;
		for(IAction action : row.actions()[0]) {
			if(action != null) size++;
		}
		
		return size;
	}
	
	private IAction next(IRow row, StateSymbol symbol) {
		if(symbol.symbol == null) return null; // grrr
		
		if(!symbol.isSymbol()) {
			for(IAction[] action : row.actions()) {
				if(action == null) continue;
				
				IRule rule = action[0].rule;
				if(rule == null) continue; // ???
				
				if(rule.isItemType()) {
					if(rule.value().equals(symbol.symbol.itemName)) {
						return action[0];
					}
				}
			}
		} else {
			return next(row, symbol.symbol.symbol);
		}
		
		return null;
	}
	
	private IAction next(IRow row, Symbol symbol) {
		for(IAction[] action : row.actions()) {
			if(action == null) continue;
			
			//IRule rule = action.state.action();
			
			//if(match(rule, symbol)) return action;
		}
		
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
	
	private class StateSymbol {
		// TODO: Use a IAction[] and index to store potential next actions.....
		private IAction action;
		private SymType symbol;
		
		
		public StateSymbol(IAction action, Symbol symbol) {
			this.symbol = new SymType(symbol);
			this.action = action;
		}
		
		public StateSymbol(IAction action, String string) {
			this.symbol = new SymType(string);
			this.action = action;
		}
		
		public StateSymbol(IAction action) {
			this.action = action;
		}
		
		public boolean isSymbol() {
			if(symbol != null) return symbol.isSymbol;
			return false;
		}
		
		public String value() {
			if(symbol == null) return null;
			if(symbol.isSymbol) return symbol.symbol == null ? null : symbol.symbol.toString();
			return symbol.itemName;
		}
		
		@Override
		public String toString() {
			if(symbol == null) return action.toString();
			return symbol.toString() + "/" + action;
		}
	}
	
	private class SymType {
		private boolean isSymbol;
		private Symbol symbol;
		private String itemName;
		
		public SymType(Symbol symbol) {
			this.symbol = symbol;
			isSymbol = true;
		}
		
		public SymType(String itemName) {
			this.itemName = itemName;
		}
		
		public String toString() {
			if(isSymbol) {
				return symbol == null ? null:symbol.toString();
			} else {
				return itemName;
			}
		}
	}
}
