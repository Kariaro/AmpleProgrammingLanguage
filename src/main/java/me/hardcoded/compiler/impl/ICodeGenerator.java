package me.hardcoded.compiler.impl;

import me.hardcoded.utils.error.CodeGenException;

public interface ICodeGenerator {
	/**
	 * Returns an output byte array from an input {@code IRProgram}.
	 * 
	 * @param program the {@code Object} to export
	 * @return a byte array
	 */
	default byte[] getBytecode(Object program) throws CodeGenException {
		return null;
	}
	
	/**
	 * Returns the current program as this instruction sets assembly language.
	 * @param program the {@code Object} to export
	 * @return a string
	 */
	default byte[] getAssembler(Object program) throws CodeGenException {
		return null;
	}
	
	/**
	 * Called uppon reseting the code generator.
	 */
	@Deprecated
	void reset();
}
