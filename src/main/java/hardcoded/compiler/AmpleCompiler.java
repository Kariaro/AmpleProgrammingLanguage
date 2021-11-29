package hardcoded.compiler;

import hardcoded.compiler.errors.CompilerException;
import hardcoded.compiler.impl.ICodeGenerator;
import hardcoded.compiler.instruction.IRProgram;
import hardcoded.configuration.CompilerConfiguration;
import hardcoded.configuration.TargetFormat;

/**
 * @author HardCoded
 */
public class AmpleCompiler {
	private CompilerConfiguration config;
	private IRProgram program;
	private byte[] bytes;
	
	public AmpleCompiler() {
		
	}
	
	public void setConfiguration(CompilerConfiguration config) {
		this.config = config;
	}
	
	/**
	 * Build using the specified compiler configuration.
	 * 
	 * @throws CompilerException a compiler exception
	 */
	public void build() throws CompilerException {
		if(config == null) {
			throw new CompilerException("Configuration has not been set");
		}
		
		IRProgram program;
		byte[] bytes;
		
		AmpleCompilerBuild builder = new AmpleCompilerBuild();
		program = builder.build(config);
		
		ICodeGenerator generator = config.getOutputFormat().createNew();
		if(config.getTargetFormat() == TargetFormat.BYTECODE) {
			bytes = generator.getBytecode(program);
		} else {
			bytes = generator.getAssembler(program);
		}
		
		this.program = program;
		this.bytes = bytes;
	}
	
	/**
	 * Returns the exported format. This result will either represent
	 * <b>Bytecode</b> or <b>Assembler</b> depending on the compiler options.
	 */
	public byte[] getBytes() {
		return bytes;
	}
	
	/**
	 * Returns the compiled ir code.
	 */
	public IRProgram getProgram() {
		return program;
	}
	
	/**
	 * Reset the compiler.
	 */
	public void reset() {
		program = null;
		config = null;
		bytes = null;
	}
}
