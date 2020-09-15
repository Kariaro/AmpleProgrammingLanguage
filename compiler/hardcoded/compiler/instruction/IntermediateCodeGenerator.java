package hardcoded.compiler.instruction;

import static hardcoded.compiler.Expression.ExprType.*;

import java.util.*;

import hardcoded.compiler.*;
import hardcoded.compiler.Block.Function;
import hardcoded.compiler.Expression.AtomExpr;
import hardcoded.compiler.Identifier.IdType;
import hardcoded.compiler.Statement.*;
import hardcoded.compiler.constants.AtomType;
import hardcoded.compiler.constants.Insts;
import hardcoded.compiler.constants.Primitives;
import hardcoded.compiler.instruction.Instruction.*;

/**
 * IR is short for 'Intermediate representation'
 * 
 * @author HardCoded
 */
public class IntermediateCodeGenerator {
	// TODO: Default pointer size is always int.
	
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
			
			Instruction inst = compileInstructions(func, func.body).first();
			blocks.add(new InstructionBlock(func, inst));
		}
		
		return blocks;
	}
	
	private boolean shouldCheck(Expression e) {
		return !(e instanceof AtomExpr);
	}
	
	private Instruction createInstructions(Function func, Expression expr) {
		return createInstructions(func, expr, null);
	}
	
	private Map<String, Reg> variables = new HashMap<>();
	private Reg addString(AtomExpr a) {
		if(strings == null) {
			strings = new InstructionBlock(".strings", new Instruction(Insts.data, new ObjectReg(a)));
			strings.returnType = Primitives.VOID;
			
			blocks.add(0, strings);
			return new RefReg(".strings", 0);
		}
		
		int counter = 0;
		{
			Instruction i = strings.start;
			while(i != null) {
				ObjectReg reg = (ObjectReg)i.params.get(0);
				
				if(reg.toString().equals(a.toString())) {
					return new RefReg(".strings", counter);
				}
				
				counter++;
				i = i.next;
			}
		}
		
		strings.start.append(new Instruction(Insts.data, new ObjectReg(a)));
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
				
				Reg next = Instruction.temp(size);
				if(ident.idtype() == IdType.param) next.index += 10000;
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
	
	private Instruction createInstructions(Function func, Expression expr, Reg request) {
		Instruction inst = new Instruction(Insts.nop, new ObjectReg(expr.toString()));
		
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
							reg_0 = Instruction.temp(a.calculateSize());
							inst = inst.append(createInstructions(func, a.first(), reg_0));
						} else {
							reg_0 = createObject(f); // NOTE: new ObjectReg(f);
						}
						
						pointer = true;
					} else {
						reg_0 = Instruction.temp(a.calculateSize());
						inst = inst.append(createInstructions(func, a, reg_0));
					}
				}
				
				if(shouldCheck(b)) {
					reg_1 = Instruction.temp(b.calculateSize());
					inst = inst.append(createInstructions(func, b, reg_1));
				}
				
				Insts action = pointer ? Insts.write:Insts.mov;
				if(!inst.hasNeighbours()) {
					inst = new Instruction(action, reg_0, reg_1);
				} else {
					inst = inst.append(new Instruction(action, reg_0, reg_1));
				}
				
				if(request != null) {
					inst = inst.append(new Instruction(action, request, reg_1));
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
					reg_0 = Instruction.temp(reg_0.size);
					inst = inst.append(createInstructions(func, a, reg_0));
				}
				
				if(shouldCheck(b)) {
					reg_1 = Instruction.temp(reg_1.size);
					inst = inst.append(createInstructions(func, b, reg_1));
				}
				
				inst = inst.append(new Instruction(Insts.convert(expr.type()), Instruction.temp(request), reg_0, reg_1));
				break;
			}
			
			case ret: {
				Expression a = expr.first();
				
				Reg reg_0 = createObject(a);
				if(shouldCheck(a)) {
					reg_0 = Instruction.temp();
					inst = inst.append(createInstructions(func, a, reg_0));
				}
				
				inst = inst.append(new Instruction(Insts.ret, reg_0));
				break;
			}
			
			case neg:
			case not:
			case nor: {
				Expression a = expr.first();
				
				Reg reg_0 = createObject(a);
				if(shouldCheck(a)) {
					reg_0 = Instruction.temp();
					inst = inst.append(createInstructions(func, a, reg_0));
				}
				
				inst = inst.append(new Instruction(Insts.convert(expr.type()), Instruction.temp(request), reg_0));
				break;
			}
			
			case addptr: {
				// TODO: ?? Remember that we need to write and not mov !!!!
				
				Expression a = expr.first();
				
				Reg reg_0 = createObject(a);
				if(shouldCheck(a)) {
					reg_0 = Instruction.temp();
					inst = inst.append(createInstructions(func, a, reg_0));
				}
				
				inst = inst.append(new Instruction(Insts.mov, Instruction.temp(request), reg_0));
				break;
			}
			
			case decptr: {
				Expression a = expr.first();
				
				Reg reg_0 = createObject(a);
				if(shouldCheck(a)) {
					reg_0 = Instruction.temp(a.calculateSize());
					inst = inst.append(createInstructions(func, a, reg_0));
				}
				
				// TODO: Reading from a Identifier should hold the primitive size and not the pointer size.
				
				if(request.size == null && reg_0.size != null) {
					if(reg_0.size.isPointer())
						request.size = AtomType.getPointer(reg_0.size, -1);
					else
						request.size = reg_0.size;
				}
				
				inst = inst.append(new Instruction(Insts.read, Instruction.temp(request), reg_0));
				break;
			}
			
			case comma: {
				for(int i = 0; i < expr.size() - 1; i++) {
					inst = inst.append(createInstructions(func, expr.get(i)));
				}
				
				if(request != null) {
					if(expr.last().hasSideEffects()) {
						inst = inst.append(createInstructions(func, expr.last(), request));
					} else {
						inst = inst.append(new Instruction(Insts.mov, request, createObject(expr.last())));
					}
				} else {
					inst = inst.append(createInstructions(func, expr.last()));
				}
				
				break;
			}
			
			case add: case sub: {
				Insts type = Insts.convert(expr.type());
				Expression a = expr.get(0), b = expr.get(1);
				
				Reg reg_0 = createObject(a);
				if(shouldCheck(a)) {
					reg_0 = Instruction.temp(a.calculateSize());
					inst = inst.append(createInstructions(func, a, reg_0));
				}

				Reg reg_1 = new ObjectReg(b);
				if(shouldCheck(b)) {
					reg_1 = Instruction.temp(b.calculateSize());
					inst = inst.append(createInstructions(func, b, reg_1));
				}
				
				if(expr.size() > 2) {
					// ????
				} else {
					inst = inst.append(new Instruction(type, Instruction.temp(request), reg_0, reg_1));
					break;
				}
				
				Reg reg_2 = Instruction.temp();
				inst = inst.append(new Instruction(type, reg_2, reg_0, reg_1));
				
				for(int i = 2; i < expr.size(); i++) {
					b = expr.get(i);
					reg_1 = createObject(b);
					if(shouldCheck(b)) {
						reg_1 = Instruction.temp(b.calculateSize());
						inst = inst.append(createInstructions(func, b, reg_1));
					}
					
					reg_0 = reg_2;
					if(i == expr.size() - 1) {
						reg_2 = Instruction.temp(request);
					} else {
						reg_2 = Instruction.temp(b.calculateSize());
					}
					inst = inst.append(new Instruction(type, reg_2, reg_0, reg_1));
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
							

							params.add(new Instruction.CallReg(ident.atomType(), e.toString()));
						}
					} else {
						params.add(new Instruction.CallReg(null, e.toString()));
					}
				}
				
				for(int i = 1; i < expr.size(); i++) {
					Expression e = expr.get(i);
					Reg reg = createObject(e);
					
					if(shouldCheck(e)) {
						reg = Instruction.temp();
						inst = inst.append(createInstructions(func, e, reg));
					}
					
					params.add(reg);
				}
				
				if(request == null) {
					params.add(0, Instruction.NONE);
				} else {
					params.add(0, Instruction.temp(request));
				}
				
				inst = inst.append(new Instruction(Insts.call, params.toArray(new Reg[0])));
				break;
			}
			
			case cand: {
				Label label_end = new Label("cand.end");
				
				if(request != null) inst = inst.append(new Instruction(Insts.mov, request, new ObjectReg(0)));
				
				for(int i = 0; i < expr.size(); i++) {
					Expression e = expr.get(i);
					Reg reg = createObject(e);
					
					if(shouldCheck(e)) {
						reg = Instruction.temp(e.calculateSize());
						inst = inst.append(createInstructions(func, e, reg));
					}
					
					inst = inst.append(new Instruction(Insts.brz, reg, label_end));
				}

				if(request != null) inst = inst.append(new Instruction(Insts.mov, request, new ObjectReg(1)));
				inst = inst.append(new Instruction(Insts.label, label_end));
				break;
			}
			
			case cor: {
				Label label_end = new Label("cor.end");
				
				if(request != null) inst = inst.append(new Instruction(Insts.mov, request, new ObjectReg(1)));
				
				for(int i = 0; i < expr.size(); i++) {
					Expression e = expr.get(i);
					Reg reg = createObject(e);
					
					if(shouldCheck(e)) {
						reg = Instruction.temp();
						inst = inst.append(createInstructions(func, e, reg));
					}
					
					inst = inst.append(new Instruction(Insts.bnz, reg, label_end));
				}

				if(request != null) inst = inst.append(new Instruction(Insts.mov, request, new ObjectReg(0)));
				inst = inst.append(new Instruction(Insts.label, label_end));
				break;
			}
			
			default: {
				System.err.println("[MISSING INSTRUCTION] -> " + expr);
			}
		}
		
		// This checks if we have modified the instruction.
		// If we did modify the instruction we should remove
		// the first entry because it's not needed anymore.
		if(inst.first() != inst.last()) {
			inst.first().next.prev = null;
			return inst.last();
		}
		
		return inst.last();
	}
	
	private Instruction createIfInstructions(Function func, IfStat stat) {
		Instruction inst = new Instruction();
		
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
			
			Reg temp = Instruction.temp(stat.condition().calculateSize());
			inst = inst.append(createInstructions(func, stat.condition(), temp));
			inst = inst.append(new Instruction(Insts.brz, temp, label_else));
			inst = inst.append(createInstructions(func, stat.body()));
			inst = inst.append(new Instruction(Insts.br, label_end));
			inst = inst.append(new Instruction(Insts.label, label_else));
			inst = inst.append(createInstructions(func, stat.elseBody()));
			inst = inst.append(new Instruction(Insts.label, label_end));
		} else {
			// ============================== //
			// if(x) { ... }
			
			// brz [end] [x]				Branch to [end] if [x] is zero
			//    ...
			// end:

			Reg temp = Instruction.temp(stat.condition().calculateSize());
			inst = inst.append(createInstructions(func, stat.condition(), temp));
			inst = inst.append(new Instruction(Insts.brz, temp, label_end));
			inst = inst.append(createInstructions(func, stat.body()));
			inst = inst.append(new Instruction(Insts.label, label_end));
		}
		
		return inst.last();
	}
	
	private Instruction createWhileInstructions(Function func, WhileStat stat) {
		Instruction inst = new Instruction();
		
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

		inst = inst.append(new Instruction(Insts.label, label_next));
		Reg temp = Instruction.temp(stat.condition().calculateSize());
		inst = inst.append(createInstructions(func, stat.condition(), temp));
		inst = inst.append(new Instruction(Insts.brz, temp, label_end));
		inst = inst.append(new Instruction(Insts.label, label_loop));
		inst = inst.append(createInstructions(func, stat.body()));
		inst = inst.append(new Instruction(Insts.br, label_next));
		inst = inst.append(new Instruction(Insts.label, label_end));
		
		return inst.last();
	}
	
	private Instruction createForInstructions(Function func, ForStat stat) {
		Instruction inst = new Instruction();
		
		Label label_next = new Label("for.next");
		Label label_loop = new Label("for.loop");
		Label label_end = new Label("for.end");
		
		if(stat.variables() != null) {
			inst = inst.append(createInstructions(func, stat.variables()));
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
				Reg temp = Instruction.temp(stat.condition().calculateSize());
				inst = inst.append(createInstructions(func, stat.condition(), temp));
				inst = inst.append(new Instruction(Insts.brz, temp, label_end));
				inst = inst.append(new Instruction(Insts.br, label_loop));
			}
			
			inst = inst.append(new Instruction(Insts.label, label_next));
			if(stat.condition() != null) {
				Reg temp = Instruction.temp(stat.condition().calculateSize());
				inst = inst.append(createInstructions(func, stat.condition(), temp));
				inst = inst.append(new Instruction(Insts.brz, temp, label_end));
			}
			

			inst = inst.append(createInstructions(func, stat.action()));
			inst = inst.append(new Instruction(Insts.label, label_loop));
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
			
			inst = inst.append(new Instruction(Insts.label, label_next));
			if(stat.condition() != null) {
				Reg temp = Instruction.temp(stat.condition().calculateSize());
				inst = inst.append(createInstructions(func, stat.condition(), temp));
				inst = inst.append(new Instruction(Insts.brz, temp, label_end));
			}
		}
		
		inst = inst.append(createInstructions(func, stat.body()));
		inst = inst.append(new Instruction(Insts.br, label_next));
		inst = inst.append(new Instruction(Insts.label, label_end));
		
		return inst.last();
	}
	
	private Instruction createExprInstructions(Function func, ExprStat stat) {
		Instruction inst = new Instruction();
		
		if(stat.list.isEmpty()) return inst;
		inst = createInstructions(func, stat.list.get(0));
		
		for(int i = 1; i < stat.list.size(); i++) {
			inst = inst.append(createInstructions(func, stat.list.get(i)));
		}
		
		return inst.last();
	}
	
	private Instruction compileInstructions(Function func, Statement stat) {
		Instruction inst = createInstructions(func, stat);
		return inst == null ? new Instruction():inst;
	}
	
	private Instruction createInstructions(Function func, Statement stat) {
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
		
		Instruction inst = new Instruction(Insts.nop, new ObjectReg(stat.toString()));
		
		if(stat.hasStatements()) {
			Instruction point = null;
			
			for(Statement s : stat.getStatements()) {
				if(point == null) {
					point = createInstructions(func, s);
					continue;
				}
				
				point = point.append(createInstructions(func, s));
			}
			
			inst = point;
		}
		
//		Utils.execute_for_all_statements(func, (parent, index, function) -> {
//			System.out.println("[" + index + "] " + parent.get(index));
//		});
		
		return inst.last();
	}
}
