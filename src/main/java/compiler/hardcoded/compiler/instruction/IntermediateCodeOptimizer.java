package hardcoded.compiler.instruction;

import java.util.*;

import hardcoded.compiler.instruction.IRInstruction.NumberReg;
import hardcoded.compiler.instruction.IRInstruction.Param;
import hardcoded.compiler.instruction.IRInstruction.Reg;

public class IntermediateCodeOptimizer {
	public IntermediateCodeOptimizer() {
		
	}
	
	public IRProgram generate(IRProgram program) {
		for(IRFunction func : program.getFunctions()) {
			simplify(func);
			
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
		
		func.fixInstructions();
	}
	
	//  eq - brz		if($B == 0)			if(!$B)
	// neq - brz		if($B != 0)			if( $B)
	//  eq - bnz		if(!($B == 0))		if( $B)
	// neq - bnz		if(!($B != 0))		if(!$B)
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
	 * All pairs of a <code>eq</code> or a <code>neq</code>
	 * followed by a <code>brz</code> or a <code>bnz</code> can
	 * be reduced if the equal instruction contains a {@code zero}.
	 * 
	 * <p>We will give each instruction different values to
	 * get to each outcome. If we have a not instruction such as
	 * <code>neq</code> or <code>bnz</code> they will give a value
	 * of {@code one}. Otherwise it will give {@code zero}.
	 * 
	 * <p>This will create the bit field.
	 *<pre>brz   eq	00
	 *brz  neq	01
	 *bnz   eq	10
	 *bnz  neq	11</pre>
	 * 
	 * <p>An example of this optimization would be the following.
	 *<pre>eq	..	[$A], [$B], [0]
	 *brz		[$A], [ ... ]
	 *==== Becomes ====
	 *bnz		[$B], [ ... ]</pre>
	 * 
	 * @param	block	the instruction block to optimize
	 */
	private void eq_bnz_optimization(IRFunction func) {
		ListIterator<IRInstruction> iter = func.list.listIterator();
		
		while(iter.hasNext()) {
			IRInstruction inst = iter.next();
			if(!iter.hasNext()) break;
			
			// Check if the type was the positive equality 'eq'
			boolean positive_eq = inst.type() == IRType.eq;
			
			if(positive_eq || inst.type() == IRType.neq) {
				IRInstruction next = inst.next();
				
				// Check if the type was the positive branch 'brz'
				boolean positive_br = next.type() == IRType.brz;
				
				// Check if the last element is a zero
				Param reg = inst.getLastParam();
				if(reg instanceof NumberReg && ((NumberReg)reg).value == 0) {
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
		
		// Make sure that we update the instructions
		func.fixInstructions();
	}
	
	/**
	 * This optimization reduces the amount of registers to
	 * the lowest amount possible by counting and replacing.
	 */
	private void counter_optimization(IRFunction func) {
		Map<Integer, Reg> map = new HashMap<>();
		int index = 0;
		
		ListIterator<IRInstruction> iter = func.list.listIterator();
		
		while(iter.hasNext()) {
			IRInstruction inst = iter.next();
			
			for(int i = 0; i < inst.params.size(); i++) {
				Param param = inst.getParam(i);
				if(!(param instanceof Reg)) continue;
				Reg reg = (Reg)param;
				
				// Keep function variables.
				if(!reg.isTemporary) continue;
				
				Reg next = map.get(reg.getIndex());
				if(next == null) {
					next = new Reg(reg.getSize(), index++);
					map.put(reg.getIndex(), next);
				}
				
				inst.params.set(i, next);
			}
		}
		
		func.fixInstructions();
	}
	
	private void flow_optimization(IRFunction func) {
		ListIterator<IRInstruction> iter = func.list.listIterator();
		
		while(iter.hasNext()) {
			IRInstruction inst = iter.next();
			// add [a], [b], [c]
			// check if a has been used inside the block..
			

			if(iter.hasNext()) {
				//    ... [a], [b], [c]
				//    mov [z], [a]
				// Should become
				//    ... [z], [b], [c]
				//    If z was zero before.
				if(inst.next().op == IRType.mov && canReduce(inst.op)) {
					Param reg = inst.params.get(0);
					Param wnt = inst.next().params.get(1);
					
					if(getReferences(func, reg) == 2 && wnt == reg) {
						inst.params.set(0, inst.next().params.get(0));
						
						iter.next();
						iter.remove();
						
						// TODO???
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
				// System.out.println("Regs -> " + reg + ", " + num);
				
				if(num < 2) {
					if(!keepIfNotReferences(inst.op)) {
						// TODO: There could be a problem if the register is pointing towards a global variable.
						iter.remove();
						
						// inst = inst.remove();
					}
				}
			}
		}
		
		func.fixInstructions();
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
	
	// TODO: Dead code optimization
	//   If an instruction is inside a code block that will never be entered it should be removed.
	
	private void simplify(IRFunction func) {
		// if(true) return block.start;
		// TODO: Find a way to check if any changes has been made to the instruction block
		
		//   brz [ ... ], [A]
		// A:
		// If there is no code between the branch and not the branch then remove them.
		//   ...
		
		remove_nops(func);
		eq_bnz_optimization(func);
		
		for(int i = 0; i < 1; i++) {
			flow_optimization(func);
			counter_optimization(func);
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
