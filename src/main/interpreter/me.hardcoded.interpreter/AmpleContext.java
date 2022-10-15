package me.hardcoded.interpreter;

import me.hardcoded.compiler.intermediate.inst.*;
import me.hardcoded.compiler.parser.type.ValueType;
import me.hardcoded.interpreter.value.Value;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

/**
 * Converts from an InstFile to a runnable interpreted file
 */
class AmpleContext {
	private final Map<InstRef, AmpleFunc> functions;
	private final AmpleFunc mainFunction;
	
	// Memory
	private final Map<Long, Value.ArrayValue> allocatedMemory;
	private final LinkedList<Long> availableMemory;
	private long nextAllocated;
	
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
		this.allocatedMemory = new HashMap<>();
		this.availableMemory = new LinkedList<>();
		
		// Allocate nullptr
		allocate(0);
	}
	
	public Value.ArrayValue allocate(int size) {
		long idx;
		if (availableMemory.isEmpty()) {
			idx = nextAllocated++;
		} else {
			idx = availableMemory.removeFirst();
		}
		Value.ArrayValue value = new Value.ArrayValue(idx << 32, size);
		allocatedMemory.put(idx, value);
		return value;
	}
	
	public Value.ArrayValue allocateString(String string) {
		long idx;
		if (availableMemory.isEmpty()) {
			idx = nextAllocated++;
		} else {
			idx = availableMemory.removeFirst();
		}
		Value.StringValue value = new Value.StringValue(idx << 32, string);
		allocatedMemory.put(idx, value);
		return value;
	}
	
	public void deallocate(long address) {
		if ((address >> 32) == 0) {
			// nullptr
			return;
		}
		
		allocatedMemory.remove(address);
		availableMemory.push(address >> 32);
	}
	
	public Value.ArrayValue getAllocated(long address) {
		Value.ArrayValue arrayValue = allocatedMemory.get(address >> 32);
		if (arrayValue != null && (int) address != 0) {
			return new Value.OffsetArrayValue(arrayValue, (int) address);
		}
		return arrayValue;
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
