package com.hardcoded.compiler;

import java.io.*;

import com.hardcoded.compiler.impl.context.LinkerScope;
import com.hardcoded.compiler.impl.serial.SerialParseTree;
import com.hardcoded.compiler.impl.statement.ProgramStat;
import com.hardcoded.compiler.lexer.AmpleLexer;
import com.hardcoded.compiler.lexer.Lang;
import com.hardcoded.compiler.linker.AmpleLinker;
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
		
		
		LOGGER.debug("PARSE_TREE");
		AmpleParseTree parse_tree = new AmpleParseTree();
		ProgramStat stat = parse_tree.process(options, lang);
		
		LOGGER.debug("PARSE_TREE_VALIDATOR");
		AmpleTreeValidator tree_validator = new AmpleTreeValidator();
		LinkerScope link = tree_validator.process(options, stat);
		
		try {
			File file = new File("res/test/oos.serial");
			
			FileOutputStream out = new FileOutputStream(file);
			SerialParseTree serial_0 = SerialParseTree.write(out, stat, link);
			out.close();
			
			FileInputStream in = new FileInputStream(file);
			SerialParseTree serial_1 = SerialParseTree.read(in);
			in.close();
			
			String string_0, string_1;
			string_0 = ObjectUtils.deepPrint(serial_0.getLinkerScope(), 24);
			string_1 = ObjectUtils.deepPrint(serial_1.getLinkerScope(), 24);
			System.out.println("Equality: " + (string_0.equals(string_1)));
		} catch(Exception e) {
			e.printStackTrace();
		}
		
		LOGGER.debug("LINKER");
		AmpleLinker linker = new AmpleLinker();
		//linker.process(options, stat, link);
	}
}
