package hardcoded.compiler;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import hardcoded.compiler.errors.CompilerException;
import hardcoded.compiler.impl.ISyntaxMarker;
import hardcoded.compiler.instruction.IRProgram;
import hardcoded.compiler.instruction.IntermediateCodeGenerator;
import hardcoded.compiler.instruction.IntermediateCodeOptimizer;
import hardcoded.compiler.parsetree.ParseTreeGenerator;
import hardcoded.compiler.parsetree.ParseTreeOptimizer;
import hardcoded.compiler.statement.Program;
import hardcoded.configuration.CompilerConfiguration;
import hardcoded.utils.Position;
import hardcoded.utils.StatementUtils;
import hardcoded.visualization.Visualization;

/**
 * @author HardCoded
 */
class AmpleCompilerBuild {
	private static final Logger LOGGER = LogManager.getLogger(AmpleCompilerBuild.class);
	
	// The parse tree generator.
	private ParseTreeGenerator parse_tree_generator;
	
	// The parse tree optimizer.
	private ParseTreeOptimizer parse_tree_optimizer;
	
	// The instruction generator.
	private IntermediateCodeGenerator icg;
	
	// The instruction optimzier.
	private IntermediateCodeOptimizer ico;
	
	// Visualization class.
	private Visualization vs;
	
	public AmpleCompilerBuild() {
		parse_tree_generator = new ParseTreeGenerator();
		parse_tree_optimizer = new ParseTreeOptimizer();
		icg = new IntermediateCodeGenerator();
		ico = new IntermediateCodeOptimizer();
		vs = Visualization.DUMMY;
	}
	
	/**
	 * Change compiler the visualization.
	 * 
	 * @param visualization
	 */
	public void setVisualizer(Visualization visualization) {
		// vs = new hardcoded.visualization.PTVisualization(); vs.hide();
		vs = visualization == null ? Visualization.DUMMY:visualization;
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
	public IRProgram build(CompilerConfiguration config) throws CompilerException {
		Program current_program = parse_tree_generator.init(config, config.getSourceFile());
		vs = new hardcoded.visualization.PTVisualization(); vs.hide();
		// vs.show(current_program);
		
		System.out.println(StatementUtils.printPretty(current_program));
		if(current_program.hasErrors()) {
			for(ISyntaxMarker marker : current_program.getSyntaxMarkers()) {
				Position position = marker.getSyntaxPosition().getStartPosition();
				
				LOGGER.warn("{}(Line: {}, Column: {}) : {}",
					marker.getCompilerMessage(),
					position.line + 1,
					position.column + 1,
					marker.getMessage()
				);
			}
			
			throw new CompilerException("Compiler errors.");
		}
		
		vs.show(current_program);
		parse_tree_optimizer.do_constant_folding(vs, current_program);
		
		IRProgram ir_program;
		ir_program = icg.generate(current_program);
		ir_program = ico.generate(ir_program);
		
		return ir_program;
	}
}
