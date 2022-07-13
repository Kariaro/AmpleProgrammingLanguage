package me.hardcoded.compiler.impl;

import me.hardcoded.compiler.intermediate.inst.InstFile;
import me.hardcoded.utils.error.CodeGenException;

public interface ICodeGenerator {
	/**
	 * Returns an output byte array from an input {@code IRProgram}
	 * 
	 * @param program the {@code Object} to export
	 * @return a byte array
	 */
	default byte[] getBytecode(InstFile program) throws CodeGenException {
		return null;
	}
	
	/**
	 * Returns the current program as this instruction sets assembly language
	 *
	 * @param program the {@code Object} to export
	 * @return a string
	 */
	default byte[] getAssembler(InstFile program) throws CodeGenException {
		return null;
	}
	
	/**
	 * Called upon resetting the code generator.
	 */
	@Deprecated
	void reset();
}
