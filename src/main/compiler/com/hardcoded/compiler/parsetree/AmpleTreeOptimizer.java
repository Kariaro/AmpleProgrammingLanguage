package com.hardcoded.compiler.parsetree;

import java.util.List;
import java.util.ListIterator;

import com.hardcoded.compiler.api.Expression;
import com.hardcoded.compiler.api.Statement;
import com.hardcoded.compiler.impl.expression.AtomExpr;
import com.hardcoded.compiler.impl.expression.EmptyExpr;
import com.hardcoded.compiler.impl.statement.*;
import com.hardcoded.logger.Log;
import com.hardcoded.options.Options;

/**
 * A parse tree optimizer
 * 
 * @author HardCoded
 * @since 0.2.0
 */
class AmpleTreeOptimizer {
	@SuppressWarnings("unused")
	private static final Log LOGGER = Log.getLogger();
	
	private AmpleExprOptimizer expr_optimizer;
	public AmpleTreeOptimizer() {
		expr_optimizer = new AmpleExprOptimizer();
	}
	
	private static interface Worker<T> {
		void run(T parent, T element, ListIterator<T> iter);
	}
	
	private AtomExpr getAtom(Statement stat) {
		if(!(stat instanceof ExprStat)) return null;
		Expression expr = ((ExprStat)stat).getExpression();
		if(expr instanceof AtomExpr) return (AtomExpr)expr;
		return null;
	}
	
	private final Worker<Statement> STAT_WORKER = new Worker<>() {
		public void run(Statement parent, Statement stat, ListIterator<Statement> iter) {
			if(parent instanceof ScopeStat && EmptyStat.isEmpty(stat)) {
				iter.remove();
				return;
			}
			
			if(stat instanceof ScopeStat) {
				ScopeStat s = (ScopeStat)stat;
				if(s.getNumElements() == 0) {
					iter.remove();
					return;
				}
				
				if(s.getNumElements() == 1) {
					Statement element = s.get(0);
					if(element instanceof ScopeStat) {
						iter.set(element);
						return;
					}
				}
				
				return;
			}
			
			if(stat instanceof IfStat) {
				IfStat s = (IfStat)stat;
				
				AtomExpr atom = getAtom(s.get(0));
				if((atom != null) && atom.isNumber()) {
					double value = atom.getNumber();
					
					if(value == 0) {
						if(s.hasElse()) {
							iter.set(s.get(2));
							return;
						}
						
						iter.remove();
						return;
					}
					
					iter.set(s.get(1));
					return;
				}
				
				return;
			}
			
			if(stat instanceof WhileStat) {
				WhileStat s = (WhileStat)stat;
				
				AtomExpr atom = getAtom(s.get(0));
				if((atom != null) && atom.isNumber()) {
					double value = atom.getNumber();
					
					if(value == 0) {
						iter.remove();
					}
				}
				
				return;
			}
			
			if(stat instanceof DoWhileStat) {
				DoWhileStat s = (DoWhileStat)stat;
				
				AtomExpr atom = getAtom(s.get(1));
				if((atom != null) && atom.isNumber()) {
					double value = atom.getNumber();
					
					if(value == 0) {
						iter.set(s.get(0));
					}
				}
				
				return;
			}
			
//			if(stat instanceof ExprStat) {
//				ExprStat s = (ExprStat)stat;
//				Expression e = s.getExpression();
//				if(e.isPure()) {
//					iter.remove();
//				}
//				
//				return;
//			}
		}
	};
	
	private final Worker<Expression> EXPR_WORKER = new Worker<>() {
		public void run(Expression parent, Expression expr, ListIterator<Expression> iter) {
			expr_optimizer.process(expr, iter);
		}
	};
	
	@SuppressWarnings("unused")
	private Options options;
	
	public ProgramStat process(Options options, ProgramStat stat) {
		//if(!options.has(Key.OPTIMIZATION)) return stat;
		this.options = options;
		
		process_stat(EmptyStat.get(), stat);
		return stat;
	}
	
	private void process_stat(Statement parent, Statement stat) {
		List<Statement> list = stat.getStatements();
		ListIterator<Statement> iter = list.listIterator();
		
		while(iter.hasNext()) {
			Statement s = iter.next();
			if(s instanceof ExprStat) {
				process_expr_start((ExprStat)s);
			}
			
			process_stat(parent, s);
			if(iter.hasPrevious()) {
				iter.previous();
				Statement c = iter.next();
				if(s != c) {
					iter.previous();
					continue;
				}
			}
			
			STAT_WORKER.run(parent, s, iter);
		}
	}
	
	private void process_expr_start(ExprStat stat) {
		process_expr(EmptyExpr.get(), stat.getExpression());
		List<Expression> list = stat.getExpressions();
		ListIterator<Expression> iter = list.listIterator();
		EXPR_WORKER.run(EmptyExpr.get(), iter.next(), iter);
	}
	
	private void process_expr(Expression parent, Expression expr) {
		List<Expression> list = expr.getExpressions();
		ListIterator<Expression> iter = list.listIterator();
		
		while(iter.hasNext()) {
			Expression e = iter.next();
//			if(e instanceof StatExpr) {
//				process_stat(worker_stat, worker_expr, ((StatExpr)s).getStatement());
//			}
			process_expr(parent, e);
			if(iter.hasPrevious()) {
				iter.previous();
				Expression c = iter.next();
				if(e != c) {
					iter.previous();
					continue;
				}
			}
			
			EXPR_WORKER.run(parent, e, iter);
		}
	}
}
