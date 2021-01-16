package hardcoded;

import java.io.*;

import hardcoded.compiler.AmpleCompilerBuild;
import hardcoded.compiler.BuildConfiguration;
import hardcoded.compiler.errors.CompilerException;
import hardcoded.compiler.instruction.IRProgram;
import hardcoded.compiler.instruction.IRSerializer;
import hardcoded.utils.ObjectUtils;

public class HCompiler {
	private BuildConfiguration config;
	private boolean hasCompiled;
	
	// Information about the compiled code
	private IRProgram program;
	private byte[] bytes;
	
	public HCompiler() {
		
	}
	
	public void setConfiguration(BuildConfiguration config) {
		this.config = config;
	}
	
	public void build() throws Exception {
		if(hasCompiled) throw new Exception("Unclosed resources. Try calling reset()");
		
		if(config == null) {
			throw new CompilerException("Configuration was not valid: config was null");
		}
		
		AmpleCompilerBuild builder = new AmpleCompilerBuild();
		program = builder.build(config);
		
		OutputFormat format = config.getOutputFormat();
		bytes = format.createNew().generate(program);
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
		config = null;
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
		String deep_0 = ObjectUtils.deepPrint(program, depth).replace("\t", "    ");
		String deep_1 = ObjectUtils.deepPrint(result, depth).replace("\t", "    ");
		
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
		
		int errors = 0;
		for(int i = 0; i < lim; i++) {
			if(deep_0.charAt(i) != deep_1.charAt(i)) {
				int min = Math.max(i - 10, 0);
				int max = Math.min(i + 10, lim);
				System.out.println(deep_0.substring(min, max) + " != " + deep_1.substring(min, max));
				System.out.println("----------");
				
				i += 10;
				
				if(errors++ > 5) break;
			}
		}
	}
}
