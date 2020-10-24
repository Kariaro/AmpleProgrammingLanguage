package hardcoded.compiler.instruction;

import static hardcoded.compiler.constants.ExprType.*;

import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;

import hardcoded.compiler.Block;
import hardcoded.compiler.Block.Function;
import hardcoded.compiler.Identifier;
import hardcoded.compiler.Identifier.IdType;
import hardcoded.compiler.constants.ExprType;
import hardcoded.compiler.Program;
import hardcoded.compiler.expression.AtomExpr;
import hardcoded.compiler.expression.Expression;
import hardcoded.compiler.expression.LowType;
import hardcoded.compiler.instruction.IRInstruction;
import hardcoded.compiler.instruction.IRInstruction.*;
import hardcoded.compiler.statement.*;

/**
 * IR is short for 'Intermediate representation'
 * 
 * @author HardCoded
 */
public class IntermediateCodeGenerator {
	private final Map<String, Param> variables = new HashMap<>();
	private final AtomicInteger atomic = new AtomicInteger();
	
	private Param temp(LowType type) {
		return new Reg(type, atomic.getAndIncrement());
	}
	
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
			atomic.set(0);
			
			program.addFunction(func, compileInstructions(func.body));
		}
		
		return program;
	}
	
	private boolean shouldCheck(Expression e) {
		if(e.type() == ExprType.nop) return false;
		
		return !(e instanceof AtomExpr);
	}
	
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
					next = temp(size);
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
	
	private List<IRInstruction> createInstructions(Expression expr, Param request) {
		List<IRInstruction> list = new ArrayList<>();
		list.add(new IRInstruction(IRType.nop, new DebugParam(expr.toString())));
		
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
							reg_0 = temp(a.size());
							list.addAll(compileInstructions(a.first(), reg_0));
						} else {
							reg_0 = createObject(f);
						}
						
						pointer = true;
					} else {
						reg_0 = temp(a.size());
						list.addAll(compileInstructions(a, reg_0));
					}
				}
				
				if(shouldCheck(b)) {
					reg_1 = temp(b.size());
					list.addAll(compileInstructions(b, reg_1));
				}
				
				IRType action = pointer ? IRType.write:IRType.mov;
				list.add(new IRInstruction(action, reg_0, reg_1));
				
				if(request != null) {
					list.add(new IRInstruction(action, request, reg_1));
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
					reg_0 = temp(a.size());
					list.addAll(compileInstructions(a, reg_0));
				}
				
				if(shouldCheck(b)) {
					reg_1 = temp(b.size());
					list.addAll(compileInstructions(b, reg_1));
				}
				
				// TODO: If this is calculated without having a value to write to we should not do this operation?
				if(request != null) {
					list.add(new IRInstruction(IRType.convert(expr.type()), request, reg_0, reg_1));
				} else {
					System.out.println("Operation?!?!?!?" + a + ", " + b);
				}
				
				break;
			}
			
			case ret: {
				Expression a = expr.first();
				
				Param reg_0 = createObject(a);
				if(shouldCheck(a)) {
					reg_0 = temp(a.size());
					list.addAll(compileInstructions(a, reg_0));
				}
				
				list.add(new IRInstruction(IRType.ret, reg_0));
				break;
			}
			
			case neg:
			case not:
			case nor: {
				Expression a = expr.first();
				
				Param reg_0 = createObject(a);
				if(shouldCheck(a)) {
					reg_0 = temp(a.size());
					list.addAll(compileInstructions(a, reg_0));
				}
				
				list.add(new IRInstruction(IRType.convert(expr.type()), request, reg_0));
				break;
			}
			
			case addptr: {
				// TODO: ?? Remember that we need to write and not mov !!!!
				
				Expression a = expr.first();
				
				Param reg_0 = createObject(a);
				if(shouldCheck(a)) {
					reg_0 = temp(a.size());
					list.addAll(compileInstructions(a, reg_0));
				}
				
				list.add(new IRInstruction(IRType.mov, request, reg_0));
				break;
			}
			
			case decptr: {
				Expression a = expr.first();
				
				Param reg_0 = createObject(a);
				if(shouldCheck(a)) {
					reg_0 = temp(a.size());
					list.addAll(compileInstructions(a, reg_0));
				}
				
				LowType next = reg_0.getSize().nextLowerPointer();
				
				if(!next.equals(request)) {
					Param temp = temp(next);
					list.add(new IRInstruction(IRType.read, temp, reg_0));
					list.add(new IRInstruction(IRType.mov, request, temp));
				} else {
					// System.out.println("Read instruction: " + request + ":" + request.getSize() + " / " + reg_0 + ":" + reg_0.getSize());
					list.add(new IRInstruction(IRType.read, request, reg_0));
				}
				
				break;
			}
			
			case comma: {
				for(int i = 0; i < expr.length() - 1; i++) {
					list.addAll(compileInstructions(expr.get(i)));
				}
				
				if(request != null) {
					if(expr.last().hasSideEffects()) {
						list.addAll(compileInstructions(expr.last(), request));
					} else {
						list.add(new IRInstruction(IRType.mov, request, createObject(expr.last())));
					}
				} else {
					list.addAll(compileInstructions(expr.last()));
				}
				
				break;
			}
			
			case add: case sub: {
				IRType type = IRType.convert(expr.type());
				Expression a = expr.get(0), b = expr.get(1);
				
				Param reg_0 = createObject(a);
				if(shouldCheck(a)) {
					reg_0 = temp(a.size());
					list.addAll(compileInstructions(a, reg_0));
				}

				Param reg_1 = createObject(b);
				if(shouldCheck(b)) {
					reg_1 = temp(b.size());
					list.addAll(compileInstructions(b, reg_1));
				}
				
				if(expr.length() < 3) {
					list.add(new IRInstruction(type, request, reg_0, reg_1));
					break;
				}
				
				Param reg_2 = temp(reg_0.getSize());
				list.add(new IRInstruction(type, reg_2, reg_0, reg_1));
				
				for(int i = 2; i < expr.length(); i++) {
					b = expr.get(i);
					reg_1 = createObject(b);
					if(shouldCheck(b)) {
						reg_1 = temp(b.size());
						list.addAll(compileInstructions(b, reg_1));
					}
					
					reg_0 = reg_2;
					if(i == expr.length() - 1) {
						reg_2 = request;
					} else {
						reg_2 = temp(b.size());
					}
					
					list.add(new IRInstruction(type, reg_2, reg_0, reg_1));
				}
				
				break;
			}
			
			case call: {
				List<Param> params = new ArrayList<>();
				
				Function func = null;
				{
					// Called function
					Expression e = expr.first();
					
					if(e instanceof AtomExpr) {
						AtomExpr a = (AtomExpr)e;
						
						if(a.isIdentifier()) {
							func = a.d_value.func();
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
						reg = temp(func.arguments.get(i - 1).low_type());//e.size());
						
						// System.out.println("Call param: " + reg + ":" + reg.getSize());
						list.addAll(compileInstructions(e, reg));
					}
					
					params.add(reg);
				}
				
				if(request == null) {
					params.add(0, IRInstruction.NONE);
				} else {
					params.add(0, request);
				}
				
				list.add(new IRInstruction(IRType.call, params.toArray(new Param[0])));
				break;
			}
			
			case cand: {
				LabelParam label_end = new LabelParam("cand.end");
				if(request != null) list.add(new IRInstruction(IRType.mov, request, new NumberReg(0, request.getSize())));
				
				for(int i = 0; i < expr.length(); i++) {
					Expression e = expr.get(i);
					Param reg = createObject(e);
					
					if(shouldCheck(e)) {
						reg = temp(e.size());
						list.addAll(compileInstructions(e, reg));
					}
					
					list.add(new IRInstruction(IRType.brz, reg, label_end));
				}
				
				if(request != null) list.add(new IRInstruction(IRType.mov, request, new NumberReg(1, request.getSize())));
				list.add(new IRInstruction(IRType.label, label_end));
				break;
			}
			
			case cor: {
				LabelParam label_end = new LabelParam("cor.end");
				if(request != null) list.add(new IRInstruction(IRType.mov, request, new NumberReg(1, request.getSize())));
				
				for(int i = 0; i < expr.length(); i++) {
					Expression e = expr.get(i);
					Param reg = createObject(e);
					
					if(shouldCheck(e)) {
						reg = temp(reg.getSize());
						list.addAll(compileInstructions(e, reg));
					}
					
					list.add(new IRInstruction(IRType.bnz, reg, label_end));
				}

				if(request != null) list.add(new IRInstruction(IRType.mov, request, new NumberReg(0, request.getSize())));
				list.add(new IRInstruction(IRType.label, label_end));
				break;
			}
			
			default: {
				if(expr instanceof AtomExpr) {
					if(request != null) {
						list.add(new IRInstruction(IRType.mov, request, createObject(expr)));
					} else {
						// If we end up here then a instruction was executed that does not affect
						// any memory or other register.
						// 
						// It's safe to ignore this instruction because it does not do anything.
					}
					
					break;
				}
				
				System.err.println("[MISSING INSTRUCTION] -> " + expr);
			}
		}
		
		// This checks if we have modified the instruction.
		// If we did modify the instruction we should remove
		// the first entry because it's not needed anymore.
		if(list.size() > 1) {
			list.remove(0); // Remove nop instruction
			// return inst.first().remove();
		}
		
		return list;
	}
	
	private List<IRInstruction> createIfInstructions(IfStat stat) {
		List<IRInstruction> list = new ArrayList<>();
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
			
			Param temp = temp(stat.getCondition().size());
			list.addAll(compileInstructions(stat.getCondition(), temp));
			list.add(new IRInstruction(IRType.brz, temp, label_else));
			list.addAll(compileInstructions(stat.getBody()));
			list.add(new IRInstruction(IRType.br, label_end));
			list.add(new IRInstruction(IRType.label, label_else));
			list.addAll(compileInstructions(stat.getElseBody()));
			list.add(new IRInstruction(IRType.label, label_end));
		} else {
			// ============================== //
			// if(x) { ... }
			
			// brz [end] [x]				Branch to [end] if [x] is zero
			//    ...
			// end:

			Param temp = temp(stat.getCondition().size());
			list.addAll(compileInstructions(stat.getCondition(), temp));
			list.add(new IRInstruction(IRType.brz, temp, label_end));
			list.addAll(compileInstructions(stat.getBody()));
			list.add(new IRInstruction(IRType.label, label_end));
		}
		
		return list;
	}
	
	private List<IRInstruction> createWhileInstructions(WhileStat stat) {
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
		List<IRInstruction> list = new ArrayList<>();
		
		Param temp = temp(stat.getCondition().size());
		
		list.add(new IRInstruction(IRType.label, label_next));
		list.addAll(compileInstructions(stat.getCondition(), temp));
		list.add(new IRInstruction(IRType.brz, temp, label_end));
		list.add(new IRInstruction(IRType.label, label_loop));
		list.addAll(compileInstructions(stat.getBody()));
		list.add(new IRInstruction(IRType.br, label_next));
		list.add(new IRInstruction(IRType.label, label_end));
		
		return list;
	}
	
	private List<IRInstruction> createForInstructions(ForStat stat) {
		List<IRInstruction> list = new ArrayList<>();
		
		LabelParam label_next = new LabelParam("for.next");
		LabelParam label_loop = new LabelParam("for.loop");
		LabelParam label_end = new LabelParam("for.end");
		
		if(stat.getVariables() != null) {
			list.addAll(compileInstructions(stat.getVariables()));
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
			//   ; Calculate action
			//   check = y;
			//   brz [check], end
			// loop:
			//   ; ...
			//   br [next]
			// end:
			
			if(stat.getCondition() != null) {
				Param temp = temp(stat.getCondition().size());
				list.addAll(compileInstructions(stat.getCondition(), temp));
				list.add(new IRInstruction(IRType.brz, temp, label_end));
				list.add(new IRInstruction(IRType.br, label_loop));
			}
			
			list.add(new IRInstruction(IRType.label, label_next));
			list.addAll(compileInstructions(stat.getAction()));
			if(stat.getCondition() != null) {
				Param temp = temp(stat.getCondition().size());
				list.addAll(compileInstructions(stat.getCondition(), temp));
				list.add(new IRInstruction(IRType.brz, temp, label_end));
			}
			
			list.add(new IRInstruction(IRType.label, label_loop));
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
			
			list.add(new IRInstruction(IRType.label, label_next));
			if(stat.getCondition() != null) {
				Param temp = temp(stat.getCondition().size());
				list.addAll(compileInstructions(stat.getCondition(), temp));
				list.add(new IRInstruction(IRType.brz, temp, label_end));
			}
		}
		
		list.addAll(compileInstructions(stat.getBody()));
		list.add(new IRInstruction(IRType.br, label_next));
		list.add(new IRInstruction(IRType.label, label_end));
		
		return list;
	}
	
	private List<IRInstruction> createExprInstructions(ExprStat stat) {
		List<IRInstruction> list = new ArrayList<>();
		
		if(stat.list.isEmpty()) return list;
		list.addAll(compileInstructions(stat.list.get(0)));
		
		for(int i = 1; i < stat.list.size(); i++) {
			list.addAll(compileInstructions(stat.list.get(i)));
		}
		
		return list;
	}
	
	private List<IRInstruction> compileInstructions(Expression expr, Param request) {
		List<IRInstruction> list = createInstructions(expr, request);
		return list == null ? new ArrayList<>():list;
	}
	
	private List<IRInstruction> compileInstructions(Expression expr) {
		List<IRInstruction> list = createInstructions(expr, null);
		return list == null ? new ArrayList<>():list;
	}
	
	private List<IRInstruction> compileInstructions(Statement stat) {
		List<IRInstruction> list = _createInstructions(stat);
		return list == null ? new ArrayList<>():list;
	}
	
	private List<IRInstruction> _createInstructions(Statement stat) {
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
		
		List<IRInstruction> list = new ArrayList<>();
		list.add(new IRInstruction(IRType.nop, new DebugParam(stat.toString())));
		
		if(stat.hasStatements()) {
			list.clear();
			for(Statement s : stat.getStatements()) {
				list.addAll(compileInstructions(s));
			}
		}
		
		return list;
	}
}
