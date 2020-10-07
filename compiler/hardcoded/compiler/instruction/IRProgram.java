package hardcoded.compiler.instruction;

import java.util.ArrayList;
import java.util.List;

import hardcoded.compiler.Block.Function;
import hardcoded.compiler.expression.LowType;

public class IRProgram {
	/**
	 * All the functions inside a IRProgram
	 */
	final List<IRFunction> list;
	
	/**
	 * All constant type data.
	 */
	final IRData data;
	
	IRProgram() {
		data = new IRData();
		list = new ArrayList<>();
	}
	
	public IRFunction[] getFunctions() {
		return list.toArray(new IRFunction[0]);
	}
	
	public IRData getIRData() {
		return data;
	}
	
	public IRFunction addFunction(Function func, IRInstruction start) {
		LowType[] params = new LowType[func.arguments.size()];
		for(int i = 0; i < params.length; i++) {
			params[i] = func.arguments.get(i).low_type();
		}
		
		IRFunction ir_func = new IRFunction(this, func.returnType.type(), func.name, params);
		
		for(IRInstruction inst : start) {
			ir_func.list.add(inst);
		}
		
		list.add(ir_func);
		return ir_func;
	}
}
