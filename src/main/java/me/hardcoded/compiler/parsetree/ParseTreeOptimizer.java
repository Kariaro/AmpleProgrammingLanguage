package me.hardcoded.compiler.parsetree;

import static me.hardcoded.compiler.expression.ExprType.*;

import java.util.ArrayList;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import me.hardcoded.compiler.expression.*;
import me.hardcoded.compiler.impl.IBlock;
import me.hardcoded.compiler.statement.*;
import me.hardcoded.utils.DebugUtils;
import me.hardcoded.utils.StatementUtils;
import me.hardcoded.visualization.Visualization;

/**
 * @author HardCoded
 */
public class ParseTreeOptimizer {
	private static final Logger LOGGER = LogManager.getLogger(ParseTreeOptimizer.class);
	
	// TODO: cor and cand has problems with some or operations...
	// TODO: Check that all comma expressions are working correctly...
	
	public ParseTreeOptimizer() {
		
	}

	public void do_constant_folding(Program current_program) {
		do_constant_folding(Visualization.DUMMY, current_program);
	}
	
	public void do_constant_folding(Visualization vs, Program currentProgram) {
		if(DebugUtils.DEBUG_PARSE_TREE_OPTIMIZATION) {
			LOGGER.info("");
			LOGGER.info("ParseTreeOptimizer");
			LOGGER.info("");
		}
		
		for(int i = 0; i < currentProgram.size(); i++) {
			IBlock block = currentProgram.get(i);
			
			if(!(block instanceof Function)) continue;
			Function func = (Function)block;
			
			StatementUtils.execute_for_all_expressions(func, (parent, index, function) -> {
				String bef = null;
				if(DebugUtils.DEBUG_PARSE_TREE_OPTIMIZATION) {
					bef = parent.get(index).toString();
				}
				
				constantFolding(parent, index, function);
				
				if(DebugUtils.DEBUG_PARSE_TREE_OPTIMIZATION) {
					String now = parent.get(index).toString();
					if(!bef.equals(now)) {
						vs.show(currentProgram);
						vs.getComponent().repaint();
						try {
							Thread.sleep(100);
						} catch(Exception e) {
							e.printStackTrace();
						}
						
						LOGGER.info("  [{}] ({}) -> ({})", index, bef, now);
					}
				}
			});
			
			StatementUtils.execute_for_all_statements(func, (parent, index, function) -> {
				Statement stat = parent.get(index);

				if(stat instanceof ForStat) {
					Expression c = ((ForStat)stat).getCondition();
					
					if(c instanceof AtomExpr a && a.isNumber() && a.isZero()) {
						parent.set(index, Statement.newEmpty());
					}
				} else if(stat instanceof WhileStat) {
					Expression c = ((WhileStat)stat).getCondition();
					
					if(c instanceof AtomExpr a && a.isNumber() && a.isZero()) {
						parent.set(index, Statement.newEmpty());
					}
				} else if(stat instanceof IfStat is) {
					Expression c = is.getCondition();
					// TODO: We should remove the IF statement if the condition is not pure. Otherwise it should be removed completely.
					
					if(c instanceof AtomExpr a && a.isNumber()) {
						if(a.isZero()) {
							if(!is.hasElseBody()) {
								parent.set(index, Statement.newEmpty());
							} else {
								parent.set(index, is.getElseBody());
							}
						} else {
							parent.set(index, is.getBody());
						}
					}
				} else if(stat instanceof NestedStat ns) {
					// Check all children if any of them are nested.
					List<Statement> list = ns.getElements();
					for(int j = 0; j < list.size(); j++) {
						Statement child = list.get(j);
						if(child.getClass() == NestedStat.class) {
							list.remove(j);
							list.addAll(j, child.getElements());
							j--;
						}
					}
				}
			});
			
			if(DebugUtils.DEBUG_PARSE_TREE_OPTIMIZATION) {
				vs.show(currentProgram);
				vs.getComponent().repaint();
			}
		}
		
		System.out.println(StatementUtils.printPretty(currentProgram));
	}
	
	public void constantFolding(List<Expression> parent, int index, Function func) {
		Expression expr = parent.get(index);
		
		/* If the left hand side of the expression is a comma	[ ( ... , x) ]
		 * then the assignment operation should be placed only
		 * on the last element of that comma expression.
		 * 
		 * An example would be that the expression 	[ ( ... , x) += 5 ]
		 * should become 							[ ( ... , x += 5) ]
		 */
		if(expr instanceof OpExpr e) {
			/* If the type of the expression is one of	[ add, cor, cand, comma ]
			 * then the expression can be expanded.
			 * 
			 * Expanding an expression is to remove nested operations and put them
			 * into one expression. 
			 * 
			 * An example of this expansion would be	[ add(1, add(2, add(3, 4))) ]
			 * where it can be expanded into			[ add(1, 2, 3, 4) ]
			 * 
			 * Expanding expression will make it easier for the compiler to convert
			 * to machine code later.
			 */
			if(e.type() == add || e.type() == cor || e.type() == cand || e.type() == comma) {
				// Try fold some structures...
				foldAddSub(e);
				
				for(int i = e.length() - 1; i >= 0; i--) {
					Expression ex = e.get(i);
					if(ex instanceof OpExpr nx && nx.type() == e.type()) {
						e.remove(i);
						e.getElements().addAll(i, nx.getElements());
						i += nx.length();
						continue;
					}
				}
			}
			
			/* If the type of the expression is of  [ sub ]
			 * we need to take special care when inlining values.
			 * 
			 * 
			 * [ sub(sub(a, b), sub(c, d), sub(e, f)) ]
			 * [ sub(a, neg(b), neg(c), d, neg(e), f) ]
			 */
			if(e.type() == sub) {
				foldAddSub(e);
				
				for(int i = 0; i < e.length(); i++) {
					if(e.get(i) instanceof OpExpr nx && nx.type() == sub) {
						if(i == 0) {
							// Special care needs to be performed.
							// [ sub(sub(a, b), ...) ]     -> [ sub(a, b, ...) ]
							e.remove(i);
							e.getElements().addAll(i, nx.getElements());
							i --;
						} else {
							// [ sub(..., sub(a, b)) ]     -> [ sub(..., a, neg(b)) ]
							e.remove(i);
							List<Expression> list = nx.getElements();
							for(int j = 0; j < list.size(); j++) {
								Expression nx_elm = nx.get(j);
								if(j == 0) {
									e.getElements().add(i + j, nx_elm);
								} else {
									if(nx_elm instanceof AtomExpr a && a.isNumber()) {
										nx_elm = (AtomExpr)ExpressionParser.compute(neg, a, null);
									} else {
										nx_elm = new OpExpr(neg, nx_elm);
									}
									e.getElements().add(i + j, nx_elm);
								}
							}
							i --;
						}
					}
				}
				
				// System.out.println(expr);
			}
	
			switch(e.type()) {
				case set -> {
					Expression a = e.first();
					Expression b = e.last();
					
					boolean hasComma = a.type() == comma || b.type() == comma;
					
					if(hasComma) {
						/* Folding the expression			[ set(comma( AAA , x), comma( BBB , y)) ]
						 * 
						 * The comma expression has already removed all elements that does not
						 * have side effects or expanded the comma expression if there was only one
						 * element inside it. This means that if we see a comma expression then we
						 * need to change it to				[ comma( AAA , BBB , set(x, y)) ]
						 */
						OpExpr o = new OpExpr(comma);
						
						if(a.type() == comma) {
							/* Folding the expression		[ set(comma( AAA , x), y) ]
							 * into							[ comma( AAA , set(x, y)) ] */
							for(int i = 0; i < a.length() - 1; i++) o.add(a.get(i));
							e.set(0, a.last());
						}
						
						if(b.type() == comma) {
							/* Folding the expression		[ set(x, comma( BBB , y)) ]
							 * into							[ comma( BBB , set(x, y)) ] */
							for(int i = 0; i < b.length() - 1; i++) o.add(b.get(i));
							e.set(1, b.last());
						}
						
						o.add(e);
						
						// Change the expression into the new format.
						parent.set(index, o);
					}
				}
				
				case comma -> {
					for(int i = 0; i < e.length() - 1; i++) {
						if(!e.get(i).hasSideEffects()) e.remove(i--);
					}
					
					if(e.length() == 1) {
						parent.set(index, e.first());
					}
				}
				
				case cast -> {
					Expression first = expr.first();
					
					if(first.size().equals(expr.size())) {
						parent.set(index, first);
					} else if(first instanceof AtomExpr a) {
						parent.set(index, a.convert(expr.size()));
					}
				}
				
				case incptr, decptr -> {
					ExprType opp = e.type() == decptr ? incptr:decptr;
					Expression ex = e.first();
					
					if(ex.type() == opp) {
						// Remove all instances of [ addptr(decptr( ... )) ]
						//                         [ decptr(addptr( ... )) ]
						parent.set(index, ex.first());
					}
				}
				
				case sub, add -> {
					 // System.out.println("> " + e);

					List<AtomExpr> list = new ArrayList<>();
					for(int i = 0; i < e.length(); i++) {
						Expression e0 = e.get(i);
						
						if(e0 instanceof AtomExpr a) {
							if(a.isNumber()) {
								if(e.type() == sub && i == 0) {
									list.add(a);
									e.remove(i);
									i--;
									
									if(e.length() > 0) {
										if(e.get(0) instanceof AtomExpr e_first && e_first.isNumber()) {
											e.set(0, ExpressionParser.compute(neg, e_first, null));
										} else {
											e.set(0, new OpExpr(neg, e.get(0)));
										}
									}
								} else {
									list.add(a);
									e.remove(i);
									i--;
								}
							} else {
								//throw new RuntimeException("You cannot add a non number value");
							}
						}
					}
					
					if(!list.isEmpty()) {
						for(; list.size() > 1;) {
							AtomExpr c = (AtomExpr)ExpressionParser.compute(add, list.get(0), list.get(1));
							list.remove(0);
							list.set(0, c);
						}
						
						if(!(list.get(0).isZero() && e.length() > 0)) {
							e.add(list.get(0));
						}
					}
					
					if(e.length() == 1) {
						parent.set(index, e.first());
					}
					
					// System.out.println("> " + e);
				}
				
				case cand -> {
					/* Folding the cand operation.
					 * 
					 * The expression		[ cand(x, y) ]
					 */
					for(int i = 0; i < e.length(); i++) {
						Expression e0 = e.get(i);
						
						if(e0.type() == comma) {
							/* Folding the expression	[ cand(x, comma( ... )) ] */
							
							if(e0.last() instanceof AtomExpr a) {
								if(a.isZero()) {
									for(; i + 1 < e.length(); ) {
										e.remove(i + 1);
									}
								} else {
									((OpExpr)e0).set(e0.length() - 1, new AtomExpr(1));
									if(i < e.length() - 1) e.remove(i--);
								}
							}
						}
						
						if(e0 instanceof AtomExpr a && a.isNumber()) {
							if(a.isZero()) {
								for(; i + 1 < e.length(); ) {
									e.remove(i + 1);
								}
							} else {
								if(i < e.length() - 1) e.remove(i--);
							}
						}
					}
					
					if(e.length() == 1) {
						if(e.first() instanceof AtomExpr e_first) {
							parent.set(index, new AtomExpr(e_first.isZero() ? 0:1));
							break;
						}
						
						parent.set(index, e.first());
					}
				}
				
				case cor -> {
					for(int i = 0; i < e.length(); i++) {
						Expression e0 = e.get(i);
						
						if(e0.type() == comma) {
							/* Folding the expression	[ cor(x, comma( ... )) ] */
							
							if(e0.last() instanceof AtomExpr a) {
								if(!a.isZero()) {
									// Replace the number with a one....
									((OpExpr)e0).set(e0.length() - 1, new AtomExpr(1));
									for(; i + 1 < e.length(); ) {
										e.remove(i + 1);
									}
								} else {
									if(i < e.length() - 1) e.remove(i--);
								}
							}
						}
						
						if(e0 instanceof AtomExpr a && a.isNumber()) {
							if(!a.isZero()) {
								for(; i + 1 < e.length();) {
									e.remove(i + 1);
								}
							} else {
								if(i < e.length() - 1) e.remove(i--);
							}
						}
					}
					
					if(e.length() == 1) {
						if(e.first() instanceof AtomExpr e_first) {
							if(e_first.isNumber()) {
								parent.set(index, new AtomExpr(e_first.isZero() ? 0:1));
							} else {
								// Check if it is not equal to zero
								parent.set(index, new OpExpr(neq, e_first, new AtomExpr(0)));
							}
							break;
						}
						
						parent.set(index, e.first());
					}
				}
				
				case neg, not, mul, div, nor, xor, shr, shl, or, and, lt, lte, gt, gte, eq, neq, mod -> {
					Expression next = ExpressionParser.compute(e.type(), e);
					if(next != null) {
						parent.set(index, next);
					}
				}
				
				case jump, leave, loop, label, ret, call -> {
					// There is nothing we can do here.
					break;
				}
				
				default -> {
					if(DebugUtils.isDeveloper()) {
						System.err.println("[NOT FOLDED]" + e);
					}
				}
			}
			
			switch(e.type()) {
				case neg -> {
					if(e.get(0) instanceof AtomExpr a && a.isNumber()) {
						// neg(<number>)  ->  -<number>
						parent.set(index, ExpressionParser.compute(neg, e));
					} else if(e.get(0).type() == neg) {
						// neg(neg(a))  ->  a
						parent.set(index, e.get(0).get(0));
					}
				}
			}
		}
	}
	
	private void foldAddSub(OpExpr expr) {
		if(expr.type() != add && expr.type() != sub) return;
		
		for(int i = 0; i < expr.length(); i++) {
			if(expr.get(i) instanceof OpExpr e && (e.type() != expr.type()) && (e.type() == add || e.type() == sub)) {
				expr.remove(i);
				
				for(int j = 0; j < e.length(); j++) {
					Expression add = e.get(j);
					
					// [ add(sub(a, b), sub(c, d)) ]  -> [ add(a, neg(b), c, neg(d)) ]  
					// [ sub(add(a, b), add(c, d)) ]  -> [ sub(a, neg(b), c,     d ) ]
					if(j != 0 && (i == 0 || e.type() == sub)) {
						// We need to neg the result in this case.
						if(add instanceof AtomExpr atom_expr && atom_expr.isNumber()) {
							add = (AtomExpr)ExpressionParser.compute(neg, add, null);
						} else {
							if(add.type() == neg) {
								add = add.get(0);
							} else {
								add = new OpExpr(neg, add);
							}
						}
					}
					
					if(i + j == expr.length()) {
						expr.add(add);
					} else {
						expr.getElements().add(i + j, add);
					}
				}
			}
		}
	}
}
