package hardcoded.vm;

import java.util.*;

import hardcoded.compiler.expression.LowType;
import hardcoded.compiler.instruction.IRFunction;
import hardcoded.compiler.instruction.IRInstruction;
import hardcoded.compiler.instruction.IRInstruction.Param;
import hardcoded.compiler.instruction.IRInstruction.Reg;
import hardcoded.compiler.instruction.IRType;

class VmFunction {
	private final IRFunction func;
	private final IRInstruction[] array;
	private final Map<String, Integer> labels;
	private final int[] registers;
	public final int bodySize;
	public final int args;
	public final int stack;
	
	
	public VmFunction(IRFunction func) {
		this.func = func;
		this.array = func.getInstructions().clone();
		this.labels = new HashMap<>();
		
		Map<Integer, LowType> sizes = new HashMap<>();
		for(int i = 0; i < array.length; i++) {
			IRInstruction inst = array[i];
			
			if(inst.type() == IRType.label) {
				labels.put(inst.getParam(0).getName(), i + 1);
			}
			
			for(Param param : inst.getParams()) {
				if(!(param instanceof Reg)) continue;
				Reg reg = (Reg)param;
				
				if(!reg.isTemporary()) continue;
				sizes.put(reg.getIndex(), reg.getSize());
			}
		}
		
		this.args = func.getNumParams();
		this.stack = sizes.size();
		this.registers = new int[args + stack];
		
		int offset = 0;
		LowType[] params = func.getParams();
		for(int i = 0; i < params.length; i++) {
			LowType type = params[i];
			int size = type.isPointer() ? LowType.getPointerSize():type.size();
			registers[i] = offset;
			offset += size;
		}
		
		for(int i = 0; i < stack; i++) {
			LowType type = sizes.get(i);
			int size = type.isPointer() ? LowType.getPointerSize():type.size();
			registers[i + params.length] = offset;
			offset += size;
		}
		bodySize = offset;
		
//		List<Integer> test = new ArrayList<>();
//		for(int i = 0; i < registers.length; i++) {
//			test.add(registers[i]);
//		}
//		System.out.println(test);
//		System.out.println("body = " + bodySize);
	}
	
	public String getName() {
		return func.getName();
	}
	
	public IRInstruction[] getInstructions() {
		return array;
	}
	
	public IRInstruction getInstruction(int index) {
		return array[index];
	}
	
	public int getNumInstructions() {
		return array.length;
	}
	
	public int getLabel(String name) {
		return labels.get(name);
	}
	
	public int getRegister(Reg reg) {
		if(reg.isTemporary()) {
			return registers[args + reg.getIndex()];
		}
		
		return registers[args - 1 - reg.getIndex()];
	}
	
	public int getArgs(int index) {
		return registers[args - 1 - index];
	}
	
	public String toString() {
		return func.toString();
	}

	public LowType getType() {
		return func.getReturnType();
	}

	public LowType getParamSize(int i) {
		return func.getParams()[i];
	}
}
