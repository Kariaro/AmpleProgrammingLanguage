package hardcoded;

import java.io.*;

import hardcoded.compiler.HCompilerBuild;
import hardcoded.compiler.errors.CompilerException;
import hardcoded.compiler.instruction.IRProgram;
import hardcoded.compiler.instruction.IRSerializer;
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
		
//		if(CompilerMain.isDeveloper()) {
//			try {
//				test(program);
//			} catch(IOException e) {
//				e.printStackTrace();
//			}
//		}
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
