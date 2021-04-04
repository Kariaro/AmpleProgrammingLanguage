package com.hardcoded.compiler.linker;

import com.hardcoded.compiler.impl.context.LinkerScope;
import com.hardcoded.compiler.impl.context.Reference;
import com.hardcoded.compiler.impl.statement.*;
import com.hardcoded.logger.Log;
import com.hardcoded.options.Options;

/**
 * @author HardCoded
 * @since 0.2.0
 */
public class AmpleLinker {
	private static final Log LOGGER = Log.getLogger(AmpleLinker.class);
	
	public AmpleLinker() {
		
	}
	
	public void process(Options options, ProgramStat stat, LinkerScope link) {
		LOGGER.debug("Started linker");
		
		/* Debug */ {
			System.out.println("----------------");
			System.out.println("Imports:");
			for(String str : link.getImportedFiles()) {
				System.out.printf("  : (%s)\n", str);
			}
			
			System.out.println("\nImported:");
			for(Reference ref : link.getImport()) {
				System.out.printf("  %4d: (%s) %s\n", ref.getUniqueIndex(), ref.getType(), ref.getName());
			}
			System.out.println("\nExported:");
			for(Reference ref : link.getExport()) {
				System.out.printf("  %4d: (%s) %s\n", ref.getUniqueIndex(), ref.getType(), ref.getName());
			}
		}
	}
}
