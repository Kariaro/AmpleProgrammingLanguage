package hardcoded.compiler.instruction;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import hardcoded.compiler.constants.IRInsts;
import hardcoded.compiler.instruction.IRInstruction.NumberReg;
import hardcoded.compiler.instruction.IRInstruction.Reg;

public class IntermediateCodeOptimizer {
	public IntermediateCodeOptimizer() {
		
	}
	
	public List<InstructionBlock> generate(List<InstructionBlock> blocks) {
		for(InstructionBlock block : blocks) {
			block.start = simplify(block).first();
			
//			System.out.println("\n" + block.returnType + ", " + block.name);
//			
//			Instruction inst = block.start;
//			int count = 0;
//			while(inst != null) {
//				int idx = count++;
//				if(inst.op == Insts.label) System.out.println();
//				System.out.printf("%4d: ", idx);
//				
//				if(inst.op != Insts.label) System.out.print("  ");
//				
//				System.out.printf("%s\n", inst);
//				inst = inst.next;
//			}
		}
		
		return blocks;
	}
	
	/**
	 * Remove all the nop instructions from a <code>InstructionBlock</code>.
	 * @param	block	the instruction block to optimize
	 */
	private void remove_nops(InstructionBlock block) {
		IRInstruction first = null;
		IRInstruction inst = block.start;
		
		do {
			if(inst.op == IRInsts.nop) {
				inst = inst.remove();
			} else {
				if(first == null) first = inst;
				inst = inst.next();
			}
		} while(inst != null);
		
		block.start = first;
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
	private void eq_bnz_optimization(InstructionBlock block) {
		IRInstruction inst = block.start;
		
		do {
			// Check if the type was the positive equality 'eq'
			boolean peq = inst.type() == IRInsts.eq;
			
			if(peq || inst.type() == IRInsts.neq) {
				IRInstruction next = inst.next();
				if(next == null) break;
				
				// Check if the type was the positive branch 'brz'
				boolean pbr = next.type() == IRInsts.brz;
				
				// Check if the last element is a zero
				Reg reg = inst.getLastParam();
				if(reg instanceof NumberReg && ((NumberReg)reg).value == 0) {
					// Check if the equality result is referenced
					int refs = getReferences(block, inst.getParam(0));
					if(refs < 3) inst.remove(); // Remove the instruction...
					next.params.set(0, inst.getParam(1));
					next.op = (pbr == peq) ? IRInsts.bnz:IRInsts.brz;
				}
			}
			
			inst = inst.next();
		} while(inst != null);
		
		// Make sure that we update the blocks first element the first element
	}
	
	/**
	 * This optimization removes all 
	 * 
	 * @param	block	the instruction block to optimize
	 */
	private void counter_optimization(InstructionBlock block) {
		Map<Integer, Reg> map = new HashMap<>();
		int index = 0;
		
		IRInstruction inst = block.start;
		
		do {
			for(int i = 0; i < inst.params.size(); i++) {
				Reg reg = inst.getParam(i);
				
				if(reg.getClass() == Reg.class) {
					Reg next = map.get(reg.index);
					if(next == null) {
						next = new Reg(reg.size, index++);
						map.put(reg.index, next);
					}
					
					inst.params.set(i, next);
				}
			}
			
			inst = inst.next();
		} while(inst != null);
	}
	
	private void flow_optimization(InstructionBlock block) {
		IRInstruction flow = block.start;
		if(flow == null) return;
		
		IRInstruction start = flow;
		IRInstruction inst = flow;
		
		while(inst != null) {
			// add [a], [b], [c]
			// check if a has been used inside the block..
			

			if(inst.next() != null) {
				//    ... [a], [b], [c]
				//    mov [z], [a]
				// Should become
				//    ... [z], [b], [c]
				//    If z was zero before.
				if(inst.next().op == IRInsts.mov && canReduce(inst.op)) {
					Reg reg = inst.params.get(0);
					Reg wnt = inst.next().params.get(1);
					
					if(getReferences(start, reg) == 2 && wnt == reg) {
						inst.params.set(0, inst.next().params.get(0));
						inst.next().remove();
					}
				}
			}
			
			// If x has not been modified and y has not been modified then
			// replace all further instructions read [ ... ], [y] with [x]
			//    read [x], [y]
			
			if(!inst.params.isEmpty()) {
				Reg reg = inst.params.get(0);
				int num = getReferences(start, reg);
				
				// Only remove temporary variables.
				// System.out.println("Regs -> " + reg + ", " + num);
				
				if(num < 2) {
					if(!keepIfNotReferences(inst.op)) {
						// TODO: There could be a problem if the register is pointing towards a global variable.
						inst = inst.remove();
					}
				}
			}
			
			if(inst == null) break;
			
			inst = inst.next();
		}
		
		block.start = start.last().first();
	}
	
	private IRInstruction simplify(InstructionBlock block) {
		// if(true) return block.start;
		// TODO: Find a way to check if any changes has been made to the instruction block
		
		//   brz [ ... ], [A]
		// A:
		// If there is no code between the branch and not the branch then remove them.
		//   ...
		
		remove_nops(block);
		eq_bnz_optimization(block);
		
		for(int i = 0; i < 1; i++) {
			flow_optimization(block);
			counter_optimization(block);
		}
		
		return block.start;
	}
	
	private int getReferences(InstructionBlock block, Reg reg) { return getReferences(block.start, reg); }
	private int getReferences(IRInstruction in, Reg reg) {
		in = in.first();
		int references = 0;
		
		for(IRInstruction inst : in) {
			for(Reg r : inst.params) {
				if(reg.equals(r)) references++;
			}
		}
		
		return references;
	}
	
	private boolean keepIfNotReferences(IRInsts type) {
		switch(type) {
			case call:
			case write:
			case data:
			case ret: return true;
			
			default: return false;
		}
	}
	
	private boolean canReduce(IRInsts type) {
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
