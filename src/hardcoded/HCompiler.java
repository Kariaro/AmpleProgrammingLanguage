package hardcoded;

import java.io.File;

import hardcoded.compiler.HCompilerBuild;

public class HCompiler {
	private File projectPath;
	
	public HCompiler() {
		
	}
	
	/**
	 * Set the compilers project path.
	 * @param filePath
	 */
	public void setProjectPath(String filePath) {
		this.projectPath = new File(filePath);
	}
	
	/**
	 * Set the compilers project path.
	 * @param path
	 */
	public void setProjectPath(File path) {
		this.projectPath = path;
	}
	
	/**
	 * Get the compilers project path.
	 * @return The path of the project.
	 */
	public File getProjectPath() {
		return projectPath;
	}
	
	/**
	 * Build the project.
	 */
	public void build() {
		new HCompilerBuild();
	}
}
