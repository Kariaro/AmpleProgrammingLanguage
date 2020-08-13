package hardcoded.parser;

import java.util.LinkedList;

import hardcoded.grammar.Grammar.*;
import hardcoded.lexer.Token;
import hardcoded.parser.LR0_ParserGenerator.*;
import hardcoded.tree.AbstractSyntaxTree;
import hc.errors.grammar.ParserException;

/**
 * A simple test to check if LR 0 was possible
 * This is not the implementation that will be used..
 * 
 * @author HardCoded
 *
 */
@Deprecated
public class LR0_Parser {
	private final ITable table;
	protected LR0_Parser(ITable table) {
		this.table = table;
		
		System.out.println(table);
		System.out.println("");
	}
	
	@Deprecated
	public GLRParser testingHACK() {
		// return new GLRParser(table);
		throw new UnsupportedOperationException("Imlementation fault");
	}
	
	public AbstractSyntaxTree parse(Token symbol) {
		System.out.println("Text: '" + symbol.toString(" ", Integer.MAX_VALUE) + "'");
		
		LinkedList<StateSymbol> reductionStack = new LinkedList<>();
		reductionStack.push(new StateSymbol(table.start()));
		
		int stateIndex = 0;
		while(true) {
			//System.out.println();
			System.out.println("Stack: " + reductionStack + ", " + stateIndex);
			
			// SET State To Top of reduction Stack
			StateSymbol state = reductionStack.getLast();
			// System.out.println("State -> " + state);
			IRow row = table.getRow(stateIndex);
			int type = isReduceRow(row) ? 1:0;
			
			if(type == 0) { // shift
				// System.out.println("  Shift Action");
				
				IAction act = next(row, symbol);
				// System.out.println("  Act -> " + act);
				
				reductionStack.add(new StateSymbol(act, symbol));
				
				if(symbol == null) {
					// TODO: Check for EOL????
					break;
				}
				
				symbol = symbol.next();
				if(act != null) stateIndex = act.index;
			} else if(type == 1) { // reduce
				// System.out.println("  Reduce Action");
				
				// TODO: Error checking... Only one rule allowed.
				//       Or create a back tracking algorithm...
				
				// System.out.println("  St0 -> " + state.action.state);
				
				IRuleList rule = state.action.state.getRules().get(0);
				int length = rule.size();
				// System.out.println("  Len -> " + length);
				
				// System.out.println("  Sta -> " + state);
				for(int i = 0; i < length; i++) reductionStack.pollLast();
				state = reductionStack.getLast();
				
				StateSymbol next = new StateSymbol(state.action, rule.itemName);
				reductionStack.add(next);
				stateIndex = state.action.index;
				
				next.action = next(table.getRow(next.action.index), next);
				if(next.action != null) {
					stateIndex = next.action.index;
				}
				
				// System.out.println("  Nxt -> " + action);
			}
			
			
			// throw new ParserException("Error at token: '" + symbol + "' (line=" + symbol.getLineIndex() + ", column=" + symbol.getColumnIndex() + ")");
		}
		
		// TODO: Check if the end of the stack is the correct type...
		
		System.out.println();
		System.out.println("Break");
		return null;
	}
	
	private boolean isReduceRow(IRow row) {
		for(IAction action : row.actions()) {
			// FIXME: Hacky will not work if there is a reduce inside a row with shifts and gotos
			if(action != null && action.type == 1) return true;
		}
		return false;
	}
	
	private IAction next(IRow row, StateSymbol symbol) {
		if(symbol.symbol == null) return null; // grrr
		
		if(!symbol.isSymbol()) {
			for(IAction action : row.actions()) {
				if(action == null) continue;
				
				IRule rule = action.rule;
				if(rule == null) continue; // ???
				
				if(rule.isItemType()) {
					if(rule.value().equals(symbol.symbol.itemName)) {
						return action;
					}
				}
			}
		} else {
			return next(row, symbol.symbol.symbol);
		}
		
		return null;
	}
	
	private IAction next(IRow row, Token symbol) {
		for(IAction action : row.actions()) {
			if(action == null) continue;
			
			IRule rule = action.state.action();
			
			if(match(rule, symbol)) return action;
			// System.out.println(action + ", " + rule);
		}
		
		return null;
	}
	
	private boolean match(IRule rule, Token symbol) {
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
	private boolean _match(RuleList set, Token symbol) {
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
		private IAction action;
		private SymType symbol;
		
		public StateSymbol(IAction action, Token symbol) {
			this.symbol = new SymType(symbol);
			this.action = action;
		}
		
		public boolean isSymbol() {
			if(symbol != null) return symbol.isSymbol;
			return false;
		}

		public StateSymbol(IAction action, String string) {
			this.symbol = new SymType(string);
			this.action = action;
		}
		
		public StateSymbol(IAction action) {
			this.action = action;
		}
		
		@Override
		public String toString() {
			if(symbol == null) return action.toString();
			return symbol.toString() + "/" + action;
		}
	}
	
	private class SymType {
		private boolean isSymbol;
		private Token symbol;
		private String itemName;
		
		public SymType(Token symbol) {
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
