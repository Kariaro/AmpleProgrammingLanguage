package me.hardcoded.exporter.asm;

import me.hardcoded.compiler.intermediate.inst.*;

import java.util.*;

class AsmProcedure {
	private final Map<InstRef, Integer> stackOffset;
	private final List<InstRef> params;
	private final String name;
	private int stackSize;
	
	public AsmProcedure(Procedure procedure) {
		this.stackOffset = new HashMap<>();
		this.params = procedure.getParameters();
		this.name = procedure.getReference().toSimpleString();
		
		Set<InstRef> seenVariables = new HashSet<>();
		
		for (InstRef ref : params) {
			int size = AsmUtils.getTypeByteSize(ref.getValueType());
			stackSize += size; // ((size + 7) & ~7);
			stackOffset.put(ref, stackSize);
			seenVariables.add(ref);
		}
		
		for (Inst inst : procedure.getInstructions()) {
			for (InstParam param : inst.getParameters()) {
				if (param instanceof InstParam.Ref refParam) {
					InstRef reference = refParam.getReference();
					if (reference.isVariable() && seenVariables.add(reference)) {
						int size = AsmUtils.getTypeByteSize(reference.getValueType());
						stackSize += size; // ((size + 7) & ~7);
						stackOffset.put(reference, stackSize);
					}
				}
			}
			
			if (inst.getOpcode() == Opcode.STACK_ALLOC) {
				// If we see a stack alloc instruction we will allocate x amount of bytes on the stack
				InstRef dst = inst.getRefParam(0).getReference();
				
				if (dst.isVariable()) {
					// Add the size of the allocated stack
					int size = (int) inst.getNumParam(1).getValue();
					stackSize += size; // ((size + 7) & ~7);
				}
			}
		}
	}
	
	public String getName() {
		return name;
	}
	
	public int getParamCount() {
		return params.size();
	}
	
	public InstRef getParam(int index) {
		return params.get(index);
	}
	
	public int getStackOffset(InstRef ref) {
		// Throw exception if undefined
		return stackOffset.get(ref);
	}
	
	public int getStackSize() {
		return stackSize;
	}
}
