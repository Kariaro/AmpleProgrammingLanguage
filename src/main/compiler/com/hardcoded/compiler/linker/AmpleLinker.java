package com.hardcoded.compiler.linker;

import java.util.ArrayList;
import java.util.List;

import com.hardcoded.compiler.api.Statement;
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
	
	// Replace all missing local variables with a LinkerExpr(Reference(<name>, <linker_id>))
	protected List<String> imported_files = new ArrayList<>();
	protected List<String> imported_functions = new ArrayList<>();
	protected List<String> imported_variables = new ArrayList<>();
	protected List<String> exported_functions = new ArrayList<>();
	protected List<String> exported_variables = new ArrayList<>();
	public void process(Options options, ProgramStat stat) {
		LOGGER.debug("Started linker");
		
		processProgram(stat);
		
		LOGGER.debug("Imports:");
		LOGGER.debug("  Files    : %s", imported_files);
		LOGGER.debug("  Functions: %s", imported_functions);
		LOGGER.debug("  Variables: %s", imported_variables);
		LOGGER.debug();
		LOGGER.debug("Exports:");
		LOGGER.debug("  Functions: %s", exported_functions);
		LOGGER.debug("  Variables: %s", exported_variables);
	}
	
	void processProgram(ProgramStat stat) {
		for(Statement s : stat.getStatements()) {
			if(s instanceof FuncStat) {
				processFunction((FuncStat)s);
				continue;
			}
			
			if(s instanceof ImportStat) {
				processImport((ImportStat)s);
				continue;
			}
			
			if(s instanceof DefineStat) {
				processDefine((DefineStat)s);
				continue;
			}
			
			LOGGER.debug("Did not check: " + s.getClass());
		}
	}
	
	void processTree(Statement stat) {
		for(Statement s : stat.getStatements()) {
			processTree(s);
		}
	}
	
	void processImport(ImportStat stat) {
		imported_files.add(stat.toString());
	}
	
	void processDefine(DefineStat stat) {
		exported_variables.add(stat.toString());
	}
	
	void processFunction(FuncStat func) {
		// Error for redefined functions
		// Error when redeclared variable found
		
		exported_functions.add(func.getName().value);
		LOGGER.debug("Function: %s", func);
		
		// Try to find all missing values
	}
}
