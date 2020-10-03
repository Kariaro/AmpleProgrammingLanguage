package hardcoded.compiler;

import java.io.File;
import java.util.List;

import hardcoded.compiler.Block.Function;
import hardcoded.compiler.assembler.AssemblyCodeExporter;
import hardcoded.compiler.assembler.AssemblyCodeGenerator;
import hardcoded.compiler.assembler.AssemblyCodeOptimizer;
import hardcoded.compiler.constants.Utils;
import hardcoded.compiler.instruction.InstructionBlock;
import hardcoded.compiler.instruction.IntermediateCodeGenerator;
import hardcoded.compiler.instruction.IntermediateCodeOptimizer;
import hardcoded.compiler.parsetree.ParseTreeGenerator;
import hardcoded.compiler.parsetree.ParseTreeOptimizer;
import hardcoded.errors.CompilerException;
import hardcoded.visualization.HCVisualization;

public class HCompilerBuild {
	private File projectPath = new File("res/project/src/");
	
	private Program current_program;
	
	/**
	 * The parse tree generator.
	 */
	private ParseTreeGenerator parse_tree_generator;

	/**
	 * The parse tree optimizer.
	 */
	private ParseTreeOptimizer parse_tree_optimizer;
	
	/**
	 * The instruction generator.
	 */
	private IntermediateCodeGenerator icg;
	
	/**
	 * The instruction optimzier.
	 */
	private IntermediateCodeOptimizer ico;
	
	private AssemblyCodeGenerator acg;
	private AssemblyCodeOptimizer aco;
	private AssemblyCodeExporter ace;
	
	public HCompilerBuild() {
		parse_tree_generator = new ParseTreeGenerator();
		parse_tree_optimizer = new ParseTreeOptimizer();
		icg = new IntermediateCodeGenerator();
		ico = new IntermediateCodeOptimizer();
		acg = new AssemblyCodeGenerator();
		aco = new AssemblyCodeOptimizer();
		ace = new AssemblyCodeExporter();
		
		String file = "main.hc";
		file = "tests/000_pointer.hc";
		// file = "tests/001_comma.hc";
		// file = "tests/002_invalid_assign.hc";
		// file = "tests/003_invalid_brackets.hc";
		// file = "tests/004_cor_cand.hc";
		// file = "tests/005_assign_comma.hc";
		// file = "tests/006_cast_test.hc";
		// file = "test_syntax.hc";
		
		// file = "tests_2/000_assign_test.hc";
		
		try {
			build(file);
		} catch(Exception e) {
			e.printStackTrace();
		}
	}
	
	private HCVisualization vs;
	
	/**
	 * Build the project.
	 * 
	 * @param	pathname	A pathname string.
	 * @throws	Exception
	 * @throws	CompilerException
	 * 			If the compilation failed
	 */
	public void build(String pathname) throws Exception {
		current_program = parse_tree_generator.init(projectPath, pathname);
		if(parse_tree_generator.hasErrors()) {
			throw new CompilerException("Compiler errors.");
		}

		vs = new HCVisualization();
		vs.hide();
		// vs.show(current_program);
		
		parse_tree_optimizer.do_constant_folding(/* vs, */ current_program);
		
		List<InstructionBlock> blocks;
		blocks = icg.generate(current_program);
		blocks = ico.generate(blocks);
		acg.generate(blocks);
		
//		System.out.println();
//		System.out.println();
//		System.out.println();
//		System.out.println();
//		
//		for(Block block : current_program.list()) {
//			if(!(block instanceof Function)) continue;
//			
//			System.out.println("========================================================");
//			Function func = (Function)block;
//			String str = Utils.printPretty(func);
//			System.out.println(str.replace("\t", "    "));
//		}
//		
//		System.out.println("========================================================");
	}
}
