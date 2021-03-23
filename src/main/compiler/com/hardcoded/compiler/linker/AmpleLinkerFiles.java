package com.hardcoded.compiler.linker;

import com.hardcoded.compiler.impl.statement.ProgramStat;
import com.hardcoded.logger.Log;
import com.hardcoded.options.Options;

/**
 * @author HardCoded
 * @since 0.2.0
 */
public class AmpleLinkerFiles {
	private static final Log LOGGER = Log.getLogger(AmpleLinkerFiles.class);
	
	public AmpleLinkerFiles() {
		
	}

	public void process(Options options, ProgramStat stat) {
		LOGGER.debug("Started linker");
		
	}
}
