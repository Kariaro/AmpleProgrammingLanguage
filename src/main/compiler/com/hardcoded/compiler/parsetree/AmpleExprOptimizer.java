package com.hardcoded.compiler.parsetree;

import java.util.ArrayList;
import java.util.List;
import java.util.ListIterator;

import com.hardcoded.compiler.api.Expression;
import com.hardcoded.compiler.impl.expression.*;
import com.hardcoded.compiler.lexer.Token;
import com.hardcoded.logger.Log;

/**
 * A parse tree expression optimizer
 * 
 * @author HardCoded
 * @since 0.2.0
 */
class AmpleExprOptimizer {
	private static final Log LOGGER = Log.getLogger();
	
	public AmpleExprOptimizer() {
		
	}
	
	private AtomExpr getAtom(Expression expr) {
		if(expr instanceof AtomExpr) return (AtomExpr)expr;
		return null;
	}
	
	private AtomExpr getAtom(double value, int start, int end) {
		return (AtomExpr)AtomExpr.get(Token.EMPTY, value).setLocation(start, end);
	}
	
	public void process(Expression expr, ListIterator<Expression> iter) {
		if(EmptyExpr.isEmpty(expr)) {
			// We can't do anything here
			return;
		}
		
		if(expr instanceof UnaryExpr) {
			UnaryExpr e = (UnaryExpr)expr;
			AtomExpr atom = getAtom(e.get(0));
			if(atom == null || !atom.isNumber()) return;
			
			double a = atom.getNumber();
			switch(e.getType()) {
				case NEG: {
					iter.set(getAtom(-a, atom.getStartOffset(), atom.getEndOffset()));
					break;
				}
				case NOR: {
					iter.set(getAtom(~((long)a), atom.getStartOffset(), atom.getEndOffset()));
					break;
				}
				case NOT: {
					iter.set(getAtom((a == 0) ? 1:0, atom.getStartOffset(), atom.getEndOffset()));
					break;
				}
				default:
			}
			return;
		}
		
		if(expr instanceof BinaryExpr) {
			BinaryExpr e = (BinaryExpr)expr;
			AtomExpr lhs = getAtom(e.get(0));
			AtomExpr rhs = getAtom(e.get(1));
			if(lhs == null || rhs == null) return;
			if(lhs.isNumber() && rhs.isNumber()) return;
			
			double a = lhs.getNumber();
			double b = rhs.getNumber();
			
			switch(expr.getType()) {
				case DIV: {
					if(b == (int)b) {
						iter.set(getAtom(a / (int)b, lhs.getStartOffset(), rhs.getEndOffset()));
					} else {
						iter.set(getAtom(a / b, lhs.getStartOffset(), rhs.getEndOffset()));
					}
					
					break;
				}
				case MUL: iter.set(getAtom(a * b, lhs.getStartOffset(), rhs.getEndOffset())); break;
				case SET: {
					break;
				}
				
				default: {
					LOGGER.warn("Missing expression type: %s", expr.getType());
				}
			}
			
			return;
		}
		
		if(expr instanceof VaryingExpr) {
			VaryingExpr e = (VaryingExpr)expr;
			
			List<Double> atoms = new ArrayList<>();
			
			switch(expr.getType()) {
				case COMMA: {
					for(int i = 0; i < e.size() - 1; i++) {
						Expression ex = e.get(i);
						
						if(ex.isPure()) {
							e.getExpressions().remove(i--);
						}
					}
					
					if(e.size() == 1) {
						iter.set(e.get(0));
					}
					return;
				}
				
				case SUB:
				case XOR:
				case AND:
				case OR:
				case ADD: {
					for(int i = 0; i < e.size(); i++) {
						Expression ex = e.get(i);
						if(ex instanceof AtomExpr) {
							AtomExpr atom = (AtomExpr)ex;
							
							if(atom.isNumber()) {
								atoms.add(atom.getNumber());
								e.getExpressions().remove(i--);
							}
						}
					}
					
					break;
				}
				
				default: return;
			}
			
			if(atoms.isEmpty()) return;
			AtomExpr next = null;
			
			switch(expr.getType()) {
				case SUB: {
					double value = atoms.get(0);
					for(int i = 1; i < atoms.size(); i++) value -= atoms.get(i);
					next = getAtom(value, e.getStartOffset(), e.getEndOffset());
					break;
				}
				case ADD: {
					double value = atoms.get(0);
					for(int i = 1; i < atoms.size(); i++) value += atoms.get(i);
					next = getAtom(value, e.getStartOffset(), e.getEndOffset());
					break;
				}
				case XOR: {
					double value = atoms.get(0);
					for(int i = 1; i < atoms.size(); i++) value = ((long)value) ^ ((long)(double)atoms.get(i));
					next = getAtom(value, e.getStartOffset(), e.getEndOffset());
					break;
				}
				case AND: {
					double value = atoms.get(0);
					for(int i = 1; i < atoms.size(); i++) value = ((long)value) & ((long)(double)atoms.get(i));
					next = getAtom(value, e.getStartOffset(), e.getEndOffset());
					break;
				}
				case OR: {
					double value = atoms.get(0);
					for(int i = 1; i < atoms.size(); i++) value = ((long)value) | ((long)(double)atoms.get(i));
					next = getAtom(value, e.getStartOffset(), e.getEndOffset());
					break;
				}
				
				default: return;
			}
			
			if(next != null) {
				if(e.size() == 0) {
					iter.set(next);
				} else {
					e.getExpressions().add(0, next);
				}
			}
		}
	}

}
