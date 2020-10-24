package hardcoded.compiler;

import java.io.*;

import hardcoded.compiler.errors.CompilerException;
import hardcoded.compiler.instruction.*;
import hardcoded.compiler.parsetree.ParseTreeGenerator;
import hardcoded.compiler.parsetree.ParseTreeOptimizer;
import hardcoded.visualization.Visualization;

public class HCompilerBuild {
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
	 * Visualization class.
	 */
	private Visualization vs;
	
	public HCompilerBuild() {
		parse_tree_generator = new ParseTreeGenerator();
		parse_tree_optimizer = new ParseTreeOptimizer();
		icg = new IntermediateCodeGenerator();
		ico = new IntermediateCodeOptimizer();
		vs = Visualization.DUMMY; // vs = new hardcoded.visualization.HCVisualization(); vs.hide();
	}
	
	/**
	 * Compile the file at the specified file into a {@code IRProgram}.
	 * 
	 * @param	file	a file
	 * @throws	Exception
	 * @throws	CompilerException
	 * 			If the compilation failed
	 * 
	 * @return a {@code IRProgram}
	 */
	public IRProgram build(File file) throws Exception {
		Program current_program = parse_tree_generator.init(file.getParentFile(), file.getName());
		if(parse_tree_generator.hasErrors()) {
			throw new CompilerException("Compiler errors.");
		}
		
		vs.show(current_program);
		parse_tree_optimizer.do_constant_folding(vs, current_program);
		
		IRProgram ir_program;
		ir_program = icg.generate(current_program);
		ir_program = ico.generate(ir_program);
		
//		if(CompilerMain.isDeveloper()) {
//			try {
//				test(ir_program);
//			} catch(IOException e) {
//				e.printStackTrace();
//			}
//		}
		
		return ir_program;
	}
	
	@SuppressWarnings("unused")
	private void test(IRProgram program) throws IOException {
		File file = new File(System.getProperty("user.home") + "/Desktop/spooky/serial.lir");
		try {
			test2(program);
		} catch(Exception e) {
			e.printStackTrace();
		}
		
		ByteArrayOutputStream bs = new ByteArrayOutputStream();
		IRSerializer.write(program, bs);
		byte[] array = bs.toByteArray();
		
		if(!file.getParentFile().exists())
			file.getParentFile().mkdirs();
		
		FileOutputStream stream = new FileOutputStream(file);
		stream.write(array);
		stream.close();
	}
	
	private void test2(IRProgram program) throws Exception {
//		File file = new File(System.getProperty("user.home") + "/Desktop/spooky/serial.lir");
//		FileInputStream in = new FileInputStream(file);
//		IRProgram result = IRSerializer.read(in);
//		in.close();
		
		ByteArrayOutputStream out = new ByteArrayOutputStream();
		IRSerializer.write(program, out);
		IRProgram result = IRSerializer.read(new ByteArrayInputStream(out.toByteArray()));
		
		int depth = 5;
		String deep_0 = IRSerializer.deepPrint("Deep(0)", program, depth).replace("\t", "    ");
		String deep_1 = IRSerializer.deepPrint("Deep(0)", result, depth).replace("\t", "    ");
		
//		System.out.println("--------------------------------------------------------------");
//		System.out.println();
//		System.out.println(deep_0);
//		System.out.println();
//		System.out.println("--------------------------------------------------------------");
//		System.out.println();
//		System.out.println(deep_1);
//		System.out.println();
//		System.out.println("--------------------------------------------------------------");
		System.out.println("Equals: " + deep_0.equals(deep_1));
		
		int lim = Math.min(deep_0.length(), deep_1.length());
		
		for(int i = 0; i < lim; i++) {
			if(deep_0.charAt(i) != deep_1.charAt(i)) {
				int min = Math.max(i - 10, 0);
				int max = Math.min(i + 10, lim);
				System.out.println(deep_0.substring(min, max) + " != " + deep_1.substring(min, max));
				
				i += 10;
			}
		}
	}
}
