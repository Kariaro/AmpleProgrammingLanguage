package me.hardcoded.exporter.impl;

import me.hardcoded.compiler.parser.stat.ProgStat;

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
	String getAssembler(ProgStat program);
	
	/**
	 * Returns the bytecode of the program
	 */
	byte[] getBytecode(ProgStat program);
}
