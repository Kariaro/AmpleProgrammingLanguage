package hardcoded.compiler.instruction;

import static hardcoded.compiler.Expression.ExprType.*;

import java.util.ArrayList;
import java.util.List;

import hardcoded.compiler.*;
import hardcoded.compiler.Block.Function;
import hardcoded.compiler.Expression.AtomExpr;
import hardcoded.compiler.Instruction.Label;
import hardcoded.compiler.Instruction.ObjectReg;
import hardcoded.compiler.Instruction.Reg;
import hardcoded.compiler.Statement.*;
import hardcoded.compiler.constants.Insts;

public class HInstructionCompiler {
	
	public HInstructionCompiler() {
		
	}
	
	public void compile(Program program) {
		// Create the correct instructions for, 'if', 'while', 'switch', 'for'
		
		for(int i = 0; i < program.size(); i++) {
			Block block = program.get(i);
			
			if(!(block instanceof Function)) continue;
			Function func = (Function)block;
			
			Instruction inst = compileInstructions(func, func.body).first();
			
			System.out.println("Instructions");
			int count = 0;
			while(inst != null) {
				if(inst.op == Insts.nop) {
					inst = inst.next;
					continue;
				}
				
				System.out.println("   [" + (count++) + "] " + inst);
				inst = inst.next;
			}
		}
	}
	
	private boolean shouldCheck(Expression e) {
		return !(e instanceof AtomExpr);
	}
	
	private Instruction createInstructions(Function func, Expression expr) {
		return createInstructions(func, expr, null);
	}
	
	private Instruction createInstructions(Function func, Expression expr, Reg request) {
		Instruction inst = new Instruction(Insts.nop, new ObjectReg(expr.toString()));
		
		switch(expr.type()) {
			case mov: {
				Expression a = expr.first(), b = expr.last();
				
				Reg reg_0 = new ObjectReg(a);
				Reg reg_1 = new ObjectReg(b);
				boolean pointer = false;
				if(shouldCheck(a)) {
					if(a.type() == decptr) {
						pointer = true;
						
						Expression f = a.first();
						
						if(shouldCheck(f)) {
							reg_0 = Instruction.temp();
							inst = inst.append(createInstructions(func, a.first(), reg_0));
						} else{
							reg_0 = new ObjectReg(f);
						}
						
						pointer = true;
					} else {
						reg_0 = Instruction.temp();
						inst = inst.append(createInstructions(func, a, reg_0));
					}
				}
				
				if(shouldCheck(b)) {
					reg_1 = Instruction.temp();
					inst = inst.append(createInstructions(func, b, reg_1));
				}
				
				Insts action = pointer ? Insts.write:Insts.mov;
				if(!inst.hasNeighbours()) {
					inst = new Instruction(action, reg_0, reg_1);
				} else {
					inst = inst.append(new Instruction(action, reg_0, reg_1));
				}
				
				break;
			}
			
			
			case add: case sub:
			case div: case mul:
			case shl: case shr:
			case lt: case lte:
			case gt: case gte:
			case eq: case neq:
			case or: case and:
			case xor: {
				Expression a = expr.first(), b = expr.last();
				
				Reg reg_0 = new ObjectReg(a);
				Reg reg_1 = new ObjectReg(b);
				if(shouldCheck(a)) {
					reg_0 = Instruction.temp();
					inst = inst.append(createInstructions(func, a, reg_0));
				}
				
				if(shouldCheck(b)) {
					reg_1 = Instruction.temp();
					inst = inst.append(createInstructions(func, b, reg_1));
				}
				
				inst = inst.append(new Instruction(Insts.convert(expr.type()), Instruction.temp(request), reg_0, reg_1));
				break;
			}
			
			case neg:
			case not:
			case nor: {
				Expression a = expr.first();
				
				Reg reg_0 = new ObjectReg(a);
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
				
				Reg reg_0 = new ObjectReg(a);
				if(shouldCheck(a)) {
					reg_0 = Instruction.temp();
					inst = inst.append(createInstructions(func, a, reg_0));
				}
				
				inst = inst.append(new Instruction(Insts.mov, Instruction.temp(request), reg_0));
				break;
			}
			
			case decptr: {
				Expression a = expr.first();
				
				Reg reg_0 = new ObjectReg(a);
				if(shouldCheck(a)) {
					reg_0 = Instruction.temp();
					inst = inst.append(createInstructions(func, a, reg_0));
				}
				
				inst = inst.append(new Instruction(Insts.read, Instruction.temp(request), reg_0));
				break;
			}
			
			case comma: {
				for(int i = 0; i < expr.size() - 1; i++) {
					inst = inst.append(createInstructions(func, expr.get(i)));
				}
				
				inst = inst.append(createInstructions(func, expr.last(), request));
				break;
			}
			
			case call: {
				List<Reg> params = new ArrayList<>();
				
				for(int i = 0; i < expr.size(); i++) {
					Expression e = expr.get(i);
					Reg reg = new ObjectReg(e);
					
					if(shouldCheck(e)) {
						reg = Instruction.temp();
						inst = inst.append(createInstructions(func, e, reg));
					}
					
					params.add(reg);
				}
				
				params.add(0, Instruction.temp(request));
				
				inst = inst.append(new Instruction(Insts.call, params.toArray(new Reg[0])));
				break;
			}
			
			default: {
				
			}
		}
		
		return inst.last();
	}
	
	private Instruction createIfInstructions(Function func, IfStat stat) {
		Instruction inst = new Instruction();
		
		Label label_end = new Label("end");
		
		if(stat.hasElseBody()) {
			// ============================== //
			// if(x) { ... } else { ... }
			
			// brz [else] [x]				Branch to [else] if [x] is zero
			//    ...
			// br [end]						Branch to [end]
			// else:
			//    ...
			// end:
			
			Label label_else = new Label("else");
			
			Reg temp = Instruction.temp();
			inst = inst.append(createInstructions(func, stat.condition(), temp));
			inst = inst.append(new Instruction(Insts.brz, label_else, temp));
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

			Reg temp = Instruction.temp();
			inst = inst.append(createInstructions(func, stat.condition(), temp));
			inst = inst.append(new Instruction(Insts.brz, label_end, temp));
			inst = inst.append(createInstructions(func, stat.body()));
			inst = inst.append(new Instruction(Insts.label, label_end));
		}
		
		// 
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
		//   br [loop]
		// end:
		
		Label label_end = new Label("end");
		Label label_next = new Label("next");
		Label label_loop = new Label("loop");

		inst = inst.append(new Instruction(Insts.label, label_next));
		Reg temp = Instruction.temp();
		inst = inst.append(createInstructions(func, stat.condition(), temp)); // Evaluate x only when there are side effects...
		inst = inst.append(new Instruction(Insts.brz, label_end, temp));
		inst = inst.append(new Instruction(Insts.label, label_loop));
		inst = inst.append(createInstructions(func, stat.body()));
		inst = inst.append(new Instruction(Insts.br, label_next));
		inst = inst.append(new Instruction(Insts.label, label_end));
		
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
		} else if(stat instanceof WhileStat) {
			return createWhileInstructions(func, (WhileStat)stat);
		} else if(stat instanceof ReturnStat) {
			ReturnStat rs = (ReturnStat)stat;
			
			if(rs.expr() == null) {
				return new Instruction(Insts.ret);
			} else {
				Expression e = rs.expr();
				
				if(e.hasSideEffects()) {
					Reg temp = Instruction.temp();
					Instruction inst = createInstructions(func, e, temp);
					return inst.append(new Instruction(Insts.ret, temp));
				}
				
				return new Instruction(Insts.ret, new ObjectReg(e));
			}
		} else if(stat instanceof ExprStat) {
			return createInstructions(func, ((ExprStat)stat).expr());
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
