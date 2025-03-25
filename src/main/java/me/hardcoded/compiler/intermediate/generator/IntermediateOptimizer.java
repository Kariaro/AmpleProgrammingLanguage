package me.hardcoded.compiler.intermediate.generator;

import me.hardcoded.compiler.intermediate.inst.*;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

public class IntermediateOptimizer {
	private static final Logger LOGGER = LogManager.getLogger(IntermediateOptimizer.class);
	private static final boolean DEBUG_IRCODE_OPTIMIZATION = true;
	
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
	
	private class Task {
		private final String name;
		private final Consumer<Procedure> consumer;
		
		public Task(String name, Consumer<Procedure> consumer) {
			this.name = name;
			this.consumer = consumer;
		}
		
		public boolean run(Procedure func) {
			List<Inst> list = func.getInstructions();
			int oldSize = list.size();
			
			consumer.accept(func);
			
			boolean modified = oldSize != list.size();
			
			if (DEBUG_IRCODE_OPTIMIZATION) {
				if (modified) {
					LOGGER.info("Optimization[{}]: {} -> {}", name, oldSize, list.size());
				}
			}
			
			return modified;
		}
	}
	
	// Optimizations
	private final List<Task> setup_optimizations;
	private final List<Task> multi_optimizations;
	
	public IntermediateOptimizer() {
		// Run only once
		setup_optimizations = List.of(
			new Task("remove_nops", this::remove_nops),
			new Task("eq_bnz", this::eq_bnz_optimization)
		);
		
		// Multi optimizations
		multi_optimizations = List.of(
			new Task("mov_bnz", this::mov_bnz_optimization),
			new Task("flow", this::flow_optimization),
			new Task("counter", this::counter_optimization),
			new Task("pass_through_label", this::pass_though_label_optimization),
			new Task("dead_code", this::dead_code_optimization)
		);
	}
	
	public IntermediateFile generate(IntermediateFile program) {
		// LOGGER.info(IRPrintUtils.printPretty(program));
		
		for (Procedure func : program.getProcedures()) {
			simplify(func);
		}
		
		// LOGGER.info(IRPrintUtils.printPretty(program));
		
		return program;
	}
	
	private void simplify(Procedure func) {
		if (func.getInstructions().size() < 1)
			return; // We should not simplify empty functions
		
		if (DEBUG_IRCODE_OPTIMIZATION) {
			LOGGER.info("============================================== [{}]", func);
		}
		
		// Run all startup tasks
		for (Task task : setup_optimizations) {
			task.run(func);
		}
		
		int max = 100;
		while (max-- > 0) {
			boolean modified = false;
			for (Task task : multi_optimizations) {
				modified |= task.run(func);
			}
			
			if (!modified) {
				// The code has not been modified.
				break;
			}
			
			// break;
		}
	}
	
	/**
	 * Remove all the nop instructions from a <code>InstructionBlock</code>.
	 *
	 * @param func the instruction block to optimize
	 */
	private void remove_nops(Procedure func) {
		Iterator<Inst> iter = func.getInstructions().iterator();
		
		while (iter.hasNext()) {
			Inst inst = iter.next();
			
			// if (inst.getOpcode() == Opcode.NOP) {
			// 	iter.remove();
			// }
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
	 * instructions can cancel each other. One such pair
	 * is the equals and branching instructions.
	 * <p>
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
	 * <PRE>
	 * brz   eq	00
	 * brz  neq	01
	 * bnz   eq	10
	 * bnz  neq	11
	 * </PRE>
	 *
	 * <p>An example of this optimization would be the following.
	 * <PRE>
	 * eq	..	[$A], [$B], [0]
	 * brz		[$A], [ ... ]
	 * ==== Becomes ====
	 * bnz		[$B], [ ... ]
	 * </PRE>
	 *
	 * @param func the instruction block to optimize
	 */
	private void eq_bnz_optimization(Procedure func) {
		var iter = func.getInstructions().listIterator();
		// IRListIterator iter = IRPrintUtils.createIterator(func);
		
		while (iter.hasNext()) {
			Inst inst = iter.next();
			if (!iter.hasNext())
				break;
			
			// Check if the type was the positive equality 'eq'
			boolean positive_eq = inst.getOpcode() == Opcode.EQ;
			// not, brz == bnz
			// not, bnz == brz
			
			// TODO: Check that the next instruction is a branch instruction!
			
			if (positive_eq || inst.getOpcode() == Opcode.NEQ || inst.getOpcode() == Opcode.NOT) {
				// Inst next = iter.peakNext();
				Inst next = iter.next();
				iter.previous();
				
				List<InstParam> next_params = next.getParameters();
				
				// Check if the type was the positive branch 'brz'
				boolean positive_br = next.getOpcode() == Opcode.JZ;
				
				// Check if the last element is a zero
				if (inst.getOpcode() == Opcode.NOT) {
					next_params.set(0, inst.getParam(1));
					next.setOpcode((positive_br) ? Opcode.JNZ : Opcode.JZ);
					iter.remove();
				} else {
					InstParam reg = inst.getParam(inst.getParamCount() - 1);
					if (reg instanceof InstParam.Num regNum && regNum.getValue() == 0) {
						// Check if the equality result is referenced
						
						int refs = getReferences(func, inst.getParam(0));
						if (refs < 3) {
							iter.remove(); // Remove the instruction...
						}
						
						next_params.set(0, inst.getParam(1));
						next.setOpcode((positive_br == positive_eq) ? Opcode.JNZ : Opcode.JZ);
					}
				}
			}
		}
	}
	
	private void mov_bnz_optimization(Procedure func) {
		var iter = func.getInstructions().listIterator();
		
		while (iter.hasNext()) {
			Inst inst = iter.next();
			if (!iter.hasNext())
				break;
			
			if (inst.getOpcode() != Opcode.MOV)
				continue;
			Inst next = iter.next();
			iter.previous();
			List<InstParam> next_params = next.getParameters();
			
			if (next.getOpcode() == Opcode.JZ) {
				next_params.set(0, inst.getParam(1));
			}
		}
	}
	
	/**
	 * This optimization reduces the amount of registers to
	 * the lowest amount possible by counting and replacing.
	 */
	private void counter_optimization(Procedure func) {
		if (true) {
			return;
		}
		Map<Integer, InstRef> map = new HashMap<>();
		int index = 0;
		
		var iter = func.getInstructions().listIterator();
		
		while (iter.hasNext()) {
			Inst inst = iter.next();
			
			List<InstParam> inst_params = inst.getParameters();
			for (int i = 0; i < inst_params.size(); i++) {
				InstParam param = inst.getParam(i);
				if (!(param instanceof InstParam.Ref reg))
					continue;
				
				// Only optimize generated registers and not variable registers.
				boolean var = reg.getReference().isVariable();
				if (var)
					continue;
				
				InstRef next = map.get(reg.getReference().getId());
				if (next == null) {
					InstRef old = reg.getReference();
					next = new InstRef(old.getName(), old.getNamespace(), old.getValueType(), index++, old.getFlags());
					map.put(reg.getReference().getId(), next);
				}
				
				inst_params.set(i, new InstParam.Ref(next));
			}
		}
	}
	
	private void flow_optimization(Procedure func) {
		int oldSize = 0;
		
		// While the code is changed we update it.
		while (oldSize != func.getInstructions().size()) {
			oldSize = func.getInstructions().size();
			
			var iter = func.getInstructions().listIterator();
			
			while (iter.hasNext()) {
				Inst inst = iter.next();
				List<InstParam> inst_params = inst.getParameters();
				// add [a], [b], [c]
				// check if a has been used inside the block..
				
				if (iter.hasNext()) {
					Inst next = iter.next();
					iter.previous();
					List<InstParam> next_params = next.getParameters();
					
					//    ... [a], [b], [c]
					//    mov [z], [a]
					// Should become
					//    ... [z], [b], [c]
					//    If z was zero before.
					if (next.getOpcode() == Opcode.MOV && canReduce(inst.getOpcode())) {
						InstParam p_reg = inst_params.get(0);
						InstParam p_wnt = next_params.get(1);
						if ((p_reg instanceof InstParam.Ref reg)
							&& (p_wnt instanceof InstParam.Ref wnt)) {
							// System.out.println("-".repeat(30));
							// System.out.println(inst);
							// System.out.println(next);
							// System.out.println(getReferences(func, reg));
							if (getReferences(func, reg) == 2 && wnt.getReference().equals(reg.getReference())) {
								inst_params.set(0, next_params.get(0));
								
								iter.next();
								iter.remove();
								iter.previous();
								continue;
							}
						}
					}
				}
				
				// If x has not been modified and y has not been modified then
				// replace all further instructions read [ ... ], [y] with [x]
				//    read [x], [y]
				
				if (!inst_params.isEmpty()) {
					InstParam reg = inst_params.get(0);
					
					// Only remove temporary variables.
					if (reg instanceof InstParam.Ref ref) {
						int num = getReferences(func, reg);
						if (num < 2) {
							if (!keepIfNotReferences(inst.getOpcode())) {
								// TODO: There could be a problem if the register is pointing towards a global variable.
								// System.out.println("Remove: " + inst + ", " + ref.getReference());
								// iter.remove();
							}
						}
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
	private void pass_though_label_optimization(Procedure func) {
		var iter = func.getInstructions().listIterator(); // IRPrintUtils.createIterator(func);
		List<Inst> list = func.getInstructions();
		
		while (iter.hasNext()) {
			Inst inst = iter.next();
			List<InstParam> inst_params = inst.getParameters();
			
			switch (inst.getOpcode()) {
				case JNZ, JZ, JMP -> {
					break;
				}
				
				default -> {
					continue;
				}
			}
			
			int paramIndex = inst.getParamCount() == 2 ? 1 : 0;
			// int paramIndex = inst.getOpcode().args == 2 ? 1 : 0;
			InstParam.Ref label = (InstParam.Ref) inst.getParam(paramIndex);
			
			int currentListIndex = iter.nextIndex() - 1;
			int index = findLabel(func, currentListIndex, label);
			
			if (inst.getOpcode() == Opcode.JMP && currentListIndex == index - 1) {
				// We can remove the current inst here
				iter.remove();
				continue;
			}
			
			// Check if the index is followed by a br or a label
			if (index + 1 < list.size()) {
				Inst pass = list.get(index + 1);
				
				if (pass.getOpcode() == Opcode.JMP || pass.getOpcode() == Opcode.LABEL) {
					inst_params.set(paramIndex, pass.getParam(0));
				}
			}
		}
	}
	
	// TODO: Dead code optimization
	//   If an instruction is inside a code block that will never be entered or if a
	//   label is never jumped to or is proceeded by another label it should be removed.
	private void dead_code_optimization(Procedure func) {
		var iter = func.getInstructions().listIterator(); // IRPrintUtils.createIterator(func);
		
		while (iter.hasNext()) {
			Inst inst = iter.next();
			
			if (!(inst.getOpcode() == Opcode.RET || inst.getOpcode() == Opcode.JMP))
				continue;
			
			while (iter.hasNext()) {
				Inst next = iter.next();
				
				if (next.getOpcode() != Opcode.LABEL) {
					iter.remove();
					continue;
				}
				
				break;
			}
		}
	}
	
	private int findLabel(Procedure func, int pivot, InstParam label) {
		List<Inst> list = func.getInstructions();
		
		// TODO: Maybe implement a pivot based search.
		for (int i = 0; i < list.size(); i++) {
			Inst inst = list.get(i);
			
			if (inst.getOpcode() == Opcode.LABEL) {
				if (inst.getParam(0).equals(label)) {
					return i;
				}
			}
		}
		
		return -1;
	}
	
	private int getReferences(Procedure func, InstParam reg) {
		if (!(reg instanceof InstParam.Ref ref)) {
			return 0;
		}
		
		int references = 0;
		for (Inst inst : func.getInstructions()) {
			for (InstParam r : inst.getParameters()) {
				if (r instanceof InstParam.Ref t) {
					if (t.getReference().equals(ref.getReference())) {
						references++;
					}
				} else if (reg.equals(r)) {
					references++;
				}
			}
		}
		
		return references;
	}
	
	private boolean keepIfNotReferences(Opcode type) {
		switch (type) {
			case CALL, STORE, RET -> {
				return true;
			}
			
			default -> {
				return false;
			}
		}
	}
	
	private boolean canReduce(Opcode type) {
		switch (type) {
			case JNZ, JZ, LABEL, RET, TRUNC, SEXT, ZEXT -> {
				return false;
			}
			
			default -> {
				return true;
			}
		}
	}
}
