package com.hardcoded.compiler.llcode;

import com.hardcoded.compiler.api.Instruction;
import com.hardcoded.compiler.api.Statement;
import com.hardcoded.compiler.impl.instruction.*;
import com.hardcoded.logger.Log;
import com.hardcoded.options.Options;

/**
 * A code optimizer
 * 
 * @author HardCoded
 * @since 0.2.0
 */
public class AmpleCodeOptimizer {
	private static final Log LOGGER = Log.getLogger();
	
	public AmpleCodeOptimizer() {
		
	}
	
	public ImCode process(Options options, ImCode code) {
		ImCode copy = new ImCode();
		
		for(InstList list : code.list()) {
			InstList list_optimized = optimize(list.clone());
			
			System.out.println("---------------------------");
			LOGGER.debug("Before (%d) after (%d)", list.size(), list_optimized.size());
			
			System.out.printf(":================:aaaaaaaaaaaaaaaaccccccccccccc %08x\n", list.hashCode());
			
			for(Inst inst : list_optimized.list()) {
				Instruction.Type type = inst.getType();
				if(type == Instruction.Type.MARKER) continue;
				if(type == Instruction.Type.LABEL) {
					System.out.printf("  %s\n", inst.getParam(0));
				} else {
					System.out.printf("    %s\n", inst);
				}
			}
			
			System.out.println("---------------------------");
			
			copy.push(list_optimized);
		}
		
		return copy;
	}
	
	private InstList optimize(InstList list) {
		
		return list;
	}
	
	private Inst optimize(Inst inst) {
		return inst;
	}
	
	<T> T throw_exception(Statement stat, String format, Object... args) {
		String extra = String.format("(line: %d, column: %d) ", stat.getStartOffset(), stat.getEndOffset());
		throw new CodeGenException(extra + format, args);
	}
}
