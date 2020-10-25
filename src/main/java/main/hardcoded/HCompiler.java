package hardcoded;

import java.io.File;
import java.io.FileOutputStream;

import hardcoded.compiler.HCompilerBuild;
import hardcoded.compiler.errors.CompilerException;
import hardcoded.compiler.instruction.IRProgram;
import hardcoded.exporter.impl.CodeGeneratorImpl;

public class HCompiler {
	private CodeGeneratorImpl codegen;
	private OutputFormat format;
	
	private File workingDirectory;
	private String sourceFile;
	private String outputFile;
	
	private boolean hasCompiled;
	
	// Information about the compiled code
	private IRProgram program;
	private byte[] bytes;
	
	public HCompiler() {
		
	}
	
	public File getWorkingDirectory() {
		return workingDirectory;
	}
	
	/**
	 * Set the working directory of the compiler.
	 * @param	file	a directory
	 */
	public void setWorkingDirectory(File directory) {
		this.workingDirectory = directory;
	}
	
	/**
	 * Set the source file of the compiler.
	 * @param	pathname	a pathname string
	 */
	public void setSourceFile(String pathname) {
		this.sourceFile = pathname;
	}
	
	/**
	 * Set the output file of the compiler.
	 * @param	pathname	a pathname string
	 */
	public void setOutputFile(String pathname) {
		this.outputFile = pathname;
	}
	
	public void setOutputFormat(String formatName) {
		setOutputFormat(OutputFormat.get(formatName));
	}
	
	public void setOutputFormat(OutputFormat format) {
		this.format = format;
	}
	
	public void build() throws Exception {
		if(hasCompiled) throw new Exception("Unclosed resources. Try calling reset()");
		if(format == null) throw new CompilerException("No output format has been selected");
		
		if(sourceFile == null) throw new CompilerException("No source file was specified");
		if(outputFile == null) throw new CompilerException("No output file was specified");
		
		File sourcePath = new File(workingDirectory, sourceFile);
		File outputPath = new File(workingDirectory, outputFile);
		
		if(sourcePath.equals(outputPath))
			throw new CompilerException("source and output file cannot be the same file");
		
		// TODO: Check if this can be reused
		HCompilerBuild builder = new HCompilerBuild();
		codegen = format.createNew();
		program = builder.build(sourcePath);
		bytes = codegen.generate(program);
		
		// TODO: Is this safe?
		FileOutputStream stream = new FileOutputStream(outputPath);
		stream.write(bytes, 0, bytes.length);
		stream.close();
		
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
