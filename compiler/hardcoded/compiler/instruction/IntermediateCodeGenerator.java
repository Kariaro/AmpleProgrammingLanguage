package hardcoded.compiler.instruction;

import static hardcoded.compiler.constants.ExprType.*;

import java.util.*;

import hardcoded.compiler.Block;
import hardcoded.compiler.Block.Function;
import hardcoded.compiler.Identifier;
import hardcoded.compiler.Identifier.IdType;
import hardcoded.compiler.Program;
import hardcoded.compiler.expression.AtomExpr;
import hardcoded.compiler.expression.Expression;
import hardcoded.compiler.expression.LowType;
import hardcoded.compiler.instruction.IRInstruction.*;
import hardcoded.compiler.statement.*;

/**
 * IR is short for 'Intermediate representation'
 * 
 * @author HardCoded
 */
public class IntermediateCodeGenerator {
	// TODO: Default pointer size is always long.
	
	public IntermediateCodeGenerator() {
		
	}
	
	private IRProgram program;
	public IRProgram generate(Program prog) {
		program = new IRProgram();
		
		for(int i = 0; i < prog.size(); i++) {
			Block block = prog.get(i);
			
			if(!(block instanceof Function)) continue;
			Function func = (Function)block;
			variables.clear();
			
			IRInstruction.reset_counter();
			IRInstruction inst = compileInstructions(func.body).first();
			program.addFunction(func, inst);
		}
		
		return program;
	}
	
	private boolean shouldCheck(Expression e) {
		return !(e instanceof AtomExpr);
	}
	
	private Map<String, Param> variables = new HashMap<>();
	private Param addString(AtomExpr a) {
		return new RefReg(".data.strings", program.getContext().getStringIndexAddIfAbsent(a.s_value));
	}
	
	private Param createObject(Expression e) {
		if(e.type() == atom) {
			AtomExpr a = (AtomExpr)e;
			
			if(a.isString()) {
				return addString(a);
			}
			
			if(a.isIdentifier()) {
				Identifier ident = a.d_value;
				String name = ident.name();
				
				if(variables.containsKey(name)) return variables.get(name);
				LowType size = e.size();
				
				Param next;
				if(ident.id_type() == IdType.param) {
					next = new Reg(ident.name(), ident.low_type(), ident.index());
				} else {
					next = IRInstruction.temp(size);
				}
				
				variables.put(name, next);
				return next;
			}
			
			if(a.isNumber()) {
				return new NumberReg(a);
			}
		}
		
		//throw new NullPointerException("Failed to create parameter for expression '" + e +"'");
		return new DebugParam(e.clone());
	}
	
	private IRInstruction createInstructions(Expression expr, Param request) {
		IRInstruction inst = new IRInstruction(IRType.nop, new DebugParam(expr.toString()));
		
		switch(expr.type()) {
			case set: {
				Expression a = expr.first(), b = expr.last();
				
				Param reg_0 = createObject(a);
				Param reg_1 = createObject(b);
				boolean pointer = false;
				if(shouldCheck(a)) {
					if(a.type() == decptr) {
						pointer = true;
						
						Expression f = a.first();
						if(shouldCheck(f)) {
							reg_0 = IRInstruction.temp(a.size());
							inst.append(compileInstructions(a.first(), reg_0));
						} else {
							reg_0 = createObject(f);
						}
						
						pointer = true;
					} else {
						reg_0 = IRInstruction.temp(a.size());
						inst.append(compileInstructions(a, reg_0));
					}
				}
				
				if(shouldCheck(b)) {
					reg_1 = IRInstruction.temp(b.size());
					inst.append(compileInstructions(b, reg_1));
				}
				
				IRType action = pointer ? IRType.write:IRType.mov;
				if(!inst.hasNeighbours()) {
					inst = new IRInstruction(action, reg_0, reg_1);
				} else {
					inst.append(new IRInstruction(action, reg_0, reg_1));
				}
				
				if(request != null) {
					inst.append(new IRInstruction(action, request, reg_1));
				}
				
				break;
			}
			
			case div: case mul:
			case shl: case shr:
			case and: case xor:
			case lt: case lte:
			case gt: case gte:
			case eq: case neq:
			case or: case mod: {
				Expression a = expr.first(), b = expr.last();
				
				Param reg_0 = createObject(a);
				Param reg_1 = createObject(b);
				if(shouldCheck(a)) {
					reg_0 = IRInstruction.temp(reg_0.getSize());
					inst.append(compileInstructions(a, reg_0));
				}
				
				if(shouldCheck(b)) {
					reg_1 = IRInstruction.temp(reg_1.getSize());
					inst.append(compileInstructions(b, reg_1));
				}
				
				// TODO: If this is calculated without having a value to write to we should not do this operation?
				if(request != null) {
					inst.append(new IRInstruction(IRType.convert(expr.type()), request, reg_0, reg_1));
				} else {
					System.out.println("Operation?!?!?!?" + a + ", " + b);
				}
				
				break;
			}
			
			case ret: {
				Expression a = expr.first();
				
				Param reg_0 = createObject(a);
				if(shouldCheck(a)) {
					reg_0 = IRInstruction.temp(reg_0.getSize());
					inst.append(compileInstructions(a, reg_0));
				}
				
				inst.append(new IRInstruction(IRType.ret, reg_0));
				break;
			}
			
			case neg:
			case not:
			case nor: {
				Expression a = expr.first();
				
				Param reg_0 = createObject(a);
				if(shouldCheck(a)) {
					reg_0 = IRInstruction.temp(reg_0.getSize());
					inst.append(compileInstructions(a, reg_0));
				}
				
				inst.append(new IRInstruction(IRType.convert(expr.type()), request, reg_0));
				break;
			}
			
			case addptr: {
				// TODO: ?? Remember that we need to write and not mov !!!!
				
				Expression a = expr.first();
				
				Param reg_0 = createObject(a);
				if(shouldCheck(a)) {
					reg_0 = IRInstruction.temp(reg_0.getSize());
					inst.append(compileInstructions(a, reg_0));
				}
				
				inst.append(new IRInstruction(IRType.mov, request, reg_0));
				break;
			}
			
			case decptr: {
				Expression a = expr.first();
				
				Param reg_0 = createObject(a);
				if(shouldCheck(a)) {
					reg_0 = IRInstruction.temp(a.size());
					inst.append(compileInstructions(a, reg_0));
				}
				
				// TODO: Reading from a Identifier should hold the primitive size and not the pointer size.
				inst.append(new IRInstruction(IRType.read, request, reg_0));
				break;
			}
			
			case comma: {
				for(int i = 0; i < expr.length() - 1; i++) {
					inst.append(compileInstructions(expr.get(i)));
				}
				
				if(request != null) {
					if(expr.last().hasSideEffects()) {
						inst.append(compileInstructions(expr.last(), request));
					} else {
						inst.append(new IRInstruction(IRType.mov, request, createObject(expr.last())));
					}
				} else {
					inst.append(compileInstructions(expr.last()));
				}
				
				break;
			}
			
			case add: case sub: {
				IRType type = IRType.convert(expr.type());
				Expression a = expr.get(0), b = expr.get(1);
				
				Param reg_0 = createObject(a);
				if(shouldCheck(a)) {
					reg_0 = IRInstruction.temp(a.size());
					inst.append(compileInstructions(a, reg_0));
				}

				Param reg_1 = createObject(b);
				if(shouldCheck(b)) {
					reg_1 = IRInstruction.temp(b.size());
					inst.append(compileInstructions(b, reg_1));
				}
				
				if(expr.length() < 3) {
					inst.append(new IRInstruction(type, request, reg_0, reg_1));
					break;
				}
				
				Param reg_2 = IRInstruction.temp(reg_0.getSize());
				inst.append(new IRInstruction(type, reg_2, reg_0, reg_1));
				
				for(int i = 2; i < expr.length(); i++) {
					b = expr.get(i);
					reg_1 = createObject(b);
					if(shouldCheck(b)) {
						reg_1 = IRInstruction.temp(b.size());
						inst.append(compileInstructions(b, reg_1));
					}
					
					reg_0 = reg_2;
					if(i == expr.length() - 1) {
						reg_2 = request;
					} else {
						reg_2 = IRInstruction.temp(b.size());
					}
					
					inst.append(new IRInstruction(type, reg_2, reg_0, reg_1));
				}
				
				break;
			}
			
			case call: {
				List<Param> params = new ArrayList<>();
				
				{
					// Called function
					Expression e = expr.first();
					
					if(e instanceof AtomExpr) {
						AtomExpr a = (AtomExpr)e;
						
						if(a.isIdentifier()) {
							params.add(new IRInstruction.FunctionLabel(a.d_value));
						}
					} else {
						params.add(new IRInstruction.LabelParam(e.toString()));
					}
				}
				
				for(int i = 1; i < expr.length(); i++) {
					Expression e = expr.get(i);
					Param reg = createObject(e);
					
					if(shouldCheck(e)) {
						// reg should be the size of the parameter for that function...
						reg = IRInstruction.temp(reg.getSize());
						inst.append(compileInstructions(e, reg));
					}
					
					params.add(reg);
				}
				
				if(request == null) {
					params.add(0, IRInstruction.NONE);
				} else {
					params.add(0, request);
				}
				
				inst.append(new IRInstruction(IRType.call, params.toArray(new Param[0])));
				break;
			}
			
			case cand: {
				LabelParam label_end = new LabelParam("cand.end");
				if(request != null) inst.append(new IRInstruction(IRType.mov, request, new NumberReg(0, request.getSize())));
				
				for(int i = 0; i < expr.length(); i++) {
					Expression e = expr.get(i);
					Param reg = createObject(e);
					
					if(shouldCheck(e)) {
						reg = IRInstruction.temp(e.size());
						inst.append(compileInstructions(e, reg));
					}
					
					inst.append(new IRInstruction(IRType.brz, reg, label_end));
				}

				if(request != null) inst.append(new IRInstruction(IRType.mov, request, new NumberReg(1, request.getSize())));
				inst.append(new IRInstruction(IRType.label, label_end));
				break;
			}
			
			case cor: {
				LabelParam label_end = new LabelParam("cor.end");
				if(request != null) inst.append(new IRInstruction(IRType.mov, request, new NumberReg(1, request.getSize())));
				
				for(int i = 0; i < expr.length(); i++) {
					Expression e = expr.get(i);
					Param reg = createObject(e);
					
					if(shouldCheck(e)) {
						reg = IRInstruction.temp(reg.getSize());
						inst.append(compileInstructions(e, reg));
					}
					
					inst.append(new IRInstruction(IRType.bnz, reg, label_end));
				}

				if(request != null) inst.append(new IRInstruction(IRType.mov, request, new NumberReg(0, request.getSize())));
				inst.append(new IRInstruction(IRType.label, label_end));
				break;
			}
			
			default: {
				if(expr instanceof AtomExpr && request != null) {
					inst.append(new IRInstruction(IRType.mov, request, createObject(expr)));
					break;
				}
				
				System.err.println("[MISSING INSTRUCTION] -> " + expr);
			}
		}
		
		//System.out.println(inst + ", [" + inst.first() + ", " + inst.last() + "]");
		
		// This checks if we have modified the instruction.
		// If we did modify the instruction we should remove
		// the first entry because it's not needed anymore.
		if(inst.first() != inst.last()) {
			// return inst.first().remove();
		}
		
		return inst;
	}
	
	private IRInstruction createIfInstructions(IfStat stat) {
		IRInstruction inst;
		
		LabelParam label_end = new LabelParam("if.end");
		
		if(stat.hasElseBody()) {
			// ============================== //
			// if(x) { ... } else { ... }
			
			// brz [else] [x]				Branch to [else] if [x] is zero
			//    ...
			// br [end]						Branch to [end]
			// else:
			//    ...
			// end:
			
			LabelParam label_else = new LabelParam("if.else");
			
			Param temp = IRInstruction.temp(stat.getCondition().size());
			inst = compileInstructions(stat.getCondition(), temp);
			inst.append(new IRInstruction(IRType.brz, temp, label_else));
			inst.append(compileInstructions(stat.getBody()));
			inst.append(new IRInstruction(IRType.br, label_end));
			inst.append(new IRInstruction(IRType.label, label_else));
			inst.append(compileInstructions(stat.getElseBody()));
			inst.append(new IRInstruction(IRType.label, label_end));
		} else {
			// ============================== //
			// if(x) { ... }
			
			// brz [end] [x]				Branch to [end] if [x] is zero
			//    ...
			// end:

			Param temp = IRInstruction.temp(stat.getCondition().size());
			inst = compileInstructions(stat.getCondition(), temp);
			inst.append(new IRInstruction(IRType.brz, temp, label_end));
			inst.append(compileInstructions(stat.getBody()));
			inst.append(new IRInstruction(IRType.label, label_end));
		}
		
		return inst;
	}
	
	private IRInstruction createWhileInstructions(WhileStat stat) {
		// ============================== //
		// while(x) { ... }
		
		// next:
		//   ; if x has side effects evaluate x here!
		// brz [end] x					Branch to [end] if x is zero
		// loop:
		//   ...
		//   ; if x has side effects jump to next...
		//   br [next]
		// end:
		
		LabelParam label_next = new LabelParam("while.next");
		LabelParam label_loop = new LabelParam("while.loop");
		LabelParam label_end = new LabelParam("while.end");

		IRInstruction inst = new IRInstruction(IRType.label, label_next);
		Param temp = IRInstruction.temp(stat.getCondition().size());
		inst.append(compileInstructions(stat.getCondition(), temp));
		inst.append(new IRInstruction(IRType.brz, temp, label_end));
		inst.append(new IRInstruction(IRType.label, label_loop));
		inst.append(compileInstructions(stat.getBody()));
		inst.append(new IRInstruction(IRType.br, label_next));
		inst.append(new IRInstruction(IRType.label, label_end));
		
		return inst;
	}
	
	private IRInstruction createForInstructions(ForStat stat) {
		IRInstruction inst = new IRInstruction();
		
		LabelParam label_next = new LabelParam("for.next");
		LabelParam label_loop = new LabelParam("for.loop");
		LabelParam label_end = new LabelParam("for.end");
		
		if(stat.getVariables() != null) {
			inst.append(compileInstructions(stat.getVariables()));
		}
		
		if(stat.getAction() != null) {
			// ============================== //
			// for(x; y; z) { ... }
			//   ; Define x
			//
			//   check = y;
			//   brz [check], end
			//   br loop
			// next:
			//   check = y;
			//   brz [check], end
			//   ; Calculate action
			// loop:
			//   ; ...
			//   br [next]
			// end:
			
			if(stat.getCondition() != null) {
				Param temp = IRInstruction.temp(stat.getCondition().size());
				inst.append(compileInstructions(stat.getCondition(), temp));
				inst.append(new IRInstruction(IRType.brz, temp, label_end));
				inst.append(new IRInstruction(IRType.br, label_loop));
			}
			
			inst.append(new IRInstruction(IRType.label, label_next));
			if(stat.getCondition() != null) {
				Param temp = IRInstruction.temp(stat.getCondition().size());
				inst.append(compileInstructions(stat.getCondition(), temp));
				inst.append(new IRInstruction(IRType.brz, temp, label_end));
			}
			

			inst.append(compileInstructions(stat.getAction()));
			inst.append(new IRInstruction(IRType.label, label_loop));
		} else {
			// ============================== //
			// for(x; y; ) { ... }
			//   ; Define x
			// next:
			//   check = y;
			//   brz [check], [end]
			//   ; ...
			//   br [next]
			// end:
			
			inst.append(new IRInstruction(IRType.label, label_next));
			if(stat.getCondition() != null) {
				Param temp = IRInstruction.temp(stat.getCondition().size());
				inst.append(compileInstructions(stat.getCondition(), temp));
				inst.append(new IRInstruction(IRType.brz, temp, label_end));
			}
		}
		
		inst.append(compileInstructions(stat.getBody()));
		inst.append(new IRInstruction(IRType.br, label_next));
		inst.append(new IRInstruction(IRType.label, label_end));
		
		return inst;
	}
	
	private IRInstruction createExprInstructions(ExprStat stat) {
		IRInstruction inst = new IRInstruction();
		
		if(stat.list.isEmpty()) return inst;
		inst.append(compileInstructions(stat.list.get(0)));
		
		for(int i = 1; i < stat.list.size(); i++) {
			inst.append(compileInstructions(stat.list.get(i)));
		}
		
		return inst;
	}
	
	private IRInstruction compileInstructions(Expression expr, Param request) {
		IRInstruction inst = createInstructions(expr, request);
		return inst == null ? new IRInstruction():inst;
	}
	
	private IRInstruction compileInstructions(Expression expr) {
		IRInstruction inst = createInstructions(expr, null);
		return inst == null ? new IRInstruction():inst;
	}
	
	private IRInstruction compileInstructions(Statement stat) {
		IRInstruction inst = _createInstructions(stat);
		return inst == null ? new IRInstruction():inst;
	}
	
	private IRInstruction _createInstructions(Statement stat) {
		if(stat == null || stat == Statement.EMPTY) return null;
		
		if(stat instanceof IfStat) {
			return createIfInstructions((IfStat)stat);
		} else if(stat instanceof ForStat) {
			return createForInstructions((ForStat)stat);
		} else if(stat instanceof WhileStat) {
			return createWhileInstructions((WhileStat)stat);
		} else if(stat instanceof ExprStat) {
			return createExprInstructions((ExprStat)stat);
		}
		
		IRInstruction inst = new IRInstruction(IRType.nop, new DebugParam(stat.toString()));
		
		if(stat.hasStatements()) {
			IRInstruction point = null;
			
			for(Statement s : stat.getStatements()) {
				if(point == null) {
					point = compileInstructions(s);
					continue;
				}
				
				point.append(compileInstructions(s));
			}
			
			inst = point;
		}
		
//		Utils.execute_for_all_statements(func, (parent, index, function) -> {
//			System.out.println("[" + index + "] " + parent.get(index));
//		});
		
		return inst.last();
	}
}
