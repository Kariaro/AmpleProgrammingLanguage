package me.hardcoded.interpreter;

import me.hardcoded.compiler.intermediate.inst.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Converts from an InstFile to a runnable interpreted file
 */
class AmpleContext {
	private final Map<InstRef, AmpleFunc> functions;
	private final AmpleFunc mainFunction;
	
	public AmpleContext(IntermediateFile file) {
		this.functions = new HashMap<>();
		
		InstRef mainRef = null;
		for (Procedure proc : file.getProcedures()) {
			functions.put(proc.getReference(), new AmpleFunc(proc));
			if ("main".equals(proc.getReference().getName())) {
				mainRef = proc.getReference();
			}
		}
		
		this.mainFunction = functions.get(mainRef);
	}
	
	public AmpleFunc getMainFunction() {
		return mainFunction;
	}
	
	public AmpleFunc getFunction(InstRef reference) {
		return functions.get(reference);
	}
	
	public static class AmpleFunc {
		private final Procedure procedure;
		private final Map<InstRef, Integer> labels;
		
		private AmpleFunc(Procedure procedure) {
			this.procedure = procedure;
			this.labels = new HashMap<>();
			
			List<Inst> list = procedure.getInstructions();
			for (int i = 0; i < list.size(); i++) {
				Inst inst = list.get(i);
				
				if (inst.getOpcode() == Opcode.LABEL) {
					InstRef ref = inst.getRefParam(0).getReference();
					labels.put(ref, i);
				}
			}
		}
		
		public int getLabel(InstRef reference) {
			return labels.get(reference);
		}
		
		public List<Inst> getInstructions() {
			return procedure.getInstructions();
		}
	}
}
