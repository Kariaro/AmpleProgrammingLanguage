package hardcoded;

import java.io.File;

import hardcoded.compiler.HCompilerBuild;

public class HCompiler {
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
	
	public void build() {
		new HCompilerBuild();
	}
}
