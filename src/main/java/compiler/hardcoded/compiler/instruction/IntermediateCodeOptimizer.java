package hardcoded.compiler.instruction;

import java.util.*;

import hardcoded.CompilerMain;
import hardcoded.compiler.constants.Utils;
import hardcoded.compiler.constants.Utils.IRListIterator;
import hardcoded.compiler.instruction.IRInstruction.*;

public class IntermediateCodeOptimizer {
	// TODO: Tail recursion optimization
	
	// TODO: Unused write optimization
	//   If a instruction changes the value of a register and is later changed with mov
	//   inside the same label region. Then the initial mov instruction should be removed.
	
	// TODO: Unused code optimization
	//   If code that does not write to memory, alter some other state of the program or
	//   affect the output of the function is found inside a function. Then it should be
	//   removed.
	
	// TODO: Reuse read optimization
	//   If a read instruction reads to a register from some memory and another instruction
	//   later inside the same block read from the same memory address. If the original
	//   register has not been changed the second read instructions register should be
	//   replaced with the original register.
	
	// TODO: Compacting optimization
	//   not [$B], [$A]
	//   mul [$C], [$B], [1]
	// Becomes
	//   mul [$C], [$A], [-1]
	
	// TODO: Constant traversal optimization
	//   If some instruction does some logic addition after beeing reset it can be
	//   optimized to just hold the value of the operation.
	//
	//   mov [$B], [3]
	//   add [$B], [$B], [2]
	// Becomes
	//   mov [$B], [5]
	
	// TODO: Loop unfolding optimization
	//   Each branch instruction inside a loop can take a small amount of time to
	//   execute so unfolding loops of a small size should be prefered for speed.
	
	public IntermediateCodeOptimizer() {}
	
	public IRProgram generate(IRProgram program) {
		for(IRFunction func : program.getFunctions()) {
			simplify(func);
		}
		
//		{
//			System.out.println(".data.strings:");
//			int index = 0;
//			for(String s : program.context.strings) {
//				System.out.printf("%4d:   \"%s\"\n", index, StringUtils.escapeString(s));
//			}
//			
//			System.out.println();
//		}
//		
		for(IRFunction func : program.getFunctions()) {
			System.out.println("\n" + func);
			for(int i = 0, line = 0; i < func.length(); i++) {
				IRInstruction inst = func.list.get(i);
				
				if(inst.op == IRType.label) {
					System.out.printf("\n%4d: %s\n", line, inst);
				} else {
					System.out.printf("%4d:   %s\n", line, inst);
					line++;
				}
			}
		}
//		
//		try {
//			Thread.sleep(100000);
//		} catch (InterruptedException e) {
//			e.printStackTrace();
//		}
		
		return program;
	}
	
	/**
	 * Remove all the nop instructions from a <code>InstructionBlock</code>.
	 * @param	block	the instruction block to optimize
	 */
	private void remove_nops(IRFunction func) {
		Iterator<IRInstruction> iter = func.list.iterator();
		
		while(iter.hasNext()) {
			IRInstruction inst = iter.next();
			
			if(inst.op == IRType.nop) {
				iter.remove();
			}
		}
	}
	
	//  eq - brz		if($B == 0)			if(!$B)
	// neq - brz		if($B != 0)			if( $B)
	//  eq - bnz		if(!($B == 0))		if( $B)
	// neq - bnz		if(!($B != 0))		if(!$B)
	// not - brz        bnz
	// not - bnz		brz
	// neq		$A, $B, 0
	// bnz		$A, ...
	// 
	/**
	 * Optimizing all the branching instructions.
	 * 
	 * <p>This optimization uses the fact that certain
	 * instructions can cancel eachother. One such pair
	 * is the equals and branching instructions.
	 * 
	 * All pairs of a {@code eq} or a {@code neq}
	 * followed by a {@code brz} or a {@code bnz} can
	 * be reduced if the equal instruction contains a {@code zero}.
	 * 
	 * <p>We will give each instruction different values to
	 * get to each outcome. If we have a not instruction such as
	 * {@code neq} or {@code bnz} they will give a value
	 * of {@code one}. Otherwise it will give {@code zero}.
	 * 
	 * <p>This will create the bit field.
	 *<PRE>
	 *brz   eq	00
	 *brz  neq	01
	 *bnz   eq	10
	 *bnz  neq	11
	 *</PRE>
	 * 
	 * <p>An example of this optimization would be the following.
	 *<PRE>
	 *eq	..	[$A], [$B], [0]
	 *brz		[$A], [ ... ]
	 *==== Becomes ====
	 *bnz		[$B], [ ... ]
	 *</PRE>
	 * 
	 * @param	block	the instruction block to optimize
	 */
	private void eq_bnz_optimization(IRFunction func) {
		IRListIterator iter = Utils.createIterator(func.list);
		
		while(iter.hasNext()) {
			IRInstruction inst = iter.next();
			if(!iter.hasNext()) break;
			
			// Check if the type was the positive equality 'eq'
			boolean positive_eq = inst.type() == IRType.eq;
			// not, brz == bnz
			// not, bnz == brz
			
			// TODO: Check that the next instruction is a branch instruction!
			
			if(positive_eq || inst.type() == IRType.neq || inst.type() == IRType.not) {
				IRInstruction next = iter.peakNext();
				
				// Check if the type was the positive branch 'brz'
				boolean positive_br = next.type() == IRType.brz;
				
				// Check if the last element is a zero
				if(inst.type() == IRType.not) {
					next.params.set(0, inst.getParam(1));
					next.op = (positive_br) ? IRType.bnz:IRType.brz;
					iter.remove();
				} else {
					Param reg = inst.getParam(inst.getNumParams() - 1);
					if(reg instanceof NumberReg && ((NumberReg)reg).getValue() == 0) {
						// Check if the equality result is referenced
						
						int refs = getReferences(func, inst.getParam(0));
						if(refs < 3) {
							iter.remove(); // Remove the instruction...
						}
						
						next.params.set(0, inst.getParam(1));
						next.op = (positive_br == positive_eq) ? IRType.bnz:IRType.brz;
					}
				}
			}
		}
	}
	
	private void mov_bnz_optimization(IRFunction func) {
		IRListIterator iter = Utils.createIterator(func.list);
		
		while(iter.hasNext()) {
			IRInstruction inst = iter.next();
			if(!iter.hasNext()) break;
			
			if(inst.op != IRType.mov) continue;
			IRInstruction next = iter.peakNext();
			
			if(next.op == IRType.brz) {
				next.params.set(0, inst.getParam(1));
			}
		}
	}
	
	/**
	 * This optimization reduces the amount of registers to
	 * the lowest amount possible by counting and replacing.
	 */
	private void counter_optimization(IRFunction func) {
		Map<Integer, Reg> map = new HashMap<>();
		int index = 0;
		
		IRListIterator iter = Utils.createIterator(func.list);
		
		while(iter.hasNext()) {
			IRInstruction inst = iter.next();
			
			for(int i = 0; i < inst.params.size(); i++) {
				Param param = inst.getParam(i);
				if(!(param instanceof Reg)) continue;
				Reg reg = (Reg)param;
				
				// Only optimize generated registers and not variable registers.
				if(!reg.isTemporary()) continue;
				
				Reg next = map.get(reg.getIndex());
				if(next == null) {
					next = new Reg(reg.getSize(), index++);
					map.put(reg.getIndex(), next);
				}
				
				inst.params.set(i, next);
			}
		}
	}
	
	private void flow_optimization(IRFunction func) {
		IRListIterator iter = Utils.createIterator(func.list);
		
		while(iter.hasNext()) {
			IRInstruction inst = iter.next();
			// add [a], [b], [c]
			// check if a has been used inside the block..
			
			
			if(iter.hasNext()) {
				IRInstruction next = iter.peakNext();
				
				//    ... [a], [b], [c]
				//    mov [z], [a]
				// Should become
				//    ... [z], [b], [c]
				//    If z was zero before.
				if(next.op == IRType.mov && canReduce(inst.op)) {
					Param reg = inst.params.get(0);
					Param wnt = next.params.get(1);
					
					if(getReferences(func, reg) == 2 && wnt == reg) {
						inst.params.set(0, next.params.get(0));
						
						iter.next();
						iter.remove();
						
						// TODO: ???
						continue;
					}
				}
			}
			
			// If x has not been modified and y has not been modified then
			// replace all further instructions read [ ... ], [y] with [x]
			//    read [x], [y]
			
			if(!inst.params.isEmpty()) {
				Param reg = inst.params.get(0);
				int num = getReferences(func, reg);
				
				// Only remove temporary variables.
				
				if(num < 2) {
					if(!keepIfNotReferences(inst.op)) {
						// TODO: There could be a problem if the register is pointing towards a global variable.
						iter.remove();
					}
				}
			}
		}
	}
	
	// TODO: Pass through label optimization
	//   If some branch instruction jumps to a label that only contains a unconditional instruction
	// then jump to the target's location.
	// =====================
	//   br [some_label]
	//     ...
	// some_label:
	//   br [another]
	//
	// Should become:
	// =====================
	//   br [another]
	//     ...
	// some_label:
	//   br [another]
	private void pass_though_label_optimization(IRFunction func) {
		IRListIterator iter = Utils.createIterator(func.list);
		List<IRInstruction> list = func.list;
		
		while(iter.hasNext()) {
			IRInstruction inst = iter.next();
			switch(inst.op) {
				case bnz:
				case brz:
				case br: break;
				default: continue;
			}
			
			int paramIndex = inst.op.args == 2 ? 1:0;
			LabelParam label = (LabelParam)inst.getParam(paramIndex);
			
			int index = findLabel(func, iter.index(), label);
			
			// Check if the index is followed by a br or a label
			if(index + 1 < list.size()) {
				IRInstruction pass = list.get(index + 1);
				
				if(pass.op == IRType.br || pass.op == IRType.label) {
					inst.params.set(paramIndex, pass.getParam(0));
				}
			}
		}
	}
	
	// TODO: Dead code optimization
	//   If an instruction is inside a code block that will never be entered or if a
	//   label is never jumped to or is proceeded by another label it should be removed.
	private void dead_code_optimization(IRFunction func) {
		IRListIterator iter = Utils.createIterator(func.list);
		
		while(iter.hasNext()) {
			IRInstruction inst = iter.next();
			
			if(!(inst.op == IRType.ret || inst.type() == IRType.br)) continue;
			
			while(iter.hasNext()) {
				IRInstruction next = iter.next();
				
				if(next.op != IRType.label) {
					iter.remove();
					continue;
				}
				
				break;
			}
		}
	}
	
	private void simplify(IRFunction func) {
		if(func.length() < 1) return; // We should not simplify empty functions
		
		// TODO: Find a way to check if any changes has been made to the instruction block
		
		//   brz [ ... ], [A]
		// A:
		// If there is no code between the branch and not the branch then remove them.
		//   ...
		logOptimization(func, null);
		remove_nops(func);
		logOptimization(func, "remove_nops");
		eq_bnz_optimization(func);
		logOptimization(func, "eq_bnz");
		
		while(true) {
			int size = func.list.size();
			
			mov_bnz_optimization(func);
			logOptimization(func, "mov_bnz");
			
			flow_optimization(func);
			logOptimization(func, "flow");
			
			counter_optimization(func);
			logOptimization(func, "counter");
			
			pass_though_label_optimization(func);
			logOptimization(func, "pass_through_label");
			
			dead_code_optimization(func);
			logOptimization(func, "dead_code");
			
			if(size != func.list.size()) {
				// If the size changed during the flow optimization we
				// should try re run the optimizations
				
				continue;
			}
			
			break;
		}
	}
	
	private int findLabel(IRFunction func, int pivot, LabelParam label) {
		List<IRInstruction> list = func.list;
		
		// TODO: Maybe implement a pivot based search.
		for(int i = 0; i < list.size(); i++) {
			IRInstruction inst = list.get(i);
			
			if(inst.op == IRType.label) {
				if(inst.getParam(0).equals(label)) {
					return i;
				}
			}
		}
		
		return -1;
	}
	
	private int size;
	@SuppressWarnings("unused")
	private void logOptimization(IRFunction func, String name) {
		if(!CompilerMain.isDeveloper()) return;
		if(true) return;
		
		if(name == null) {
			size = func.length();
			System.out.println("============================================== [" + func + "]");
		} else {
			System.out.printf("Optimization[%s]: %d -> %d\n", name, size, func.length());
			size = func.length();
		}
	}
	
	private int getReferences(IRFunction func, Param reg) {
		int references = 0;
		
		for(IRInstruction inst : func.list) {
			for(Param r : inst.params) {
				if(reg.equals(r)) references++;
			}
		}
		
		return references;
	}
	
	private boolean keepIfNotReferences(IRType type) {
		switch(type) {
			case call:
			case write:
			case data:
			case ret: return true;
			
			default: return false;
		}
	}
	
	private boolean canReduce(IRType type) {
		switch(type) {
			case bnz:
			case brz:
			case label:
			case data:
			case ret: return false;
			
			default: return true;
		}
	}
}
