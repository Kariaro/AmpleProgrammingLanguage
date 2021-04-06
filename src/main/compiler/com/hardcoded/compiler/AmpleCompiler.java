package com.hardcoded.compiler;

import java.io.IOException;
import java.util.List;

import com.hardcoded.compiler.api.Instruction;
import com.hardcoded.compiler.impl.context.LinkerScope;
import com.hardcoded.compiler.impl.instruction.ImCode;
import com.hardcoded.compiler.impl.instruction.Inst;
import com.hardcoded.compiler.impl.instruction.InstList;
import com.hardcoded.compiler.impl.statement.ProgramStat;
import com.hardcoded.compiler.lexer.AmpleLexer;
import com.hardcoded.compiler.lexer.Lang;
import com.hardcoded.compiler.llcode.AmpleCodeGenerator;
import com.hardcoded.compiler.parsetree.AmpleParseTree;
import com.hardcoded.compiler.parsetree.AmpleTreeIndexer;
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
		
		LOGGER.debug("PARSE_TREE_INDEXER");
		AmpleTreeIndexer tree_indexer = new AmpleTreeIndexer();
		LinkerScope link = tree_indexer.process(options, stat);
		
//		try {
//			File file = new File("res/test/oos.serial");
//			
//			FileOutputStream out = new FileOutputStream(file);
//			SerialParseTree serial_0 = SerialParseTree.write(out, stat, link);
//			out.close();
//			
//			FileInputStream in = new FileInputStream(file);
//			SerialParseTree serial_1 = SerialParseTree.read(in);
//			in.close();
//			
//			String string_0, string_1;
//			string_0 = ObjectUtils.deepPrint(serial_0.getStatement(), 100);
//			string_1 = ObjectUtils.deepPrint(serial_1.getStatement(), 100);
//			System.out.println("Equality: " + (string_0.equals(string_1)));
//			
////			int line = 0;
////			for(int i = 0; i < string_0.length() - 1000; i += 1000) {
////				String str_0 = string_0.substring(i, i + 1000);
////				String str_1 = string_1.substring(i, i + 1000);
////				
////				line += str_0.length() - str_0.replace("\n", "").length();
////				
////				if(!str_0.equals(str_1)) {
////					System.out.println("Line: " + line);
////					break;
////				}
////			}
//		} catch(Exception e) {
//			e.printStackTrace();
//		}
		
		LOGGER.debug("INTERMEDIATE_CODE_GENERATION");
		AmpleCodeGenerator code_generator = new AmpleCodeGenerator();
		ImCode code = code_generator.process(options, stat);
		
		{
			List<InstList> lists = code.list();
			
			for(InstList list : lists) {
				System.out.printf(":================: %08x\n", list.hashCode());
				
				for(Inst inst : list.list()) {
					Instruction.Type type = inst.getType();
					if(type == Instruction.Type.MARKER) continue;
					if(type == Instruction.Type.LABEL) {
						System.out.printf("  %s\n", inst.getParam(0));
					} else {
						System.out.printf("    %s\n", inst);
					}
				}
			}
		}
		
		//LOGGER.debug("LINKER");
		//AmpleLinker linker = new AmpleLinker();
		//linker.process(options, stat, link);
	}
}
