package hardcoded.compiler.instruction;

import java.util.List;

import hardcoded.compiler.constants.Insts;
import hardcoded.compiler.instruction.Instruction.Reg;

public class IntermediateCodeOptimizer {
//	public static void main(String[] args) {
//		IntermediateCodeOptimizer opt = new IntermediateCodeOptimizer();
//		
//		Instruction inst = new Instruction(Insts.nop);
//		inst = inst.append(new Instruction(Insts.add));
//		inst = inst.append(new Instruction(Insts.sub));
//		inst = inst.append(new Instruction(Insts.nop));
//		inst = inst.append(new Instruction(Insts.add));
//		inst = inst.append(new Instruction(Insts.sub));
//		inst = inst.append(new Instruction(Insts.nop));
//		inst = inst.append(new Instruction(Insts.add));
//		inst = inst.append(new Instruction(Insts.sub));
//		inst = inst.append(new Instruction(Insts.nop));
//		
//		opt.generate(java.util.Arrays.asList(new InstructionBlock("test", hardcoded.compiler.constants.Primitives.VOID, inst.first())));
//	}
	
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
	 * Remove all the nops inside of a instruction block.
	 */
	private Instruction remove_nops(Instruction start) {
		Instruction inst = start;
		
		// Remove all nops inside the block.
		do {
			if(inst.op == Insts.nop) {
				inst = inst.remove();
				
				if(inst == null) return new Instruction(Insts.nop);
			}
			
			if(inst.next == null) 
				return inst.first();
			
			inst = inst.next;
		} while(inst != null);
		
		return start.last().first();
	}
	
	private Instruction flow_optimization(Instruction inst) {
		Instruction start = inst;
		do {
			// add [a], [b], [c]
			// check if a has been used inside the block..
			

			if(inst.next != null) {
				//    ... [a], [b], [c]
				//    mov [z], [a]
				// Should become
				//    ... [z], [b], [c]
				
				if(inst.next.op == Insts.mov && canReduce(inst.op)) {
					Reg reg = inst.params.get(0);
					Reg wnt = inst.next.params.get(1);
					
					if(getReferences(start, reg) == 2 && wnt == reg) {
						inst.params.set(0, inst.next.params.get(0));
						inst.next.remove();
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
			
			inst = inst.next;
		} while(inst != null);
		
		return start;
	}
	
	private Instruction simplify(InstructionBlock block) {
		// if(true) return block.start;
		// TODO: Check if any changes has been made
		
		Instruction start = block.start;
		start = remove_nops(start);
		for(int i = 0; i < 100; i++) {
			start = flow_optimization(start);
		}
		
		return start;
	}
	
	private int getReferences(Instruction inst, Reg reg) {
		inst = inst.first();
		int references = 0;
		
		do {
			for(Reg r : inst.params) {
				if(reg.equals(r)) references++;
			}
			
			inst = inst.next;
		} while(inst != null);
		
		return references;
	}
	
	private boolean keepIfNotReferences(Insts type) {
		switch(type) {
			case call:
			case write:
			case data:
			case ret: return true;
			
			default: return false;
		}
	}
	
	private boolean canReduce(Insts type) {
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
