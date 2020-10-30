package hardcoded.compiler.instruction;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import hardcoded.compiler.Function;
import hardcoded.compiler.expression.LowType;

public class IRProgram {
	/**
	 * All context type data.
	 */
	protected final IRContext context;
	
	/**
	 * All the functions inside this IRProgram
	 */
	protected final List<IRFunction> list;
	
	protected IRProgram(IRContext context, List<IRFunction> list) {
		this.context = context;
		this.list = list;
	}
	
	protected IRProgram() {
		context = new IRContext();
		list = new ArrayList<>();
	}
	
	public List<IRFunction> getFunctions() {
		return Collections.unmodifiableList(list);
	}
	
	public IRContext getContext() {
		return context;
	}
	
	protected IRFunction addFunction(Function func, List<IRInstruction> list) {
		LowType[] params = new LowType[func.arguments.size()];
		for(int i = 0; i < params.length; i++) {
			params[i] = func.arguments.get(i).getLowType();
		}
		
		IRFunction ir_func = new IRFunction(func.returnType.type(), func.name, params);
		
		for(IRInstruction inst : list) {
			ir_func.list.add(inst);
		}
		
		this.list.add(ir_func);
		return ir_func;
	}
}
