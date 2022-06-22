package me.hardcoded.exporter.asm;

import me.hardcoded.compiler.intermediate.inst.Inst;
import me.hardcoded.compiler.intermediate.inst.InstParam;
import me.hardcoded.compiler.intermediate.inst.InstRef;
import me.hardcoded.compiler.intermediate.inst.Procedure;
import me.hardcoded.compiler.parser.type.ValueType;

import java.util.*;

class AsmProcedure {
	private final Map<InstRef, Integer> stackOffset;
	private int paramSize;
	private int stackSize;
	
	public AsmProcedure(Procedure procedure) {
		stackOffset = new HashMap<>();
		{
			List<InstRef> params = procedure.getParameters();
			for (InstRef ref : params) {
				ValueType type = ref.getValueType();
				paramSize += AsmCodeGenerator.getTypeByteSize(type);
			}
			
			// -8 is params
			int offset = 0;
			for (InstRef ref : params) {
				stackOffset.put(ref, offset - paramSize - 8);
				offset += AsmCodeGenerator.getTypeByteSize(ref.getValueType());
			}
		}
		
		{
			Set<InstRef> seenVariables = new HashSet<>();
			for (Inst inst : procedure.getInstructions()) {
				for (InstParam param : inst.getParameters()) {
					if (param instanceof InstParam.Ref refParam) {
						InstRef reference = refParam.getReference();
						if (reference.isVariable() && seenVariables.add(reference)) {
							stackOffset.put(reference, stackSize + 8);
							stackSize += AsmCodeGenerator.getTypeByteSize(reference.getValueType());
						}
					}
				}
			}
		}
	}
	
	public int getStackOffset(InstRef ref) {
		// Throw exception if undefined
		return stackOffset.get(ref);
	}
	
	public int getStackSize() {
		return stackSize;
	}
}
