package me.hardcoded.interpreter;

import me.hardcoded.compiler.intermediate.inst.*;
import me.hardcoded.compiler.parser.type.ValueType;
import me.hardcoded.interpreter.value.Memory;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Converts from an InstFile to a runnable interpreted file
 */
class AmpleContext {
	private final Map<InstRef, AmpleFunc> functions;
	private final List<AmpleFunc> codeBlocks;
	private final AmpleFunc mainFunction;
	private final Memory memory;
	
	public AmpleContext(IntermediateFile file) {
		this.functions = new HashMap<>();
		this.codeBlocks = new ArrayList<>();
		
		int blocks = 0;
		InstRef mainRef = null;
		for (Procedure proc : file.getProcedures()) {
			switch (proc.getType()) {
				case FUNCTION -> {
					functions.put(proc.getReference(), new AmpleFunc(proc));
					if ("main".equals(proc.getReference().getName())) {
						mainRef = proc.getReference();
					}
				}
				case CODE -> {
					// We want to save these
					codeBlocks.add(new AmpleFunc(proc));
				}
			}
		}
		
		this.mainFunction = functions.get(mainRef);
		this.memory = new Memory();
	}
	
	public Memory getMemory() {
		return memory;
	}
	
	public AmpleFunc getMainFunction() {
		return mainFunction;
	}
	
	public AmpleFunc getFunction(InstRef reference) {
		return functions.get(reference);
	}
	
	public int getCodeBlocks() {
		return codeBlocks.size();
	}
	
	public AmpleFunc getCodeBlock(int index) {
		return codeBlocks.get(index);
	}
	
	public static class AmpleFunc {
		private final Procedure procedure;
		private final Map<InstRef, Integer> labels;
		
		public static int getTypeByteSize(ValueType type) {
			return (type.getDepth() > 0) ? ValueType.getPointerSize() : (type.getSize() >> 3);
		}
		
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
		
		public int getParamCount() {
			return procedure.getParameters().size();
		}
		
		public boolean isVararg() {
			List<InstRef> parameters = procedure.getParameters();
			return !parameters.isEmpty() && parameters.get(parameters.size() - 1).getValueType().isVarargs();
		}
		
		public List<InstRef> getParameters() {
			return procedure.getParameters();
		}
		
		public int getLabel(InstRef reference) {
			return labels.get(reference);
		}
		
		public List<Inst> getInstructions() {
			return procedure.getInstructions();
		}
		
		@Override
		public String toString() {
			return "AmpleFunc name='" + procedure.getReference() + "'";
		}
	}
}
