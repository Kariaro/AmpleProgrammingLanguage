package hardcoded.compiler;

import static hardcoded.compiler.Expression.AtomType.*;
import static hardcoded.compiler.Expression.ExprType.*;

import java.io.File;
import java.util.*;

import hardcoded.compiler.Block.Function;
import hardcoded.compiler.Expression.*;
import hardcoded.compiler.Statement.*;
import hardcoded.compiler.constants.*;
import hardcoded.compiler.expression.ExpressionParser;
import hardcoded.compiler.instruction.HInstructionCompiler;
import hardcoded.compiler.optimizer.HCompilerInit;
import hardcoded.compiler.types.*;
import hardcoded.errors.CompilerException;

public class HCompilerBuild {
	private File projectPath = new File("res/project/src/");
	
	private Program current_program;
	
	/**
	 * The syntax tree builder.
	 */
	private HCompilerInit init;
	
	/**
	 * The instruction compiler.
	 */
	private HInstructionCompiler hic;
	
	
	// TODO: Convert strings into decptr(<ptr to string>);
	// TODO: Only if string is const otherwise stack....
	public HCompilerBuild() {
		hic = new HInstructionCompiler();
		init = new HCompilerInit();
		
		String file = "main.hc";
		// file = "tests/000_pointer.hc";
		// file = "tests/001_comma.hc";
		// file = "tests/002_invalid_assign.hc";
		// file = "tests/003_invalid_brackets.hc";
		// file = "tests/004_cor_cand.hc";
		// file = "tests/005_assign_comma.hc";
		// file = "test_syntax.hc";
		
		try {
			build(file);
		} catch(Exception e) {
			e.printStackTrace();
		}
	}
	
	private hardcoded.visualization.HC2Visualization vs = null;
	/**
	 * 
	 * @param	pathname	A pathname string.
	 * @throws	Exception
	 * @throws	CompilerException
	 * 			If the compilation failed
	 */
	public void build(String pathname) throws Exception {
		current_program = init.init(projectPath, pathname);
		if(init.hasErrors()) {
			throw new CompilerException("Compiler errors.");
		}
		
		vs = new hardcoded.visualization.HC2Visualization();
		vs.show(current_program);
		//Thread.sleep(5000);
		doConstantFolding();
		hic.compile(current_program);
		
		
		for(Block block : current_program.list()) {
			if(!(block instanceof Function)) continue;
			
			System.out.println("========================================================");
			Function func = (Function)block;
			String str = Utils.printPretty(func);
			System.out.println(str.replace("\t", "    "));
		}
		
		System.out.println("========================================================");
	}
	
	private void constantFolding(List<Expression> parent, int index, Function func) {
		Expression expr = parent.get(index);
		// System.out.println("Folding: [" + func.name + "], [" + expr + "]");
		
		/* If the left hand side of the expression is a comma	[ ( ... , x) ]
		 * then the assignment operation should be placed only
		 * on the last element of that comma expression.
		 * 
		 * An example would be that the expression 	[ ( ... , x) += 5 ]
		 * should become 							[ ( ... , x += 5) ]
		 */
		
		
		if(expr instanceof CastExpr) {
			CastExpr e = (CastExpr)expr;
			
			// TODO: Operator overrides..
			if(e.first() instanceof AtomExpr) {
				AtomExpr a = (AtomExpr)e.first();
				
				if(a.isNumber()) {
					Type cast = e.type;
					
					if(cast instanceof PrimitiveType) {
						PrimitiveType pt = (PrimitiveType)cast;
						parent.set(index, a.convert(pt.getType()));
					} else if(cast instanceof PointerType) {
						// Casting into a pointer is always allowed
						// PointerType pt = (PointerType)cast;
						
						// (int*)((int)(double));
						parent.set(index, a.convert(i64));
					}
				}
			}
		}
		
		if(expr instanceof OpExpr) {
			OpExpr e = (OpExpr)expr;
			
			/* If the type of the expression is one of	[ add, sub, cor, cand, comma ]
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
			if(e.type == add || e.type == sub || e.type == cor || e.type == cand || e.type == comma) {
				for(int i = e.size() - 1; i >= 0; i--) {
					Expression ex = e.get(i);
					if(ex instanceof OpExpr) {
						OpExpr nx = (OpExpr)ex;
						
						if(nx.type == e.type) {
							e.list.remove(i);
							e.list.addAll(i, nx.list);
							i += nx.list.size();
							continue;
						}
					}
				}
			}
	
			switch(e.type) {
				case set: {
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
							for(int i = 0; i < a.size() - 1; i++) o.add(a.get(i));
							e.set(0, a.last());
						}
						
						if(b.type() == comma) {
							/* Folding the expression		[ set(x, comma( BBB , y)) ]
							 * into							[ comma( BBB , set(x, y)) ] */
							for(int i = 0; i < b.size() - 1; i++) o.add(b.get(i));
							e.set(1, b.last());
						}
						
						o.add(e);
						
						// Change the expression into the new format.
						parent.set(index, o);
					}
					
					break;
				}
				
				case comma: {
					System.out.println("Befor -> '" + e.list + "'");
					
					for(int i = 0; i < e.size() - 1; i++) {
						if(!e.get(i).hasSideEffects()) e.list.remove(i--);
					}
					
					System.out.println("After -> '" + e.list + "'");
					
					if(e.size() == 1) parent.set(index, e.first());
					break;
				}
				
				case sub: case add: {
					List<AtomExpr> list = new ArrayList<>();
					for(int i = 0; i < e.size(); i++) {
						Expression e0 = e.get(i);
						
						if(e0 instanceof AtomExpr) {
							AtomExpr a = (AtomExpr)e0;
							
							if(a.isNumber()) {
								list.add(a);
								e.list.remove(i);
								i--;
							} else {
								//throw new RuntimeException("You cannot add a non number value");
							}
						}
					}
					
					if(!list.isEmpty()) {
						for(; list.size() > 1;) {
							AtomExpr c = (AtomExpr)ExpressionParser.compute(e.type, list.get(0), list.get(1));
							list.remove(0);
							list.set(0, c);
						}
						
						if(!(list.get(0).isZero() && e.size() > 0)) {
							e.list.add(list.get(0));
						}
					}
					
					if(e.size() == 1) parent.set(index, e.first());
					break;
				}
				
				case cand: {
					for(int i = 0; i < e.size(); i++) {
						Expression e0 = e.get(i);
						
						if(e0 instanceof AtomExpr) {
							AtomExpr a = (AtomExpr)e0;
							
							if(a.isNumber()) {
								if(a.isZero()) {
									for(; i + 1 < e.size(); ) {
										e.list.remove(i + 1);
									}
								} else {
									if(i < e.size() - 1) e.list.remove(i--);
								}
							}
						}
					}
					
					if(e.size() == 1) parent.set(index, e.first());
					break;
				}
				
				case cor: {
					for(int i = 0; i < e.size(); i++) {
						Expression e0 = e.get(i);
						
						if(e0 instanceof AtomExpr) {
							AtomExpr a = (AtomExpr)e0;
							
							if(a.isNumber()) {
								if(!a.isZero()) {
									for(; i + 1 < e.size(); ) {
										e.list.remove(i + 1);
									}
								} else {
									if(i < e.size() - 1) e.list.remove(i--);
								}
							}
						}
					}
					
					if(e.size() == 1) parent.set(index, e.first());
					break;
				}
				
				case neg: case not:
				case mul: case div:
				case nor: case xor:
				case shr: case shl:
				case or: case and:
				case lt: case lte:
				case gt: case gte:
				case eq: case neq: {
					Expression next = ExpressionParser.compute(e.type, e);
					if(next != null) parent.set(index, next); break;
				}
				
				
				default: {
					
				}
			}
		}
	}
	
	private void doConstantFolding() {
		for(int i = 0; i < current_program.size(); i++) {
			Block block = current_program.get(i);
			
			if(!(block instanceof Function)) continue;
			Function func = (Function)block;
			
			Utils.execute_for_all_expressions(func, (parent, index, function) -> {
				String bef = "" + parent.get(index);
				constantFolding(parent, index, function);
				
				String now = "" + parent.get(index);
				
				if(!bef.equals(now)) {
					vs.show(current_program);
					vs.getComponent().repaint();
//					try {
//						Thread.sleep(1000);
//					} catch (InterruptedException e) {
//						e.printStackTrace();
//					}
					
					System.out.println("[" + index + "] (" + bef + ")\n[" + index + "] (" + now + ")\n");
				}
			});
			
			Utils.execute_for_all_statements(func, (parent, index, function) -> {
				Statement stat = parent.get(index);
				
				if(stat instanceof ForStat) {
					Expression c = ((ForStat)stat).condition();
					if(c instanceof AtomExpr) {
						AtomExpr a = (AtomExpr)c;
						if(a.isNumber() && a.isZero()) parent.set(index, Statement.EMPTY);
					}
				}
				
				if(stat instanceof WhileStat) {
					Expression c = ((WhileStat)stat).condition();
					if(c instanceof AtomExpr) {
						AtomExpr a = (AtomExpr)c;
						if(a.isNumber() && a.isZero()) parent.set(index, Statement.EMPTY);
					}
				}
				
				if(stat instanceof IfStat) {
					IfStat is = (IfStat)stat;
					Expression c = is.condition();
					if(c instanceof AtomExpr) {
						AtomExpr a = (AtomExpr)c;
						if(a.isNumber()) {
							if(a.isZero()) {
								if(is.elseBody() == null) {
									parent.set(index, Statement.EMPTY);
								} else {
									parent.set(index, is.elseBody());
								}
							} else {
								parent.set(index, is.body());
							}
						}
					}
				}
			});
		}
	}

}
