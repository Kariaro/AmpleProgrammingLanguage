package hardcoded;

import java.io.File;
import java.io.FileOutputStream;

import hardcoded.compiler.HCompilerBuild;
import hardcoded.compiler.errors.CompilerException;
import hardcoded.compiler.instruction.IRProgram;
import hardcoded.exporter.impl.CodeGeneratorImpl;

public class HCompiler {
	private HCompilerBuild builder = new HCompilerBuild();
	private CodeGeneratorImpl codegen;
	private OutputFormat format;
	private File sourcePath;
	private File binaryPath;
	private String fileName;
	
	private boolean hasCompiled;
	
	// Information about the compiled code
	private IRProgram program;
	private byte[] bytes;
	
	public HCompiler() {
		
	}
	
	public void setSourcePath(String pathname) {
		this.sourcePath = new File(pathname);
	}
	
	public void setSourcePath(File path) {
		this.sourcePath = path;
	}
	
	public void setBinaryPath(String pathname) {
		this.binaryPath = new File(pathname);
	}
	
	public void setBinaryPath(File path) {
		this.binaryPath = path;
	}
	
	public void setFileName(String name) {
		this.fileName = name;
	}
	
	public File getProjectPath() {
		return sourcePath;
	}
	
	public void setOutputFormat(String formatName) {
		setOutputFormat(OutputFormat.get(formatName));
	}
	
	public void setOutputFormat(OutputFormat format) {
		this.format = format;
	}
	
	public void build() throws Exception {
		if(hasCompiled)
			throw new Exception("Unclosed resources. Try calling reset()");
		if(format == null) throw new CompilerException("No output format has been selected");
		if(fileName == null) throw new CompilerException("Entry file name was null");
		
		codegen = format.createNew();
		
		File entryFile = new File(sourcePath, fileName);
		program = builder.build(entryFile);
		bytes = codegen.generate(program);
		
		// TODO: Is this safe?
		String outputName = fileName;
		{
			int index = fileName.lastIndexOf('.');
			if(index >= 0) {
				outputName = outputName.substring(0, index) + format.extension;
			}
		}
		
		File binaryFile = new File(binaryPath, outputName);
		FileOutputStream stream = new FileOutputStream(binaryFile);
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
	 * @return
	 */
	public IRProgram getProgram() {
		return program;
	}
	
	public void reset() {
		// TODO: Check if the builder is reusable or if it sill has information about the last compile
		builder = new HCompilerBuild();
		program = null;
		bytes = null;
	}
}
