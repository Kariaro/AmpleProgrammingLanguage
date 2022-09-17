package me.hardcoded.interpreter;

import me.hardcoded.compiler.intermediate.inst.Inst;
import me.hardcoded.compiler.intermediate.inst.InstParam;
import me.hardcoded.compiler.intermediate.inst.IntermediateFile;
import me.hardcoded.interpreter.AmpleContext.AmpleFunc;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * Ample code context class for running the language
 */
class AmpleRunner {
	private static final Logger LOGGER = LogManager.getLogger(AmpleRunner.class);
	
	public void run(IntermediateFile instFile) throws AmpleInterpreterException {
		AmpleContext context = new AmpleContext(instFile);
		AmpleFunc main = context.getMainFunction();
		
		if (main == null) {
			throw new AmpleInterpreterException();
		}
		
		// Update the inst format
		runFunction(main, context);
	}
	
	public void runFunction(AmpleFunc func, AmpleContext context) {
		// TODO: Implement proper stack
		LOGGER.debug("runFunction: {}", func);
		
		int index = 0;
		while (index < 100) {
			index = executeInstruction(index, func, context);
		}
	}
	
	public int executeInstruction(int index, AmpleFunc func, AmpleContext context) {
		Inst inst = func.getInstructions().get(index);
		
		LOGGER.debug("  exec: ({})", inst);
		switch (inst.getOpcode()) {
			case LABEL -> {
				return index + 1;
			}
			case STACK_ALLOC -> {
				InstParam.Ref ref = inst.getRefParam(0);
				InstParam.Num num = inst.getNumParam(1);
				
			}
			default -> {
				throw new RuntimeException("Unknown instruction '%s'".formatted(inst.getOpcode()));
			}
		}
		
		return index + 1;
	}
}
