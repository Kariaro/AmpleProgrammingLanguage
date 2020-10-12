package hardcoded.compiler;

import java.io.*;

import hardcoded.compiler.errors.CompilerException;
import hardcoded.compiler.instruction.*;
import hardcoded.compiler.parsetree.ParseTreeGenerator;
import hardcoded.compiler.parsetree.ParseTreeOptimizer;
import hardcoded.exporter.impl.CodeGeneratorImpl;
import hardcoded.exporter.spooky.SpookyCodeGenerator;
import hardcoded.exporter.x86.AssemblyCodeGenerator;
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
	
	/**
	 * The code exporter.
	 */
	private CodeGeneratorImpl cei;
	
	public HCompilerBuild() {
		parse_tree_generator = new ParseTreeGenerator();
		parse_tree_optimizer = new ParseTreeOptimizer();
		icg = new IntermediateCodeGenerator();
		ico = new IntermediateCodeOptimizer();
		cei = new AssemblyCodeGenerator();
		cei = new SpookyCodeGenerator();
		
		String file = "main.hc";
		file = "tests/000_pointer.hc";
		file = "prim.hc";
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
			
			System.out.println();
			System.out.println("+-----------------+");
			System.out.println("| COMPILED OUTPUT |");
			System.out.println("+-----------------+");
			System.out.println();
			
//			StringBuilder sb = new StringBuilder();
//			
//			int index = 1;
//			for(byte b : bytes) {
//				if(index++ > 31) {
//					index = 1;
//					sb.append(String.format("%02x\n", b));
//				} else {
//					sb.append(String.format("%02x ", b));
//				}
//			}
//			
//			System.out.println(sb.toString().trim());
		} catch(Exception e) {
			e.printStackTrace();
		}
	}
	
	private HCVisualization vs;
	
	/**
	 * Build the project.
	 * 
	 * @param	pathname	a pathname string
	 * @throws	Exception
	 * @throws	CompilerException
	 * 			If the compilation failed
	 * 
	 * @return a byte array of the compiled output
	 */
	public byte[] build(String pathname) throws Exception {
		current_program = parse_tree_generator.init(projectPath, pathname);
		if(parse_tree_generator.hasErrors()) {
			throw new CompilerException("Compiler errors.");
		}

		vs = new HCVisualization();
		vs.hide();
		// vs.show(current_program);
		
		parse_tree_optimizer.do_constant_folding(/* vs, */ current_program);
		
		IRProgram ir_program;
		ir_program = icg.generate(current_program);
		ir_program = ico.generate(ir_program);
		
		try {
			test(ir_program);
		} catch(IOException e) {
			e.printStackTrace();
		}
		
		return null;
		//return cei.generate(ir_program);
		
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
	
	public void test(IRProgram program) throws IOException {
		ByteArrayOutputStream bs = new ByteArrayOutputStream();
		IRSerializer.write(program, bs);
		byte[] array = bs.toByteArray();
		
		try {
			test2(program);
		} catch(Exception e) {
			e.printStackTrace();
		}
		
		
		File file = new File(System.getProperty("user.home") + "/Desktop/spooky/serial.lir");
		if(!file.getParentFile().exists())
			file.getParentFile().mkdirs();
		
		FileOutputStream stream = new FileOutputStream(file);
		stream.write(array);
		stream.close();
	}
	
	public void test2(IRProgram program) throws Exception {
		ByteArrayOutputStream out = new ByteArrayOutputStream();
		IRSerializer.write(program, out);
		IRProgram result = IRSerializer.read(new ByteArrayInputStream(out.toByteArray()));
		
		int depth = 5;
		String deep_0 = IRSerializer.deepPrint("Deep(0)", program, depth).replace("\t", "    ");
		String deep_1 = IRSerializer.deepPrint("Deep(1)", result, depth).replace("\t", "    ");
		
		System.out.println("--------------------------------------------------------------");
		System.out.println();
		System.out.println(deep_0);
		System.out.println();
		System.out.println("--------------------------------------------------------------");
		System.out.println();
		System.out.println(deep_1);
		System.out.println();
		System.out.println("--------------------------------------------------------------");
	}
}
