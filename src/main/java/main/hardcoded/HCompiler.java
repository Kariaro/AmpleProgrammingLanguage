package hardcoded;

import java.io.File;

import hardcoded.compiler.HCompilerBuild;
import hardcoded.compiler.errors.CompilerException;

public class HCompiler {
	private HCompilerBuild builder = new HCompilerBuild();
	private OutputFormat format;
	private File projectPath;
	
	public HCompiler() {
		
	}
	
	public void setProjectPath(String filePath) {
		this.projectPath = new File(filePath);
	}
	
	public void setProjectPath(File path) {
		this.projectPath = path;
	}
	
	public File getProjectPath() {
		return projectPath;
	}
	
	public void setOutputFormat(String formatName) {
		setOutputFormat(OutputFormat.get(formatName));
	}
	
	public void setOutputFormat(OutputFormat format) {
		this.format = format;
	}
	
	public void build() {
		if(format == null)
			throw new CompilerException("No output format has been selected");
		
		builder.setOutputFormat(format);
		
		String file = "main.hc";
		// file = "tests/000_pointer.hc";
		file = "prim.hc";
		// file = "tests/001_comma.hc";
		// file = "tests/002_invalid_assign.hc";
		// file = "tests/003_invalid_brackets.hc";
		// file = "tests/004_cor_cand.hc";
		// file = "tests/005_assign_comma.hc";
		// file = "tests/006_cast_test.hc";
		// file = "test_syntax.hc";
		
		// file = "tests_2/000_assign_test.hc";
		
		try {
			builder.build(file);
			
//			StringBuilder sb = new StringBuilder();
//			
//			int index = 1;
//			for(byte b : bytes) {
//				if(index++ > 31) {
//					index = 1;
//					sb.append(String.format("%02x\n", b));
//				} else {
//					sb.append(String.format("%02x ", b));
//				}
//			}
//			
//			System.out.println(sb.toString().trim());
		} catch(Exception e) {
			e.printStackTrace();
		}
	}
}
