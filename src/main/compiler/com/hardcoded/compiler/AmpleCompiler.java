package com.hardcoded.compiler;

import java.io.IOException;

import com.hardcoded.compiler.impl.statement.ProgramStat;
import com.hardcoded.compiler.lexer.AmpleLexer;
import com.hardcoded.compiler.lexer.Lang;
import com.hardcoded.compiler.parsetree.AmpleParseTree;
import com.hardcoded.compiler.parsetree.AmpleTreeValidator;
import com.hardcoded.logger.Log;
import com.hardcoded.options.Options;
import com.hardcoded.options.Options.Key;
import com.hardcoded.utils.FileUtils;

/**
 * 
 * @author HardCoded
 * @since 0.2.0
 */
public class AmpleCompiler {
	private static final Log LOGGER = Log.getLogger(AmpleCompiler.class);
	
	public AmpleCompiler() {
		
	}
	
	public void singleBuild(Options options) {
		String path = options.get(Key.INPUT_FILE);
		byte[] bytes = new byte[0];
		try {
			bytes = FileUtils.readFileBytes(path);
		} catch(IOException e) {
			LOGGER.throwing(e);
		}
		
		Lang lang = Lang.wrap(AmpleLexer.getLexer().parse(bytes));
		
		LOGGER.info("Starting build");
		AmpleParseTree parse_tree = new AmpleParseTree();
		ProgramStat stat = parse_tree.process(options, lang);
		
		AmpleTreeValidator tree_validator = new AmpleTreeValidator();
		tree_validator.process(options, stat);
		
		//AmpleLinker linker = new AmpleLinker();
		//linker.process(options, stat);
	}
}
