package me.hardcoded.interpreter;

import me.hardcoded.compiler.intermediate.inst.InstFile;
import me.hardcoded.interpreter.AmpleFile.AmpleFunc;

/**
 * Ample code context class for running the language
 */
class AmpleRunner {
	
	public void run(InstFile instFile) throws AmpleInterpreterException {
		AmpleFile context = new AmpleFile(instFile);
		AmpleFunc main = context.getMainFunction();
		
		if (main == null) {
			throw new AmpleInterpreterException();
		}
		
		// Update the inst format
		runFunction(main, context);
	}
	
	public void runFunction(AmpleFunc func, AmpleFile context) {
		// TODO: Implement proper stack
		int index = 0;
	}
}
