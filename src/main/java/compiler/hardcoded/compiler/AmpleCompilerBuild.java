package hardcoded.compiler;

import hardcoded.compiler.errors.CompilerException;
import hardcoded.compiler.errors.SyntaxMarker;
import hardcoded.compiler.instruction.IRProgram;
import hardcoded.compiler.instruction.IntermediateCodeGenerator;
import hardcoded.compiler.instruction.IntermediateCodeOptimizer;
import hardcoded.compiler.parsetree.ParseTreeGenerator;
import hardcoded.compiler.parsetree.ParseTreeOptimizer;
import hardcoded.visualization.Visualization;

public class AmpleCompilerBuild {
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
	
	public AmpleCompilerBuild() {
		parse_tree_generator = new ParseTreeGenerator();
		parse_tree_optimizer = new ParseTreeOptimizer();
		icg = new IntermediateCodeGenerator();
		ico = new IntermediateCodeOptimizer();
		vs = Visualization.DUMMY; vs = new hardcoded.visualization.PTVisualization(); vs.hide();
	}
	
	/**
	 * Compile the file at the specified file into a {@code IRProgram}.
	 * 
	 * @param	config
	 * 
	 * @throws	Exception
	 * @throws	CompilerException
	 * 			if the compilation failed
	 * 
	 * @return a {@code IRProgram}
	 */
	public IRProgram build(BuildConfiguration config) throws Exception {
		Program current_program = parse_tree_generator.init(config, config.getStartFile());
		
		if(current_program.hasErrors()) {
			for(SyntaxMarker marker : current_program.getSyntaxMarkers()) {
				System.err.printf("%s (%s:%s) : %s\n",
					marker.getCompilerMessage(),
					marker.getLineIndex(),
					marker.getColumnIndex(),
					marker.getMessage()
				);
			}
			
			throw new CompilerException("Compiler errors.");
		}
		
		vs.show(current_program);
		parse_tree_optimizer.do_constant_folding(vs, current_program);
		
		for(Function func : current_program.list()) {
			String str = hardcoded.compiler.constants.Utils.printPretty(func).replace("\t", "    ");
			System.out.println(str);
		}
		
		IRProgram ir_program;
		ir_program = icg.generate(current_program);
		ir_program = ico.generate(ir_program);
		
		return ir_program;
	}
}
