package hardcoded.compiler.impl;

import hardcoded.compiler.instruction.IRProgram;
import hardcoded.utils.error.CodeGenException;

public interface ICodeGenerator {
	/**
	 * Returns a output byte array from a input {@code IRProgram}.
	 * 
	 * @param program the {@code IRProgram} to export
	 * @return a byte array
	 */
	byte[] getBytecode(IRProgram program) throws CodeGenException;
	
	/**
	 * Returns the current program as this instruction sets assembly language.
	 * @param program the {@code IRProgram} to export
	 * @return a string
	 */
	byte[] getAssembler(IRProgram program) throws CodeGenException;
	
	/**
	 * Called uppon reseting the code generator.
	 */
	@Deprecated
	void reset();
}
