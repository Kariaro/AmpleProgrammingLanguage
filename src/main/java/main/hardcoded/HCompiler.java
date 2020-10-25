package hardcoded;

import java.io.File;

import hardcoded.compiler.HCompilerBuild;
import hardcoded.compiler.errors.CompilerException;
import hardcoded.compiler.instruction.IRProgram;
import hardcoded.exporter.impl.CodeGeneratorImpl;

public class HCompiler {
	private CodeGeneratorImpl codegen;
	private OutputFormat format;
	
	private File sourceFile;
	
	private boolean hasCompiled;
	
	// Information about the compiled code
	private IRProgram program;
	private byte[] bytes;
	
	public HCompiler() {
		
	}
	
	/**
	 * Set the source file of the compiler.
	 * @param	pathname	a pathname string
	 */
	public void setSourceFile(File file) {
		this.sourceFile = file;
	}
	
	public void setOutputFormat(String formatName) {
		setOutputFormat(OutputFormat.get(formatName));
	}
	
	public void setOutputFormat(OutputFormat format) {
		this.format = format;
	}
	
	public void build() throws Exception {
		if(hasCompiled) throw new Exception("Unclosed resources. Try calling reset()");
		if(format == null) throw new CompilerException("No output format was specified");
		if(sourceFile == null) throw new CompilerException("No source file was specified");
		
		// TODO: Check if this can be reused
		HCompilerBuild builder = new HCompilerBuild();
		codegen = format.createNew();
		program = builder.build(sourceFile);
		bytes = codegen.generate(program);
		
		hasCompiled = true;
	}
	
	/**
	 * Returns the compiled bytes.
	 * @return the compiled bytes
	 */
	public byte[] getBytes() {
		return bytes;
	}
	
	/**
	 * Returns the compiled ir program.
	 * @return the compiled ir program
	 */
	public IRProgram getProgram() {
		return program;
	}
	
	public void reset() {
		program = null;
		bytes = null;
	}
}
