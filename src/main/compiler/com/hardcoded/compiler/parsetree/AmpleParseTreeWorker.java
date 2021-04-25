package com.hardcoded.compiler.parsetree;

import com.hardcoded.compiler.impl.context.LinkerScope;
import com.hardcoded.compiler.impl.statement.ProgramStat;
import com.hardcoded.compiler.lexer.Lang;
import com.hardcoded.logger.Log;
import com.hardcoded.options.Options;

/**
 * A parse tree worker.
 * 
 * This worker will take a input {@code Lang} and convert it into a parse tree.
 * 
 * @author HardCoded
 * @since 0.2.0
 */
public class AmpleParseTreeWorker {
	private static final Log LOGGER = Log.getLogger();
	
	private ProgramStat stat;
	private LinkerScope link;
	
	public boolean process(Options options, Lang lang) {
		LOGGER.debug("PARSE_TREE");
		AmpleParseTree parse_tree = new AmpleParseTree();
		stat = parse_tree.process(options, lang);
		
		LOGGER.debug("PARSE_TREE_INDEXER");
		AmpleTreeIndexer tree_indexer = new AmpleTreeIndexer();
		link = tree_indexer.process(options, stat);
		
		LOGGER.debug("PARSE_TREE_OPTIMIZER");
		AmpleTreeOptimizer tree_optimizer = new AmpleTreeOptimizer();
		stat = tree_optimizer.process(options, stat);
		
		return true;
	}
	
	public ProgramStat getProgram() {
		return stat;
	}
	
	public LinkerScope getLink() {
		return link;
	}
}
