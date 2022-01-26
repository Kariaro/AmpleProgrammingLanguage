package me.hardcoded.exporter.impl;

import me.hardcoded.compiler.instruction.IRProgram;

public interface IAmpleExporter {
//
//	/**
//	 * Returns the name of this exporter
//	 * @return the name of this exporter
//	 */
//	String getExportName();
//
//	/**
//	 * Returns the assembler instance
//	 * @return the assembler instance
//	 */
//	IAmpleAssembler getAssembler();
//
//	/**
//	 * Returns the code generator instance
//	 * @return the code generator instance
//	 */
//	IAmpleCodeGenerator getCodeGenerator();
//
	
	/**
	 * Returns the assembler of the program
	 */
	String getAssembler(IRProgram program);
	
	/**
	 * Returns the bytecode of the program
	 */
	byte[] getBytecode(IRProgram program);
}
