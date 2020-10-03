package hardcoded.compiler.instruction;

import static hardcoded.compiler.Expression.ExprType.*;

import java.util.*;

import hardcoded.compiler.*;
import hardcoded.compiler.Block.Function;
import hardcoded.compiler.Expression.AtomExpr;
import hardcoded.compiler.Identifier.IdType;
import hardcoded.compiler.Statement.*;
import hardcoded.compiler.constants.AtomType;
import hardcoded.compiler.constants.IRInsts;
import hardcoded.compiler.constants.Primitives;
import hardcoded.compiler.instruction.IRInstruction.*;

/**
 * IR is short for 'Intermediate representation'
 * 
 * @author HardCoded
 */
public class IntermediateCodeGenerator {
	// TODO: Default pointer size is always long.
	
	public IntermediateCodeGenerator() {
		
	}
	
	private List<InstructionBlock> blocks;
	private InstructionBlock strings;
	
	public List<InstructionBlock> generate(Program program) {
		blocks = new ArrayList<>();
		
		for(int i = 0; i < program.size(); i++) {
			Block block = program.get(i);
			
			if(!(block instanceof Function)) continue;
			Function func = (Function)block;
			variables.clear();
			
			IRInstruction.reset_counter();
			IRInstruction inst = compileInstructions(func, func.body).first();
			InstructionBlock b = new InstructionBlock(func, inst);
			
			{
				String str = "";
				for(int j = 0; j < func.arguments.size(); j++) {
					Identifier id = func.arguments.get(j);
					str += ", " + id.atomType();
					
				}
				
				if(!str.isEmpty()) {
					b.extra = str.substring(2);
				}
			}
			
			blocks.add(b);
		}
		
//		for(InstructionBlock block : blocks) {
//			Instruction inst = block.start;
//			
//			System.out.println("\n" + block.returnType + ", " + block.name);
//			
//			int count = 0;
//			while(inst != null) {
//				int idx = count++;
//				if(inst.op == Insts.label) System.out.println();
//				System.out.printf("%4d: ", idx);
//				
//				if(inst.op != Insts.label) System.out.print("  ");
//				
//				System.out.printf("%s\n", inst);
//				inst = inst.next();
//			}
//		}
//		
		return blocks;
	}
	
	private boolean shouldCheck(Expression e) {
		return !(e instanceof AtomExpr);
	}
	
	private Map<String, Reg> variables = new HashMap<>();
	private Reg addString(AtomExpr a) {
		if(strings == null) {
			strings = new InstructionBlock(".strings", new IRInstruction(IRInsts.data, new ObjectReg(a)));
			strings.returnType = Primitives.VOID;
			
			blocks.add(0, strings);
			return new RefReg(".strings", 0);
		}
		
		int counter = 0;
		{
			IRInstruction inst = strings.start;
			for(IRInstruction i : inst) {
				ObjectReg reg = (ObjectReg)i.params.get(0);
				
				if(reg.toString().equals(a.toString())) {
					return new RefReg(".strings", counter);
				}
				
				counter++;
			}
		}
		
		strings.start.append(new IRInstruction(IRInsts.data, new ObjectReg(a)));
		return new RefReg(".strings", counter);
	}
	
	private Reg createObject(Expression e) {
		if(e.type() == atom) {
			AtomExpr a = (AtomExpr)e;
			
			if(a.isString()) {
				return addString(a);
			}
			
			if(a.isIdentifier()) {
				Identifier ident = a.d_value;
				String name = ident.name();

				// System.out.println(ident + ", " + ident.atomType());
				if(variables.containsKey(name)) return variables.get(name);
				AtomType size = e.calculateSize();
				
//				if(true) {
//					NamedReg next = new NamedReg(name);
//					next.size = size;
//					variables.put(name, next);
//					return next;
//				}
				
				Reg next;
				if(ident.idtype() == IdType.param) {
					next = new NamedReg("@" + ident.name());
					next.size = ident.atomType();
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
		
		// System.out.println("Creating object reg ? " + e);
		return new ObjectReg(e.clone());
	}
	
	private IRInstruction createInstructions(Function func, Expression expr, Reg request) {
		IRInstruction inst = new IRInstruction(IRInsts.nop, new ObjectReg(expr.toString()));
		
		switch(expr.type()) {
			case set: {
				Expression a = expr.first(), b = expr.last();
				
				Reg reg_0 = createObject(a);
				Reg reg_1 = createObject(b);
				boolean pointer = false;
				if(shouldCheck(a)) {
					if(a.type() == decptr) {
						pointer = true;
						
						Expression f = a.first();
						
						if(shouldCheck(f)) {
							reg_0 = IRInstruction.temp(a.calculateSize());
							inst.append(compileInstructions(func, a.first(), reg_0));
						} else {
							reg_0 = createObject(f);
						}
						
						pointer = true;
					} else {
						reg_0 = IRInstruction.temp(a.calculateSize());
						inst.append(compileInstructions(func, a, reg_0));
					}
				}
				
				if(shouldCheck(b)) {
					reg_1 = IRInstruction.temp(b.calculateSize());
					inst.append(compileInstructions(func, b, reg_1));
				}
				
				IRInsts action = pointer ? IRInsts.write:IRInsts.mov;
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
			case lt: case lte:
			case gt: case gte:
			case eq: case neq:
			case or: case and:
			case xor: {
				Expression a = expr.first(), b = expr.last();
				
				Reg reg_0 = createObject(a);
				Reg reg_1 = createObject(b);
				if(shouldCheck(a)) {
					reg_0 = IRInstruction.temp(reg_0.size);
					inst.append(compileInstructions(func, a, reg_0));
				}
				
				if(shouldCheck(b)) {
					reg_1 = IRInstruction.temp(reg_1.size);
					inst.append(compileInstructions(func, b, reg_1));
				}
				
				inst.append(new IRInstruction(IRInsts.convert(expr.type()), IRInstruction.temp(request), reg_0, reg_1));
				break;
			}
			
			case ret: {
				Expression a = expr.first();
				
				Reg reg_0 = createObject(a);
				if(shouldCheck(a)) {
					reg_0 = IRInstruction.temp();
					inst.append(compileInstructions(func, a, reg_0));
				}
				
				inst.append(new IRInstruction(IRInsts.ret, reg_0));
				break;
			}
			
			case neg:
			case not:
			case nor: {
				Expression a = expr.first();
				
				Reg reg_0 = createObject(a);
				if(shouldCheck(a)) {
					reg_0 = IRInstruction.temp();
					inst.append(compileInstructions(func, a, reg_0));
				}
				
				inst.append(new IRInstruction(IRInsts.convert(expr.type()), IRInstruction.temp(request), reg_0));
				break;
			}
			
			case addptr: {
				// TODO: ?? Remember that we need to write and not mov !!!!
				
				Expression a = expr.first();
				
				Reg reg_0 = createObject(a);
				if(shouldCheck(a)) {
					reg_0 = IRInstruction.temp();
					inst.append(compileInstructions(func, a, reg_0));
				}
				
				inst.append(new IRInstruction(IRInsts.mov, IRInstruction.temp(request), reg_0));
				break;
			}
			
			case decptr: {
				Expression a = expr.first();
				
				Reg reg_0 = createObject(a);
				if(shouldCheck(a)) {
					reg_0 = IRInstruction.temp(a.calculateSize());
					inst.append(compileInstructions(func, a, reg_0));
				}
				
				// TODO: Reading from a Identifier should hold the primitive size and not the pointer size.
				
				if(request.size == null && reg_0.size != null) {
					if(reg_0.size.isPointer())
						request.size = AtomType.getPointer(reg_0.size, -1);
					else
						request.size = reg_0.size;
				}
				
				inst.append(new IRInstruction(IRInsts.read, IRInstruction.temp(request), reg_0));
				break;
			}
			
			case comma: {
				for(int i = 0; i < expr.size() - 1; i++) {
					inst.append(compileInstructions(func, expr.get(i)));
				}
				
				if(request != null) {
					if(expr.last().hasSideEffects()) {
						inst.append(compileInstructions(func, expr.last(), request));
					} else {
						inst.append(new IRInstruction(IRInsts.mov, request, createObject(expr.last())));
					}
				} else {
					inst.append(compileInstructions(func, expr.last()));
				}
				
				break;
			}
			
			case add: case sub: {
				IRInsts type = IRInsts.convert(expr.type());
				Expression a = expr.get(0), b = expr.get(1);
				
				Reg reg_0 = createObject(a);
				if(shouldCheck(a)) {
					reg_0 = IRInstruction.temp(a.calculateSize());
					inst.append(compileInstructions(func, a, reg_0));
				}

				Reg reg_1 = createObject(b);
				if(shouldCheck(b)) {
					reg_1 = IRInstruction.temp(b.calculateSize());
					inst.append(compileInstructions(func, b, reg_1));
				}
				
				if(expr.size() > 2) {
					// ????
				} else {
					inst.append(new IRInstruction(type, IRInstruction.temp(request), reg_0, reg_1));
					break;
				}
				
				Reg reg_2 = IRInstruction.temp();
				inst.append(new IRInstruction(type, reg_2, reg_0, reg_1));
				
				for(int i = 2; i < expr.size(); i++) {
					b = expr.get(i);
					reg_1 = createObject(b);
					if(shouldCheck(b)) {
						reg_1 = IRInstruction.temp(b.calculateSize());
						inst.append(compileInstructions(func, b, reg_1));
					}
					
					reg_0 = reg_2;
					if(i == expr.size() - 1) {
						reg_2 = IRInstruction.temp(request);
					} else {
						reg_2 = IRInstruction.temp(b.calculateSize());
					}
					
					inst.append(new IRInstruction(type, reg_2, reg_0, reg_1));
				}
				
				break;
			}
			
			case call: {
				List<Reg> params = new ArrayList<>();
				
				{
					// Called function
					Expression e = expr.first();
					
					if(e instanceof AtomExpr) {
						AtomExpr a = (AtomExpr)e;
						
						if(a.isIdentifier()) {
							Identifier ident = a.d_value;
							

							params.add(new IRInstruction.CallReg(ident.atomType(), e.toString()));
						}
					} else {
						params.add(new IRInstruction.CallReg(null, e.toString()));
					}
				}
				
				for(int i = 1; i < expr.size(); i++) {
					Expression e = expr.get(i);
					Reg reg = createObject(e);
					
					if(shouldCheck(e)) {
						reg = IRInstruction.temp();
						inst.append(compileInstructions(func, e, reg));
					}
					
					params.add(reg);
				}
				
				if(request == null) {
					params.add(0, IRInstruction.NONE);
				} else {
					params.add(0, IRInstruction.temp(request));
				}
				
				inst.append(new IRInstruction(IRInsts.call, params.toArray(new Reg[0])));
				break;
			}
			
			case cand: {
				Label label_end = new Label("cand.end");
				if(request != null) inst.append(new IRInstruction(IRInsts.mov, request, new NumberReg(0)));
				
				for(int i = 0; i < expr.size(); i++) {
					Expression e = expr.get(i);
					Reg reg = createObject(e);
					
					if(shouldCheck(e)) {
						reg = IRInstruction.temp(e.calculateSize());
						inst.append(compileInstructions(func, e, reg));
					}
					
					inst.append(new IRInstruction(IRInsts.brz, reg, label_end));
				}

				if(request != null) inst.append(new IRInstruction(IRInsts.mov, request, new NumberReg(1)));
				inst.append(new IRInstruction(IRInsts.label, label_end));
				break;
			}
			
			case cor: {
				Label label_end = new Label("cor.end");
				if(request != null) inst.append(new IRInstruction(IRInsts.mov, request, new NumberReg(1)));
				
				for(int i = 0; i < expr.size(); i++) {
					Expression e = expr.get(i);
					Reg reg = createObject(e);
					
					if(shouldCheck(e)) {
						reg = IRInstruction.temp();
						inst.append(compileInstructions(func, e, reg));
					}
					
					inst.append(new IRInstruction(IRInsts.bnz, reg, label_end));
				}

				if(request != null) inst.append(new IRInstruction(IRInsts.mov, request, new NumberReg(0)));
				inst.append(new IRInstruction(IRInsts.label, label_end));
				break;
			}
			
			default: {
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
	
	private IRInstruction createIfInstructions(Function func, IfStat stat) {
		IRInstruction inst;
		
		Label label_end = new Label("if.end");
		
		if(stat.hasElseBody()) {
			// ============================== //
			// if(x) { ... } else { ... }
			
			// brz [else] [x]				Branch to [else] if [x] is zero
			//    ...
			// br [end]						Branch to [end]
			// else:
			//    ...
			// end:
			
			Label label_else = new Label("if.else");
			
			Reg temp = IRInstruction.temp(stat.condition().calculateSize());
			inst = compileInstructions(func, stat.condition(), temp);
			inst.append(new IRInstruction(IRInsts.brz, temp, label_else));
			inst.append(compileInstructions(func, stat.body()));
			inst.append(new IRInstruction(IRInsts.br, label_end));
			inst.append(new IRInstruction(IRInsts.label, label_else));
			inst.append(compileInstructions(func, stat.elseBody()));
			inst.append(new IRInstruction(IRInsts.label, label_end));
		} else {
			// ============================== //
			// if(x) { ... }
			
			// brz [end] [x]				Branch to [end] if [x] is zero
			//    ...
			// end:

			Reg temp = IRInstruction.temp(stat.condition().calculateSize());
			inst = compileInstructions(func, stat.condition(), temp);
			inst.append(new IRInstruction(IRInsts.brz, temp, label_end));
			inst.append(compileInstructions(func, stat.body()));
			inst.append(new IRInstruction(IRInsts.label, label_end));
		}
		
		return inst;
	}
	
	private IRInstruction createWhileInstructions(Function func, WhileStat stat) {
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
		
		Label label_next = new Label("while.next");
		Label label_loop = new Label("while.loop");
		Label label_end = new Label("while.end");

		IRInstruction inst = new IRInstruction(IRInsts.label, label_next);
		Reg temp = IRInstruction.temp(stat.condition().calculateSize());
		inst.append(compileInstructions(func, stat.condition(), temp));
		inst.append(new IRInstruction(IRInsts.brz, temp, label_end));
		inst.append(new IRInstruction(IRInsts.label, label_loop));
		inst.append(compileInstructions(func, stat.body()));
		inst.append(new IRInstruction(IRInsts.br, label_next));
		inst.append(new IRInstruction(IRInsts.label, label_end));
		
		return inst;
	}
	
	private IRInstruction createForInstructions(Function func, ForStat stat) {
		IRInstruction inst = new IRInstruction();
		
		Label label_next = new Label("for.next");
		Label label_loop = new Label("for.loop");
		Label label_end = new Label("for.end");
		
		if(stat.variables() != null) {
			inst.append(compileInstructions(func, stat.variables()));
		}
		
		if(stat.action() != null) {
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
			
			if(stat.condition() != null) {
				Reg temp = IRInstruction.temp(stat.condition().calculateSize());
				inst.append(compileInstructions(func, stat.condition(), temp));
				inst.append(new IRInstruction(IRInsts.brz, temp, label_end));
				inst.append(new IRInstruction(IRInsts.br, label_loop));
			}
			
			inst.append(new IRInstruction(IRInsts.label, label_next));
			if(stat.condition() != null) {
				Reg temp = IRInstruction.temp(stat.condition().calculateSize());
				inst.append(compileInstructions(func, stat.condition(), temp));
				inst.append(new IRInstruction(IRInsts.brz, temp, label_end));
			}
			

			inst.append(compileInstructions(func, stat.action()));
			inst.append(new IRInstruction(IRInsts.label, label_loop));
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
			
			inst.append(new IRInstruction(IRInsts.label, label_next));
			if(stat.condition() != null) {
				Reg temp = IRInstruction.temp(stat.condition().calculateSize());
				inst.append(compileInstructions(func, stat.condition(), temp));
				inst.append(new IRInstruction(IRInsts.brz, temp, label_end));
			}
		}
		
		inst.append(compileInstructions(func, stat.body()));
		inst.append(new IRInstruction(IRInsts.br, label_next));
		inst.append(new IRInstruction(IRInsts.label, label_end));
		
		return inst;
	}
	
	private IRInstruction createExprInstructions(Function func, ExprStat stat) {
		IRInstruction inst = new IRInstruction();
		
		if(stat.list.isEmpty()) return inst;
		inst.append(compileInstructions(func, stat.list.get(0)));
		
		for(int i = 1; i < stat.list.size(); i++) {
			inst.append(compileInstructions(func, stat.list.get(i)));
		}
		
		return inst;
	}
	
	private IRInstruction compileInstructions(Function func, Expression expr, Reg request) {
		IRInstruction inst = createInstructions(func, expr, request);
		return inst == null ? new IRInstruction():inst;
	}
	
	private IRInstruction compileInstructions(Function func, Expression expr) {
		IRInstruction inst = createInstructions(func, expr, null);
		return inst == null ? new IRInstruction():inst;
	}
	
	private IRInstruction compileInstructions(Function func, Statement stat) {
		IRInstruction inst = _createInstructions(func, stat);
		return inst == null ? new IRInstruction():inst;
	}
	
	private IRInstruction _createInstructions(Function func, Statement stat) {
		if(stat == null || stat == Statement.EMPTY) return null;
		
		if(stat instanceof IfStat) {
			return createIfInstructions(func, (IfStat)stat);
		} else if(stat instanceof ForStat) {
			return createForInstructions(func, (ForStat)stat);
		}else if(stat instanceof WhileStat) {
			return createWhileInstructions(func, (WhileStat)stat);
		} else if(stat instanceof ExprStat) {
			return createExprInstructions(func, (ExprStat)stat);
		}
		
		IRInstruction inst = new IRInstruction(IRInsts.nop, new ObjectReg(stat.toString()));
		
		if(stat.hasStatements()) {
			IRInstruction point = null;
			
			for(Statement s : stat.getStatements()) {
				if(point == null) {
					point = compileInstructions(func, s);
					continue;
				}
				
				point.append(compileInstructions(func, s));
			}
			
			inst = point;
		}
		
//		Utils.execute_for_all_statements(func, (parent, index, function) -> {
//			System.out.println("[" + index + "] " + parent.get(index));
//		});
		
		return inst.last();
	}
}
