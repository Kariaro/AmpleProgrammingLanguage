package hardcoded.compiler;

import java.io.*;

import hardcoded.CompilerMain;
import hardcoded.OutputFormat;
import hardcoded.compiler.errors.CompilerException;
import hardcoded.compiler.instruction.*;
import hardcoded.compiler.parsetree.ParseTreeGenerator;
import hardcoded.compiler.parsetree.ParseTreeOptimizer;
import hardcoded.exporter.impl.CodeGeneratorImpl;
import hardcoded.visualization.Visualization;

public class HCompilerBuild {
	private File projectPath = new File("res/project/src/");
	
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
	
	/**
	 * Visualization class.
	 */
	private Visualization vs;
	
	public HCompilerBuild() {
		parse_tree_generator = new ParseTreeGenerator();
		parse_tree_optimizer = new ParseTreeOptimizer();
		icg = new IntermediateCodeGenerator();
		ico = new IntermediateCodeOptimizer();
		vs = Visualization.DUMMY; // new HCVisualization();
		vs.hide();
	}
	
	public void setOutputFormat(OutputFormat format) {
		this.cei = format.createNew();
	}
	
	
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
		if(cei == null) throw new CompilerException("No output format has been selected.");
		
		Program current_program = parse_tree_generator.init(projectPath, pathname);
		if(parse_tree_generator.hasErrors()) {
			throw new CompilerException("Compiler errors.");
		}
		
		// vs.show(current_program);
		parse_tree_optimizer.do_constant_folding(/* vs, */current_program);
		
		IRProgram ir_program;
		ir_program = icg.generate(current_program);
		ir_program = ico.generate(ir_program);
		
		byte[] bytes = cei.generate(ir_program);
		
		if(CompilerMain.isDeveloper()) {
			try {
				test(ir_program);
			} catch(IOException e) {
				e.printStackTrace();
			}
		}
		
		return bytes;
	}
	
	public void test(IRProgram program) throws IOException {
		ByteArrayOutputStream bs = new ByteArrayOutputStream();
		IRSerializer.write(program, bs);
		byte[] array = bs.toByteArray();
		
		File file = new File(System.getProperty("user.home") + "/Desktop/spooky/serial.lir");
		if(!file.getParentFile().exists())
			file.getParentFile().mkdirs();
		
		FileOutputStream stream = new FileOutputStream(file);
		stream.write(array);
		stream.close();
	}
	
//	public void test2(IRProgram program) throws Exception {
//		ByteArrayOutputStream out = new ByteArrayOutputStream();
//		IRSerializer.write(program, out);
//		IRProgram result = IRSerializer.read(new ByteArrayInputStream(out.toByteArray()));
//		
//		int depth = 5;
//		String deep_0 = IRSerializer.deepPrint("Deep(0)", program, depth).replace("\t", "    ");
//		String deep_1 = IRSerializer.deepPrint("Deep(0)", result, depth).replace("\t", "    ");
//		
//		System.out.println("--------------------------------------------------------------");
//		System.out.println();
//		System.out.println(deep_0);
//		System.out.println();
//		System.out.println("--------------------------------------------------------------");
//		System.out.println();
//		System.out.println(deep_1);
//		System.out.println();
//		System.out.println("--------------------------------------------------------------");
//		System.out.println("Equals: " + deep_0.equals(deep_1));
//		
//		int lim = Math.min(deep_0.length(), deep_1.length());
//		
//		for(int i = 0; i < lim; i++) {
//			if(deep_0.charAt(i) != deep_1.charAt(i)) {
//				int min = Math.max(i - 10, 0);
//				int max = Math.min(i + 10, lim);
//				System.out.println(deep_0.substring(min, max) + " != " + deep_1.substring(min, max));
//				
//				break;
//			}
//		}
//	}
}
