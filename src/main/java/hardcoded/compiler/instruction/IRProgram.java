package hardcoded.compiler.instruction;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import hardcoded.compiler.constants.Identifier;
import hardcoded.compiler.expression.LowType;
import hardcoded.compiler.statement.Function;

/**
 * @author HardCoded
 */
public class IRProgram {
	/**
	 * All context type data.
	 */
	protected final IRContext context;
	
	/**
	 * All the functions inside this IRProgram.
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
		List<Identifier> func_arguments = func.getArguments();
		
		LowType[] params = new LowType[func_arguments.size()];
		for(int i = 0; i < params.length; i++) {
			params[i] = func_arguments.get(i).getLowType();
		}
		
		IRFunction ir_func = new IRFunction(func.getReturnType().type(), func.getName(), func_arguments);
		
		for(IRInstruction inst : list) {
			ir_func.list.add(inst);
		}
		
		this.list.add(ir_func);
		return ir_func;
	}
}
