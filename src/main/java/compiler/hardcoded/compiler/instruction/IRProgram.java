package hardcoded.compiler.instruction;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import hardcoded.compiler.Block.Function;
import hardcoded.compiler.expression.LowType;

// TODO: Make this class serializable
public class IRProgram implements java.io.Serializable {
	private static final long serialVersionUID = -7513138804605254084L;

	/**
	 * All the functions inside a IRProgram
	 */
	private final List<IRFunction> list;
	
	/**
	 * All context type data.
	 */
	private final IRContext context;
	
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
	
	protected IRFunction addFunction(Function func, IRInstruction start) {
		LowType[] params = new LowType[func.arguments.size()];
		for(int i = 0; i < params.length; i++) {
			params[i] = func.arguments.get(i).low_type();
		}
		
		IRFunction ir_func = new IRFunction(func.returnType.type(), func.name, params);
		
		for(IRInstruction inst : start) {
			ir_func.list.add(inst);
		}
		
		list.add(ir_func);
		return ir_func;
	}
}
