package me.hardcoded.compiler.impl;

import me.hardcoded.compiler.context.AmpleConfig;
import me.hardcoded.compiler.intermediate.inst.IntermediateFile;
import me.hardcoded.utils.error.CodeGenException;

public abstract class ICodeGenerator {
	protected final AmpleConfig ampleConfig;
	
	protected ICodeGenerator(AmpleConfig ampleConfig) {
		this.ampleConfig = ampleConfig;
	}
	
	/**
	 * Returns an output byte array from an input {@code IRProgram}
	 * 
	 * @param program the {@code Object} to export
	 * @return a byte array
	 */
	public abstract byte[] getBytecode(IntermediateFile program) throws CodeGenException;
	
	/**
	 * Returns the current program as this instruction sets assembly language
	 *
	 * @param program the {@code Object} to export
	 * @return a string
	 */
	public abstract byte[] getAssembler(IntermediateFile program) throws CodeGenException;
	
	/**
	 * Called upon resetting the code generator.
	 */
	@Deprecated
	public abstract void reset();
}
