package me.hardcoded.exporter.impl;

import me.hardcoded.compiler.parser.stat.ProgStat;

public interface IAmpleExporter {
	/**
	 * Returns the assembler of the program
	 */
	String getAssembler(ProgStat program);
	
	/**
	 * Returns the bytecode of the program
	 */
	byte[] getBytecode(ProgStat program);
}
