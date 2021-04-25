package com.hardcoded.compiler.llcode;

import com.hardcoded.compiler.impl.instruction.ImCode;
import com.hardcoded.compiler.impl.statement.ProgramStat;
import com.hardcoded.logger.Log;
import com.hardcoded.options.Options;

/**
 * A code generator
 * 
 * @author HardCoded
 * @since 0.2.0
 */
public class AmpleCodeWorker {
	private static final Log LOGGER = Log.getLogger();
	private ImCode code;
	
	public AmpleCodeWorker() {
		
	}
	
	public boolean process(Options options, ProgramStat stat) {
		LOGGER.debug("INTERMEDIATE_CODE_GENERATION");
		AmpleCodeGenerator code_generator = new AmpleCodeGenerator();
		code = code_generator.process(options, stat);
		
		LOGGER.debug("INTERMEDIATE_CODE_OPTIMIZATION");
		AmpleCodeOptimizer code_optimizer = new AmpleCodeOptimizer();
		code_optimizer.process(options, code);
		
		return true;
	}
	
	public ImCode getCode() {
		return code;
	}
}
